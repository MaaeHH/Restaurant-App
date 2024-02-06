package com.example.restaurantapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantapp.R
import com.example.restaurantapp.ui.RPageFragment.ReviewAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.nav_header_main.*

class LoginFragment : Fragment() {

    //lateinit var mDatabase : DatabaseReference
    private lateinit var loginViewModel: LoginViewModel
    public lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginViewModel =
            ViewModelProvider(this).get(LoginViewModel::class.java)
        var root = inflater.inflate(R.layout.fragment_login, container, false)

        auth = Firebase.auth
        // val username = root.findViewById<EditText>(R.id.username)
        val email = root.findViewById<EditText>(R.id.email)
        val confirmEmail = root.findViewById<EditText>(R.id.emailConfirm)
        val password = root.findViewById<EditText>(R.id.password)
        val confirmPassword = root.findViewById<EditText>(R.id.passwordConfirm)
        val login = root.findViewById<Button>(R.id.signIn)
        val register = root.findViewById<Button>(R.id.register)
        val loading = root.findViewById<ProgressBar>(R.id.loading)
        var mAuth = FirebaseAuth.getInstance()
        var user = FirebaseAuth.getInstance().currentUser
        var uid: String? = null


        if (user != null) {
            root = inflater.inflate(R.layout.fragment_profile, container, false)
            val username = root.findViewById<TextView>(R.id.editTextTextPersonName)
            val signout = root.findViewById<Button>(R.id.logout)
            val reviews = root.findViewById<RecyclerView>(R.id.reviewsRecycler)
//Get all the elements
            uid = user.uid
            username.text = user.email
//Set the UI and username
            reviews.setLayoutManager(LinearLayoutManager(root.context))

            signout.setOnClickListener {

                FirebaseAuth.getInstance().signOut()

                Toast.makeText(this.context, "Signed out", Toast.LENGTH_LONG).show()
            }
//Make the signout button sign us out

            val resultsDatabase =
                FirebaseDatabase.getInstance("https://restaurant-app-f89a0-default-rtdb.europe-west1.firebasedatabase.app")
            val myRef = resultsDatabase.getReference("Reviews")
//Initialise the reviews database

            var output: ArrayList<ReviewAdapter.review> = ArrayList()
            myRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    for (b in snapshot.children) {
                        //for(b in a.children){
                        var userID: String =
                            b.child("userID").getValue<String>(String::class.java)!!
                        var email: String =
                            b.child("email").getValue<String>(String::class.java)!!
                        var reviewText: String =
                            b.child("reviewText").getValue<String>(String::class.java)!!
                        var rating: Int = b.child("rating").getValue<Int>(Int::class.java)!!
                        var placeID: String =
                            b.child("placeID").getValue<String>(String::class.java)!!

                        var rev: ReviewAdapter.review =
                            ReviewAdapter.review(userID, email, reviewText, rating, placeID)
                        if (rev.userID == uid) {
                            output.add(rev)
                        }

                        // }
//Populate the output array from the firebase database
                    }

                    reviews.adapter = ReviewAdapter(output)
//Set the adapter for the recyclerview with the output


                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onCancelled(error: DatabaseError) {}

            })

        }


        var registerMode: Boolean = false
        val edittexts = arrayOf<EditText>(email, confirmEmail, password, confirmPassword)
        confirmEmail.isEnabled = false
        confirmPassword.isEnabled = false
        confirmEmail.visibility = View.INVISIBLE
        confirmPassword.visibility = View.INVISIBLE

        login.setOnClickListener {
            if (registerMode) {
                //if in registermode, disable registermode stuff and go out of registermode
                confirmEmail.isEnabled = false
                confirmPassword.isEnabled = false
                confirmEmail.visibility = View.INVISIBLE
                confirmPassword.visibility = View.INVISIBLE
                register.isEnabled = true
                registerMode = false
            } else {
                //else, log in

                mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user = mAuth.currentUser
                            uid = user!!.uid
                            //mDatabase.child(uid).child("Username").setValue(username)
                            //startActivity(Intent(LoginFragment::class.java))
                            Toast.makeText(
                                this.context,
                                "Successfully signed in",
                                Toast.LENGTH_LONG
                            ).show()

                            inflater.inflate(R.layout.fragment_profile, container, false)
                        } else {
                            Toast.makeText(
                                this.context,
                                "Error signing in",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }
        register.setOnClickListener {
            if (!registerMode) {
                //if not in registermode, enable everything and go into registermode
                confirmEmail.isEnabled = true
                confirmPassword.isEnabled = true
                confirmEmail.visibility = View.VISIBLE
                confirmPassword.visibility = View.VISIBLE
                register.isEnabled = validateSignUp(
                    email.text.toString(),
                    confirmEmail.text.toString(),
                    password.text.toString(),
                    confirmPassword.text.toString()
                )
                registerMode = true
            } else {
                //else, register the account
                mAuth.createUserWithEmailAndPassword(
                    email.text.toString(),
                    password.text.toString()
                ).addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user = mAuth.currentUser
                        uid = user!!.uid
                        //mDatabase.child(uid).child("Username").setValue(username)
                        //startActivity(Intent(LoginFragment::class.java))
                        Toast.makeText(
                            this.context,
                            "Successfully registered :)",
                            Toast.LENGTH_LONG
                        ).show()

                        inflater.inflate(R.layout.fragment_profile, container, false)
                    } else {
                        Toast.makeText(
                            this.context,
                            "Error registering, try again later :(",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }

        for (thing in edittexts) {
            thing.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (registerMode) {
                        register.isEnabled = validateSignUp(
                            email.text.toString(),
                            confirmEmail.text.toString(),
                            password.text.toString(),
                            confirmPassword.text.toString()
                        )
                    } else {
                        register.isEnabled = true
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }


        return root
    }


    private fun validateSignUp(
        email: String,
        confirmEmail: String,
        password: String,
        confirmPassword: String //This takes all the sign up fields and returns true if valid
    ): Boolean {
        var b: Boolean = false

        if (email == confirmEmail && password == confirmPassword) {
            b = validateEmail(email) && validatePassword(password)
        } //If the fields are the same, validate them
        return b
    }


    private fun validatePassword(password: String): Boolean {
        return password.length > 5 //Password is valid if its length is greater than 5
    }

    private fun validateEmail(email: String): Boolean {
        return email.contains('@') //Email is valid if it contains an @
    }
}