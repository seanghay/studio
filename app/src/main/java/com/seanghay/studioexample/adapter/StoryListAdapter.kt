package com.seanghay.studioexample.adapter

import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.color
import androidx.core.text.scale
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seanghay.studioexample.R
import com.seanghay.studioexample.StoryEntity
import kotlinx.android.synthetic.main.item_video.view.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class StoryListAdapter(
    var items: List<StoryEntity> = emptyList()
) : RecyclerView.Adapter<StoryListAdapter.ViewHolder>() {


    var onItemClicked: (StoryEntity) -> Unit = {}
    var onSharedClick: (StoryEntity) -> Unit = {}
    var onDeleteClick: (StoryEntity) -> Unit = {}


    private val retriver = MediaMetadataRetriever()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun patch(items: List<StoryEntity>) {

        val callback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return this@StoryListAdapter.items[oldItemPosition].id == items[newItemPosition].id
            }

            override fun getOldListSize(): Int {
                return this@StoryListAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return items.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return this@StoryListAdapter.items[oldItemPosition] == items[newItemPosition]
            }
        }

        val diff = DiffUtil.calculateDiff(callback)
        diff.dispatchUpdatesTo(this)
        this.items = items
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val popupMenu = PopupMenu(itemView.context, itemView.more)

        init {
            popupMenu.inflate(R.menu.more)

            itemView.setOnClickListener {
                onItemClicked(items[adapterPosition])
            }

            itemView.more.setOnClickListener {
                popupMenu.show()
            }

            popupMenu.setOnMenuItemClickListener {
                if (it.itemId == R.id.share) {
                    val item = items[adapterPosition]
                    onSharedClick(item)
                    true

                } else if (it.itemId == R.id.delete) {
                    val item = items[adapterPosition]
                    onDeleteClick(item)
                    true
                } else false
            }
        }

        fun bind(item: StoryEntity) {
            val relativeTime = DateUtils.getRelativeTimeSpanString(item.createdAt).toString()
            retriver.setDataSource(item.path)
            val duration =
                retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
            itemView.title.text = SpannableStringBuilder(item.title)
                .append(" • ")
                .scale(.8f) {
                    color(Color.parseColor("#3a3a3a")) {
                        append(relativeTime)
                    }
                }

            itemView.subtitle.setText(
                SpannableStringBuilder(formatDate(item.createdAt) + " • ")
                    .color(Color.BLACK) { append(formatDuration(duration)) }
            )


            Glide.with(itemView)
                .load(item.path)
                .centerCrop()
                .into(itemView.thumbnail)

        }


        private fun formatDuration(millis: Long): String {
            return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        millis
                    )
                ),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        millis
                    )
                )
            )
        }

        private fun formatDate(time: Long): String {
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(Date(time))
        }

    }


}