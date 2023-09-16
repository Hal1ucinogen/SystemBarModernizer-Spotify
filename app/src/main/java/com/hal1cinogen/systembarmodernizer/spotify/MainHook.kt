package com.hal1cinogen.systembarmodernizer.spotify

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
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
        processLyrics(lpp)
        processQueue(lpp)
        processDevicePicker(lpp)
    }

    private fun processMain(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClass(ACTIVITY_MAIN, lpp.classLoader)
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
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            })
    }

    private fun processNowPlaying(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClass(ACTIVITY_PLAYING, lpp.classLoader)
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
        val clazz = XposedHelpers.findClass(ACTIVITY_LYRICS, lpp.classLoader)
        XposedHelpers.findAndHookMethod(
            clazz,
            METHOD_ON_CREATE,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val window = activity.window ?: return
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val dex1 = 0x7f0b0749
                    val bottomContainer = activity.findViewById<ViewGroup>(dex1) ?: return
                    val height = getNavigationHeight(activity)
                    bottomContainer.updateLayoutParams<ViewGroup.LayoutParams> {
                        (this as MarginLayoutParams).bottomMargin = height
                    }
                }
            })
    }

    private fun processQueue(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClass(ACTIVITY_QUEUE, lpp.classLoader)
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
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

    private fun processDevicePicker(lpp: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClass(ACTIVITY_DEVICE_PICKER, lpp.classLoader)
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val window = activity.window ?: return
                    window.navigationBarColor = Color.parseColor(BACKGROUND_COLOR_GRAY)
                }
            })
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
}