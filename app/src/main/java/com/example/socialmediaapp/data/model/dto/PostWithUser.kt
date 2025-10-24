package com.example.socialmediaapp.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostWithUser(
    val id: String,
    val userId: String,
    val username: String,
    val title: String,
    val description: String?,
    val path: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)