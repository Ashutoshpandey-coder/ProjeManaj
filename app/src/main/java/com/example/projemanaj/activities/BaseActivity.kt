package com.example.projemanaj.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.projemanaj.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressDialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text : String){
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.progress_dialog)
        mProgressDialog.findViewById<TextView>(R.id.tv_progress_message).text = text
        mProgressDialog.show()
    }
    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }
    fun getCurrentUserId() : String{
        // this is the current user id , so that we show his/her projects he is assigned to
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    // We don't want that activity is closed accidentally
    fun doubleBackToExit(){
        //first back button it just show toast and again pressed it exits
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this,
            getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT)
            .show()
        //if the user press twice with in 2 sec then exit otherwise reset boolean
        //otherwise if user press once and second one after 30 sec it just exit
        Handler(Looper.myLooper()!!).postDelayed({
            doubleBackToExitPressedOnce = false
        },2000)
    }

    fun showErrorSnackBar(message : String){
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content)
            ,message
            ,Snackbar.LENGTH_LONG)
        //set the background view
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snack_bar_error_color))
        //show the snackBar
        snackBar.show()

    }
}