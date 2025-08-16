package com.example.notes_manager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.R
import com.example.notes_manager.domain.Post

class NotesAdapter : ListAdapter<Post, NotesAdapter.VH>(DIFF) {
    object DIFF : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(o: Post, n: Post) = o.id == n.id
        override fun areContentsTheSame(o: Post, n: Post) = o == n
    }
    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
    ) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val subtitle: TextView = itemView.findViewById(R.id.subtitle)
        fun bind(p: Post) {
            title.text = p.title
            subtitle.text = "id=${p.id}, author=${p.authorId} • ${p.body.take(60)}"
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(p)
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
