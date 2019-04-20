package `in`.ouon.travelpa


import `in`.ouon.travelpa.model.LocationModel
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.place_item.view.*


class LocationListAdapter(
    val locationList: ArrayList<LocationModel>,
    val ctx: Context,
    val home: String
) :
    RecyclerView.Adapter<LocationListAdapter.AddressViewHolder>() {

    private lateinit var database: DatabaseReference
    private lateinit var mDatabase: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        database = FirebaseDatabase.getInstance().reference
        mDatabase = database
        return AddressViewHolder(LayoutInflater.from(ctx).inflate(R.layout.place_item, parent, false))
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val location = locationList[position]
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        holder.title.text = location.title
        holder.desc.text = location.desc
        Glide.with(ctx).load(location.image).into(holder.img)
        if (home.equals("main")) {
            holder.cardRowMain.setOnClickListener {
                val intent = Intent(ctx, MapsActivity::class.java)
                intent.putExtra("key", location.id)
                ctx.startActivity(intent)
            }
        } else if (home.equals("maps")) {
//            Toast.makeText(ctx,"₹ "+location.price,Toast.LENGTH_LONG).show()
            var key: String = ""
            holder.price.text = "₹ " + location.price
            holder.price.visibility = View.VISIBLE
            holder.check.visibility = View.VISIBLE
            holder.cardRowMain.setOnClickListener {
                if (key == "") {
                    mDatabase.child("users").child(userId).child("cart").child("areatours").child(location.id)
                        .setValue(location).addOnSuccessListener {
                        Glide.with(ctx).load(R.drawable.ic_check).into(holder.check)
                        key = "0"
                    }
                } else {
                    mDatabase.child("users").child(userId).child("cart").child("areatours").child(location.id)
                        .removeValue().addOnSuccessListener {
                        Glide.with(ctx).load(R.drawable.ic_uncheck).into(holder.check)
                        key = ""
                    }
                }
            }


        }
    }

    class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.title_upade_home
        val desc = view.desc_update
        val img = view.image_view_horizontal_home
        val cardRowMain = view.cardRowMain
        val price = view.textViewPrice
        val check = view.check
    }
}
