package `in`.ouon.travelpa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_payment_summary.*

class PaymentSummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_summary)

        btHome.setOnClickListener { v ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}
