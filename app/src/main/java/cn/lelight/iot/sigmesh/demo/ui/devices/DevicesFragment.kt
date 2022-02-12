package cn.lelight.iot.sigmesh.demo.ui.devices

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.lelight.iot.sigmesh.demo.MainActivity
import cn.lelight.iot.sigmesh.demo.R
import cn.lelight.iot.sigmesh.demo.databinding.FragmentDashboardBinding
import cn.lelight.leiot.data.LeDataCenter
import cn.lelight.leiot.data.bean.AllRoomBean
import cn.lelight.leiot.data.bean.DeviceBean
import cn.lelight.leiot.data.bean.GroupBean
import cn.lelight.leiot.data.bean.RoomBean
import cn.lelight.leiot.data.bean.base.DpBean
import cn.lelight.leiot.data.leenum.DeviceType
import cn.lelight.leiot.data.leenum.dps.LightDp
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.adapter.CommonAdapter
import cn.lelight.leiot.sdk.adapter.ViewHolder
import cn.lelight.leiot.sdk.api.IDataManger
import cn.lelight.leiot.sdk.api.IGroupManger
import cn.lelight.leiot.sdk.api.IRoomManger
import cn.lelight.leiot.sdk.api.callback.ICreateCallback
import cn.lelight.leiot.sdk.api.callback.data.IHomeDataChangeListener
import cn.lelight.leiot.sdk.api.callback.data.IHomeRoomGroupChangeListener
import cn.lelight.leiot.sdk.utils.LeLogUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.tabs.TabLayout

class DevicesFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private var allRoomBeans = ArrayList<RoomBean>()
    private var allDevices = ArrayList<DeviceBean>()
    private val tempDevices = ArrayList<DeviceBean>()

    private var targetRoomId = 0
    private var targetGroupId = 0

    private val targetDevices = java.util.ArrayList<DeviceBean>()
    private val targetRoomGroups = java.util.ArrayList<GroupBean>()

    private var dataManger: IDataManger? = null
    private var roomManger: IRoomManger? = null
    private var groupManger: IGroupManger? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DevicesViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        (requireActivity() as MainActivity).isInit.observeForever {
            if (it) {
                // 初始化成功
                dataManger = LeHomeSdk.getDataManger()
                roomManger = LeHomeSdk.getRoomManger()
                groupManger = LeHomeSdk.getGroupManger()
                //
                initData()
                //
                initListener()
            }
        }

        binding.tabRoom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab!!.position
                val roomBean = allRoomBeans[position]
                initRoomData(roomBean)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        // 所有设备的点击事件
        binding.tvAllRoomDevices.setOnClickListener {
            if (targetRoomId == -1) {
                initRoomData(allRoomBeans[0])
            } else {
                initRoomData(dataManger!!.getRoomBean(targetRoomId))
            }
            //
            binding.tvAllRoomDevices.setTextColor(Color.RED)
            binding.tvAllRoomDevices.textSize = 16f
            //
            if (binding.lvDataGroups.adapter != null) {
                (binding.lvDataGroups.adapter as GroupAdapter).notifyDataSetChanged()
            }
        }

        binding.btnAddRoom.setOnClickListener {
            MaterialDialog.Builder(requireActivity())
                .title("输入房间名字")
                .input("", "", false) { dialog, input -> //
                    roomManger!!.creatRoom(input.toString(), object : ICreateCallback {
                        override fun onAddSuccess() {
                            Toast.makeText(requireActivity(), "添加成功", Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onAddFail(msg: String) {
                            Toast.makeText(
                                requireActivity(),
                                "添加失败:$msg",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
                .show()
        }

        binding.btnAddGroup.setOnClickListener {
            if (targetRoomId == -1) {
                Toast.makeText(requireActivity(), "请在房间中添加群组", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //
            //
            MaterialDialog.Builder(requireActivity())
                .title("输入群组名字")
                .input("", "", false) { dialog, input -> //
                    groupManger!!.creatGroup(
                        input.toString(),
                        targetRoomId,
                        object : ICreateCallback {
                            override fun onAddSuccess() {
                                Toast.makeText(
                                    requireActivity(),
                                    "添加成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onAddFail(msg: String) {
                                Toast.makeText(
                                    requireActivity(),
                                    "添加失败:$msg",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
                .show()
        }

        binding.btnOpenAll.setOnClickListener {
            turnOnOff(true)
        }

        binding.btnCloesAll.setOnClickListener {
            turnOnOff(false)
        }

        //
        binding.btnAddDeviceToGroup.setOnClickListener {
            if (targetRoomId == -1) {
                Toast.makeText(requireContext(), "请在房间中添加", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (targetGroupId != -1) {
                val groupBean = dataManger!!.getGroupBean(targetGroupId)
                if (groupBean != null) {
                    addOrDelDeivceToGroup(0, groupBean)
                }
            } else {
                val roomBean = dataManger!!.getRoomBean(targetRoomId)
                if (roomBean != null) {
                    addOrDelDeivceToRoomBean(0, roomBean)
                }
            }
        }

        //
        binding.btnDelDeviceToGroup.setOnClickListener {
            if (targetRoomId == -1) {
                Toast.makeText(requireContext(), "请在房间中删除", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (targetGroupId != -1) {
                val groupBean = dataManger!!.getGroupBean(targetGroupId)
                if (groupBean != null) {
                    addOrDelDeivceToGroup(1, groupBean)
                }
            } else {
                val roomBean = dataManger!!.getRoomBean(targetRoomId)
                if (roomBean != null) {
                    addOrDelDeivceToRoomBean(1, roomBean)
                }
            }
        }

        return root
    }

    private fun turnOnOff(open: Boolean) {
        if (targetRoomId == -1 && targetGroupId == -1) {
            // 所有房间，所有设备
            val roomBean = AllRoomBean()
            roomManger!!.controlRoom(
                roomBean,
                DeviceType.Light,
                DpBean(LightDp.POWER.dpId, LightDp.POWER.type, open)
            )
        } else {
            if (targetGroupId == -1) {
                //("某个房间")
                val roomBean = LeDataCenter.getInstance().roomBeanHashMap[targetRoomId]
                if (roomBean != null) {
                    roomManger!!.controlRoom(
                        roomBean,
                        DeviceType.Light,
                        DpBean(LightDp.POWER.dpId, LightDp.POWER.type, open)
                    )
                }
            } else {
                //("某个群组")
                val groupBean = LeDataCenter.getInstance().groupBeanHashMap[targetGroupId]
                if (groupBean != null) {
                    groupManger!!.controlGroup(
                        groupBean, DeviceType.Light,
                        DpBean(LightDp.POWER.dpId, LightDp.POWER.type, open)
                    )
                }
            }
        }
    }

    private fun initListener() {
        // 监听blemesh
        LeHomeSdk.getInstance().setHomeDataChangeListener(object : IHomeDataChangeListener {
            override fun onDeviceAdd(deviceBean: DeviceBean) {
                deviceUpdateUI()
            }

            override fun onDeviceUpdate(deviceBean: DeviceBean) {
                deviceUpdateUI()
            }

            override fun onDeviceDeleted(deviceBean: DeviceBean) {
                deviceUpdateUI()
            }
        })
        //
        LeHomeSdk.getInstance()
            .setHomeRoomGroupChangeListener(object : IHomeRoomGroupChangeListener {
                override fun onRoomBeanAdd(roomBean: RoomBean) {
                    updateRoomInfo()
                }

                override fun onRoomBeanUpdate(roomBean: RoomBean) {
                    updateRoomInfo()
                }

                override fun onRoomBeanDeleted(roomBean: RoomBean) {
                    updateRoomInfo()
                }

                override fun onGroupBeanAdd(groupBean: GroupBean) {
                    updateRoomInfo()
                }

                override fun onGroupBeanUpdate(groupBean: GroupBean) {
                    updateRoomInfo()
                }

                override fun onGroupBeanDeleted(groupBean: GroupBean) {
                    updateRoomInfo()
                }
            })
    }

    private fun deviceUpdateUI() {
        if (targetGroupId != -1) {
            val groupBean = LeDataCenter.getInstance().groupBeanHashMap[targetGroupId]
            groupBean?.let { initGroupData(it) }
        } else {
            if (targetRoomId != -1) {
                val roomBean = LeDataCenter.getInstance().roomBeanHashMap[targetRoomId]
                roomBean?.let { initRoomData(it) }
            } else {
                initRoomData(allRoomBeans[0])
            }
        }
    }

    private fun initData() {
        //
        allDevices = java.util.ArrayList(dataManger!!.allDevices)
        //
        updateRoomInfo()
        //
        initRoomData(allRoomBeans[0])
    }

    private fun updateRoomInfo() {
        allRoomBeans = ArrayList(dataManger!!.allRoomBeans)
        LeLogUtil.e("allRoomBeans size:" + allRoomBeans.size)
        // 初始化房间/群组
        // todo 让tab 跳转到最新添加的item
        initRoomTabLayout()
    }

    private fun initRoomTabLayout() {
        binding.tabRoom.removeAllTabs()
        //
        for (allRoomBean in allRoomBeans) {
            val tab: TabLayout.Tab = binding.tabRoom.newTab()
            tab.text = allRoomBean.name
            binding.tabRoom.addTab(tab)
        }
    }

    private fun initRoomData(roomBean: RoomBean) {
        targetRoomId = roomBean.roomId
        targetGroupId = -1
        //
        val devIds = roomBean.devIds
        //
        targetDevices.clear()
        for (devId in devIds) {
            val deviceBean = dataManger!!.getDeviceBean(devId)
            if (deviceBean != null) {
                targetDevices.add(deviceBean)
            }
        }
        //
        binding.lvDataDevices.setAdapter(DevicesAdapter(requireActivity(), targetDevices))
        //
        val groupIds = roomBean.groupIds
        // todo
        targetRoomGroups.clear()
        for (groupId in groupIds) {
            val groupBean = dataManger!!.getGroupBean(groupId)
            if (groupBean != null) {
                targetRoomGroups.add(groupBean)
            }
        }
        //
        binding.lvDataGroups.setAdapter(
            GroupAdapter(
                requireActivity(),
                targetRoomGroups
            )
        )
        //
        binding.tvAllRoomDevices.setTextColor(Color.RED)
        binding.tvAllRoomDevices.setTextSize(16f)
    }

    inner class GroupAdapter(context: Context, datas: List<GroupBean>) :
        CommonAdapter<GroupBean>(context, datas, R.layout.item_data_group) {
        override fun convert(holder: ViewHolder, groupBean: GroupBean) {
            //
            if (targetGroupId == groupBean.groupId) {
                holder.getTextView(R.id.tv_group_name).setTextColor(Color.RED)
                holder.getTextView(R.id.tv_group_name).textSize = 16f
                binding.tvAllRoomDevices.setTextColor(Color.BLACK)
                binding.tvAllRoomDevices.setTextSize(12f)
            } else {
                holder.getTextView(R.id.tv_group_name).setTextColor(Color.BLACK)
                holder.getTextView(R.id.tv_group_name).textSize = 12f
            }
            //
            holder.getTextView(R.id.tv_group_name).text = groupBean.name
            //
        }

        override fun convertForDifferentPos(holder: ViewHolder, item: GroupBean, position: Int) {
            holder.getmConverView().setOnClickListener { //
                val groupBean: GroupBean = targetRoomGroups.get(position)
                //
                initGroupData(groupBean)
                //
                notifyDataSetChanged()
            }
        }
    }

    private fun initGroupData(groupBean: GroupBean) {
        targetGroupId = groupBean.groupId
        //
        targetDevices.clear()
        //
        val devIds = groupBean.devIds
        for (devId in devIds) {
            val deviceBean = dataManger!!.getDeviceBean(devId)
            if (deviceBean != null) {
                targetDevices.add(deviceBean)
            }
        }
        //
        binding.lvDataDevices.setAdapter(DevicesAdapter(requireActivity(), targetDevices))
    }

    /**
     * 添加设备到群组
     *
     * @param groupBean
     */
    private fun addOrDelDeivceToGroup(type: Int, groupBean: GroupBean) {
        // 选择设备
        val name: MutableList<String?> = java.util.ArrayList()
        tempDevices.clear()
        for (allDevice in allDevices) {
            if (type == 0 && !groupBean.devIds.contains(allDevice.getDevId())) {
                name.add(allDevice.getMac())
                tempDevices.add(allDevice)
            }
            if (type == 1 && groupBean.devIds.contains(allDevice.getDevId())) {
                name.add(allDevice.getMac())
                tempDevices.add(allDevice)
            }
        }
        //
        val title =
            if (type == 0) "选择设备(添加到群组:" + groupBean.name + ")" else "选择设备(从群组:" + groupBean.name + "中删除)"
        MaterialDialog.Builder(requireActivity())
            .title(title)
            .items(name)
            .itemsCallback { dialog, itemView, position, text ->
                val deviceBean = tempDevices[position]
                //
                if (type == 0) {
                    deviceBean.addToGroupBean(groupBean)
                } else {
                    deviceBean.delGroupBean(groupBean)
                }
            }.show()
    }

    /**
     * 添加设备到房间
     *
     * @param roomBean
     */
    private fun addOrDelDeivceToRoomBean(type: Int, roomBean: RoomBean) {
        // 选择设备
        val name: MutableList<String?> = java.util.ArrayList()
        tempDevices.clear()
        for (allDevice in allDevices) {
            if (type == 0 && !roomBean.devIds.contains(allDevice.getDevId())) {
                name.add(allDevice.getMac())
                tempDevices.add(allDevice)
            }
            if (type == 1 && roomBean.devIds.contains(allDevice.getDevId())) {
                name.add(allDevice.getMac())
                tempDevices.add(allDevice)
            }
        }
        //
        val title =
            if (type == 0) "选择设备(添加到房间:" + roomBean.name + ")" else "选择设备(从房间:" + roomBean.name + "中删除)"
        //
        MaterialDialog.Builder(requireActivity())
            .title(title)
            .items(name)
            .itemsCallback { dialog, itemView, position, text ->
                val deviceBean = tempDevices[position]
                //
                if (type == 0) {
                    deviceBean.addToRoomBean(roomBean)
                } else {
                    deviceBean.delRoomBean(roomBean)
                }
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}