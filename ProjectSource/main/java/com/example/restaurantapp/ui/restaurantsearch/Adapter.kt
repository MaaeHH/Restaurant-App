package com.example.restaurantapp.ui.restaurantsearch

import android.content.Intent
import android.graphics.Color.WHITE
import android.graphics.Color.red
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.restaurantapp.R
import com.example.restaurantapp.ui.RPageFragment.RPageActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
//import com.example.restaurantapp.ui.appPage.ui.main.RPageActivity
import java.util.ArrayList

class Adapter(private val imageModelArrayList: ArrayList<SearchFragment.Place>) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    /*
     * Inflate our views using the layout defined in row_layout.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.search2, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val x = imageModelArrayList.get(position)

        val info = x

        //holder.imgView.setImageResource(info.getImages())
        holder.imgView.setImageBitmap(info.image)
        holder.txtMsg.text = info.name
        holder.addressDisplay.text = info.address
        holder.idView.text = info.id


        var count: Int = 0
        var total: Int = 0
        val resultsDatabase =
            FirebaseDatabase.getInstance("https://restaurant-app-f89a0-default-rtdb.europe-west1.firebasedatabase.app")
        val myRef = resultsDatabase.getReference("Reviews")
        if (info.id != null) {
            myRef.child(info.id!!).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    var rating: Int = snapshot.child("rating").getValue<Int>(Int::class.java)!!

                    total = total + rating
                    count = count + 1

                    if (total != 0 && count != 0) {
                        var averageRating: Float = total.toFloat() / count.toFloat()
                        setBGColour(averageRating, holder)
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }


    }

    //This takes the average rating and sets the background colour to the correct colour
    fun setBGColour(rating: Float, holder: ViewHolder) {
        var colour = R.color.white
        if (rating <= 1) {
            colour = R.color.oneStar
        } else if (rating <= 2) {
            colour = R.color.twoStar
        } else if (rating <= 3) {
            colour = R.color.threeStar
        } else if (rating <= 5) {
            colour = R.color.FourFiveStar
        }

        holder.relLayout.setBackgroundResource(colour)
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

        var imgView = itemView.findViewById(R.id.imageViewIcon) as ImageView
        var txtMsg = itemView.findViewById(R.id.textView) as TextView
        var idView = itemView.findViewById(R.id.textView3) as TextView
        var addressDisplay = itemView.findViewById(R.id.textView3) as TextView
        var relLayout = itemView.findViewById(R.id.relLayout) as RelativeLayout

        init {
            itemView.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            //val msg = txtMsg.text

            //val snackbar = Snackbar.make(v, "$msg" + "R.string.msg", Snackbar.LENGTH_LONG)
            //snackbar.show()
            val intent = Intent(v.getContext(), RPageActivity::class.java)
            if (idView.text.toString() != null && idView.text.toString() != "") {
                intent.putExtra("placeID", idView.text.toString())

                v.getContext().startActivity(intent)
            }

            /*val intent = Intent(v.getContext(), RestaurantPageActivity::class.java)
            intent.putExtra("placeID", idView.text)

            startActivity(intent)*/
        }
    }
}