package com.example.benchmarkapp

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
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

        val minFreqs = getCpuInfo.takeMinCpuFreqs(core_count+1)
        val maxFreqs = getCpuInfo.takeMaxCpuFreqs(core_count+1)

        //取得したコアをテキストビューに反映する
        for (i in 0..core_count){
            val stringBuilder =StringBuilder()
            stringBuilder.append("Core: ${i+1}")
                .append("\n")
                .append("Min: ${minFreqs[i]/1000}MHz")
                .append("\n")
                .append("Max :${maxFreqs[i]/1000}MHz")
            tv_cores[i].setText(stringBuilder.toString())
        }

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

        // 設定した間隔おきにログを取得する
        val mTimer = Timer(true)
        val mHandler=Handler()
        mTimer.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post(Runnable {
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


    fun calcCpuUsage(){
        val count=getCpuInfo.countCoreNum()
        val minFreqs=getCpuInfo.takeMinCpuFreqs(count + 1)
        val maxFreqs=getCpuInfo.takeMaxCpuFreqs(count + 1)


        // 設定した間隔おきにログを取得する
        val mTimer = Timer(true)
        val mHandler=Handler()
        mTimer.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post(Runnable {
                    val currentFreqs=getCpuInfo.takeCurrentCpuFreqs(count+1)
                    val cpuUsages=getCpuInfo.getCpuUsages(count+1,minFreqs,maxFreqs,currentFreqs)
                    Log.d(TAG,"CpuUsages:"+cpuUsages.contentToString())

                })
            }
        }, 1, 1000) //1ミリ秒後にintervalミリ秒ごとの繰り返し
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

