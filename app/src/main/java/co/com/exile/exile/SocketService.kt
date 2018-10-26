package co.com.exile.exile

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.util.Log

import java.net.CookieManager
import java.net.CookiePolicy

import co.com.exile.exile.network.SiCookieStore2
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class SocketService : Service() {

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null


    private val url: String?
        get() {
            val sharedPref = getSharedPreferences("UrlPref", Context.MODE_PRIVATE)
            return sharedPref.getString("url", null)
        }

    private val serviceReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val command = intent.getStringExtra("command")
            webSocket?.send(command)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val cookieStore2 = SiCookieStore2(baseContext)
        val cookieManager = CookieManager(cookieStore2, CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(cookieManager))
                .build()
        start()

        val intentFilter = IntentFilter(ACTION_TO_SERVICE)
        registerReceiver(serviceReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket!!.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
    }

    private fun start() {
        val url = getUrl(getString(R.string.socket_url))
        val request = Request.Builder().url(url).build()
        val listener = SocketListener()
        webSocket = client?.newWebSocket(request, listener)
        client?.dispatcher()?.executorService()?.shutdown()
    }

    internal inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket?, response: Response?) {
            Log.e("tales7", "its open: " + response?.message())
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            sendCommandResponse(text)
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            Log.e("tales7", "error:" + response?.message())
        }
    }

    private fun sendCommandResponse(text: String?) {
        val intent = Intent()
        intent.action = ACTION_TO_ACTIVITY
        intent.putExtra("response", text)
        sendBroadcast(intent)
    }

    private fun getUrl(serviceUrl: String): String {
        var serviceUrl = serviceUrl
        if (serviceUrl.substring(0, 1) == "/") {
            serviceUrl = serviceUrl.substring(1)
        }
        return Uri.parse(url)
                .buildUpon()
                .appendEncodedPath(serviceUrl)
                .build()
                .toString().replace("https", "ws").replace("http", "ws")
    }

    companion object {

        private const val ACTION_TO_SERVICE = "ToService"
        private const val ACTION_TO_ACTIVITY = "ToActivity"

        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}
