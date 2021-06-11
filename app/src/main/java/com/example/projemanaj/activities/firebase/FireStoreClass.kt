package com.example.projemanaj.activities.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanaj.R
import com.example.projemanaj.activities.*
import com.example.projemanaj.models.Board
import com.example.projemanaj.models.User
import com.example.projemanaj.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/*This is our data base class which store all the things
* first store sign in info
* */
class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity,userInfo : User){

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener{
                e->
                    Log.e(activity.javaClass.simpleName,"Error occurred ${e.message}")
            }

    }
    fun createBoard(activity: CreateBoardActivity,boardInfo : Board){

        mFireStore.collection(Constants.BOARD)
            .document()
            .set(boardInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Board created Successfully")
                Toast.makeText(activity, "Board created Successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener{
                exception->
                activity.hideProgressDialog()
                Log.e("Error","Error while creating a board",exception)
            }
    }
    fun getBoardDetails(activity: TaskListActivity,documentId: String){
        mFireStore.collection(Constants.BOARD)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName,document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)



            }.addOnFailureListener{
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while loading a board",e)
            }

    }
    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARD)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList : ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board  = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }

                activity.populateBoardListToUI(boardList)
            }.addOnFailureListener{
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while loading a board",e)
            }

    }
    fun addUpdateTaskList(activity: Activity, board: Board){
        //the key will be string and the value will be any in hashmap
        val taskListHashMap = HashMap<String,Any>()
        //assign task list to the boards.list
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        //Now create an entry in the database
        mFireStore.collection(Constants.BOARD)
                //if we pass nothing it will create new entry and if we pass id it just override
            .document(board.documentId)
        //override it
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Task updated successfully")
                if (activity is TaskListActivity)
                     activity.addUpdateTaskListSuccess()
                else if (activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }.addOnFailureListener{
                exception->
                if (activity is TaskListActivity)
                     activity.hideProgressDialog()
                else if (activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating board",exception)
            }
    }
    fun updateUserProfileData(activity: Activity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap).addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"profile data update successfully")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                when(activity){
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is ProfileActivity->{
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener{
                exception->
                when(activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is ProfileActivity->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName,"Error while creating a board." ,exception)
                Toast.makeText(activity, "Error while updating profile!", Toast.LENGTH_SHORT).show()
            }

    }
    fun loadUserData(activity : Activity, readBoardList : Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null)
                    when(activity){
                        is SignInActivity->{
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity->{
                            activity.updateNavigationUserDetails(loggedInUser,readBoardList)
                        }
                        is ProfileActivity ->{
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }


            }.addOnFailureListener{
                    e->
                when(activity){
                    is SignInActivity->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                    is ProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName,"Error occurred ${e.message}")
            }

    }

    fun getCurrentUserId() : String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
//        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun getAssignedMemberListDetails(activity: Activity,assignedTo : ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            //we use where in constant id == user.id.
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val userList : ArrayList<User> = ArrayList()

                for (i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if(activity is MembersActivity)
                activity.setupMembersLists(userList)
                else if(activity is TaskListActivity)
                    activity.boardMemberDetailList(userList)
            }.addOnFailureListener{
                exception->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating members list",exception)
            }
    }
    fun getMemberDetail(activity: MembersActivity,email : String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document ->
                if (document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)

                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar(activity.getString(R.string.error_message_member_search))
                }
            }.addOnFailureListener{
                exception->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"error while getting user details",exception)
            }
    }
    fun assignMembersToBoard(activity: MembersActivity,board: Board,user : User){
        // for updating things we need to make a hashmap
        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARD)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener{
                exception->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating a board",exception)
            }


    }
}