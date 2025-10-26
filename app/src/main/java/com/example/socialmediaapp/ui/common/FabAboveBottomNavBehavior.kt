package com.example.socialmediaapp.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabAboveBottomNavBehavior(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        return dependency is com.google.android.material.bottomnavigation.BottomNavigationView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        // Move the FAB up along with the BottomNavigationView
        val offset = dependency.translationY - child.height - 16
        child.translationY = offset
        return true
    }
}
