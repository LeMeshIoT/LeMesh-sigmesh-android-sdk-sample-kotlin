package cn.lelight.iot.sigmesh.demo.ui.devices

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.Toast
import cn.lelight.iot.sigmesh.demo.R
import cn.lelight.leiot.data.bean.DeviceBean
import cn.lelight.leiot.data.bean.base.DpBean
import cn.lelight.leiot.data.leenum.DevSubType
import cn.lelight.leiot.data.leenum.dps.LightDp
import cn.lelight.leiot.sdk.adapter.CommonAdapter
import cn.lelight.leiot.sdk.adapter.ViewHolder
import cn.lelight.leiot.sdk.api.ability.light.BrightnessAbility
import cn.lelight.leiot.sdk.api.ability.light.CctWyAbility
import cn.lelight.leiot.sdk.api.callback.IControlCallback
import cn.lelight.leiot.sdk.api.callback.IDeleteDeviceCallback
import com.afollestad.materialdialogs.MaterialDialog.*
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
            val select: MutableList<String?> =
                ArrayList()
            val dpIds: List<Int> =
                ArrayList(deviceBean.getDps().keys)
            for (key in dpIds) {
                select.add("dpId:" + key + " value:" + deviceBean.getDps()[key])
            }
            Builder(mContext)
                .title("选择控制内容")
                .items(select)
                .itemsCallback { dialog, itemView, position, text ->
                    val dpId = dpIds[position]
                    if (deviceBean.getDevSubType() == DevSubType.Light_WY.type) {
                        // 灯具
                        for (value in LightDp.values()) {
                            if (value.dpId == dpId) {
                                showDpDialog(deviceBean, value)
                                break
                            }
                        }
                    }
                }.show()
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
                        }
                    })
                }
                .negativeText("取消")
                .show()
        }
    }

    private fun showDpDialog(deviceBean: DeviceBean, value: LightDp) {
        if (value.type == 1) {
            // bool
            Builder(mContext)
                .title("选择控制内容")
                .items(*"打开,关闭".split(",".toRegex()).toTypedArray())
                .itemsCallback { dialog, itemView, position, text ->
                    deviceBean.sendDp(
                        DpBean(value.dpId, value.type, position == 0),
                        object : IControlCallback {
                            override fun onSuccess() {}
                            override fun onFail(code: Int, msg: String) {
                                Toast.makeText(mContext, "fail:$msg", Toast.LENGTH_SHORT).show()
                            }
                        })
                }.show()
        } else if (value.type == 2) {
            // 数值
            if (value.dpId == LightDp.BRIGHT.dpId) {
                Builder(mContext)
                    .title("选择亮度")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .input("范围(1~1000)", "", false,
                        InputCallback { dialog, input ->
                            val value1 = input.toString().toInt()
                            if (value1 < 1 || value1 > 1000) {
                                Toast.makeText(mContext, "范围异常", Toast.LENGTH_SHORT).show()
                                return@InputCallback
                            }
                            if (deviceBean is BrightnessAbility) {
                                val brightnessAbility = deviceBean as BrightnessAbility
                                brightnessAbility.changeBrightness(value1)
                            }
                        })
                    .inputRange(1, 4)
                    .show()
            } else if (value.dpId == LightDp.CCT.dpId) {
                Builder(mContext)
                    .title("选择色温")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .input("范围(0~1000)", "", false,
                        InputCallback { dialog, input ->
                            val value1 = input.toString().toInt()
                            if (value1 < 0 || value1 > 1000) {
                                Toast.makeText(mContext, "范围异常", Toast.LENGTH_SHORT).show()
                                return@InputCallback
                            }
                            if (deviceBean is CctWyAbility) {
                                val cctWyAbility = deviceBean as CctWyAbility
                                cctWyAbility.changeWy(value1)
                            }
                        })
                    .inputRange(1, 4)
                    .show()
            }
            // Toast.makeText(mContext, "暂不支持", Toast.LENGTH_SHORT).show();
        }
    }
}