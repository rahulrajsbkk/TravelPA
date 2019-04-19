package `in`.ouon.travelpa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    internal var morefirebaseDatabase: FirebaseDatabase? = null
    internal var moreRef: DatabaseReference? = null
    private val mcontext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.setHasFixedSize(true)
        recyclerView.setLayoutManager(GridLayoutManager(mcontext, 2))
        AddtoCart.setOnClickListener{
//            startActivity(Intent(this@MainActivity, CartActivity::class.java))
            Toast.makeText(this@MainActivity, "Cart Clicked.", Toast.LENGTH_SHORT).show()
        }

        profile.setOnClickListener { v ->
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

    }

    override fun onStart() {
        super.onStart()


    }
}
