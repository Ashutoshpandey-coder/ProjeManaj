package com.example.projemanaj.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.databinding.ActivityProfileBinding
import com.example.projemanaj.models.User
import com.example.projemanaj.utils.Constants
import com.example.projemanaj.utils.Constants.Companion.PICK_IMAGE_REQUEST_CODE
import com.example.projemanaj.utils.Constants.Companion.READ_STORAGE_PERMISSION_CODE
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException


class ProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUrl : String = ""
    private lateinit var mUserDetails : User



    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()

        FireStoreClass().loadUserData(this)

        binding.profileImageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE.toString()
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)

            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                  Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding.btnUpdate.setOnClickListener{
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                if(binding.etName.text!!.isEmpty()){
                    Toast.makeText(this, "please enter a valid name", Toast.LENGTH_SHORT).show()
                }else if(binding.etMobile.text.toString().isNotEmpty() && binding.etMobile.text.toString().length != 10) {
                    Toast.makeText(this, "Enter a 10 digit Mobile number", Toast.LENGTH_SHORT)
                        .show()
                }else{
                    showProgressDialog(resources.getString(R.string.please_wait))

                    updateUserProfileData()
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //show image chooser
//                showImageChooser()
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "Oops! you just denied the permission for storage. You can allow it from the settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarProfileActivity)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = getString(R.string.my_profile_title)
        }

        binding.toolbarProfileActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.profileImageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun setUserDataInUI(user: User) {

        mUserDetails = user
        // here we have to download image from firebase storage
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.profileImageView)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L) {
            binding.etMobile.setText(user.mobile.toString())
        }
    }
    //create a hash map and set everything
    private fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()
        var anyChangesMade = false

        if(mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageUrl
            anyChangesMade = true
        }
        if (binding.etName.text.toString() != mUserDetails.name && binding.etName.text.toString().isNotEmpty()){
            userHashMap[Constants.NAME] = binding.etName.text.toString()
            anyChangesMade = true
        }
        //first if for if user removed his number from profile then we have to update with 0l
        if (binding.etMobile.text.toString().isEmpty()){
            userHashMap[Constants.MOBILE] = 0L
            anyChangesMade = true
        } else if (binding.etMobile.text.toString() != mUserDetails.mobile.toString() ) {
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
            anyChangesMade = true
        }

            //Now call the method in firestore class which u created
            if (anyChangesMade) {
                FireStoreClass().updateUserProfileData(this, userHashMap)
            }else{
                hideProgressDialog()
                Toast.makeText(this, "Profile is already updated", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        //for uploading to cloud we have to follow the structure
        //take reference of storage
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance()
                .reference.child( // so we can get a unique name
                    "USER_IMAGE" +
                            System.currentTimeMillis()
                            + "." +
                            Constants.getFileExtension(mSelectedImageFileUri,this)
                )
            // put the file
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                //if it is successful get the snapShot of the task
                // from task we get download url
                //we need this url in order to store in the user data
                taskSnapshot ->
                Log.i("Firebase Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    //uri is the actual link to the database storage
                    //this the downloadable image uri
                    uri ->
                    Log.i("Downloadable image Uri", uri.toString())
                    mProfileImageUrl  = uri.toString()
                    // Update the user profile which the user changed
//                    hideProgressDialog()
//                    //TODO updateUserProfileData
                    updateUserProfileData()

                }
            }.addOnFailureListener{
                exception  ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }


    fun profileUpdateSuccess(){
        hideProgressDialog()
        // for profile and name of user on main activity
        setResult(Activity.RESULT_OK)

        finish()
    }
}