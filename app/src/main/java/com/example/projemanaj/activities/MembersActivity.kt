package com.example.projemanaj.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.adapters.MemberListItemsAdapter
import com.example.projemanaj.databinding.ActivityMembersBinding
import com.example.projemanaj.databinding.ItemCardBinding
import com.example.projemanaj.models.Board
import com.example.projemanaj.models.User
import com.example.projemanaj.utils.Constants
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var mAssignedMemberList : ArrayList<User>
    private lateinit var mBoardDetails : Board
    private var anyChangesMade : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!

        }
        showProgressDialog(getString(R.string.please_wait))

        FireStoreClass().getAssignedMemberListDetails(this,mBoardDetails.assignedTo)

        setUpActionBar()


    }
    fun setupMembersLists(list : ArrayList<User>){

        mAssignedMemberList = list
        hideProgressDialog()


        val adapter = MemberListItemsAdapter(this,list)

        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        binding.rvMembersList.adapter = adapter
    }
    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar!= null){
            actionBar.title = resources.getString(R.string.members)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        binding.toolbarMembersActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun memberDetails(user : User){
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMembersToBoard(this,mBoardDetails,user)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
               val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()
                //implement adding member logic
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetail(this,email)
            }else{
                Toast.makeText(this,
                    "Please enter members email address.",
                    Toast.LENGTH_SHORT).show()
            }

        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
             dialog.dismiss()
        }
        dialog.show()
    }
    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMemberList.add(user)

        anyChangesMade = true
        //after clossing dialog it update the recycler view again with all the new members
        //it requires a list so we have to update a list
        setupMembersLists(mAssignedMemberList)

        //sent the notification to the user or assigned member
        sendNotificationToUserAsync(mBoardDetails.name,user.fcmToken)

    }

    override fun onBackPressed() {
        if (anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
    private inner class sendNotificationToUserAsync(val boardName : String, val token : String)
        : AsyncTask<Any,Void,String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(getString(R.string.please_wait))
        }
        override fun doInBackground(vararg params: Any?): String {
            var result : String
            var connection : HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY} = ${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()

                dataObject.put(Constants.FCM_KEY_TITLE,
                    "Assigned to the board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the board by " +
                            "${mAssignedMemberList[0].name}")
                jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO,token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult : Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    var sb = StringBuilder()
                    var line : String?
                    try {
                        while (reader.readLine().also { line = it } != null){
                            sb.append(line + "\n")
                        }
                    }catch (e : IOException){
                        e.printStackTrace()
                    }
                    finally {
                        try {
                            inputStream.close()
                        }catch (e : IOException){
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                }else{
                    result = connection.responseMessage
                }
            }catch (e : SocketTimeoutException){
                result = "Connection Timeout"
            }catch (e : Exception){
                result = "Error : " + e.message
            }finally{
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            Log.e("Json Response result :",result!!)
        }



    }
}