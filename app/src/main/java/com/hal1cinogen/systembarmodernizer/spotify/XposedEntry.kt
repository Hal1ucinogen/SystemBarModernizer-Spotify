package com.hal1cinogen.systembarmodernizer.spotify

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.hal1cinogen.systembarmodernizer.spotify.tool.XposedPluginLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
class XposedEntry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
    }

    override fun handleLoadPackage(lpp: XC_LoadPackage.LoadPackageParam) {
        if (lpp.packageName != TARGET_PACKAGE_NAME) return
        hook(lpp)
    }

    private fun hook(lpp: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("loaded - ${lpp.packageName}")
        XposedHelpers.findAndHookMethod(Instrumentation::class.java,
            "callApplicationOnCreate",
            Application::class.java,
            object : XC_MethodHook() {

                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    XposedBridge.log("Application onCreate")
                    val context = param?.args?.firstOrNull() as? Context ?: return
                    XposedPluginLoader.load(SpPlugin::class.java, context, lpp)
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
                override fun afterHookedMethod(param: MethodHookParam?) {
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
                val window = (param.thisObject as? Activity)?.window ?: return
                window.navigationBarColor = Color.TRANSPARENT
                window.decorView.systemUiVisibility = 768
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        })
        XposedBridge.log("Naughty | p.hz4 - END")
    }
}