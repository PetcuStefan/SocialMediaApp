package com.example.socialmediaapp.ui.postDetails

import android.net.Uri
import android.view.View
import android.widget.MediaController
import coil.load
import com.example.socialmediaapp.App
import com.example.socialmediaapp.R
import com.example.socialmediaapp.databinding.ActivityPostDetailBinding
import io.github.jan.supabase.storage.storage

class PostMediaHandler(
    private val context: android.content.Context,
    private val binding: ActivityPostDetailBinding
) {
    fun handleMedia(path: String?) {
        if (path.isNullOrEmpty()) {
            binding.ivMedia.visibility = View.GONE
            binding.videoView.visibility = View.GONE
            return
        }

        if (path.endsWith(".mp4") || path.endsWith(".mov")) {
            binding.videoView.visibility = View.VISIBLE
            binding.ivMedia.visibility = View.GONE
            val mediaController = MediaController(context)
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
    }
}
