package com.example.socialmediaapp.data.repository

import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.entity.Comment
import com.example.socialmediaapp.data.model.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import io.github.jan.supabase.postgrest.from

class CommentRepository {

    suspend fun getCommentsForPost(postId: String): List<Comment> = withContext(Dispatchers.IO) {
        try {
            // Fetch all comments first
            val allComments = App.supabase.from("comments")
                .select()
                .decodeList<Comment>() ?: emptyList()

            // Filter locally by postId
            allComments.filter { it.postId == postId }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        try {
            App.supabase.from("users")
                .select()
                .decodeList<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Post a new top-level comment
     */
    suspend fun postComment(postId: String?, userId: String?, content: String): Boolean = withContext(Dispatchers.IO) {
        if (postId == null || userId == null) return@withContext false
        try {
            val newComment = Comment(
                id = UUID.randomUUID().toString(),
                upperId = null,
                postId = postId,
                userId = userId,
                content = content
            )
            App.supabase.from("comments").insert(newComment)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun postReply(postId: String, userId: String, content: String, parentId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val replyComment = Comment(
                id = UUID.randomUUID().toString(),
                upperId = parentId,
                postId = postId,
                userId = userId,
                content = content
            )
            App.supabase.from("comments").insert(replyComment)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
