package com.example.socialmediaapp.data.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.entity.User
import com.example.socialmediaapp.utils.SessionManager
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class AuthRepository(
    private val sessionManager: SessionManager
) {

    /**
     * Register a new user.
     * Returns true when registration succeeded, false when username already exists or insert failed.
     */
    suspend fun register(username: String, password: String): Boolean {
        // 1) fetch any users with this username (we use decodeList and filter in Kotlin to avoid chaining issues)
        val existingUsers = App.supabase
            .from("users")
            .select()
            .decodeList<User>()

        if (existingUsers.any { it.username.equals(username, ignoreCase = true) }) {
            return false // username taken
        }

        // 2) Hash the password (bcrypt). Do this on an IO dispatcher.
        val hashed = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        // 3) Prepare new user map (id as uuid string)
        val newUser = mapOf(
            "id" to UUID.randomUUID().toString(),
            "username" to username,
            "password" to hashed
        )

        // 4) Insert into users table
        return try {
            App.supabase
                .from("users")
                .insert(newUser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    /**
     * Login by username + password. On success stores session and returns true.
     */
    suspend fun login(username: String, password: String): Boolean {
        // 1) Fetch the user (filter in Kotlin)
        val users = App.supabase
            .from("users")
            .select()
            .decodeList<User>()

        val user = users.firstOrNull { it.username.equals(username, ignoreCase = true) } ?: return false

        // 2) Verify password using bcrypt
        val verification = BCrypt.verifyer().verify(password.toCharArray(), user.password)
        if (verification.verified) {
            // 3) Save session (SessionManager is an instance; it handles encrypted prefs)
            sessionManager.saveSession(user.id)
            return true
        }

        return false
    }

    /**
     * Logout the current user: clear local session.
     */
    fun logout() {
        sessionManager.clearSession()
    }
}
