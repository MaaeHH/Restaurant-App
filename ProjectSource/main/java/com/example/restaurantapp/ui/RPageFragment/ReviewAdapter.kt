package com.example.restaurantapp.ui.RPageFragment

import android.content.Intent
import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.restaurantapp.R
import com.example.restaurantapp.ui.RPageFragment.RPageActivity
import org.json.JSONArray
//import com.example.restaurantapp.ui.appPage.ui.main.RPageActivity
import java.util.ArrayList

class ReviewAdapter(private val imageModelArrayList: ArrayList<review>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    /*
     * Inflate our views using the layout defined in row_layout.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.review, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val x = imageModelArrayList?.get(position)

        val info = x!!

        //holder.imgView.setImageResource(info.getImages())
        //holder.imgView.setImageBitmap(info.image)
        holder.email.text = info.email
        holder.reviewTxt.text = info.reviewText
        holder.hidden.text = info.userID
        holder.stars.numStars = info.rating
        holder.stars.rating = info.rating.toFloat()
    }

    /*
     * Get the maximum size of the
     */
    override fun getItemCount(): Int {

        return imageModelArrayList.size


    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {


        var email = itemView.findViewById(R.id.UserEmail) as TextView
        var reviewTxt = itemView.findViewById(R.id.ReviewText) as TextView
        var hidden = itemView.findViewById(R.id.hiddenField) as TextView
        var stars = itemView.findViewById(R.id.ratingStars) as RatingBar

        //var addressDisplay = itemView.findViewById(R.id.textView2) as TextView
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {

        }
    }

    data class review(
        var userID: String,
        var email: String,
        var reviewText: String,
        var rating: Int,
        var placeID: String
    )
}

