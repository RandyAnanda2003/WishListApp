package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanAdapter(
    private val context: Context,
    private val plans: List<MyPlanData>,
    private val onEditClick: (MyPlanData) -> Unit,
    private val onDeleteClick: (MyPlanData) -> Unit
) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plans[position]
        holder.bind(plan, onEditClick, onDeleteClick)
    }

    override fun getItemCount() = plans.size

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvPlanTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvPlanDate)
        private val editButton: Button = itemView.findViewById(R.id.btnEditPlan)
        private val deleteButton: Button = itemView.findViewById(R.id.btnDeletePlan)

        fun bind(plan: MyPlanData, onEditClick: (MyPlanData) -> Unit, onDeleteClick: (MyPlanData) -> Unit) {
            titleTextView.text = plan.title
            dateTextView.text = plan.tanggal

            editButton.setOnClickListener { onEditClick(plan) }
            deleteButton.setOnClickListener { onDeleteClick(plan) }
        }
    }
}
