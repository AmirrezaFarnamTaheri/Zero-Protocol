package com.ghostbattery.ui.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostbattery.R
import com.ghostbattery.data.model.HelpItem

class HelpAdapter(private val items: List<HelpItem>) :
    RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    class HelpViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_help_title)
        val tvContent: TextView = view.findViewById(R.id.tv_help_content)
        val ivArrow: ImageView = view.findViewById(R.id.iv_arrow)
        val container: View = view.findViewById(R.id.card_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_topic, parent, false)
        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content

        // Handle Expansion
        val visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        holder.tvContent.visibility = visibility

        // Rotate Arrow
        holder.ivArrow.rotation = if (item.isExpanded) 180f else 0f

        holder.container.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = items.size
}
