package cn.djzhao.filesavetest

import android.app.Application
import com.blankj.utilcode.util.Utils

/**
 *
 *
 * @author djzhao
 * @date 21/06/28
 */
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // AndroidUtilCode
        Utils.init(this)
    }
}