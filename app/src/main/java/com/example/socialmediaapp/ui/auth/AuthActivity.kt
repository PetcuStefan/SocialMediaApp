package com.example.socialmediaapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.data.repository.AuthRepository
import com.example.socialmediaapp.utils.SessionManager
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.EditText
import android.widget.TextView
import com.example.socialmediaapp.R
import com.example.socialmediaapp.ui.main.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        sessionManager = SessionManager(this)
        authRepo = AuthRepository(sessionManager)

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerBtn = findViewById<Button>(R.id.btnRegister)
        val status = findViewById<TextView>(R.id.tvStatus)

        loginBtn.setOnClickListener {
            lifecycleScope.launch {
                val success = authRepo.login(username.text.toString(), password.text.toString())
                if (success) {
                    // Navigate to FeedActivity
                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // close AuthActivity
                } else {
                    status.text = "Invalid credentials."
                }
            }
        }


        registerBtn.setOnClickListener {
            lifecycleScope.launch {
                val success = authRepo.register(username.text.toString(), password.text.toString())
                if (success) {
                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    status.text = "Username already taken."
                }
            }
        }

    }
}
