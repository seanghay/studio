package com.seanghay.studioexample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story.view.*
import java.io.File

class SlideAdapter(var items: List<SlideEntity>) : RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {

    var selectionChange: () -> Unit = {}
    var onLongPress: () -> Unit = {}


    var selectedAt = -1
        set(value) {
            field = value
            selectionChange()
        }


    fun select(at: Int) {
        if (at == selectedAt) return
        notifyItemChanged(selectedAt)
        selectedAt = at
        notifyItemChanged(selectedAt)
    }

    fun deselectAll() {
        selectedAt = -1
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        return SlideViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_story
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.bind(items[position])
    }

    private fun fireLongPress() {
        onLongPress()
    }

    inner class SlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                select(adapterPosition)
            }

            itemView.setOnLongClickListener {
                select(adapterPosition)
                fireLongPress()
                true
            }
        }

        fun bind(item: SlideEntity) {
            itemView.isSelected = selectedAt == adapterPosition
            Picasso.get()
                .load(File(item.path))
                .fit()
                .centerCrop()
                .into(itemView.imageView)

            itemView.imageView.alpha = if (itemView.isSelected) 1f else .7f
        }
    }
}