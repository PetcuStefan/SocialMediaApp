package com.example.socialmediaapp.ui.postDetails

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.data.repository.CommentRepository
import com.example.socialmediaapp.databinding.ActivityPostDetailBinding
import com.example.socialmediaapp.utils.SessionManager
import kotlinx.coroutines.*

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mediaHandler: PostMediaHandler
    private lateinit var commentRepo: CommentRepository
    private lateinit var commentRenderer: CommentRenderer

    private var postId: String? = null
    private var userId: String? = null
    private var username: String? = null

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
        val mediaPath = intent.getStringExtra("path")

        // Post info
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.tvPostInfo.text = "Posted by: $username"

        // Handlers & Repos
        mediaHandler = PostMediaHandler(this, binding)
        commentRepo = CommentRepository()
        commentRenderer = CommentRenderer(binding.commentsContainer) { parentId, replyText ->
            replyToComment(replyText, parentId)
        }

        mediaHandler.handleMedia(mediaPath)
        loadComments()

        binding.btnPostComment.setOnClickListener {
            val content = binding.etAddComment.text.toString().trim()
            if (content.isNotEmpty()) postComment(content)
            else Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComments() {
        val safePostId = postId ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val comments = commentRepo.getCommentsForPost(safePostId)
            val users = commentRepo.getUsers()
            val merged = mergeCommentsAndUsers(comments, users)
            withContext(Dispatchers.Main) {
                commentRenderer.render(merged)
            }
        }
    }

    private fun mergeCommentsAndUsers(
        comments: List<com.example.socialmediaapp.data.model.entity.Comment>,
        users: List<com.example.socialmediaapp.data.model.entity.User>
    ) = comments.mapNotNull { comment ->
        val user = users.firstOrNull { it.id == comment.userId }
        user?.let {
            com.example.socialmediaapp.data.model.dto.CommentWithUser(
                id = comment.id,
                content = comment.content,
                createdAt = comment.createdAt,
                upperId = comment.upperId,
                user = it
            )
        }
    }.sortedByDescending { it.createdAt ?: "" }

    private fun postComment(content: String) {
        val safePostId = postId ?: return
        val safeUserId = userId ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val success = commentRepo.postComment(safePostId, safeUserId, content)
            withContext(Dispatchers.Main) {
                if (success) {
                    binding.etAddComment.text.clear()
                    loadComments()
                } else Toast.makeText(this@PostDetailActivity, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun replyToComment(content: String, parentCommentId: String) {
        val safePostId = postId ?: return
        val safeUserId = userId ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val success = commentRepo.postReply(safePostId, safeUserId, content, parentCommentId)
            withContext(Dispatchers.Main) {
                if (success) loadComments()
                else Toast.makeText(this@PostDetailActivity, "Failed to post reply", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
