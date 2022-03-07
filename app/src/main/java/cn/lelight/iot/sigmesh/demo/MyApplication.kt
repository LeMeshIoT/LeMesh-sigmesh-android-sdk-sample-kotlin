package cn.lelight.iot.sigmesh.demo

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.core.InitCallback

class MyApplication : Application() {

    private val appid = ""
    private val mac = ""
    private val secret = ""

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // 前置初始化
        LeHomeSdk.attachBaseContext(this)
    }

    override fun onCreate() {
        super.onCreate()

        initSdk(this)
    }

    fun initSdk(application: Application) {
        LeHomeSdk.init(
            application,
            appid,
            mac,
            secret
        ) { result ->
            Log.e("MainActivity", "sdk init result $result")
            //
            var msg = when (result) {
                InitCallback.SUCCESS -> {
                    SigDemoInstance.get().init(applicationContext)
                    SigDemoInstance.get().isInit.value = true
                    "sdk 初始化成功"
                }
                InitCallback.ALREADY_INITIALED -> {
                    "sdk已经初始化过了"
                }
                InitCallback.UNAUTH_SDK -> {
                    "非法授权sdk"
                }
                InitCallback.AUTH_FAID -> {
                    // 检查是否设备有网络
                    "授权失败"
                }
                else -> {
                    "其它异常:$result"
                }
            }
            Toast.makeText(application.applicationContext, msg, Toast.LENGTH_SHORT)
                .show()
        }
    }
}