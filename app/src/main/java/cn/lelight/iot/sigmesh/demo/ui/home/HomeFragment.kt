package cn.lelight.iot.sigmesh.demo.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        SigDemoInstance.get().sigConnectStatus.observe(viewLifecycleOwner){
            binding.textHome2.text = it
            materialDialog?.dismiss()
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
                                Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show()
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
            requireActivity().startActivity(
                Intent(
                    requireActivity(),
                    AddDevicesActivity::class.java
                )
            )
        }

        binding.btnStartConnect.setOnClickListener {
            //
            materialDialog = MaterialDialog.Builder(requireActivity())
                .title("正在搜索设备")
                .content("请稍候...")
                .progress(true, 0)
                .show()
            //
            LeHomeSdk.getBleManger().bleScanManger.startScanProSigBleInfo(
                3, // 搜索3秒
                true,
                object : IBleScanUnProCallback {
                    override fun scanDeviceNotify(p0: HashMap<String, ExtendedBluetoothDevice>?) {

                    }

                    override fun scanTimeOutResult(deviceHashMap: HashMap<String, ExtendedBluetoothDevice>) {
                        // todo 找信号最强的连接
                        for (value in deviceHashMap.values) {
                            LeHomeSdk.getBleSigMeshManger().startConnectProxyNode(value)
                            break
                        }
                    }

                    override fun scanFail(p0: String?) {
                        // todo
                    }

                })
        }

        binding.btnConfigTime.setOnClickListener {
            val callback = object : IResultCallback {
                override fun success() {
                    Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show()
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
            val callback = object : IResultCallback {
                override fun success() {
                    Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show()
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

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}