package com.example.socialmediaapp.ui.feed

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.entity.Post
import com.example.socialmediaapp.databinding.ActivityNewPostBinding
import com.example.socialmediaapp.utils.SessionManager
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPostBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim().takeIf { it.isNotEmpty() }

            if (title.isNotEmpty()) {
                submitPost(title, description)
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun submitPost(title: String, description: String?) {
        val userId = sessionManager.getSession() ?: return

        // Create ISO-8601 timestamp for API 24+
        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val post = Post(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            description = description,
            createdAt = timestamp,
            updatedAt = timestamp,
            path=null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                App.supabase
                    .from("posts")
                    .insert(post)

                runOnUiThread {
                    Toast.makeText(this@NewPostActivity, "Post created!", Toast.LENGTH_SHORT).show()
                    // Set result so FeedActivity knows to refresh
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@NewPostActivity, "Error creating post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



}
