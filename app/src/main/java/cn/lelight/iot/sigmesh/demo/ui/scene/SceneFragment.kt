package cn.lelight.iot.sigmesh.demo.ui.scene

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.lelight.iot.sigmesh.demo.MainActivity
import cn.lelight.iot.sigmesh.demo.R
import cn.lelight.iot.sigmesh.demo.SigDemoInstance
import cn.lelight.iot.sigmesh.demo.databinding.FragmentNotificationsBinding
import cn.lelight.leiot.data.bean.SceneBean
import cn.lelight.leiot.data.leenum.ModuleType
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.adapter.CommonAdapter
import cn.lelight.leiot.sdk.adapter.ViewHolder
import cn.lelight.leiot.sdk.api.IDataManger
import cn.lelight.leiot.sdk.api.callback.IResultCallback
import cn.lelight.leiot.sdk.api.callback.sigmesh.IBleSigMeshSceneManger
import cn.lelight.leiot.sdk.api.callback.sigmesh.IBleSigMeshSceneStatusCallback
import com.afollestad.materialdialogs.MaterialDialog

class SceneFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private var sigSceneManger: IBleSigMeshSceneManger? = null
    private var dataManger: IDataManger? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(SceneViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnQueryScene2.setOnClickListener {
            if (!SigDemoInstance.get().isInit.value!!) {
                return@setOnClickListener
            }

            sigSceneManger?.queryDevicesScenes()
        }

        binding.btnAddScene.setOnClickListener {

            if (!SigDemoInstance.get().isInit.value!!) {
                return@setOnClickListener
            }

            MaterialDialog.Builder(requireActivity())
                .title("????????????(demo)")
                .content("???????????????????????????????????????????????????")
                .input(
                    "??????????????????", "", false
                ) { dialog, input ->
                    // todo ??????????????????????????????????????????demo??????????????????
                    val devices = dataManger!!.getDevicesByModuleType(ModuleType.BLE_SIG_MESH)
                    //
                    sigSceneManger!!.createScene(
                        input.toString(),
                        devices,
                        object : IResultCallback {
                            override fun success() {
                                // ?????????????????????????????????????????????????????????

                            }

                            override fun fail(code: Int, msg: String) {
                                //
                            }
                        })
                }.show()
        }

        SigDemoInstance.get().isInit.observeForever {
            if (it) {
                // ???????????????
                dataManger = LeHomeSdk.getDataManger()
                sigSceneManger = LeHomeSdk.getBleSigMeshManger().sigSceneManger
                //
                updateSceneData()
                //
                initListener()
                //
                sigSceneManger?.queryDevicesScenes()
            }
        }

        return root
    }

    private fun initListener() {
        // ??????????????????
        sigSceneManger!!.setStatusCallback(object : IBleSigMeshSceneStatusCallback {
            override fun onSceneAdd(sceneBean: SceneBean) {
                updateSceneData()
            }

            override fun onSceneDelete(sceneBean: SceneBean) {
                updateSceneData()
            }

            override fun onSceneUpdate(sceneBean: SceneBean) {
                updateSceneData()
            }
        })
    }

    private fun updateSceneData() {
        val allSigScenes = sigSceneManger!!.allSigScenes
        binding.lvDataScene.setAdapter(
            SceneAdapter(
                requireActivity(),
                allSigScenes
            )
        )
    }

    override fun onDestroy() {
        sigSceneManger!!.setStatusCallback(null)
        super.onDestroy()
    }

    inner class SceneAdapter(context: Context?, datas: List<SceneBean>) :
        CommonAdapter<SceneBean>(context, datas, R.layout.item_data_scene) {
        override fun convert(holder: ViewHolder, sceneBean: SceneBean) {
            //
            holder.getTextView(R.id.tv_scene_name).text = sceneBean.name
            //
            holder.getTextView(R.id.tv_scene_cmd).text =
                "0x" + String.format("%04x", sceneBean.sceneId) + " ?????????:" + sceneBean.devIds.size
            //
            holder.getTextView(R.id.tv_scene_kind).text = "" + sceneBean.moduleType
            //
            holder.getTextView(R.id.tv_scene_action).setOnClickListener {
                sigSceneManger?.loadScene(sceneBean, object : IResultCallback {
                    override fun success() {
                        Toast.makeText(requireActivity(), "????????????", Toast.LENGTH_SHORT).show()
                    }

                    override fun fail(code: Int, msg: String) {
                        Toast.makeText(requireActivity(), "????????????:" + msg, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            //
            holder.getTextView(R.id.tv_scene_del).setOnClickListener {
                sigSceneManger?.deleteScene(sceneBean, object : IResultCallback {
                    override fun success() {
                        Toast.makeText(requireActivity(), "????????????", Toast.LENGTH_SHORT).show()
                    }

                    override fun fail(code: Int, msg: String) {
                        Toast.makeText(requireActivity(), "????????????:" + msg, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}