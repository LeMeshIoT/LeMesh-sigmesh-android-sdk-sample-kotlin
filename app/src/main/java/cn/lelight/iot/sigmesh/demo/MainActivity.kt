package cn.lelight.iot.sigmesh.demo

import android.app.Application
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
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.core.InitCallback
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController

    private val appid = "appid"
    private val mac = "mac"
    private val secret = "secret"

    private val _isInit = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isInit: LiveData<Boolean> = _isInit

    //
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
                    SigDemoInstance.get().init(application.applicationContext)
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


}