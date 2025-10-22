package com.example.socialmediaapp.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostWithComments(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val path: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val comments: List<CommentWithUser> = emptyList()
)