package cn.lelight.iot.sigmesh.demo.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.api.callback.sigmesh.LeSigMeshStatusChangeCallback

class HomeViewModel : ViewModel(), LeSigMeshStatusChangeCallback {

    private val _text2 = MutableLiveData<String>().apply {
        value = "未连接"
    }
    val text2: LiveData<String> = _text2

    fun initCallabck() {
        LeHomeSdk.getBleSigMeshManger().setStatusChangeListener(this)
    }

    override fun onConnectStatusChange(mac: String?, isConnect: Boolean) {
        Log.e("TEST", "onConnectStatusChange $mac $isConnect")

        var msg = if (isConnect) {
            "已连接"
        } else {
            "已断开"
        }

        _text2.value = "$mac $msg"
    }
}