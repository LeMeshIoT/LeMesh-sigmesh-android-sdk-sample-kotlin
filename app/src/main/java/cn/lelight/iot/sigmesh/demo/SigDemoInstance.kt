package cn.lelight.iot.sigmesh.demo

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import cn.lelight.leiot.data.config.SigConfigInfo
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.api.callback.sigmesh.LeSigMeshStatusChangeCallback
import cn.lelight.leiot.sdk.utils.MD5Util

class SigDemoInstance private constructor() : LeSigMeshStatusChangeCallback {

    companion object {
        private var instance: SigDemoInstance? = null
            get() {
                if (field == null) {
                    field = SigDemoInstance()
                }
                return field
            }

        fun get(): SigDemoInstance {
            return instance!!
        }
    }

    var sigConnectStatus = MutableLiveData<String>().apply {
        value = "未连接"
    }

    private val proAddress = 0x6000
    private val seqNumber = 0

    fun init(context: Context) {
        // todo 自行检查是否开启蓝牙
        val bleSigMeshManger = LeHomeSdk.getBleSigMeshManger()
        // todo 自行确定netkey 和 appkey 等信息
        bleSigMeshManger.initPlugin(
            context,
            SigConfigInfo(
                MD5Util.getMD5("netkey"),
                MD5Util.getMD5("appkey"),
                proAddress,
                seqNumber
            )
        ) {
            Log.e("MainActivity", "sig sdk init result $it")
            // 设置监听
            bleSigMeshManger.setStatusChangeListener(this)
        }
    }

    override fun onConnectStatusChange(mac: String?, isConnect: Boolean) {
        Log.e("TEST", "onConnectStatusChange $mac $isConnect")

        var msg = if (isConnect) {
            "已连接"
        } else {
            "已断开"
        }

        sigConnectStatus.value = "$mac $msg"
    }
}