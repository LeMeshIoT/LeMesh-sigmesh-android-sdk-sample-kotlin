package cn.lelight.iot.sigmesh.demo

import android.app.Application
import android.content.Context
import cn.lelight.leiot.sdk.LeHomeSdk

class MyApplication:Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // 前置初始化
        LeHomeSdk.attachBaseContext(this);
    }
}