package com.example.socialmediaapp.ui.postDetails

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import coil.load
import com.example.socialmediaapp.R
import com.example.socialmediaapp.data.model.dto.CommentWithUser
import com.example.socialmediaapp.App
import android.view.ViewOutlineProvider
import android.graphics.Outline
import io.github.jan.supabase.storage.storage

class CommentRenderer(
    private val container: LinearLayout,
    private val replyCallback: (parentCommentId: String, replyText: String) -> Unit
) {

    fun render(comments: List<CommentWithUser>) {
        container.removeAllViews()
        val topLevel = comments.filter { it.upperId == null }
        topLevel.forEach { comment ->
            container.addView(createCommentView(comment, comments, 0))
        }
    }

    private fun createCommentView(
        comment: CommentWithUser,
        allComments: List<CommentWithUser>,
        level: Int
    ): View {
        val cardView = CardView(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(level * 40, 12, 0, 12) }
            radius = 16f
            cardElevation = 6f
            setContentPadding(20, 16, 20, 16)
        }

        val horizontal = LinearLayout(container.context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val ivProfile = ImageView(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80).apply { setMargins(0, 0, 16, 0) }
            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            setImageResource(R.drawable.ic_profile_placeholder)
        }

        comment.user.profilePicture?.let {
            if (it.isNotEmpty()) {
                val url = App.supabase.storage.from("profile-pictures").publicUrl(it)
                ivProfile.load(url) { placeholder(R.drawable.ic_profile_placeholder) }
            }
        }

        val vertical = LinearLayout(container.context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvUser = TextView(container.context).apply {
            text = comment.user.username
            textSize = 16f
            setTextColor(container.context.resources.getColor(android.R.color.holo_blue_dark, null))
        }

        val tvContent = TextView(container.context).apply {
            text = comment.content
            textSize = 15f
        }

        val tvReply = TextView(container.context).apply {
            text = "Reply"
            textSize = 14f
            setTextColor(container.context.resources.getColor(android.R.color.holo_blue_dark, null))
            setOnClickListener {
                val input = android.widget.EditText(container.context)
                input.hint = "Write a reply..."
                input.setPadding(32, 16, 32, 16)
                input.background = container.context.resources.getDrawable(R.drawable.comment_input_bg, null)

                android.app.AlertDialog.Builder(container.context)
                    .setTitle("Reply to ${comment.user.username}")
                    .setView(input)
                    .setPositiveButton("Post") { _, _ ->
                        val replyText = input.text.toString().trim()
                        if (replyText.isNotEmpty()) replyCallback(comment.id, replyText)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        vertical.addView(tvUser)
        vertical.addView(tvContent)
        vertical.addView(tvReply)
        horizontal.addView(ivProfile)
        horizontal.addView(vertical)
        cardView.addView(horizontal)

        val nestedContainer = LinearLayout(container.context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val nestedReplies = allComments.filter { it.upperId == comment.id }
        if (nestedReplies.isNotEmpty()) {
            cardView.setOnClickListener {
                nestedContainer.visibility = if (nestedContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                nestedContainer.removeAllViews()
                nestedReplies.forEach { nested ->
                    nestedContainer.addView(createCommentView(nested, allComments, level + 1))
                }
            }
        }

        val wrapper = LinearLayout(container.context).apply {
            orientation = LinearLayout.VERTICAL
            addView(cardView)
            addView(nestedContainer)
        }

        return wrapper
    }
}
