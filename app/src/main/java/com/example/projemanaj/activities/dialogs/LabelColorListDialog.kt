package com.example.projemanaj.activities.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanaj.R
import com.example.projemanaj.adapters.LabelColorListItemsAdapter


abstract class LabelColorListDialog (
    context: Context,
    private var list : ArrayList<String>,
    private val title : String = "",
    private var mSelectedColor : String = ""
        ) : Dialog(context){


    private var adapter : LabelColorListItemsAdapter? = null
    @SuppressLint("InflateParams")
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
        adapter = LabelColorListItemsAdapter(context,list,mSelectedColor)
        rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.MyViewHolder.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }

    }
    protected abstract fun onItemSelected(color :String)
}