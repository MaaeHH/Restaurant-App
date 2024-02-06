package com.example.restaurantapp.ui.recommended

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.restaurantapp.ui.restaurantsearch.Adapter
import com.example.restaurantapp.ui.restaurantsearch.SearchFragment
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class recommendedFragment : Fragment() {

    private lateinit var recommendedViewModel: RecommendedViewModel
    lateinit var placesClient : PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var currentSearch : ArrayList<SearchFragment.Place> = ArrayList<SearchFragment.Place>()

    private var lat :Double =51.621441
    private var lng :Double =-3.943646
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        recommendedViewModel =
                ViewModelProvider(this).get(RecommendedViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_reccomended, container, false)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        var rView = root.findViewById<RecyclerView>(R.id.recommendedRecycler) as RecyclerView

        rView.adapter = Adapter(ArrayList<SearchFragment.Place>())

        context?.let{
            Places.initialize(it, "AIzaSyDwrTThRoynVPQ1Hygj2-H4dU_sJYU809g")
            placesClient = Places.createClient(it)
            val layoutManager = LinearLayoutManager(it)
            rView.layoutManager = layoutManager
        }//Initialise the places API

        val t = Thread {
            currentSearch = search()
        }//Create a thread that will search the places API and start it

        t.start()
        t.join() //Wait for the thread to finish

        rView.adapter = Adapter(currentSearch) //Set the adapter to the results of the search
        //val adapter: Adapter()
        //recyclerView.setAdapter(adapter)

        return root
    }

    fun getLastKnownLocation() { //This just updates the lat and lng variables by using the phone's gps
        context?.let{
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location->
                    if (location != null) {
                        // use your location object
                        // get latitude , longitude and other info from this
                        lat = location.latitude
                        lng = location.longitude
                    }

                }
        }
    }




    private fun search(): ArrayList<SearchFragment.Place> {

        val API_KEY = "AIzaSyDwrTThRoynVPQ1Hygj2-H4dU_sJYU809g"
        val LOG_TAG = "ListRest"

        var resultList: ArrayList<SearchFragment.Place> = ArrayList<SearchFragment.Place>()
        var conn: HttpURLConnection? = null
        val jsonResults = StringBuilder()
        try {
            getLastKnownLocation()

            var aaa = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +lat.toString()+"%2C"+lng.toString()+"&radius=50000&type=restaurant&fields=reference%2Cformatted_address%2Cname%2Cphotos%2Cplace_id&key="+API_KEY
            //We initialise a string to be the google places API query

            val url = URL(aaa)
            conn = url.openConnection() as HttpURLConnection
            //Open up the URL and connect

            val input = InputStreamReader(conn.inputStream)
            var read: Int
            val buff = CharArray(1024)
            while (input.read(buff).also { read = it } != -1) {
                jsonResults.append(buff, 0, read)
            }
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Error processing Places API URL", e)
            return resultList
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Error connecting to Places API", e)
            return resultList
        } finally {
            conn?.disconnect()
        }
        try {
            // Create a JSON object hierarchy from the results
            val jsonObj = JSONObject(jsonResults.toString())
            val predsJsonArray = jsonObj.getJSONArray("results")

            // Extract the descriptions from the results
            resultList = ArrayList<SearchFragment.Place>()
            var x = predsJsonArray.length()
            for (i in 0 until predsJsonArray.length()) {
                var place = SearchFragment.Place()
                place.reference = predsJsonArray.getJSONObject(i).getString("reference")
                place.name = predsJsonArray.getJSONObject(i).getString("name")
                //place.photos = predsJsonArray.getJSONObject(i).getJSONArray("photos")
                try{
                    place.photos = predsJsonArray.getJSONObject(i).getJSONArray("photos")
                }catch(e: Exception){

                }

                place.address = predsJsonArray.getJSONObject(i).getString("vicinity")
                place.id = predsJsonArray.getJSONObject(i).getString("place_id")



                resultList.add(place)
            }

            for (i in 0 until predsJsonArray.length()) {
                var place = resultList[i]
                //If we have a photo, get that photo and set it
                if(place.photos != null){
                    var image = place.photos!!.getJSONObject(0).getString("photo_reference")
                    var photoAPI = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference="+ image + "&key=" + API_KEY
                    place.image = BitmapFactory.decodeStream(URL(photoAPI).openConnection().getInputStream())
                }else{
                    //Else use this nice placeholder image of some penguins I found on google
                    var photoAPI = "https://www.visitsealife.com/auckland/media/om0dw4at/king-penguin.jpg"
                    place.image = BitmapFactory.decodeStream(URL(photoAPI).openConnection().getInputStream())
                }
            }


        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Error processing JSON results", e)
        }
        return resultList

    }







}