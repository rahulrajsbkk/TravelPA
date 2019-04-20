package `in`.ouon.travelpa

import `in`.ouon.travelpa.model.LocationModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var mDatabase: DatabaseReference
    private val mcontext: Context = this
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        database = FirebaseDatabase.getInstance().reference
//        mRecyclerView=recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(mcontext, 2);
        AddtoCart.setOnClickListener{
            //            startActivity(Intent(this@MainActivity, CartActivity::class.java))
            Toast.makeText(this@MainActivity, "Cart Clicked.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        listAddress()
    }

    private fun listAddress() {
        val addressRef = database.child("location")

        addressRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val locationList = ArrayList<LocationModel>()
                for (locationChild: DataSnapshot in snapshot.children) {
                    val location = locationChild.getValue(LocationModel::class.java)
                    if (location != null) {
                        location.id = locationChild.key.toString()
                        locationList.add(location)
                    }
                }
                recyclerView.adapter =
                    LocationListAdapter(locationList, this@MainActivity,"main")

            }

        })
    }

}

