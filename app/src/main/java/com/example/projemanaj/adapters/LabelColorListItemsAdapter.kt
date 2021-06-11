package com.example.projemanaj.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanaj.R
import com.example.projemanaj.databinding.DialogListBinding
import com.example.projemanaj.databinding.ItemLayoutColorBinding

class LabelColorListItemsAdapter(private val context : Context,
                                 private val list : ArrayList<String>
                                 ,private val mSelectedColor : String)
    : RecyclerView.Adapter<LabelColorListItemsAdapter.MyViewHolder>(){

    var onItemClickListener : MyViewHolder.OnItemClickListener? = null

    class MyViewHolder(val binding : ItemLayoutColorBinding) : RecyclerView.ViewHolder(binding.root) {
        interface OnItemClickListener{
            fun onClick(position: Int,color :String)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_layout_color,parent,false)
        return MyViewHolder(ItemLayoutColorBinding.bind(view))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))

        if(item == mSelectedColor){
            holder.binding.ivSelectedColor.visibility = View.VISIBLE
        }else{
            holder.binding.ivSelectedColor.visibility = View.GONE
        }
        holder.itemView.setOnClickListener{
            if (onItemClickListener != null){
                onItemClickListener!!.onClick(position,item)

            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}