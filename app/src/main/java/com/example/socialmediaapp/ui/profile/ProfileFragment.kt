package com.example.socialmediaapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.socialmediaapp.databinding.FragmentProfileBinding
import com.example.socialmediaapp.utils.SessionManager
import com.example.socialmediaapp.data.model.entity.User
import com.example.socialmediaapp.App
import com.example.socialmediaapp.ui.auth.AuthActivity
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        loadUserInfo()

        // Logout button
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadUserInfo() {
        val userId = sessionManager.getSession() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users: List<User> = App.supabase.from("users").select().decodeList<User>()
                val user = users.firstOrNull { it.id == userId }

                requireActivity().runOnUiThread {
                    if (user != null) {
                        binding.tvUsername.text = user.username
                        // Optional: load profile picture using Glide or Coil if user.profilePicture != null
                    } else {
                        binding.tvUsername.text = "Unknown User"
                    }
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

