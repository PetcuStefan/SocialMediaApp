package com.example.socialmediaapp.ui.feed

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.socialmediaapp.App
import com.example.socialmediaapp.R
import com.example.socialmediaapp.databinding.ActivityPostDetailBinding
import io.github.jan.supabase.storage.storage

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("username")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val path = intent.getStringExtra("path")

        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.tvPostInfo.text = "Posted by: ${username}"

        if (!path.isNullOrEmpty()) {
            if (path.endsWith(".mp4") || path.endsWith(".mov")) {
                binding.videoView.visibility = View.VISIBLE
                binding.ivMedia.visibility = View.GONE
                val mediaController = MediaController(this)
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setMediaController(mediaController)
                binding.videoView.setVideoURI(Uri.parse(path))
                binding.videoView.start()
            } else {
                binding.ivMedia.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE

                val url = if (path.startsWith("http")) path
                else App.supabase.storage.from("post-media").publicUrl(path)

                binding.ivMedia.load(url) {
                    placeholder(R.drawable.ic_profile_placeholder)
                    error(R.drawable.ic_profile_placeholder)
                }
            }
        } else {
            binding.ivMedia.visibility = View.GONE
            binding.videoView.visibility = View.GONE
        }
    }
}
