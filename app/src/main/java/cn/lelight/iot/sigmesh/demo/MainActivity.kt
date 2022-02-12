package cn.lelight.iot.sigmesh.demo

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cn.lelight.iot.sigmesh.demo.databinding.ActivityMainBinding
import cn.lelight.leiot.data.config.SigConfigInfo
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.api.callback.sigmesh.LeSigMeshStatusChangeCallback
import cn.lelight.leiot.sdk.core.InitCallback
import cn.lelight.leiot.sdk.utils.MD5Util
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(), LeSigMeshStatusChangeCallback {

    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController

    private val appid = "appid"
    private val mac = "mac"
    private val secret = "secret"

    //
    private val proAddress = 0x6000
    private val seqNumber = 0

    private val _isInit = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isInit: LiveData<Boolean> = _isInit

    //
    var log1 = "sdk 未初始化"
    var log2 = "未连接"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_device, R.id.navigation_scene
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initSdk(application)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
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
                    startInitSigMesh(application.applicationContext)
                    _isInit.value = true
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
                    "其它异常"
                }
            }
            Toast.makeText(application.applicationContext, msg, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun startInitSigMesh(context: Context) {
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
            //
            log1 =
                "初始化完成:\nSigSdkVer:${bleSigMeshManger.version}\nnetkey:${bleSigMeshManger.netKey}\nappkey:${bleSigMeshManger.appKey}\n地址:0x${
                    String.format(
                        "%04x",
                        proAddress
                    )
                }"
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

        log2 = "$mac $msg"
    }
}