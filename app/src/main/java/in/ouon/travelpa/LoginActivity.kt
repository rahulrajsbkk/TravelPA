package `in`.ouon.travelpa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btLoginFB.setOnClickListener { v ->
            val phone = etPhoneNo.editText!!.text.toString()
        }
    }
}
