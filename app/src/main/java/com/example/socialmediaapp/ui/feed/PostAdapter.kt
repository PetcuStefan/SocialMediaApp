package com.example.socialmediaapp.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaapp.data.model.dto.PostWithUser
import com.example.socialmediaapp.databinding.ItemPostBinding

class PostAdapter : ListAdapter<PostWithUser, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostWithUser) {
            binding.usernameTextView.text = post.username
            binding.titleTextView.text = post.title
            binding.descriptionTextView.text = post.description ?: ""
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PostWithUser>() {
        override fun areItemsTheSame(oldItem: PostWithUser, newItem: PostWithUser): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PostWithUser, newItem: PostWithUser): Boolean =
            oldItem == newItem
    }
}
