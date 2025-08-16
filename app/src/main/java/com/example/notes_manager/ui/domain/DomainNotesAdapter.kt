package com.example.notes_manager.ui.domain

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.R
import com.example.notes_manager.domain.model.Note

class DomainNotesAdapter : ListAdapter<Note, DomainNotesAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(o: Note, n: Note) = o.id == n.id
        override fun areContentsTheSame(o: Note, n: Note) = o == n
    }

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_note_domain, parent, false)
    ) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvTags: TextView  = itemView.findViewById(R.id.tvTags)
        private val tvBody: TextView  = itemView.findViewById(R.id.tvBody)

        fun bind(n: Note) {
            tvTitle.text = n.title
            tvBody.text = n.body
            tvTags.text = if (n.tags.isEmpty()) "без тегов" else n.tags.joinToString(" • ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
