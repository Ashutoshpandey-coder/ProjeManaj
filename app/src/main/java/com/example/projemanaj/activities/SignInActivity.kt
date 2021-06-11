package com.example.projemanaj.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.databinding.ActivitySignInBinding
import com.example.projemanaj.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : BaseActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setUpActionBar()

//        window.insetsController?.hide(WindowInsets.Type.statusBars())
        //it is deprecated and both works same for full screen and hide little window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }

    }
    fun signInSuccess(user : User){
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
    private fun setUpActionBar(){

        setSupportActionBar(binding.toolbarSignInActivity)

        val actionBar = supportActionBar

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignInActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun signInRegisteredUser(){
        val email = binding.etEmail.text.toString().trim{it <= ' '}
        val password = binding.etPassword.text.toString().trim{it <= ' '}

        if (validateFrom(email,password)){
            showProgressDialog(getString(R.string.please_wait))
                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this) { task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("Sign In", "signInWithCustomToken:success")
//                            val user = auth.currentUser
//                            startActivity(Intent(this,MainActivity::class.java))
//                            finish()
                            FireStoreClass().loadUserData(this)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign In", "signInWithCustomToken:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
        }

    }
    private fun validateFrom( email : String, password : String) : Boolean{
        return when {
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