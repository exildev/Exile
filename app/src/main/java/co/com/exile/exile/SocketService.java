package co.com.exile.exile;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.net.CookieManager;
import java.net.CookiePolicy;

import co.com.exile.exile.R;
import co.com.exile.exile.network.SiCookieStore2;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class SocketService extends Service {

    private static final String ACTION_TO_SERVICE = "ToService";
    private static final String ACTION_TO_ACTIVITY = "ToActivity";

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final String FRIEND_COMMAND = "friends";
    private static final String ROOMS_COMMAND = "rooms";
    private static final String NOTIFICATION_COMMAND = "notification";

    private OkHttpClient client;
    private WebSocket webSocket;

    public SocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SiCookieStore2 cookieStore2 = new SiCookieStore2(getBaseContext());
        CookieManager cookieManager = new CookieManager(cookieStore2, CookiePolicy.ACCEPT_ALL);
        client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        start();

        if (serviceReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(ACTION_TO_SERVICE);
            registerReceiver(serviceReceiver, intentFilter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
    }

    private void start() {
        String url = getUrl(getString(R.string.socket_url));
        Request request = new Request.Builder().url(url).build();
        SocketListener listener = new SocketListener();
        webSocket = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }

    class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.e("tales7", "its open: " + response.message());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            sendCommandResponse(text);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e("tales7", "error:" + response.message());
        }
    }

    private void sendCommandResponse(String text) {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_TO_ACTIVITY);
        new_intent.putExtra("response", text);
        sendBroadcast(new_intent);
    }


    private String getURL() {
        SharedPreferences sharedPref = getSharedPreferences("UrlPref", Context.MODE_PRIVATE);
        return sharedPref.getString("url", null);
    }

    protected String getUrl(String serviceUrl) {
        if (serviceUrl.substring(0, 1).equals("/")) {
            serviceUrl = serviceUrl.substring(1);
        }
        return Uri.parse(getURL())
                .buildUpon()
                .appendEncodedPath(serviceUrl)
                .build()
                .toString().replace("http", "ws");
    }

    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            webSocket.send(command);
        }
    };
}
