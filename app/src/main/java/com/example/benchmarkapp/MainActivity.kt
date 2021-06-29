package com.example.benchmarkapp

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    //MasterBranch Now
    private val REQUEST_CODE : Int = 1000
    private val TAG ="TestApp"
    private val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val getCpuInfo=GetCpuInfo()
    private var core_count:Int = 0
    private lateinit var getRamInfo: GetRamInfo

    //ROMの取得
    private lateinit var tv_ROM_Info:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //permissionチェック
        checkPermission(permissions, REQUEST_CODE)

        //Android11以降のpermissionチェック
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            checkLogPermission()
        }

        //モニタースタート
        val serviceButton:Button=findViewById(R.id.monitor_start)
        serviceButton.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            startForegroundService(intent)
        }

        //モニターストップ
        val stopButton:Button=findViewById(R.id.monitor_stop)
        stopButton.setOnClickListener {
            val intent = Intent(this,MyService::class.java)
            stopService(intent)
        }


        //テキストビューの設定　コア数の取得
        val tv_cores: List<TextView> = listOf(
            findViewById(R.id.tv_corenum1),
            findViewById(R.id.tv_corenum2),
            findViewById(R.id.tv_corenum3),
            findViewById(R.id.tv_corenum4),
            findViewById(R.id.tv_corenum5),
            findViewById(R.id.tv_corenum6),
            findViewById(R.id.tv_corenum7),
            findViewById(R.id.tv_corenum8),
            )

        core_count = getCpuInfo.countCoreNum()
        if (core_count == -1){
            Toast.makeText(this,"CPUを読み取ることができません",Toast.LENGTH_SHORT).show()
            return
        }

            val minFreqs = getCpuInfo.takeMinCpuFreqs(core_count + 1)
            val maxFreqs = getCpuInfo.takeMaxCpuFreqs(core_count + 1)

            //取得したコアをテキストビューに反映する
            for (i in 0..core_count) {
                val stringBuilder = StringBuilder()
                stringBuilder.append("Core: ${i + 1}")
                    .append("\n")
                    .append("Min: ${minFreqs[i] / 1000}MHz")
                    .append("\n")
                    .append("Max :${maxFreqs[i] / 1000}MHz")
                tv_cores[i].setText(stringBuilder.toString())
            }



        //現在のCPU周波数の設定
        val tv_currFreqs:List<TextView> = listOf(
            findViewById(R.id.tv_currFreq1),
            findViewById(R.id.tv_currFreq2),
            findViewById(R.id.tv_currFreq3),
            findViewById(R.id.tv_currFreq4),
            findViewById(R.id.tv_currFreq5),
            findViewById(R.id.tv_currFreq6),
            findViewById(R.id.tv_currFreq7),
            findViewById(R.id.tv_currFreq8),
            )

        //RAMの取得処理
        getRamInfo =GetRamInfo(applicationContext)
        val tv_total_RAM:TextView=findViewById(R.id.tv_total_RAM)
        val tv_RAM_Info:TextView=findViewById(R.id.tv_RAM_Info)
        tv_total_RAM.setText("TOTAL RAM: ${getRamInfo.totalmemory.toInt()}MB")

        //ROMの取得
        tv_ROM_Info =findViewById(R.id.tv_RomInfo)
        getRomUsage()



        // 定期取得処理は全部ここに入れる
        val mTimer = Timer(true)
        val mHandler=Handler()
        mTimer.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post(Runnable {
                    //現在のRAM使用率を取得
                    val sbRAM=StringBuilder()
                    getRamInfo.update_property()
                    sbRAM.append("Used RAM: ${getRamInfo.usedmem.toInt()}MB")
                        .append("\n")
                        .append("Free RAM: ${getRamInfo.availmem.toInt()}MB")
                        .append("\n")
                        .append("RAM Usage: ${"%.2f".format(getRamInfo.ramUsage)}%")
                    tv_RAM_Info.setText(sbRAM.toString())

                    //現在のCPU周波数を取得
                    val currentFreqs=getCpuInfo.takeCurrentCpuFreqs(core_count+1)
                    val cpuUsages=getCpuInfo.getCpuUsages(core_count+1,minFreqs,maxFreqs,currentFreqs)
                    Log.d(TAG,"CpuUsages:"+cpuUsages.contentToString())
                    for (i in 0..core_count){
                        val stringBuilder2=StringBuilder()
                        stringBuilder2.append("Now: ${currentFreqs[i]/1000}MHz")
                            .append("\n")
                            .append("Usage: ${"%.1f".format(cpuUsages[i])}%")
                        tv_currFreqs[i].setText(stringBuilder2.toString())
                    }


                })
            }
        }, 1, 1000) //1ミリ秒後にintervalミリ秒ごとの繰り返し




    }




    @SuppressLint("DiscouragedPrivateApi")
    private fun getRomUsage(){
        val sb = java.lang.StringBuilder()

        val storageManager = getSystemService(StorageManager::class.java)

        //システムストレージの取得
        val statFs_system = StatFs("/system")
        // 総容量
        val totalSpase1: Long = statFs_system.blockCountLong * statFs_system.blockSizeLong / 1024L / 1024L
        // 空き容量
        val freeSpase1: Long = statFs_system.availableBlocksLong * statFs_system.blockSizeLong / 1024L / 1024L
        // 使用容量
        val usedSpase1: Long = totalSpase1 - freeSpase1
        Log.d(TAG,"Label:システムストレージ")
        Log.d(TAG, " Used space: ${String.format(Locale.US, "%,12d", usedSpase1)}MB")
        Log.d(TAG, " Free space: ${String.format(Locale.US, "%,12d", freeSpase1)}MB")
        Log.d(TAG, "Total space: ${String.format(Locale.US, "%,12d", totalSpase1)}MB")

        sb.append("システムストレージ  path: /system")
            .append("\n")
            .append("Used Space : $usedSpase1 MB  Total Space : $totalSpase1 MB " )


        for (storageVolume: StorageVolume in storageManager.storageVolumes) {
            // ストレージボリュームのリストを取得し、1件づつ処理
            // ストレージボリュームから絶対パスを取得
            val path: String = when {
                Build.VERSION.SDK_INT>= Build.VERSION_CODES.R -> {
                    // Android 11以降
                    storageVolume.directory?.absolutePath
                }
                else -> {
                    // Android 10以前
                    // NOTE: Android10以前ではgetPathがprivateなため無理やり実行して取得
                    val getPath = StorageVolume::class.java.getDeclaredMethod("getPath")
                    getPath.invoke(storageVolume) as String?
                }
            } ?: continue // 絶対パスが取得できない場合は、スキップ

            val label: String = when {
                Build.VERSION.SDK_INT>= Build.VERSION_CODES.R -> {
                    // Android 11以降
                    storageVolume.getDescription(applicationContext)
                }
                else -> {
                    storageVolume.getDescription(applicationContext)
                }
            } ?: continue // 絶対パスが取得できない場合は、スキップ

            val statFs = StatFs(path)
            // 総容量
            val totalSpase: Long = statFs.blockCountLong * statFs.blockSizeLong / 1024L / 1024L
            // 空き容量
            val freeSpase: Long = statFs.availableBlocksLong * statFs.blockSizeLong / 1024L / 1024L
            // 使用容量
            val usedSpase: Long = totalSpase - freeSpase

            Log.d(TAG, "Path: $path")
            Log.d(TAG,"Label: $label")
            Log.d(TAG, " Used space: ${String.format(Locale.US, "%,12d", usedSpase)}MB")
            Log.d(TAG, " Free space: ${String.format(Locale.US, "%,12d", freeSpase)}MB")
            Log.d(TAG, "Total space: ${String.format(Locale.US, "%,12d", totalSpase)}MB")

            sb.append("\n")
                .append("$label  path: $path")
                .append("\n")
                .append("Used Space : $usedSpase MB  Total Space : $totalSpase MB " )

        }

        tv_ROM_Info.setText(sb.toString())

    }






    //Permissionチェックのメソッド
    private fun checkPermission(permissions: Array<String>?, request_code: Int) {
        // 許可されていないものだけダイアログが表示される
        ActivityCompat.requestPermissions(this, permissions!!, request_code)
    }

    // requestPermissionsのコールバック
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                var i = 0
                while (i < permissions.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        /*     Toast toast = Toast.makeText(this,
                                "Added Permission: " + permissions[i], Toast.LENGTH_SHORT);
                        toast.show(); */
                    } else {
                        val toast = Toast.makeText(this,
                                "設定より権限をオンにした後、アプリを再起動してください", Toast.LENGTH_LONG)
                        toast.show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        //Fragmentの場合はgetContext().getPackageName()
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    i++
                }
            }
            else -> {
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    private fun checkLogPermission(){
        if (Environment.isExternalStorageManager()){
            //todo when permission is granted
            Log.d(TAG,"MANAGE_EXTERNAL_STORAGE is Granted")
        }else{
            //request for the permission
            val logIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package",packageName,null)
            logIntent.setData(uri)
            startActivity(logIntent)
        }
    }

}

