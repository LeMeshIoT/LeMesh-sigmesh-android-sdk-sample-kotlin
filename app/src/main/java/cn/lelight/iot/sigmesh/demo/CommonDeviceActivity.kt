package cn.lelight.iot.sigmesh.demo

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lelight.iot.sigmesh.demo.databinding.ActivityCommonDeviceBinding
import cn.lelight.leiot.data.bean.DeviceBean
import cn.lelight.leiot.data.bean.base.DpBean
import cn.lelight.leiot.data.leenum.DeviceType
import cn.lelight.leiot.data.leenum.dps.CurtainDp
import cn.lelight.leiot.data.leenum.dps.DpType
import cn.lelight.leiot.data.leenum.dps.LightDp
import cn.lelight.leiot.data.leenum.dps.SwitchDp
import cn.lelight.leiot.sdk.LeHomeSdk
import cn.lelight.leiot.sdk.adapter.CommonAdapter
import cn.lelight.leiot.sdk.adapter.ViewHolder
import cn.lelight.leiot.sdk.api.callback.IControlCallback
import cn.lelight.leiot.sdk.api.callback.data.IHomeDataChangeListener
import com.afollestad.materialdialogs.MaterialDialog

class CommonDeviceActivity : AppCompatActivity() {

    private var dps: ArrayList<DpPackageBean> = ArrayList()
    private var targetBean: DeviceBean? = null
    private var dpAdapter: DpAdapter? = null

    private lateinit var binding: ActivityCommonDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //
        //
        val dataManger = LeHomeSdk.getDataManger()
        if (dataManger == null) {
            finish()
            return
        }

        val id = intent.getStringExtra("ID")

