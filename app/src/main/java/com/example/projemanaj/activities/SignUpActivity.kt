package com.example.projemanaj.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.databinding.ActivitySignUpBinding
import com.example.projemanaj.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {

    private lateinit var binding : ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }
    fun userRegisteredSuccess(){
        Toast.makeText(
            this,
            "you have successfully registered",
            Toast.LENGTH_SHORT
        )
            .show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }
    private fun registerUser(){
        val name = binding.etName.text.toString().trim{it <= ' '}
        val email = binding.etEmail.text.toString().trim{it <= ' '}
        val password = binding.etPassword.text.toString().trim{it <= ' '}

        if (validateFrom(name,email,password)){
//            Toast.makeText(this, "now you can register user", Toast.LENGTH_SHORT).show()
            showProgressDialog(getString(R.string.please_wait))
            //Now lets register the user
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
//                hideProgressDialog()
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    //Now this is done by firebase fireStore fun register user
//                    Toast.makeText(
//                        this,
//                        "$name you have successfully registered with email id $registeredEmail",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                    FirebaseAuth.getInstance().signOut()
//                    finish()

                    val user = User(firebaseUser.uid,name, registeredEmail)
                    //now call fireStore method
                    // we need brackets because we don't use object we call it direct from class
                    FireStoreClass().registerUser(this,user)

                } else {
                    Toast.makeText(
                        this,
                        "Registration Failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
    private fun validateFrom(name : String , email : String, password : String) : Boolean{
        return when {
                TextUtils.isEmpty(name) ->{
                    showErrorSnackBar("Please enter a name")
                    false
                }
            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            } else ->{
                true
            }
        }
    }
}