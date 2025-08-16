package com.example.notes_manager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes_manager.R
import com.example.notes_manager.domain.Post as DomainPost

class NotesAdapter : ListAdapter<DomainPost, NotesAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<DomainPost>() {
        override fun areItemsTheSame(o: DomainPost, n: DomainPost) = o.id == n.id
        override fun areContentsTheSame(o: DomainPost, n: DomainPost) = o == n
    }

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
    ) {
        private val tvTitle: TextView = itemView.findViewById(R.id.title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.subtitle)
        fun bind(p: DomainPost) {
            tvTitle.text = p.title
            tvSubtitle.text = "id=${p.id}, author=${p.authorId} • ${p.body.take(60)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
