package `in`.ouon.travelpa

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    internal val TAG: String = "LOGIN"
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mAuth: FirebaseAuth
    private var otpSent = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        etOTP.visibility = View.INVISIBLE
        tvOTP.visibility = View.INVISIBLE

        //-------------------------Phone Auth----------------------------

        btPhoneLogin.setOnClickListener { v ->
            val phone = etPhoneNo.editText!!.text.toString()
            if (TextUtils.isEmpty(phone)) {
                etPhoneNo.editText!!.error = "Please Enter Your Mobile Number"
            } else {
                if (!otpSent) {
                    btPhoneLogin.visibility = View.INVISIBLE
                    btPhoneLogin.isEnabled = false
                    progressPhone.visibility = View.VISIBLE
                    sendVerificationCode(phone)
                } else {
                    val otp = etOTP.editText!!.text.toString()
                    if (TextUtils.isEmpty(otp)) {
                        etOTP.editText!!.error = "Please Enter Your OTP"
                    } else {
                        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                        btPhoneLogin.visibility = View.INVISIBLE
                        btPhoneLogin.isEnabled = false
                        progressPhone.visibility = View.VISIBLE
                        signInWithPhoneAuthCredential(credential)
                    }
                }
            }

        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,             // Activity (for callback binding)
            verficationCallback
        )
    }


    val verficationCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            Log.d(TAG, "onVerificationCompleted:$credential")

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {

            Log.w(TAG, "onVerificationFailed", e)

            btPhoneLogin.visibility = View.VISIBLE
            progressPhone.visibility = View.GONE
            btPhoneLogin.isEnabled = true

            etOTP.visibility = View.INVISIBLE
            tvOTP.visibility = View.INVISIBLE

            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@LoginActivity, "Add Country Code With Phone Number", Toast.LENGTH_SHORT).show()

            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(
                    this@LoginActivity,
                    "The SMS quota for the project has been exceeded",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        override fun onCodeSent(
            verificationId: String?,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG, "onCodeSent:" + verificationId!!)
            storedVerificationId = verificationId
            resendToken = token
            otpSent = true
            btPhoneLogin.visibility = View.VISIBLE
            progressPhone.visibility = View.GONE
            btPhoneLogin.isEnabled = true
            btPhoneLogin.text = "Submit OTP"
            etOTP.visibility = View.VISIBLE
            tvOTP.visibility = View.VISIBLE

            Thread(Runnable {
                var i = 60
                while (i >= 0) {
                    runOnUiThread {
                        if (i == 0) {
                            tvOTP.visibility = View.GONE
                        }
                        tvOTP.text = "OTP Will Recive in " + i + " Secs"
                    }
                    Thread.sleep(1000)
                    --i
                }
            }).start()

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user

                    Toast.makeText(this, "Loggined", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                    // ...
                } else {

                    btPhoneLogin.visibility = View.VISIBLE
                    progressPhone.visibility = View.GONE
                    btPhoneLogin.isEnabled = true
                    etOTP.visibility = View.VISIBLE

                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Wrong OTP. Please Try again..", Toast.LENGTH_SHORT).show()

                    }
                }
            }
    }

    //-----------------------End Phone Auth-----------------

}
