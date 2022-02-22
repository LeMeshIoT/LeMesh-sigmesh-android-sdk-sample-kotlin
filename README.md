# LeMesh-sigmesh-android-sdk-sample-kotlin

#### 输入授权信息 

```
// MainActivity
// 输入对应信息,否则初始化的时候会报 "非法授权sdk"
private val appid = "appid"
private val mac = "mac"
private val secret = "secret"
```

#### SigMesh网络相关设置

```
// SigDemoInstance#init 中可以设置netkey,appkey,地址等信息
fun init(context: Context) {
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
            // 设置监听
            bleSigMeshManger.setStatusChangeListener(this)
        }
    }
```

#### 流程说明

1. 初始化sdk
2. 初始化sigmesh模块
3. 添加设备(如果之前添加了可以忽略这步)
4. 搜索已经配网的设备
5. 连接设备
6. 控制设备
