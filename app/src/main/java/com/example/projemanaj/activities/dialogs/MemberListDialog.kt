package com.example.projemanaj.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanaj.R
import com.example.projemanaj.adapters.LabelColorListItemsAdapter
import com.example.projemanaj.adapters.MemberListItemsAdapter
import com.example.projemanaj.models.User

abstract class MemberListDialog(
    context : Context,
    private val list : ArrayList<User>,
    private val title : String= "",
) : Dialog(context) {

    private var adapter : MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list,null)

        val tvTitle : TextView = view.findViewById(R.id.tv_title)
        val rvList : RecyclerView = view.findViewById(R.id.rv_list)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(tvTitle,rvList)

    }
    private fun setUpRecyclerView(tvTitle : TextView, rvList : RecyclerView){
        tvTitle.text = title
        rvList.layoutManager = LinearLayoutManager(context)
        adapter = MemberListItemsAdapter(context,list)
        rvList.adapter = adapter

        adapter!!.setOnClickListener(object : MemberListItemsAdapter.OnClickListener{
            override fun onClick(position: Int,user : User,action: String) {
                dismiss()
                onItemSelected(user,action)
            }
        })

    }
    protected abstract fun onItemSelected(user: User,action : String)

}