package com.hal1cinogen.systembarmodernizer.spotify

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.Exception


class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpp: XC_LoadPackage.LoadPackageParam) {
        if (lpp.packageName != TARGET_PACKAGE_NAME) return
        hook(lpp)
    }

    private fun hook(lpp: XC_LoadPackage.LoadPackageParam) {
        processMain(lpp)
        processNowPlaying(lpp)
//        processRemoteVolume(lpp)
//        processTranslucentVolume(lpp)
        processLyrics(lpp)
        processQueue(lpp)
        processDevicePicker(lpp)
//        processBottomSheet(lpp)
//        processBlendStory(lpp)
    }

    private fun processMain(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(ACTIVITY_MAIN, lpp.classLoader)
            ?: XposedHelpers.findClassIfExists(ACTIVITY_MAIN_2, lpp.classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val window = activity.window ?: return

                    // Just remove translucent overlay on status bar
                    /*                    val flags = window.attributes.flags
                                        val viewFlags = window.decorView.systemUiVisibility
                                        XposedBridge.log("afterHookedMethod: window flags - ${Integer.toHexString(flags)}")
                                        XposedBridge.log("afterHookedMethod: system ui flags - ${Integer.toHexString(viewFlags)}")*/
//                    val transparentThemeId = 0x7f140427
//                    activity.setTheme(transparentThemeId)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    WindowCompat.setDecorFitsSystemWindows(window, false)
//                    val dex1 = 0x7f0b0d85
//                    val bottomBar = activity.findViewById<FrameLayout>(dex1) ?: return
//                    val resources = activity.resources
//                    val resourceId =
//                        resources.getIdentifier("navigation_bar_height", "dimen", "android")
//                    val height: Int = resources.getDimensionPixelSize(resourceId)
//                    bottomBar.updatePadding(bottom = height)
//                ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { v, insets ->
//                    v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
//                    insets
//                }

                    // bottom bar gradient background
                    /* val idHexBottomGradient = 0x7f0b01ed
                     val bottomGradient = activity.findViewById<View>(idHexBottomGradient) ?: return
                     bottomGradient.alpha = 0.5f
                     val drawable = bottomGradient.background
                     XposedBridge.log("bottom gradient is a gradient background - ${drawable is GradientDrawable}")*/
                }
            })
    }

    private fun processNowPlaying(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(ACTIVITY_PLAYING, lpp.classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val window = (param.thisObject as? Activity)?.window ?: return
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            })
    }

    private fun processLyrics(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(ACTIVITY_LYRICS, lpp.classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                @SuppressLint("DiscouragedApi")
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val window = activity.window ?: return
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val decorView = window.decorView
                    val resources = decorView.resources
                    val id = resources.getIdentifier("footer_container", "id", activity.packageName)
                    if (id == 0) return
                    val bottomContainer = activity.findViewById<ViewGroup>(id) ?: return
                    val height = getNavigationHeight(activity)
                    bottomContainer.updateLayoutParams<ViewGroup.LayoutParams> {
                        (this as MarginLayoutParams).bottomMargin = height
                    }
                }
            })
    }

    private fun processQueue(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(ACTIVITY_QUEUE, lpp.classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as? Activity ?: return
                    val window = activity.window ?: return
                    window.navigationBarColor = Color.parseColor(BACKGROUND_COLOR_GRAY)
                }
            })
        // com.spotify.nowplayingqueue.queue.view.NowPlayingQueueFragment
    }

    private fun processBlendStory(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClass(ACTIVITY_BLEND_STORY, lpp.classLoader)
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val window = activity.window ?: return
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
//                    "com.spotify.campaigns.storytelling.container.StorytellingContainerFragment"
                    val dex1 = 0x7f0b1348
                    XposedBridge.log("Naughty - blend")
                    val bottomContainer = activity.findViewById<View>(dex1)
                    XposedBridge.log("Naughty - blend container is - ${bottomContainer.id}")
                    val height = getStatusHeight(activity)
                    bottomContainer?.updateLayoutParams<ViewGroup.LayoutParams> {
                        (this as MarginLayoutParams).topMargin = height
                    }
                }
            })
    }

    private fun processDevicePicker(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(ACTIVITY_DEVICE_PICKER, lpp.classLoader) ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val window = activity.window ?: return
                    window.navigationBarColor = Color.parseColor(BACKGROUND_COLOR_GRAY)
                }
            })
    }

    private fun processRemoteVolume(lpp: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Naughty | package name - ${lpp.packageName} - remote volume")
        val clazz = XposedHelpers.findClass(ACTIVITY_REMOTE_VOLUME, lpp.classLoader)
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam?) {
                    val window = (param?.thisObject as? Activity)?.window ?: return
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            })
    }

    private fun processTranslucentVolume(lpp: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Naughty | package name - ${lpp.packageName} - translucent volume")
        val clazz = XposedHelpers.findClass(ACTIVITY_TRANSLUCENT_VOLUME, lpp.classLoader)
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val window = (param.thisObject as? Activity)?.window ?: return
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            })
    }

    private fun processBottomSheet(lpp: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Naughty | p.hz4 - START")
        val clazz = XposedHelpers.findClassIfExists("p.hz4", lpp.classLoader)
        XposedBridge.log("Naughty | p.hz4 - clazz exists - ${clazz == null} - $clazz")
        clazz.constructors.first().parameterCount
        clazz.constructors.forEach {
            XposedBridge.log("Naughty | p.hz4 - clazz constructor - parameter size - ${it.parameterCount}")
        }
        XposedHelpers.findAndHookConstructor(
            "p.hz4",
            lpp.classLoader,
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                    XposedBridge.log("Naughty | p.hz4 - HOOKED AFTER - START")
                    val edgeToEdgeField =
                        param?.thisObject?.javaClass?.getDeclaredField("k0") ?: return
                    edgeToEdgeField.isAccessible = true
                    edgeToEdgeField.set(param.thisObject, true)
                    XposedBridge.log("Naughty | p.hz4 - HOOKED AFTER - END")
                }
            })
        XposedHelpers.findAndHookMethod(clazz, "onAttachedToWindow", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                val frameLayoutField = param?.thisObject?.javaClass?.declaredFields?.firstOrNull {
                    XposedBridge.log("Naughty | p.hz4 - field name - ${it.name}")
                    it.name == "g"
                } ?: return
                frameLayoutField.isAccessible = true
                val frameLayout = frameLayoutField.get(param.thisObject) as FrameLayout
                frameLayout.fitsSystemWindows = false
                val coordinatorField = param.thisObject.javaClass.getDeclaredField("h")
                coordinatorField.isAccessible = true
                val coordinatorLayout = coordinatorField.get(param.thisObject) as? CoordinatorLayout
                coordinatorLayout?.fitsSystemWindows = false
                val window = (param?.thisObject as? Activity)?.window ?: return
                window.navigationBarColor = Color.TRANSPARENT
                window.decorView.systemUiVisibility = 768
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        })
        XposedBridge.log("Naughty | p.hz4 - END")
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