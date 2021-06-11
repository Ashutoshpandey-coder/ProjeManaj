package com.example.projemanaj.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanaj.R
import com.example.projemanaj.activities.TaskListActivity
import com.example.projemanaj.databinding.ItemTaskBinding
import com.example.projemanaj.models.Task
import java.util.*

open class TaskListItemAdapter(
    private val context: Context,
    private val list: ArrayList<Task>
) :
    RecyclerView.Adapter<TaskListItemAdapter.MyViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    class MyViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        //this is all xml stuff
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        //set that the screen only takes 70 percent of the screen
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        //set the margin to left and right
        layoutParams.setMargins(
            ((15.toDp()).toPx()), 0, (40.toPx()).toDp(), 0
        )
        view.layoutParams = layoutParams

        return MyViewHolder(ItemTaskBinding.bind(view))

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        if (position == list.size - 1) {
            //if we don't have any entry
            //only add card show visible
            holder.binding.tvAddTaskList.visibility = View.VISIBLE
            //and whole linear layout is invisible
            holder.binding.llTaskItem.visibility = View.GONE
        } else {
            //if it has an entry
            holder.binding.tvAddTaskList.visibility = View.GONE
            holder.binding.llTaskItem.visibility = View.VISIBLE
        }
        //set the title of task which is created by user
        holder.binding.tvTaskListTitle.text = model.title
        holder.binding.tvAddTaskList.setOnClickListener {
            holder.binding.tvAddTaskList.visibility = View.GONE
            holder.binding.cvAddTaskListName.visibility = View.VISIBLE
        }
        //if someone clicks on close image button
        holder.binding.ibCloseListName.setOnClickListener {
            //only add card show visible
            holder.binding.tvAddTaskList.visibility = View.VISIBLE
            //and whole linear layout is invisible
            holder.binding.cvAddTaskListName.visibility = View.GONE
        }
        //if someone clicks on done button
        holder.binding.ibDoneListName.setOnClickListener {
            //create entry in database and display the task list
            val listName = holder.binding.etTaskListName.text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.createTaskList(listName)
                }
            } else {
                Toast.makeText(context, "Please Enter a List Name", Toast.LENGTH_SHORT).show()
            }
        }
//        for editing
        holder.binding.ibEditListName.setOnClickListener {
            holder.binding.etEditTaskListName.setText(model.title)
            holder.binding.llTitleView.visibility = View.GONE
            holder.binding.cvEditTaskListName.visibility = View.VISIBLE
        }
        //cancel button
        holder.binding.ibCloseEditableView.setOnClickListener {
            holder.binding.cvEditTaskListName.visibility = View.GONE
            holder.binding.llTitleView.visibility = View.VISIBLE
        }
        //done editing button
        holder.binding.ibDoneEditListName.setOnClickListener {
            //implement editing
            val listName = holder.binding.etEditTaskListName.text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.updateTaskList(position, listName, model)
                }
            } else {
                Toast.makeText(context, "Please enter a list name", Toast.LENGTH_SHORT).show()
            }

        }
        //deleting task list name title
        holder.binding.ibDeleteList.setOnClickListener {
            //delete the list
            alertDialogForDeleteList(position, model.title)
        }
        //for addings cards
        holder.binding.tvAddCard.setOnClickListener {
            holder.binding.tvAddCard.visibility = View.GONE
            holder.binding.cvAddCard.visibility = View.VISIBLE
        }
        //cancel icon
        holder.binding.ibCloseCardName.setOnClickListener {
            holder.binding.tvAddCard.visibility = View.VISIBLE
            holder.binding.cvAddCard.visibility = View.GONE
        }
        //done icon
        holder.binding.ibDoneCardName.setOnClickListener {
            val cardName = holder.binding.etCardName.text.toString()
            if (cardName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    //Add a card
                    context.addCardToTaskList(position, cardName)
                }
            } else {
                Toast.makeText(context, "Please enter a card name", Toast.LENGTH_SHORT).show()
            }
        }
        holder.binding.rvCardList.layoutManager = LinearLayoutManager(context)
        holder.binding.rvCardList.setHasFixedSize(true)
        val adapter = CardListItemsAdapter(context, model.cards)
        holder.binding.rvCardList.adapter = adapter

        adapter.setOnClickListener(object : CardListItemsAdapter.OnClickListener {
            override fun onClick(cardPosition: Int) {
                if (context is TaskListActivity) {
                    context.cardDetails(position, cardPosition)
                }
            }

        })

        //drag functionality between cards vertical movement
        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        holder.binding.rvCardList.addItemDecoration(dividerItemDecoration)

        //Item touch helper
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                dragger: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val draggedPosition = dragger.adapterPosition
                val targetPosition = target.adapterPosition

                if (mPositionDraggedFrom == -1) {
                    mPositionDraggedFrom = draggedPosition
                }
                mPositionDraggedTo = targetPosition
                Collections.swap(list[position].cards, draggedPosition, targetPosition)
                adapter.notifyItemMoved(draggedPosition, targetPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            // once the drag and dropping is over
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedTo != mPositionDraggedFrom) {
                    (context as TaskListActivity).updateCardsInTaskList(
                        position,
                        list[position].cards
                    )
                }
                mPositionDraggedFrom = -1
                mPositionDraggedTo = -1
            }
        })
        helper.attachToRecyclerView(holder.binding.rvCardList)
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set the title for alert dialog
        builder.setTitle("Alert")
        //set the message
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing postive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()//dialog will be dismissed
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    override fun getItemCount(): Int {
        return list.size
    }

    //Get the density of the screen and convert it into Integer
    // Actually we want to see the cards in 70% of the screen
    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
}