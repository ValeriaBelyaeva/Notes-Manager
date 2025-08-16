package com.example.notes_manager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.R
import com.example.notes_manager.data.local.model.NoteWithTags

class NotesRoomAdapter : ListAdapter<NoteWithTags, NotesRoomAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<NoteWithTags>() {
        override fun areItemsTheSame(o: NoteWithTags, n: NoteWithTags) = o.note.id == n.note.id
        override fun areContentsTheSame(o: NoteWithTags, n: NoteWithTags) = o == n
    }

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_note_room, parent, false)
    ) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvTags: TextView  = itemView.findViewById(R.id.tvTags)
        private val tvBody: TextView  = itemView.findViewById(R.id.tvBody)

        fun bind(it: NoteWithTags) {
            tvTitle.text = it.note.title
            tvBody.text = it.note.body
            val tags = it.tags.map { t -> t.name }.joinToString(" • ")
            tvTags.text = if (tags.isBlank()) "без тегов" else tags
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
