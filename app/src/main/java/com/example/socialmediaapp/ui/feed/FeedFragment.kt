package com.example.socialmediaapp.ui.feed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialmediaapp.App
import com.example.socialmediaapp.data.model.dto.PostWithUser
import com.example.socialmediaapp.data.model.entity.Post
import com.example.socialmediaapp.data.model.entity.User
import com.example.socialmediaapp.databinding.FragmentFeedBinding
import com.example.socialmediaapp.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.ui.postDetails.PostDetailActivity
import io.github.jan.supabase.postgrest.from

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter

    // Activity Result API for NewPostActivity
    private val newPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            loadFeed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        adapter = PostAdapter { post ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
                putExtra("post_id", post.id)
                putExtra("username", post.username)
                putExtra("title", post.title)
                putExtra("description", post.description)
                putExtra("path", post.path)
            }
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Show FAB in MainActivity and assign click to launch NewPostActivity
        (requireActivity() as? MainActivity)?.showFab {
            val intent = Intent(requireContext(), NewPostActivity::class.java)
            newPostLauncher.launch(intent)
        }

        loadFeed()
        return binding.root
    }

    /** Fetch posts from Supabase and map to PostWithUser */
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

                requireActivity().runOnUiThread {
                    adapter.submitList(postsWithUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
