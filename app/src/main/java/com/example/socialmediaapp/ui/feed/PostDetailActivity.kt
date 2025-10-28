package com.example.socialmediaapp.ui.feed

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.socialmediaapp.App
import com.example.socialmediaapp.R
import com.example.socialmediaapp.data.model.entity.Comment
import com.example.socialmediaapp.databinding.ActivityPostDetailBinding
import com.example.socialmediaapp.utils.SessionManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var sessionManager: SessionManager

    private var postId: String? = null
    private var username: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        userId = sessionManager.getSession()

        postId = intent.getStringExtra("post_id")
        username = intent.getStringExtra("username")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val path = intent.getStringExtra("path")

        // Set post details
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.tvPostInfo.text = "Posted by: $username"

        handleMedia(path)
        loadComments()

        binding.btnPostComment.setOnClickListener {
            val content = binding.etAddComment.text.toString().trim()
            if (content.isNotEmpty()) {
                postComment(content)
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleMedia(path: String?) {
        if (!path.isNullOrEmpty()) {
            if (path.endsWith(".mp4") || path.endsWith(".mov")) {
                binding.videoView.visibility = View.VISIBLE
                binding.ivMedia.visibility = View.GONE
                val mediaController = MediaController(this)
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setMediaController(mediaController)
                binding.videoView.setVideoURI(Uri.parse(path))
                binding.videoView.start()
            } else {
                binding.ivMedia.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE

                val url = if (path.startsWith("http")) path
                else App.supabase.storage.from("post-media").publicUrl(path)

                binding.ivMedia.load(url) {
                    placeholder(R.drawable.ic_profile_placeholder)
                    error(R.drawable.ic_profile_placeholder)
                }
            }
        } else {
            binding.ivMedia.visibility = View.GONE
            binding.videoView.visibility = View.GONE
        }
    }

    private fun loadComments() {
        if (postId.isNullOrEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch all comments
                val comments = App.supabase.from("comments")
                    .select()
                    .decodeList<com.example.socialmediaapp.data.model.entity.Comment>()

                // Fetch all users
                val users = App.supabase.from("users")
                    .select()
                    .decodeList<com.example.socialmediaapp.data.model.entity.User>()

                // Filter locally by postId and join with users
                val commentsWithUser = comments
                    .filter { it.postId == postId }
                    .mapNotNull { comment ->
                        val user = users.firstOrNull { u -> u.id == comment.userId }
                        user?.let {
                            com.example.socialmediaapp.data.model.dto.CommentWithUser(
                                id = comment.id,
                                content = comment.content,
                                createdAt = comment.createdAt,
                                user = it
                            )
                        }
                    }
                    .sortedByDescending { it.createdAt ?: "" }

                withContext(Dispatchers.Main) {
                    displayComments(commentsWithUser)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Failed to load comments: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun displayComments(comments: List<com.example.socialmediaapp.data.model.dto.CommentWithUser>) {
        binding.commentsContainer.removeAllViews()

        for (comment in comments) {
            val tv = TextView(this)
            tv.text = "${comment.user.username}: ${comment.content}"
            tv.textSize = 14f
            tv.setPadding(0, 8, 0, 8)
            binding.commentsContainer.addView(tv)
        }
    }

    private fun postComment(content: String) {
        if (postId == null || userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newComment = Comment(
                    id = UUID.randomUUID().toString(),
                    upperId = null,
                    postId = postId!!,
                    userId = userId!!,
                    content = content
                )

                App.supabase.from("comments").insert(newComment)

                withContext(Dispatchers.Main) {
                    binding.etAddComment.text.clear()
                    loadComments()
                    Toast.makeText(this@PostDetailActivity, "Comment posted", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Failed to post comment: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
