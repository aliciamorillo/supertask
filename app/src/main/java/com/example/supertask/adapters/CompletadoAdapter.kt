package com.example.supertask.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.supertask.R
import com.example.supertask.models.Task
import kotlinx.android.synthetic.main.item_task.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CompletadoAdapter(
    private val mDataSet: ArrayList<Task>,
    var eliminar: (task: Task) -> Unit,
    var click: (tarea: Task) -> Unit
) :
    RecyclerView.Adapter<CompletadoAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return MainViewHolder(v)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val miTarea = mDataSet[position]
        holder.addData(miTarea)

        holder.itemView.setOnLongClickListener {
            eliminar(miTarea)
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener {
            click(miTarea)
        }

    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    inner class MainViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun addData(data: Task) {
            itemView.tvTitle.text = data.name
            itemView.tvCategory.text = data.category

            itemView.tvDate.text = formatDate(data.date, "dd/MM/YYYY HH:mm")

            if (data.isPriority) {
                itemView.tvPriority.text = "Prioritario"
                itemView.tvPriority.visibility = View.VISIBLE
            } else {
                itemView.tvPriority.text = ""
                itemView.tvPriority.visibility = View.GONE
            }

            if (data.isPriority) {
                itemView.btPriority.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green))
            } else {
                itemView.btPriority.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.grey))
            }

        }

        fun formatDate(date: Date, formatTarget: String): String? {
            val formatter = SimpleDateFormat(formatTarget, Locale.getDefault())
            return formatter.format(date)
        }

    }
}
