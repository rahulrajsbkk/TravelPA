package `in`.ouon.travelpa

import `in`.ouon.travelpa.utils.AppUtils
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_set_profile.*
import java.text.SimpleDateFormat
import java.util.*


class SetProfileActivity : AppCompatActivity() {

    private lateinit var myCalendar: Calendar
    private lateinit var status: String
    private lateinit var userId: String
    private lateinit var userDb: DatabaseReference
    private var avatarURI: Uri? = null
    private lateinit var userStorage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_profile)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            userId = FirebaseAuth.getInstance().currentUser!!.uid
        }

        myCalendar = Calendar.getInstance()
        userDb = FirebaseDatabase.getInstance().reference.child(AppUtils.USER_DB_KEY)
        userStorage = FirebaseStorage.getInstance().reference.child(AppUtils.USER_DB_KEY)

        initUI()

        val date: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDate()
            }

        etBirthday.editText!!.setOnClickListener { v ->
            DatePickerDialog(
                this@SetProfileActivity, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        ivAvatar.setOnClickListener { v ->
            CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }

        btUpdateProfile.setOnClickListener { v ->

            val name = etName.editText?.text.toString()
            val email: String
            val phone: String

            if (status == AppUtils.GOOGLE_SIGN_IN) {
                email = FirebaseAuth.getInstance().currentUser!!.email.toString()
                phone = etEmail.editText?.text.toString()
            } else {
                phone = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
                email = etEmail.editText?.text.toString()
            }

            val dob = etBirthday.editText?.text.toString()
            val location = etLocation.editText?.text.toString()

            if (TextUtils.isEmpty(name)) {
                etName.editText?.error = "Enter Your Name"
            } else if (TextUtils.isEmpty(etEmail.editText?.text)) {

                if (status == AppUtils.GOOGLE_SIGN_IN) {
                    etEmail.editText?.error = "Enter Your Phone Number"
                } else {
                    etEmail.editText?.error = "Enter Your Email"
                }

            } else if (status == AppUtils.PHONE_SIGN_IN && !Patterns.EMAIL_ADDRESS.matcher(etEmail.editText?.text).matches()) {
                etEmail.editText?.error = "Enter A Valid Email"
            } else if (TextUtils.isEmpty(dob)) {
                etBirthday.editText?.error = "Select Your Birthday"
            } else if (TextUtils.isEmpty(location)) {
                etLocation.editText?.error = "Enter Your Location"
            } else if (avatarURI == null) {
                Snackbar.make(v, "Select Your Profile Avatar", Snackbar.LENGTH_SHORT).show()
            } else {


                btUpdateProfile.visibility = View.INVISIBLE
                progressSetProfile.visibility = View.VISIBLE

                val avatarRef = userStorage.child(userId).child("avatar.jpg")
                val uploadTask = avatarRef.putFile(avatarURI!!)

                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        avatarRef.downloadUrl.addOnSuccessListener { uri ->
                            val userInfo = HashMap<String, Any?>()

                            userInfo["name"] = name
                            userInfo["dob"] = dob
                            userInfo["location"] = location
                            userInfo["email"] = email
                            userInfo["phone"] = phone
                            userInfo["avatar"] = uri.toString()

                            userDb.child(userId).setValue(userInfo).addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()

                                } else {
                                    Toast.makeText(
                                        this,
                                        "Profile Set Failed\n" + task.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.addOnFailureListener { exception ->
                            btUpdateProfile.visibility = View.VISIBLE
                            progressSetProfile.visibility = View.INVISIBLE
                            Toast.makeText(this@SetProfileActivity, exception.toString(), Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        btUpdateProfile.visibility = View.VISIBLE
                        progressSetProfile.visibility = View.INVISIBLE
                        Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }
    }

    private fun initUI() {
        val intent = intent
        status = intent.getStringExtra(AppUtils.SIGN_IN_METHODE)

        if (status == AppUtils.GOOGLE_SIGN_IN) {

            etEmail.hint = "Phone Number"
            etEmail.editText!!.inputType = InputType.TYPE_CLASS_PHONE

        } else if (status == AppUtils.PHONE_SIGN_IN) {

            etEmail.hint = "Email"
            etEmail.editText!!.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        }
    }


    private fun updateDate() {
        val myFormat = "dd/MM/yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        etBirthday.editText!!.setText(sdf.format(myCalendar.time))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                avatarURI = result.uri

                Glide.with(this).load(avatarURI).into(ivAvatar)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

}
