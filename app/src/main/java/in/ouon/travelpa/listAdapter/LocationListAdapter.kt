package `in`.ouon.travelpa


import `in`.ouon.travelpa.model.LocationModel
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.place_item.view.*


class LocationListAdapter(
    val locationList: ArrayList<LocationModel>,
    val ctx: Context
) :
    RecyclerView.Adapter<LocationListAdapter.AddressViewHolder>() {

    private lateinit var database: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        database = FirebaseDatabase.getInstance().reference
        return AddressViewHolder(LayoutInflater.from(ctx).inflate(R.layout.place_item, parent, false))
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val location = locationList[position]
        holder.title.text = location.title
        holder.desc.text = location.desc
        Glide.with(ctx).load("your-imaage-url").into(holder.img)


    }

    class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.title_upade_home
        val desc = view.desc_update
        val img = view.image_view_horizontal_home
    }
}
