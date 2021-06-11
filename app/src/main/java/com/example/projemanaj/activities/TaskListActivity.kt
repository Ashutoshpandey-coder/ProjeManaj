package com.example.projemanaj.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanaj.R
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.adapters.TaskListItemAdapter
import com.example.projemanaj.databinding.ActivityTaskListBinding
import com.example.projemanaj.models.Board
import com.example.projemanaj.models.Card
import com.example.projemanaj.models.Task
import com.example.projemanaj.models.User
import com.example.projemanaj.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var mBoardDocumentId :String
    private lateinit var binding : ActivityTaskListBinding
    private lateinit var mBoardDetails : Board
    lateinit var mAssignedMembersDetailList : ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog(getString(R.string.please_wait))

        FireStoreClass().getBoardDetails(this,mBoardDocumentId)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent,MEMBERS_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = mBoardDetails.name
        }
        binding.toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun updateCardsInTaskList(taskListPosition: Int,cards : ArrayList<Card>){
        // remove the add card button which is actually not a card
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog(getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }
    fun boardDetails(board : Board){
        hideProgressDialog()

        mBoardDetails = board

        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().getAssignedMemberListDetails(this,mBoardDetails.assignedTo)

    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        //hide one and reuest another one
        showProgressDialog(getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }
    fun createTaskList(taskListName : String){
        //create a new task and created by is carried by firestore class
        val task = Task(taskListName,FireStoreClass().getCurrentUserId())
        //update the list
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun updateTaskList(position : Int,listName : String,model : Task){
        val task = Task(listName,model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        showProgressDialog(getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        showProgressDialog(getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }
    fun addCardToTaskList(position: Int,cardName : String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        val cardAssignedUserList : ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FireStoreClass().getCurrentUserId())

        val card = Card(cardName,FireStoreClass().getCurrentUserId(),cardAssignedUserList)

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList
        )
        //put everything into the board
        //card parent task and task's parent board
        mBoardDetails.taskList[position] = task
        showProgressDialog(getString(R.string.please_wait))
        //update the board
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAIL_REQUEST_CODE){
            //refresh the page
            showProgressDialog(getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this,mBoardDocumentId)
        } else{
            Log.e("Cancelled","Cancelled by user")
        }
    }
    fun cardDetails(taskListPosition : Int , cardPosition : Int){
        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMembersDetailList)
        startActivityForResult(intent,CARD_DETAIL_REQUEST_CODE)
    }
    fun boardMemberDetailList(list : ArrayList<User>){
        mAssignedMembersDetailList = list

        hideProgressDialog()

        val addTaskList  = Task(getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.rvTaskList.setHasFixedSize(true)
        val adapter = TaskListItemAdapter(this,mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter

    }
    //    override fun onResume() {
//        //when resume always update the data
//        showProgressDialog(getString(R.string.please_wait))
//        FireStoreClass().getBoardDetails(this,mBoardDocumentId)
//        super.onResume()
//    }
    companion object{
        const val MEMBERS_REQUEST_CODE : Int = 13
        const val CARD_DETAIL_REQUEST_CODE : Int = 14
    }
}