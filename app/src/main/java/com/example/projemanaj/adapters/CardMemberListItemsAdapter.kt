package com.example.projemanaj.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanaj.R
import com.example.projemanaj.databinding.ItemCardSelectedMemberBinding
import com.example.projemanaj.models.SelectedMembers

class CardMemberListItemsAdapter(private val context : Context,
                                 private val list : ArrayList<SelectedMembers>,
                                    private val assignMembers : Boolean)
    : RecyclerView.Adapter<CardMemberListItemsAdapter.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    class ViewHolder(val binding : ItemCardSelectedMemberBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card_selected_member,parent,false)
        return ViewHolder(ItemCardSelectedMemberBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        if (position == list.size -1 && assignMembers){
            holder.binding.ivAddMembersImage.visibility = View.VISIBLE
            holder.binding.ivSelectedMemberImage.visibility = View.GONE
        }else{
            holder.binding.ivAddMembersImage.visibility = View.GONE
            holder.binding.ivSelectedMemberImage.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.binding.ivSelectedMemberImage)
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                onClickListener!!.onClick()
            }
        }

    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnClickListener{
        fun onClick()
    }
}