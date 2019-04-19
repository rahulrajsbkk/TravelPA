package `in`.ouon.travelpa

import `in`.ouon.travelpa.model.User
import `in`.ouon.travelpa.utils.AppUtils
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {

    private lateinit var userDb: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        profileView.visibility = View.GONE

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            userId = FirebaseAuth.getInstance().currentUser!!.uid
        }


        userDb = FirebaseDatabase.getInstance().reference.child(AppUtils.USER_DB_KEY)

        userDb.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(User::class.java)
                userProfile!!.userId = snapshot.key.toString()
                profileView.visibility = View.VISIBLE
                progressProfile.visibility = View.GONE

                Glide.with(this@UserProfileActivity).load(userProfile.avatar).placeholder(R.drawable.ic_default_avatar)
                    .into(ivUserAvatar)
                tvUserName.text = userProfile.name
                tvLocation.text = userProfile.location

            }

        })

    }
}
