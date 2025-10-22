package com.example.socialmediaapp.data.repository

import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.entity.User
import io.github.jan.supabase.postgrest.from

class UserRepository {
    private val client = App.supabase

    suspend fun getAllUsers(): List<User> {
        return client.from("users").select().decodeList<User>()
    }
}
