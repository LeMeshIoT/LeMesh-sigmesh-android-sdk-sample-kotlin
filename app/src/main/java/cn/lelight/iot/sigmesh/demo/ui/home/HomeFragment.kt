package cn.lelight.iot.sigmesh.demo.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.lelight.iot.sigmesh.demo.BuildConfig
import cn.lelight.iot.sigmesh.demo.MainActivity
import cn.lelight.iot.sigmesh.demo.SigDemoInstance
import cn.lelight.iot.sigmesh.demo.databinding.FragmentHomeBinding
import cn.lelight.iot.sigmesh.demo.ui.adddevice.AddDevicesActivity
import cn.lelight.leiot.data.ble.ExtendedBluetoothDevice
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.api.callback.IBleScanUnProCallback
import cn.lelight.leiot.sdk.api.callback.IResultCallback
import cn.lelight.leiot.sdk.blesigmesh.bean.SigConfig
import com.afollestad.materialdialogs.MaterialDialog

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var materialDialog: MaterialDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        SigDemoInstance.get().sigConnectStatus.observe(viewLifecycleOwner) {
            binding.textHome2.text = it
            materialDialog?.dismiss()
        }

        if (LeHomeSdk.getBleSigMeshManger() != null) {
            binding.textHome.text =
                "ver:${LeHomeSdk.getBleSigMeshManger().version}\n${BuildConfig.BUILDTIME}"
        }

        binding.btnConfigTtl.setOnClickListener {
            MaterialDialog.Builder(requireActivity())
                .title("设置ttl")
                .content("消息转发次数,默认7,范围1~127")
                .input(
                    "", "", false
                ) { dialog, input ->
                    // todo 检查范围
                    LeHomeSdk.getBleSigMeshManger().config(
                        SigConfig.TTL,
                        input.toString().toInt(),
                        object : IResultCallback {
                            override fun success() {
                                toast("设置成功")
                            }

                            override fun fail(p0: Int, p1: String?) {
                                // todo
                            }
                        })
                }
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .show()
        }

        binding.btnAddDevice.setOnClickListener {
            // 添加设备
            if (!checkIsInitSdk()) {
                toast("未初始化")
                return@setOnClickListener
            }
            // 判断下
            requireActivity().startActivity(
                Intent(
                    requireActivity(),
                    AddDevicesActivity::class.java
                )
            )
        }

        binding.btnStartConnect.setOnClickListener {
            //
            if (!checkIsInitSdk()) {
                toast("未初始化")
                return@setOnClickListener
            }
            //
            materialDialog = MaterialDialog.Builder(requireActivity())
                .title("正在搜索设备")
                .content("请稍候...")
                .progress(true, 0)
                .show()
            //
            LeHomeSdk.getBleSigMeshManger().autoConnectProxyNode(
                3, // 搜索3秒
                object : IResultCallback {
                    override fun success() {
                        Log.e("test", "scan success")
                        materialDialog?.dismiss()
                    }

                    override fun fail(p0: Int, p1: String?) {
                        Log.e("test", "scanFail $p0 $p1")
                        materialDialog?.dismiss()
                    }

                })
        }

        binding.btnConfigTime.setOnClickListener {
            if (!checkIsInitSdk()) {
                toast("未初始化")
                return@setOnClickListener
            }
            //
            val callback = object : IResultCallback {
                override fun success() {
                    toast("设置成功")
                }

                override fun fail(p0: Int, p1: String?) {
                    // todo
                }
            }
            MaterialDialog.Builder(requireActivity())
                .title("设置灯光渐变时间")
                .items("立变,0.5s,1.5s,3s,5s".split(","))
                .itemsCallback { dialog, itemView, position, text ->
                    when (position) {
                        0 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_ON_OFF, 0, callback)
                        }
                        1 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_ON_OFF, 5, callback)
                        }
                        2 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_ON_OFF, 15, callback)
                        }
                        3 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_ON_OFF, 30, callback)
                        }
                        4 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_ON_OFF, 50, callback)
                        }
                        else -> {}
                    }
                }.show()
        }
        binding.btnConfigSceneTime.setOnClickListener {
            //
            if (!checkIsInitSdk()) {
                toast("未初始化")
                return@setOnClickListener
            }
            //
            val callback = object : IResultCallback {
                override fun success() {
                    toast("设置成功")
                }

                override fun fail(p0: Int, p1: String?) {
                    // todo
                }
            }
            MaterialDialog.Builder(requireActivity())
                .title("设置情景渐变时间")
                .items("立变,0.5s,1.5s,3s,5s".split(","))
                .itemsCallback { dialog, itemView, position, text ->
                    when (position) {
                        0 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_SCENE_ON_OFF, 0, callback)
                        }
                        1 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_SCENE_ON_OFF, 5, callback)
                        }
                        2 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_SCENE_ON_OFF, 15, callback)
                        }
                        3 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_SCENE_ON_OFF, 30, callback)
                        }
                        4 -> {
                            LeHomeSdk.getBleSigMeshManger()
                                .config(SigConfig.TIME_SCENE_ON_OFF, 50, callback)
                        }
                        else -> {}
                    }
                }.show()
        }

        binding.btnConfigSeq.setOnClickListener {
            //
            if (!checkIsInitSdk()) {
                toast("未初始化")
                return@setOnClickListener
            }
            //
            val callback = object : IResultCallback {
                override fun success() {
                    toast("设置成功")
                }

                override fun fail(p0: Int, p1: String?) {
                    // todo
                }
            }

            val config = LeHomeSdk.getBleSigMeshManger().getConfig(
                SigConfig.PRO_SEQ
            )

            MaterialDialog.Builder(requireActivity())
                .title("设置SEQ(当前:$config)")
                .content("设置消息指令的seq值,若seq值低于设备中缓存的,将无法控制设备\n(0~16777216)")
                .input("", "", false) { _, input ->
                    LeHomeSdk.getBleSigMeshManger().config(
                        SigConfig.PRO_SEQ, input.toString().toInt(),
                        callback
                    )
                }
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .show()
        }

        binding.btnConfigReset.setOnClickListener {
            //
            if (!checkIsInitSdk()) {
                toast("未初始化")
                return@setOnClickListener
            }
            //
            val callback = object : IResultCallback {
                override fun success() {
                    toast("设置成功")
                }

                override fun fail(p0: Int, p1: String?) {
                    // todo
                }
            }
            MaterialDialog.Builder(requireActivity())
                .title("确定")
                .content("将删除所有节点(仅删除缓存数据)，设备需要重置添加")
                .onPositive { dialog, which ->
                    LeHomeSdk.getBleSigMeshManger().resetNetWork(callback)
                }
                .positiveText("确定")
                .negativeText("取消")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .show()
        }

        return root
    }

    private fun checkIsInitSdk(): Boolean {
        if (requireActivity() is MainActivity) {
            return SigDemoInstance.get().isInit.value!!
        }
        return false
    }

    fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}