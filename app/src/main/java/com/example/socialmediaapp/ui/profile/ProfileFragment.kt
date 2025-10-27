package com.example.socialmediaapp.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.load
import coil.request.CachePolicy
import com.example.socialmediaapp.App
import com.example.socialmediaapp.R
import com.example.socialmediaapp.data.model.entity.User
import com.example.socialmediaapp.databinding.FragmentProfileBinding
import com.example.socialmediaapp.ui.auth.AuthActivity
import com.example.socialmediaapp.utils.SessionManager
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadProfilePicture(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        loadUserProfile()

        binding.btnChooseProfilePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val userId = sessionManager.getSession() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val users = App.supabase.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .decodeList<User>()
            val user = users.firstOrNull() ?: return@launch
            val username = user.username
            val profilePicturePath = user.profilePicture

            withContext(Dispatchers.Main) {
                binding.tvUsername.text = username
                if (!profilePicturePath.isNullOrEmpty()) {
                    val url = App.supabase.storage
                        .from("profile-pictures")
                        .publicUrl(profilePicturePath)
                    binding.ivProfilePicture.load(url) {
                        placeholder(R.drawable.ic_profile_placeholder)
                        error(R.drawable.ic_profile_placeholder)
                        memoryCachePolicy(CachePolicy.ENABLED)
                        diskCachePolicy(CachePolicy.ENABLED)
                    }
                } else {
                    binding.ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val userId = sessionManager.getSession() ?: return
        val fileName = "$userId.jpg"

        CoroutineScope(Dispatchers.IO).launch {
            val bytes = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) return@launch

            val users = App.supabase.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .decodeList<User>()
            val user = users.firstOrNull()
            val oldFileName = user?.profilePicture

            if (!oldFileName.isNullOrEmpty()) {
                App.supabase.storage
                    .from("profile-pictures")
                    .delete(listOf(oldFileName))
            }

            App.supabase.storage
                .from("profile-pictures")
                .upload(fileName, bytes, upsert = true)

            App.supabase.postgrest["users"]
                .update(mapOf("profile_picture" to fileName)) {
                    filter { eq("id", userId) }
                }

            val url = App.supabase.storage
                .from("profile-pictures")
                .publicUrl(fileName)
            val urlWithCacheBuster = "$url?ts=${System.currentTimeMillis()}"

            withContext(Dispatchers.Main) {
                binding.ivProfilePicture.load(urlWithCacheBuster) {
                    placeholder(R.drawable.ic_profile_placeholder)
                    error(R.drawable.ic_profile_placeholder)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
