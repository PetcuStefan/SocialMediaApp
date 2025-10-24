package com.example.socialmediaapp.ui.feed

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialmediaapp.databinding.ActivityFeedBinding
import com.example.socialmediaapp.data.model.dto.PostWithUser
import com.example.socialmediaapp.data.model.entity.Post
import com.example.socialmediaapp.data.model.entity.User
import com.example.socialmediaapp.App
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var adapter: PostAdapter

    // ✅ New way: register for activity result
    private val newPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadFeed() // Refresh feed after new post is added
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Launch NewPostActivity using new API
        binding.fabAddPost.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            newPostLauncher.launch(intent)
        }

        loadFeed()
    }

    private fun loadFeed() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val posts = App.supabase.from("posts").select().decodeList<Post>()
                val users = App.supabase.from("users").select().decodeList<User>()

                val postsWithUser = posts.map { post ->
                    val user = users.firstOrNull { it.id == post.userId }
                    PostWithUser(
                        id = post.id,
                        userId = post.userId,
                        username = user?.username ?: "Unknown",
                        title = post.title,
                        description = post.description,
                        path = post.path,
                        createdAt = post.createdAt,
                        updatedAt = post.updatedAt
                    )
                }.sortedByDescending { it.createdAt }


                runOnUiThread { adapter.submitList(postsWithUser) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}