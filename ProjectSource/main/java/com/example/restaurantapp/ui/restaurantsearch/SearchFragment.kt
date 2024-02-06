package com.example.restaurantapp.ui.restaurantsearch

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import org.json.JSONArray

class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        /*val textView: TextView = root.findViewById(R.id.text_gallery)
        searchViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        val searchButton = root.findViewById<ImageButton>(R.id.searchButton)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val searchInput = root.findViewById<EditText>(R.id.mySearchBar)
        val recyclerView = root.findViewById<View>(R.id.theRecyclerView) as RecyclerView
        var currentSearch: ArrayList<Place> = ArrayList<Place>()
        recyclerView.adapter = Adapter(currentSearch)

        context?.let {
            Places.initialize(it, "AIzaSyDwrTThRoynVPQ1Hygj2-H4dU_sJYU809g")
            placesClient = Places.createClient(it)
            val layoutManager = LinearLayoutManager(it)
            recyclerView.layoutManager = layoutManager

            //placesClient.findCurrentPlace()
        }//Initialise the places client again


        //val adapter: Adapter()
        //recyclerView.setAdapter(adapter)

        searchButton.setOnClickListener {
            val t = Thread {
                if (searchInput.text.toString() != "") {
                    currentSearch = search(searchInput.text.toString())
                }

            }
            t.start()
            t.join()
            recyclerView.adapter = Adapter(currentSearch)
        }//Make search button use the search function passing the input
        return root
    }

    fun getLastKnownLocation() {//This just updates the lat and lng
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {

                        lat = location.latitude
                        lng = location.longitude
                    }

                }
        }


    }


    fun search(inpSearch: String): ArrayList<Place> {
        //This works similarly to the recommended search but takes a query instead
        val API_KEY = "AIzaSyDwrTThRoynVPQ1Hygj2-H4dU_sJYU809g"
        val LOG_TAG = "ListRest"

        var resultList: ArrayList<Place> = ArrayList<Place>()
        var conn: HttpURLConnection? = null
        val jsonResults = StringBuilder()
        try {
            getLastKnownLocation()

            val aaa =
                "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?type=restaurant&input=" + inpSearch + "&inputtype=textquery&fields=reference%2Cformatted_address%2Cname%2Cphotos%2Cplace_id&key=" + API_KEY

            val url = URL(aaa)
            conn = url.openConnection() as HttpURLConnection


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
            val predsJsonArray = jsonObj.getJSONArray("candidates")

            // Extract the descriptions from the results
            resultList = ArrayList(predsJsonArray.length())
            for (i in 0 until predsJsonArray.length()) {
                var place = Place()
                place.reference = predsJsonArray.getJSONObject(i).getString("reference")
                place.name = predsJsonArray.getJSONObject(i).getString("name")

                try {
                    place.photos = predsJsonArray.getJSONObject(i).getJSONArray("photos")
                } catch (e: Exception) {

                }

                place.address = predsJsonArray.getJSONObject(i).getString("formatted_address")
                place.id = predsJsonArray.getJSONObject(i).getString("place_id")




                resultList.add(place)
            }

            for (i in 0 until predsJsonArray.length()) {
                var place = resultList[i]
                //If we have a photo, get that photo and set it
                if (place.photos != null) {
                    var image = place.photos!!.getJSONObject(0).getString("photo_reference")
                    var photoAPI =
                        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=" + image + "&key=" + API_KEY
                    place.image =
                        BitmapFactory.decodeStream(URL(photoAPI).openConnection().getInputStream())
                } else {
                    //Else use this nice placeholder image of some penguins I found on google
                    var photoAPI =
                        "https://www.visitsealife.com/auckland/media/om0dw4at/king-penguin.jpg"
                    place.image =
                        BitmapFactory.decodeStream(URL(photoAPI).openConnection().getInputStream())
                }


            }


        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Error processing JSON results", e)
        }
        return resultList

    }

    class Place { //A place object has all this stuff
        var reference: String? = null
        var name: String? = null
        var icon: String? = null
        var photos: JSONArray? = null
        var address: String? = null
        var id: String? = null
        var image: Bitmap? = null
        override fun toString(): String {
            return name!! //This is what returns the name of each restaurant for array list
        }
    }


}