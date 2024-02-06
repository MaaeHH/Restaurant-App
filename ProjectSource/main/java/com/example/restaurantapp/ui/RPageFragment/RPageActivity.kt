package com.example.restaurantapp.ui.RPageFragment

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.example.restaurantapp.ui.RPageFragment.databinding.RestaurantPage2Binding
import com.example.restaurantapp.R
import com.example.restaurantapp.databinding.RestaurantPageBinding
import com.example.restaurantapp.ui.restaurantsearch.SearchFragment
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class RPageActivity : AppCompatActivity() {

    private lateinit var binding: RestaurantPageBinding
    var currentSearch: ArrayList<SearchFragment.Place>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();

        binding = RestaurantPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val myintent = this.intent
        val placeID = myintent.getStringExtra("placeID")


        var title = findViewById(R.id.title) as TextView
        var picture = findViewById(R.id.imageView3) as ImageView

        var ratingBar = findViewById(R.id.ratingBar) as RatingBar
        var description = findViewById(R.id.description) as TextView
        var reviewInput = findViewById(R.id.reviewBox) as EditText
        var reviewPageButton = findViewById(R.id.reviewsButton) as Button
        var makeReviewButton = findViewById(R.id.postReview) as Button
        var inpRating = findViewById(R.id.inpRating) as RatingBar
        val t = Thread {
            if (placeID != null) {
                currentSearch = search(placeID)
            }//This makes a thread to search for the place

        }
        t.start()
        t.join()

        var place = currentSearch?.get(0)
        //Starts the thread, waits for it to finish and set the place this page is about to the
        //result of the search
        if (place != null) {
            title.text = place.name
            picture.setImageBitmap(place.image)
            description.text = place.address
        }//It sets all the stuff to the info from the search


        reviewPageButton.setOnClickListener {
            val intent = Intent(it.getContext(), ReviewPageActivity::class.java)

            intent.putExtra("placeID", placeID)

            it.getContext().startActivity(intent)
        }//Sets the review page button to take us to the review page

        var database =
            FirebaseDatabase.getInstance("https://restaurant-app-f89a0-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Reviews")

        makeReviewButton.setOnClickListener {
            //Sets the make review button to add the review to the database
            var signedIn = FirebaseAuth.getInstance().currentUser

            if (signedIn != null && placeID != null) {

                var review = ReviewAdapter.review(
                    signedIn.uid,
                    signedIn.email.toString(),
                    reviewInput.text.toString(),
                    inpRating.rating.toInt(),
                    placeID
                )

                database.child(placeID).child(signedIn.uid).setValue(review)

            } else {

            }

            var count: Int = 0
            var total: Int = 0
            val resultsDatabase =
                FirebaseDatabase.getInstance("https://restaurant-app-f89a0-default-rtdb.europe-west1.firebasedatabase.app")
            val myRef = resultsDatabase.getReference("Reviews")
            if (placeID != null) {
                myRef.child(placeID).addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                        var rating: Int = snapshot.child("rating").getValue<Int>(Int::class.java)!!

                        total = total + rating
                        count = count + 1


                        if (total != 0 && count != 0) {
                            var averageRating: Float = total.toFloat() / count.toFloat()
                            ratingBar.rating = averageRating
                        }//Gets the average rating and sets the rating bar to it

                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onCancelled(error: DatabaseError) {}
                })


            }
        }

        // Set up the user interaction to manually show or hide the system UI.
        //fullscreenContent = binding.fullscreenContent
        //fullscreenContent.setOnClickListener { toggle() }

        //fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //binding.dummyButton.setOnTouchListener(delayHideTouchListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.


    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    fun search(placeID: String): ArrayList<SearchFragment.Place>? {

        val API_KEY = "AIzaSyDwrTThRoynVPQ1Hygj2-H4dU_sJYU809g"
        val PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place"
        val TYPE_AUTOCOMPLETE = "/autocomplete"
        val TYPE_DETAILS = "/details"
        val TYPE_SEARCH = "/nearbysearch"
        val OUT_JSON = "/json?"
        val LOG_TAG = "ListRest"

        var resultList: ArrayList<SearchFragment.Place>? = null
        var conn: HttpURLConnection? = null
        val jsonResults = StringBuilder()
        try {

            var aaa =
                "https://maps.googleapis.com/maps/api/place/details/json?&fields=reference%2Cformatted_address%2Cname%2Cphotos&place_id=" + placeID + "&key=" + API_KEY


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
            val predsJsonArray = jsonObj.getJSONObject("result")

            // Extract the descriptions from the results
            resultList = ArrayList(predsJsonArray.length())
            for (i in 0 until predsJsonArray.length()) {
                var place = SearchFragment.Place()
                place.reference = predsJsonArray.getString("reference")
                place.name = predsJsonArray.getString("name")

                try {
                    place.photos = predsJsonArray.getJSONArray("photos")
                } catch (e: Exception) {

                }

                place.address = predsJsonArray.getString("formatted_address")
                //place.id = predsJsonArray.getString("place_id")


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


}