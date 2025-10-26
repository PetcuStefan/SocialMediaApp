package com.example.socialmediaapp.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class FabAboveBottomNavBehavior(
    context: Context,
    attrs: AttributeSet
) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        // Only depend on BottomNavigationView
        return dependency is BottomNavigationView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        if (dependency is BottomNavigationView) {
            // Calculate translation
            val offset = dependency.height / 0.9f // FAB moves half as much
            child.translationY = dependency.translationY - offset
        }
        return true
    }
}
