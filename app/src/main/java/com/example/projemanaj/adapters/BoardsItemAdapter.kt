package com.example.projemanaj.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanaj.R
import com.example.projemanaj.models.Board
import de.hdodenhof.circleimageview.CircleImageView

open class BoardsItemAdapter(private val context : Context,
                             private val list : ArrayList<Board>)
            : RecyclerView.Adapter<BoardsItemAdapter.MyViewHolder>() {

    private var onClickListener : OnClickListener? = null

    class MyViewHolder(view : View) : RecyclerView.ViewHolder(view){
     val circularImage : CircleImageView = view.findViewById(R.id.iv_board_image)
     val name : TextView = view.findViewById(R.id.tv_name)
     val createdBy : TextView = view.findViewById(R.id.tv_created_by)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_board,parent,false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder:MyViewHolder, position: Int) {
        val model = list[position]

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.circularImage)
        holder.name.text = model.name
        holder.createdBy.text = "Created by: ${model.createdBy}"

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position,model)

        }
    }
    interface OnClickListener{
        fun onClick(position: Int,model : Board)
    }
    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }
}