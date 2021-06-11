package com.example.projemanaj.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanaj.R
import com.example.projemanaj.activities.TaskListActivity
import com.example.projemanaj.databinding.ItemCardBinding
import com.example.projemanaj.databinding.ItemTaskBinding
import com.example.projemanaj.models.Card
import com.example.projemanaj.models.SelectedMembers

class CardListItemsAdapter(private val context : Context,
                           private val list : ArrayList<Card> ) : RecyclerView.Adapter<CardListItemsAdapter.MyViewHolder>() {
    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemCardBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_card,parent,false)))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        holder.binding.tvCardName.text = model.name

        if (model.labelColor.isNotEmpty()){
            holder.binding.viewLabelColor.visibility = View.VISIBLE
            holder.binding.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
        }else{
            holder.binding.viewLabelColor.visibility = View.GONE
        }
        //this is for the recycler view of members in card in TaskList Activity
        if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0){
            val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

            for(i in context.mAssignedMembersDetailList.indices){
                for (j in model.assignedTo){
                    if (context.mAssignedMembersDetailList[i].id == j){
                        val selectedMembers = SelectedMembers(
                            context.mAssignedMembersDetailList[i].id,
                            context.mAssignedMembersDetailList[i].image
                        )
                        selectedMembersList.add(selectedMembers)
                    }
                }

            }
            if (selectedMembersList.size >0){
                //if in member list only one member are there who is currently logged in then no need to show his id in list
                if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                    holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                }else{
                    holder.binding.rvCardSelectedMembersList.visibility = View.VISIBLE
                    holder.binding.rvCardSelectedMembersList.layoutManager = GridLayoutManager(context , 4)
                    val adapter = CardMemberListItemsAdapter(context,selectedMembersList,false)
                    holder.binding.rvCardSelectedMembersList.adapter = adapter

                    adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            if (onClickListener != null){
                                onClickListener!!.onClick(position)
                            }
                        }

                    })
                }
            }else{
                holder.binding.rvCardSelectedMembersList.visibility = View.GONE
            }

        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                onClickListener!!.onClick(position)
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
        fun onClick(cardPosition: Int)
    }
    class MyViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root){

    }
}