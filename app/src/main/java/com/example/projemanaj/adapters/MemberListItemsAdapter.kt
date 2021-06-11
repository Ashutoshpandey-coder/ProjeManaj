package com.example.projemanaj.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanaj.R
import com.example.projemanaj.databinding.ItemMemberBinding
import com.example.projemanaj.models.User
import com.example.projemanaj.utils.Constants

class MemberListItemsAdapter(private val context : Context,
                             private val list : ArrayList<User>) : RecyclerView.Adapter<MemberListItemsAdapter.MyViewHolder>() {

    private var onClickListener :OnClickListener? = null
    class MyViewHolder(val binding : ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_member,parent,false)
        return MyViewHolder(ItemMemberBinding.bind(view))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.binding.ivMemberImage)

        holder.binding.tvMembersName.text = model.name
        holder.binding.tvMembersEmail.text = model.email

        if(model.selected){
            holder.binding.ivSelectedMembers.visibility = View.VISIBLE
        }else{
            holder.binding.ivSelectedMembers.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                if (model.selected){
                    onClickListener!!.onClick(position,model,Constants.UN_SELECT)
                }else{
                    onClickListener!!.onClick(position,model,Constants.SELECT)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }
    interface OnClickListener{
        fun onClick(position: Int,user: User,action : String)
    }
}