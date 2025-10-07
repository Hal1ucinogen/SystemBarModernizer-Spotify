package com.hal1ucinogen.systembarmodernizer.spotify

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import com.hal1ucinogen.systembarmodernizer.spotify.tool.Task
import de.robv.android.xposed.XposedBridge

open class SpBasePlugin {

    @SuppressLint("DiscouragedApi")
    protected fun onActivityCreated(activity: Activity) {
        XposedBridge.log("Activity onCreate - $activity")
        val window = activity.window ?: return
        when (activity.javaClass.name) {
            ACTIVITY_PLAYING -> {
                Task.onMain {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                    window.statusBarColor = Color.TRANSPARENT
                    window.isStatusBarContrastEnforced = false
                    window.decorView.post {
                        window.navigationBarColor = Color.TRANSPARENT
                        window.isNavigationBarContrastEnforced = false
                    }
                }
            }

            ACTIVITY_BLEND_STORY -> {
                Task.onMain {
                    setSystemBarTransparent(window)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val resources = window.decorView.resources
                    val id = resources.getIdentifier("content", "id", activity.packageName)
                    if (id == 0) return@onMain
                    val container = activity.findViewById<FrameLayout>(id) ?: return@onMain
                    val child = container.children.firstOrNull() as? ViewGroup ?: return@onMain
                    val target = child.children.lastOrNull() as? ViewGroup ?: return@onMain
                    target.children.forEach {
                        XposedBridge.log("target | $it")
                    }
                    val height = getStatusHeight(activity)
                    val progressId =
                        resources.getIdentifier("stories_progress_bar", "id", activity.packageName)
                    val progress = target.findViewById<View>(progressId) ?: return@onMain
                    val nowMargin = progress.marginTop
                    XposedBridge.log("nowMargin | $nowMargin")
                    progress.updateLayoutParams<ViewGroup.LayoutParams> {
                        (this as ViewGroup.MarginLayoutParams).topMargin = height + nowMargin
                    }
                }
            }

            ACTIVITY_TRACK_CREDITS -> {
                Task.onMain(100) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.parseColor(COLOR_TOOLBAR)
                    window.navigationBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            }
        }
    }

    private fun removeTranslucentStatusOverlay(window: Window) {
        Task.onMain(100) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setSystemBarTransparent(window)
            setSystemBarsLight(window)
        }
    }

    private fun setSystemBarTransparent(window: Window) {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setSystemBarsLight(window: Window, light: Boolean = true) {
        WindowInsetsControllerCompat(window, window.decorView).run {
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    private fun getNavigationHeight(activity: Activity): Int {
        val height: Int = try {
            val resources = activity.resources
            val resourceId =
                resources.getIdentifier("navigation_bar_height", "dimen", "android")
            resources.getDimensionPixelSize(resourceId)
        } catch (e: Exception) {
            0
        }
        return height
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    private fun getStatusHeight(activity: Activity): Int {
        val height: Int = try {
            val resources = activity.resources
            val resourceId =
                resources.getIdentifier("status_bar_height", "dimen", "android")
            resources.getDimensionPixelSize(resourceId)
        } catch (e: Exception) {
            0
        }
        return height
    }
}