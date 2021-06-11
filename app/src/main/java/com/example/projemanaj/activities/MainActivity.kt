package com.example.projemanaj.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.adapters.BoardsItemAdapter
import com.example.projemanaj.databinding.ActivityMainBinding
import com.example.projemanaj.models.Board
import com.example.projemanaj.models.User
import com.example.projemanaj.utils.Constants
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : BaseActivity() , NavigationView.OnNavigationItemSelectedListener {
    companion object{
        const val MY_PROFILE_REQUEST_CODE : Int = 11
       const val CREATE_BOARD_REQUEST_CODE = 12
    }
    private lateinit var binding : ActivityMainBinding
    private lateinit var mUsername : String
    private lateinit var recyclerView : RecyclerView
    private lateinit var noBoardAvailable : TextView
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.rv_boards_list)
        noBoardAvailable = findViewById(R.id.tv_no_boards_available)

        mSharedPreferences = this.getSharedPreferences(Constants.PROJEMANAJ_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if (tokenUpdated){
            showProgressDialog(getString(R.string.please_wait))
            FireStoreClass().loadUserData(this,true)

        }else{
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if(it.isComplete){
                    val fbToken = it.result.toString()
                    // DO your thing with your firebase token
                    updateFCMToken(fbToken)
                }
            }
        }

        FireStoreClass().loadUserData(this,true)

        setUpActionBar()

        binding.navView.setNavigationItemSelectedListener (this)
        val fabButton : FloatingActionButton = findViewById(R.id.fab_create_board)
        fabButton.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUsername)
            startActivityForResult(intent,CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun populateBoardListToUI(boardList : ArrayList<Board>){
        hideProgressDialog()

        if (boardList.size > 0){
            recyclerView.visibility = View.VISIBLE
            noBoardAvailable.visibility = View.GONE

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)

            val myAdapter = BoardsItemAdapter(this,boardList)
            recyclerView.adapter = myAdapter

            myAdapter.setOnClickListener(object : BoardsItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }

            })

        }else{
            recyclerView.visibility = View.GONE
            noBoardAvailable.visibility = View.VISIBLE
        }
    }

    private fun setUpActionBar(){
        val toolbar :Toolbar? = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            //Toggle drawer
            toggleDrawer()
        }

    }
    //toggle the drawer if open close it and if close open it
    private fun toggleDrawer(){
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
           doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile ->{
                //for result because although we update image and name in data base but still is not changing in main menu activity
                startActivityForResult(Intent(this@MainActivity,ProfileActivity::class.java),MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this@MainActivity,IntroActivity::class.java)
                //This flag are used for closing all activities in background and clear all the other intents
                    //now if any one use it will make a new instance
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
           FireStoreClass().loadUserData(this)
        }else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardsList(this)
        }
        else{
            Log.e("Cancelled","Cancelled on activity result")
        }
    }

    fun updateNavigationUserDetails(user : User, readBoardList : Boolean){
        val username : TextView = findViewById(R.id.tv_username)
        val userImage : CircleImageView  = findViewById(R.id.nav_user_image)

        mUsername = user.name

        hideProgressDialog()

        // here we have to download image from firebase storage

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(userImage)

        username.text = user.name

        if (readBoardList){
            showProgressDialog(getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this)
        }
    }
    fun tokenUpdateSuccess(){
        val editor : SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog(getString(R.string.please_wait))
        FireStoreClass().loadUserData(this,false)
    }
    private fun updateFCMToken(token : String){
        val userHashMap = HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(getString(R.string.please_wait))
        FireStoreClass().updateUserProfileData(this,userHashMap)
    }
}