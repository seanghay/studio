package com.seanghay.studioexample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story.view.*
import java.io.File

class SlideAdapter(var items: List<SlideEntity>) : RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {

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

    inner class SlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: SlideEntity) {
            Picasso.with(itemView.context)
                .load(File(item.path))
                .fit()
                .centerCrop()
                .into(itemView.imageView)
        }
    }
}