        targetBean = dataManger.getDeviceBean(id)
        if (targetBean == null) {
            finish()
            return
        }
        //
        if (supportActionBar != null) {
            supportActionBar!!.title = targetBean!!.mac
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        //
        initData()

        LeHomeSdk.getInstance().setHomeDataChangeListener(object : IHomeDataChangeListener {
            override fun onDeviceAdd(deviceBean: DeviceBean) {
//                initData();
            }

            override fun onDeviceUpdate(deviceBean: DeviceBean) {
                if (dpAdapter != null) {
                    dpAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onDeviceDeleted(deviceBean: DeviceBean) {
//                initData();
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initData() {
        if (targetBean!!.getType() == DeviceType.Light.type) {
            for (value in LightDp.values()) {
                // 添加灯具的功能列表
                val dpPackageBean = DpPackageBean()
                dpPackageBean.id = value.dpId
                dpPackageBean.type = value.type
                dpPackageBean.name = value.getName()
                //
                dps.add(dpPackageBean)
            }
        } else if (targetBean!!.getType() == DeviceType.Curtain.type) {
            for (value in CurtainDp.values()) {
                // 添加灯具的功能列表
                val dpPackageBean = DpPackageBean()
                dpPackageBean.id = value.dpId
                dpPackageBean.type = value.type
                dpPackageBean.name = value.getName()
                //
                dps.add(dpPackageBean)
            }
        } else if (targetBean!!.getType() == DeviceType.Switch.type) {
            for (value in SwitchDp.values()) {
                // 添加灯具的功能列表
                val dpPackageBean = DpPackageBean()
                dpPackageBean.id = value.dpId
                dpPackageBean.type = value.type
                dpPackageBean.name = value.getName()
                //
                dps.add(dpPackageBean)
            }
        }
        //
        for (dp in dps) {
            //==//LelogUtil.e("-----" + dp.toString());
        }
        dpAdapter = DpAdapter(this, dps)
        binding.lvDps.setAdapter(dpAdapter)
    }

    inner class DpAdapter(context: Context?, datas: List<DpPackageBean>) :
        CommonAdapter<DpPackageBean>(context, datas, R.layout.item_common_dp) {
        override fun convert(holder: ViewHolder, dpPackageBean: DpPackageBean) {
            holder.getTextView(R.id.tv_dp_id).text = dpPackageBean.id.toString() + ""
            holder.getTextView(R.id.tv_dp_type).text = dpPackageBean.type.toString() + ""
            for (value in DpType.values()) {
                if (value.type == dpPackageBean.type) {
                    holder.getTextView(R.id.tv_dp_type).text = value.getName()
                    break
                }
            }
            //
            if (targetBean!!.getDps().containsKey(dpPackageBean.id)) {
                holder.getTextView(R.id.tv_dp_value)
                    .setText(targetBean!!.getDps().get(dpPackageBean.id).toString() + "")
            } else {
                holder.getTextView(R.id.tv_dp_value).text = "无"
            }
            holder.getTextView(R.id.tv_dp_name).text = dpPackageBean.name
            //
            holder.getmConverView().setOnClickListener {
                if (dpPackageBean.type == DpType.BOOL.type) {
                    showBoolDialog(dpPackageBean)
                } else if (dpPackageBean.type == DpType.VALUE.type) {
                    showInputValueDialog(dpPackageBean)
                } else if (dpPackageBean.type == DpType.STR.type) {
                    showInputStrDialog(dpPackageBean)
                }
            }
        }
    }

    private fun showInputValueDialog(dpPackageBean: DpPackageBean) {
        //
        MaterialDialog.Builder(this)
            .title(dpPackageBean.name!!)
            .content("请根据具体范围输入数值\n本弹窗不做范围限制，仅作调试测试用")
            .input(
                "输入数值", "", false
            ) { dialog, input ->
                try {
                    val value = input.toString().toInt()
                    // todo
                    targetBean!!.sendDp(
                        DpBean(dpPackageBean.id, dpPackageBean.type, value),
                        object : IControlCallback {
                            override fun onSuccess() {
                                Toast.makeText(
                                    this@CommonDeviceActivity,
                                    "发送成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFail(code: Int, msg: String) {
                                Toast.makeText(
                                    this@CommonDeviceActivity,
                                    "发送失败:$msg",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .inputType(InputType.TYPE_CLASS_NUMBER)
            .show()
    }

    private fun showInputStrDialog(dpPackageBean: DpPackageBean) {
        MaterialDialog.Builder(this)
            .title(dpPackageBean.name!!)
            .content("请根据具体操作输入字符\n本弹窗不做内容判断，仅作调试测试用")
            .input(
                "输入内容", "", false
            ) { dialog, input ->
                try {
                    val value = input.toString()
                    // todo
                    targetBean!!.sendDp(
                        DpBean(dpPackageBean.id, dpPackageBean.type, value),
                        object : IControlCallback {
                            override fun onSuccess() {
                                Toast.makeText(
                                    this@CommonDeviceActivity,
                                    "发送成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFail(code: Int, msg: String) {
                                Toast.makeText(
                                    this@CommonDeviceActivity,
                                    "发送失败:$msg",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .show()
    }

    private fun showBoolDialog(dpPackageBean: DpPackageBean) {
        MaterialDialog.Builder(this)
            .title(dpPackageBean.name!!)
            .items(*"打开,关闭".split(",".toRegex()).toTypedArray())
            .itemsCallback { dialog, itemView, position, text ->
                targetBean!!.sendDp(
                    DpBean(dpPackageBean.id, dpPackageBean.type, position == 0),
                    object : IControlCallback {
                        override fun onSuccess() {}
                        override fun onFail(code: Int, msg: String) {
                            Toast.makeText(
                                this@CommonDeviceActivity,
                                "fail:$msg",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    })
            }.show()
    }

    inner class DpPackageBean {
        var id = 0
        var type = 0
        var name: String? = null
        override fun toString(): String {
            return "DpPackageBean{" +
                    "id=" + id +
                    ", type=" + type +
                    ", name='" + name + '\'' +
                    '}'
        }
    }
}