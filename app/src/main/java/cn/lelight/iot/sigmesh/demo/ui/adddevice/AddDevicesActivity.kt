package cn.lelight.iot.sigmesh.demo.ui.adddevice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import cn.lelight.iot.sigmesh.demo.R
import cn.lelight.iot.sigmesh.demo.databinding.ActivityAddDevicesBinding
import cn.lelight.leiot.data.bean.DeviceBean
import cn.lelight.leiot.data.ble.ExtendedBluetoothDevice
import cn.lelight.leiot.data.ble.LeVerInfo
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.adapter.CommonAdapter
import cn.lelight.leiot.sdk.adapter.ViewHolder
import cn.lelight.leiot.sdk.api.IBleManger
import cn.lelight.leiot.sdk.api.callback.IBleScanUnProCallback
import cn.lelight.leiot.sdk.api.callback.sigmesh.LeSigMeshAddDeviceCallback
import com.afollestad.materialdialogs.MaterialDialog

class AddDevicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDevicesBinding

    private lateinit var bleManger: IBleManger

    private val addDevices = java.util.HashMap<String, ExtendedBluetoothDevice>()

    private var dialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //
        bleManger = LeHomeSdk.getBleManger()
        bleManger.bleScanManger.startScanUnProSigBleInfo(-1, true, object :
            IBleScanUnProCallback {
            override fun scanDeviceNotify(p0: HashMap<String, ExtendedBluetoothDevice>) {
                Log.i("AddDevicesActivity", "scanDeviceNotify " + p0.size)
                updateDataUi(p0)
            }

            override fun scanTimeOutResult(p0: HashMap<String, ExtendedBluetoothDevice>) {
                Log.i("scanTimeOutResult", "scanTimeOutResult " + p0.size)
                updateDataUi(p0)
            }

            override fun scanFail(msg: String) {
                Log.i("scanFail", "scanFail $msg")
            }
        })
        //
        binding.btnStopScan.setOnClickListener {
            bleManger.bleScanManger.stopScan()
            //
            binding.btnStartAdd.visibility = View.VISIBLE
            binding.btnStopScan.visibility = View.GONE
        }
        //
        binding.btnStartAdd.setOnClickListener {
            // 开始添加
            if (addDevices.size > 0) {
                dialog = MaterialDialog.Builder(this)
                    .title("正在添加")
                    .content("正在添加(1/" + addDevices.size + ")")
                    .progress(true, 0)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .show()
                LeHomeSdk.getBleSigMeshManger()
                    .startProvisionDevices(ArrayList(addDevices.values),
                        object : LeSigMeshAddDeviceCallback {
                            var success = 0
                            var fail = 0
                            override fun onAddDeviceFail(mac: String, error: String) {
                                fail++
                                dialog!!.setContent(
                                    "正在添加(${success + fail + 1}/${addDevices.size})" +
                                            "\n成功：" + success +
                                            "\n失败：" + fail
                                )
                            }

                            override fun onAddDeviceSuccess(deviceBean: DeviceBean) {
                                success++
                                dialog!!.setContent(
                                    ("正在添加(" + (success + fail + 1) + "/" + addDevices.size + ")" +
                                            "\n成功：" + (success) +
                                            "\n失败：" + (fail))
                                )
                            }

                            override fun onAddDevicesFinish(
                                successDevices: List<ExtendedBluetoothDevice>,
                                failDevices: List<ExtendedBluetoothDevice>
                            ) {
                                runOnUiThread {
                                    dialog!!.dismiss()
                                    dialog =
                                        MaterialDialog.Builder(this@AddDevicesActivity)
                                            .title("添加完成")
                                            .content("成功：$success\n失败：$fail")
                                            .positiveText("好的")
                                            .onPositive { dialog, which ->
                                                dialog.dismiss()
                                                finish()
                                            }
                                            .show()
                                }
                            }
                        })
            }
        }
    }

    private fun updateDataUi(p0: HashMap<String, ExtendedBluetoothDevice>) {
        if (p0.size > 0) {
            binding.llScan1.visibility = View.GONE
            binding.llScan2.visibility = View.VISIBLE
            //
        }
        //
        binding.lvScanDevices.adapter =
            DeviceAdapter(this@AddDevicesActivity, ArrayList(p0.values))
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManger.bleScanManger.stopScan()
    }

    inner class DeviceAdapter(context: Context, datas: List<ExtendedBluetoothDevice>) :
        CommonAdapter<ExtendedBluetoothDevice>(context, datas, R.layout.item_un_devices) {

        override fun convert(holder: ViewHolder, extendedBluetoothDevice: ExtendedBluetoothDevice) {
            val macStr: String = extendedBluetoothDevice.getAddress().replace(":".toRegex(), "")
            //
            val name = "未命名"
            //
            holder.getTextView(R.id.tv_unpair_name).setText(name)
            holder.getTextView(R.id.tv_unpair_name2).setText(extendedBluetoothDevice.getName())
            //
            //
            holder.getTextView(R.id.tv_unpair_mac).setText("Mac:$macStr")
            holder.getTextView(R.id.tv_unpair_ri).setText(
                "信号: " + extendedBluetoothDevice.getRssi()
            )
            //
            val leVerInfoByMac: LeVerInfo = bleManger.getBleScanManger().getLeVerInfoByMac(macStr)
            if (leVerInfoByMac != null) {
                holder.getTextView(R.id.tv_unpair_desc).setVisibility(View.VISIBLE)
                holder.getTextView(R.id.tv_unpair_desc).setText(
                    "编号:" + leVerInfoByMac.nopStr
                            + " 日期:" + leVerInfoByMac.getpVer()
                            + " PID:0x" + String.format(
                        "%04x",
                        leVerInfoByMac.pid
                    )
                )
            } else {
                holder.getTextView(R.id.tv_unpair_desc).setVisibility(View.GONE)
            }
            //
            //
            val btn_add_device: CheckBox = holder.getView<CheckBox>(R.id.btn_add_device)
            btn_add_device.setOnCheckedChangeListener(null)
            btn_add_device.isChecked = addDevices.containsKey(macStr)
            btn_add_device.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    addDevices.put(macStr, extendedBluetoothDevice)
                } else {
                    addDevices.remove(macStr)
                }
            }
            holder.getmConverView().setOnClickListener(View.OnClickListener {
                btn_add_device.isChecked = !btn_add_device.isChecked
            })
        }

    }
}