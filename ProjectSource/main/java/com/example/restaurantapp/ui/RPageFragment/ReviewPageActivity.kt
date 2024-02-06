package com.example.restaurantapp.ui.RPageFragment

import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.example.restaurantapp.databinding.ActivityReviewPageBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

//import com.example.restaurantapp.ui.RPageFragment.databinding.ActivityReviewPageBinding

class ReviewPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityReviewPageBinding.inflate(layoutInflater)


        val myintent = this.intent
        val placeID = myintent.getStringExtra("placeID")
        //Gets the placeID from the thing that opened this
        val recyclerView =
            binding.root.findViewById<RecyclerView>(R.id.reviewRecycler) as RecyclerView
        recyclerView.setLayoutManager(LinearLayoutManager(this))

        val resultsDatabase =
            FirebaseDatabase.getInstance("https://restaurant-app-f89a0-default-rtdb.europe-west1.firebasedatabase.app")
        val myRef = resultsDatabase.getReference("Reviews")
        //Get the results database instance

        var output: ArrayList<ReviewAdapter.review> = ArrayList<ReviewAdapter.review>()
        if (placeID != null) {
            myRef.child(placeID).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    var userID: String =
                        snapshot.child("userID").getValue<String>(String::class.java)!!
                    var email: String =
                        snapshot.child("email").getValue<String>(String::class.java)!!
                    var reviewText: String =
                        snapshot.child("reviewText").getValue<String>(String::class.java)!!
                    var rating: Int = snapshot.child("rating").getValue<Int>(Int::class.java)!!
                    var placeID: String =
                        snapshot.child("placeID").getValue<String>(String::class.java)!!
                    //This will just grab all the fields we need to make a review object
                    var rev: ReviewAdapter.review =
                        ReviewAdapter.review(userID, email, reviewText, rating, placeID)
                    output.add(rev)
                    recyclerView.adapter = ReviewAdapter(output)
                    //it adds them to the output arraylist and then sets the adapter

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onCancelled(error: DatabaseError) {}

            })

        }



        setContentView(binding.root)
    }


}