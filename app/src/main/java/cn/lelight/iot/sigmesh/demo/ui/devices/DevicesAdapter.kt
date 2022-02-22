package cn.lelight.iot.sigmesh.demo.ui.devices

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import cn.lelight.iot.sigmesh.demo.CommonDeviceActivity
import cn.lelight.iot.sigmesh.demo.R
import cn.lelight.leiot.data.bean.DeviceBean
import cn.lelight.leiot.sdk.adapter.CommonAdapter
import cn.lelight.leiot.sdk.adapter.ViewHolder
import cn.lelight.leiot.sdk.api.callback.IDeleteDeviceCallback
import com.afollestad.materialdialogs.MaterialDialog.Builder
import com.google.gson.Gson

internal class DevicesAdapter(context: Context?, datas: List<DeviceBean?>?) :
    CommonAdapter<DeviceBean>(context, datas, R.layout.item_data_device) {
    override fun convert(holder: ViewHolder, deviceBean: DeviceBean) {
        holder.getTextView(R.id.tv_device_name).text = deviceBean.getMac()
        holder.getTextView(R.id.tv_device_kind).text =
            "0x" + String.format("%02x", deviceBean.getDevSubType()).toUpperCase()
        holder.getTextView(R.id.tv_device_dps).text = Gson().toJson(deviceBean.getDps())
        //
        holder.getmConverView().setOnClickListener {
            // 点击事件
            mContext.startActivity(Intent(mContext, CommonDeviceActivity::class.java).apply {
                putExtra("ID", deviceBean.devId)
            })
        }
        //
        holder.getView<View>(R.id.tv_device_del).setOnClickListener { //
            Builder(mContext)
                .title("确定删除设备?")
                .content("请保持设备通电在线,否则需要8短重置才可重新添加")
                .positiveText("开始删除")
                .onPositive { dialog, which ->
                    deviceBean.onDelete(object : IDeleteDeviceCallback {
                        override fun onDeleteSuccess() {
                            Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show()
                        }

                        override fun onDeleteFail(msg: String) {
                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
                            //
                            showDeleteData(msg, deviceBean)
                        }
                    })
                }
                .negativeText("取消")
                .show()
        }
    }

    private fun showDeleteData(msg: String, deviceBean: DeviceBean) {
        Builder(mContext)
            .title("删除失败")
            .content("原因:$msg\n是否强制删除本地数据?\n(设备需要8短重置)")
            .positiveText("开始删除")
            .onPositive { dialog, which ->
                deviceBean.onDeleteData()
            }
            .negativeText("取消")
            .show()
    }
}