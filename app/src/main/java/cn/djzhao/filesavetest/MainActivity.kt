package cn.djzhao.filesavetest

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


private const val TAG = "MainActivity"
private const val NEED_STORAGE_PERMISSION = 0
private const val CREATE_FILE = 1
private const val PICK_FILE = 2
private const val FILE_MANAGER = 3

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
    }

    @DelicateCoroutinesApi
    fun readFileSilent(view: View) {
        if (!checkPermission()) {
            ToastUtils.showShort("无权限")
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "start Read")
            val externalStoragePath = PathUtils.getExternalStoragePath()
            val fileName = "appinfo.properties"
            val filePath =
                externalStoragePath + File.separator + "Documents" + File.separator + fileName
            if (FileUtils.isFileExists(filePath)) {
                Log.d(TAG, "File Existed")
                val readFile2String = FileIOUtils.readFile2String(filePath)
                if (readFile2String.isEmpty()) {
                    ToastUtils.showShort("无法读取")
                } else {
                    ToastUtils.showShort(readFile2String)
                }
            }
            Log.d(TAG, "Read finished")
        }
    }

    @DelicateCoroutinesApi
    fun saveFileSilent(view: View) {
        if (!checkPermission()) {
            ToastUtils.showShort("无权限")
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "start save")
            val externalStoragePath = PathUtils.getExternalStoragePath()
            val fileName = "appinfo.properties"
            val outputPath =
                externalStoragePath + File.separator + "Documents" + File.separator + fileName
            val writeFileFromString = FileIOUtils.writeFileFromString(outputPath, "fooooo:baaaar")
            if (writeFileFromString) {
                Log.d(TAG, "saved")
                ToastUtils.showShort("已存储")
            } else {
                ToastUtils.showShort("无法存储")
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (checkPermission()) {
            return
        }
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, FILE_MANAGER)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, FILE_MANAGER)
            }
        } else {
            // Android 11以下
            when {
                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)
                -> ToastUtils.showShort("App必须获得存储权限才可以正常使用")

                else -> requestPermissions(
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    NEED_STORAGE_PERMISSION
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            NEED_STORAGE_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ToastUtils.showShort("权限已经获得")
                } else {
                    ToastUtils.showShort("权限被拒绝，APP可能无法正常运行")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            FILE_MANAGER -> {
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        ToastUtils.showShort("权限已获得")
                    } else {
                        ToastUtils.showShort("权限被拒绝，APP可能无法正常运行")
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}