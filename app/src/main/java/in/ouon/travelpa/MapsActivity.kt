package `in`.ouon.travelpa

import `in`.ouon.travelpa.model.LocationModel
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_maps.*
import android.transition.Slide
import android.view.Gravity
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.firebase.auth.FirebaseAuth

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var mDb: DatabaseReference
    private lateinit var locations: LocationModel
    private lateinit var recycler: RecyclerView
    private lateinit var mainLoc:LocationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        database = FirebaseDatabase.getInstance().reference
        mDb = database
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        locations = LocationModel()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val id: String = intent.getStringExtra("key")

        database = database.child("location").child(id)
        mDb = mDb.child("users").child(userId).child("cart").child("areatours")


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val zoom = 15F
        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationModel::class.java)
                if (location != null) {
                    val locationPoint  = LatLng(location.lat, location.lng)
                    mMap.addMarker(MarkerOptions().position(locationPoint).title(location.title))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationPoint, zoom))
                    mainLoc=location
                }
            }
        })
        mDb.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                val locationPoint = LatLng(mainLoc.lat, mainLoc.lng)
                mMap.addMarker(MarkerOptions().position(locationPoint).title(mainLoc.title))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationPoint, zoom))
                for (locationChild: DataSnapshot in snapshot.children) {
                    val location = locationChild.getValue(LocationModel::class.java)
                    if (location != null) {
                        locationPoint = LatLng(location.lat, location.lng)
                        mMap.addMarker(MarkerOptions().position(kerala).title(location.title))
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kerala, 10F))
                    }
                }
            }
        })
        button.setOnClickListener {
            // Initialize a new layout inflater instance
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            // Inflate a custom view using layout inflater
            val view = inflater.inflate(R.layout.another_view, null)

            // Initialize a new instance of popup window
            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            )

            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }


            // If API level 23 or higher then execute the code
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut

            }

            // Get the widgets reference from custom view
            val tv = view.findViewById<TextView>(R.id.text_view)
            val buttonPopup = view.findViewById<Button>(R.id.button_popup)
            recycler = view.findViewById<RecyclerView>(R.id.attractionsRecycler)
            listAddress()

            // Set click listener for popup window's text view
            tv.setOnClickListener {
                // Change the text color of popup window's text view
                tv.setTextColor(Color.RED)
            }

            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener {
                // Dismiss the popup window
                popupWindow.dismiss()

            }

            // Set a dismiss listener for popup window
            popupWindow.setOnDismissListener {
                Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
            }

            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(layoutMap)
            popupWindow.showAtLocation(
                layoutMap, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }

    }

    private fun listAddress() {
//val database = FirebaseDatabase.getInstance().reference
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this@MapsActivity, LinearLayoutManager.HORIZONTAL, false)
        val addressRef = database.child("areatours")
        val locationList1 = ArrayList<LocationModel>()
        addressRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (locationChild: DataSnapshot in snapshot.children) {
                    val location = locationChild.getValue(LocationModel::class.java)
                    if (location != null) {
                        location.id = locationChild.key.toString()
                        locationList1.add(location)
                    }
                }
                recycler.adapter =
                    LocationListAdapter(locationList1, this@MapsActivity, "maps")
            }

        })
    }

}
