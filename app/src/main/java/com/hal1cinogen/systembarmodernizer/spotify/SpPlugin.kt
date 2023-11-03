package com.hal1cinogen.systembarmodernizer.spotify

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.Exception

class SpPlugin : SpBasePlugin() {

    @Keep
    fun main(context: Context, lpp: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                METHOD_ON_CREATE,
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        val activity = param?.thisObject as? Activity ?: return
                        onActivityCreated(activity)
                    }
                })
/*            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onResume",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        val activity = param?.thisObject as? Activity ?: return
                        onActivityResumed(activity)
                    }
                })
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onPause",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        val activity = param?.thisObject as? Activity ?: return
                        onActivityPaused(activity)
                    }
                })*/
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }
}