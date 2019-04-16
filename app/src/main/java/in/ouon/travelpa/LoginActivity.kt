package `in`.ouon.travelpa

import `in`.ouon.travelpa.utils.AppUtils
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    internal val TAG: String = "LOGIN"
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mAuth: FirebaseAuth
    private var otpSent = false
    private lateinit var gso: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleApiClient
    private val RES_CODE = 121
    private lateinit var userDB: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()
        userDB = FirebaseDatabase.getInstance().reference.child(AppUtils.USER_DB_KEY)

        if (mAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()


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
                    sendVerificationCode("+91$phone")
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

        btLoginGoogle.setOnClickListener { v ->
            progressGoogle.visibility = View.VISIBLE
            btLoginGoogle.visibility = View.INVISIBLE
            signInWithGoogle()
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
                            tvOTP.visibility = View.INVISIBLE
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
                    val user = task.result?.user!!.uid
                    login(user, AppUtils.PHONE_SIGN_IN)
                    Toast.makeText(this, "Loggined", Toast.LENGTH_SHORT).show()

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

    //-----------------------End Phone Auth--------------------


    //-----------------------Google Sign in--------------------

    private fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleSignInClient)
        startActivityForResult(signInIntent, RES_CODE)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        progressGoogle.visibility = View.INVISIBLE
        btLoginGoogle.visibility = View.VISIBLE
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Loggined", Toast.LENGTH_SHORT).show()
                    val user = task.result!!.user!!.uid
                    login(user, AppUtils.GOOGLE_SIGN_IN)

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(layoutLogin, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                }
                progressGoogle.visibility = View.INVISIBLE
                btLoginGoogle.visibility = View.VISIBLE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RES_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                progressGoogle.visibility = View.INVISIBLE
                btLoginGoogle.visibility = View.VISIBLE
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun login(user: String, signInMethod: String) {
        userDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.hasChild(user)) {
                    //User Profile Already Set Up

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    //Setting Up User Profile
                    val intent = Intent(this@LoginActivity, SetProfileActivity::class.java)
                    intent.putExtra(AppUtils.SIGN_IN_METHODE, signInMethod)
                    startActivity(intent)
                    finish()
                }

            }

            override fun onCancelled(p0: DatabaseError) {

                //Setting Up User Profile

                val intent = Intent(this@LoginActivity, SetProfileActivity::class.java)
                intent.putExtra(AppUtils.SIGN_IN_METHODE, signInMethod)
                startActivity(intent)
                finish()

            }
        })
    }

}
