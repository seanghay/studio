package com.seanghay.studioexample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seanghay.studio.gles.transition.Transition
import kotlinx.android.synthetic.main.item_transition.view.*

class TransitionsAdapter(
    var items: List<Transition>
): RecyclerView.Adapter<TransitionsAdapter.TransitionViewHolder>() {


    var selectionChange: () -> Unit = {}
    var onLongPressed: () -> Unit = {}


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




    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: TransitionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransitionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transition, parent, false)
        return TransitionViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_transition
    }

    override fun getItemCount(): Int = items.size

    private fun longPressFired() {
        onLongPressed()
    }

    inner class TransitionViewHolder(view: View): RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                select(adapterPosition)
            }

            itemView.setOnLongClickListener {
                select(adapterPosition)
                longPressFired()
                true
            }
        }

        fun bind(transition: Transition) {
            with(itemView) {
                textView.text = transition.name
                isSelected = selectedAt == adapterPosition
            }
        }
    }
}