package com.example.projemanaj.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.databinding.ActivityCreateBoardBinding
import com.example.projemanaj.models.Board
import com.example.projemanaj.utils.Constants
import com.example.projemanaj.utils.Constants.Companion.PICK_IMAGE_REQUEST_CODE
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri : Uri? = null
    private  var mUsername : String? = null
    private var mBoardImageUrl : String = ""

    companion object{
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 15
    }
    private lateinit var binding : ActivityCreateBoardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

       setUpActionBar()

        if (intent.hasExtra(Constants.NAME)){
            mUsername = intent.getStringExtra(Constants.NAME)
        }

        binding.ivBoardImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READ_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        }
        binding.btnCreate.setOnClickListener{
            if (mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(getString(R.string.please_wait))

                createBoard()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE ){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(
                this,
                "Oops! you just denied the permission for storage. You can also set it on in settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null){

            mSelectedImageFileUri = data.data

            Glide
                .with(this)
                .load(mSelectedImageFileUri)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(binding.ivBoardImage)
        }
    }
    private fun uploadBoardImage(){
        showProgressDialog(getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance()
                .reference.child( // so we can get a unique name
                    "BOARD_IMAGE" +
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
                Log.i("FB Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    //uri is the actual link to the database storage
                    //this the downloadable image uri
                        uri ->
                    Log.i("Downloadable image Uri", uri.toString())
                    mBoardImageUrl  = uri.toString()
                    // Update the user profile which the user changed
//                    hideProgressDialog()
                        createBoard()

                }
            }.addOnFailureListener{
                    exception  ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }
    private fun createBoard(){
        val assignedUsersArrayLists : ArrayList<String> = ArrayList()
        assignedUsersArrayLists.add(getCurrentUserId())

        val board = Board(binding.etBoardName.text.toString(),
            mBoardImageUrl,
            mUsername!!,
            assignedUsersArrayLists)
        
        if (binding.etBoardName.text!!.isNotEmpty()) {
            FireStoreClass().createBoard(this, board)
        }else{
            hideProgressDialog()
            Toast.makeText(this, "Please enter a board name", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarCreateBoard)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.title = resources.getString(R.string.create_board_title)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        binding.toolbarCreateBoard.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun boardCreatedSuccessfully(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }
}