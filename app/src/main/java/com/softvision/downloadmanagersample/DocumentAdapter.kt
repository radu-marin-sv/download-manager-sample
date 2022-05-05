package com.softvision.downloadmanagersample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DocumentAdapter : ListAdapter<Document, DocumentAdapter.DocumentViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        return DocumentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_document, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(document: Document) {
            (itemView as TextView).text = "${document.title}:\n ${document.uri}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document) = oldItem.uri == newItem.uri

        override fun areContentsTheSame(oldItem: Document, newItem: Document) = oldItem == newItem
    }


}