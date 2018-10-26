package co.com.exile.exile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

open class BaseActivity : AppCompatActivity() {

    private var url: String? = null

    override fun onResume() {
        super.onResume()

        if (getURL() == null) {
            startActivityForResult(Intent(this, UrlActivity::class.java), 1)
        } else {
            url = getURL()
        }

        Log.e("tales5", "url: " + url!!)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        if (getURL() == null) {
            startActivityForResult(Intent(this, UrlActivity::class.java), 1)
        } else {
            url = getURL()
        }

        Log.e("tales5", "url: " + url!!)
    }

    private fun getURL(): String? {
        val sharedPref = getSharedPreferences("UrlPref", Context.MODE_PRIVATE)
        return sharedPref.getString("url", null)
    }

    protected fun getUrl(serviceUrl: String): String {
        var serviceUrl = serviceUrl
        if (serviceUrl.substring(0, 1) == "/") {
            serviceUrl = serviceUrl.substring(1)
        }
        return Uri.parse(getURL())
                .buildUpon()
                .appendEncodedPath(serviceUrl)
                .build()
                .toString()
    }
}
