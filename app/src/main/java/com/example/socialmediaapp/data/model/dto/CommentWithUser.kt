package com.example.socialmediaapp.data.model.dto

import com.example.socialmediaapp.data.model.entity.User
import kotlinx.serialization.Serializable

@Serializable
data class CommentWithUser(
    val id: String,
    val content: String,
    val createdAt: String? = null,
    val user: User
)