package com.example.socialmediaapp.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.dto.PostWithUser
import com.example.socialmediaapp.databinding.ItemPostBinding
import io.github.jan.supabase.storage.storage

class PostAdapter(
    private val onPostClick: (PostWithUser) -> Unit
) : ListAdapter<PostWithUser, PostAdapter.PostViewHolder>(DiffCallback()) {

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
            binding.usernameTextView.text = "Posted by: ${post.username}"
            binding.titleTextView.text = post.title
            binding.descriptionTextView.text = post.description ?: ""

            // Load image (if exists)
            if (!post.path.isNullOrEmpty()) {
                val imageUrl = if (post.path.startsWith("http"))
                    post.path
                else
                    App.supabase.storage.from("post-media").publicUrl(post.path!!)

                binding.postImageView.load(imageUrl)
                binding.postImageView.visibility = View.VISIBLE
            } else {
                binding.postImageView.visibility = View.GONE
            }

            // Handle click
            binding.root.setOnClickListener { onPostClick(post) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PostWithUser>() {
        override fun areItemsTheSame(oldItem: PostWithUser, newItem: PostWithUser): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PostWithUser, newItem: PostWithUser): Boolean =
            oldItem == newItem
    }
}
