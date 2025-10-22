package com.example.socialmediaapp.data.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String?,
    val path: String?,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)