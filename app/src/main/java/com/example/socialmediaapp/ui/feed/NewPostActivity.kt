package com.example.socialmediaapp.ui.feed

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.entity.Post
import com.example.socialmediaapp.databinding.ActivityNewPostBinding
import com.example.socialmediaapp.utils.SessionManager
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import io.github.jan.supabase.postgrest.from

class NewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPostBinding
    private lateinit var sessionManager: SessionManager

    private var selectedPhotoUri: Uri? = null
    private var selectedPhotoFileName: String? = null

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            val name = it.lastPathSegment?.split("/")?.last() ?: "selected_image"
            selectedPhotoFileName = name
            binding.tvSelectedMedia.text = name
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnPickMedia.setOnClickListener {
            pickPhotoLauncher.launch("image/*") // only images
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim().takeIf { it.isNotEmpty() }
            val link = binding.etLink.text.toString().trim().takeIf { it.isNotEmpty() }

            if (title.isNotEmpty()) {
                submitPost(title, description, selectedPhotoUri, link)
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitPost(
        title: String,
        description: String?,
        photoUri: Uri?,
        link: String?
    ) {
        val userId = sessionManager.getSession() ?: return

        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.getDefault()
        ).apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var mediaPath: String? = null

                // Upload photo if selected
                photoUri?.let { uri ->
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                    val fileName = "${UUID.randomUUID()}.${contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"}"

                    App.supabase.storage
                        .from("post-media")
                        .upload(fileName, bytes, upsert = true)

                    mediaPath = fileName
                }

                // If no photo, use the link if provided
                if (mediaPath == null) mediaPath = link

                val post = Post(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    description = description,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    path = mediaPath
                )

                App.supabase
                    .from("posts")
                    .insert(post)

                runOnUiThread {
                    Toast.makeText(this@NewPostActivity, "Post created!", Toast.LENGTH_SHORT).show()
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
