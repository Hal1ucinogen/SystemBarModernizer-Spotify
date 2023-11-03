package com.hal1cinogen.systembarmodernizer.spotify

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.hal1cinogen.systembarmodernizer.spotify.tool.Task
import de.robv.android.xposed.XposedBridge

open class SpBasePlugin {

    @SuppressLint("DiscouragedApi")
    protected fun onActivityCreated(activity: Activity) {
        XposedBridge.log("Activity onCreate - $activity")
        val window = activity.window ?: return
        when (activity.javaClass.name) {
            ACTIVITY_MAIN, ACTIVITY_MAIN_2 -> {
                Task.onMain {
                    // Enable edge-to-edge
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    // FIXME Podcast page bottom padding

                    // Process bottom navigation padding
                    val resources = window.decorView.resources
                    val navBottomId =
                        resources.getIdentifier("navigation_bar", "id", activity.packageName)
                    if (navBottomId != 0) {
                        val bottomBar =
                            activity.findViewById<FrameLayout>(navBottomId) ?: return@onMain
                        bottomBar.updatePadding(bottom = getNavigationHeight(activity))
/*                        ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { v, insets ->
                            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
                            insets
                        }*/
                    }

                    // Modify bottom bar gradient background
                    val bottomGradientId =
                        resources.getIdentifier("bottom_gradient", "id", activity.packageName)
                    if (bottomGradientId == 0) return@onMain
                    val bottomGradient =
                        activity.findViewById<View>(bottomGradientId) ?: return@onMain
                    bottomGradient.alpha = 0.9f
                    /*val drawable = bottomGradient.background
                    XposedBridge.log("bottom gradient is a gradient background - ${drawable is GradientDrawable}")*/
                }
            }

            ACTIVITY_PLAYING, ACTIVITY_REMOTE_VOLUME -> {
                Task.onMain {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    setSystemBarTransparent(window)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            }

            ACTIVITY_LYRICS -> {
                Task.onMain {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    setSystemBarTransparent(window)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val resources = window.decorView.resources
                    val id = resources.getIdentifier("footer_container", "id", activity.packageName)
                    if (id == 0) return@onMain
                    val bottomContainer = activity.findViewById<ViewGroup>(id) ?: return@onMain
                    val height = getNavigationHeight(activity)
                    bottomContainer.updateLayoutParams<ViewGroup.LayoutParams> {
                        (this as ViewGroup.MarginLayoutParams).bottomMargin = height
                    }
                }
            }

            ACTIVITY_QUEUE, ACTIVITY_DEVICE_PICKER -> {
                setSystemBarTransparent(window)
                window.setBackgroundDrawable(ColorDrawable(Color.parseColor(COLOR_BACKGROUND_GRAY)))
            }

            ACTIVITY_BLEND_STORY -> {
                Task.onMain {
                    window.setBackgroundDrawable(ColorDrawable(Color.parseColor(COLOR_BACKGROUND_BLEND)))
                    window.statusBarColor = Color.parseColor(COLOR_STATUS_BLEND)
                    window.navigationBarColor = Color.TRANSPARENT
//                    "com.spotify.campaigns.storytelling.container.StorytellingContainerFragment"
/*                    val dex1 = 0x7f0b1348
                    XposedBridge.log("Naughty - blend")
                    val bottomContainer = activity.findViewById<View>(dex1)
                    XposedBridge.log("Naughty - blend container is - ${bottomContainer.id}")
                    val height = getStatusHeight(activity)
                    bottomContainer?.updateLayoutParams<ViewGroup.LayoutParams> {
                        (this as ViewGroup.MarginLayoutParams).topMargin = height
                    }*/
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