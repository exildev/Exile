package co.com.exile.exile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseFragment extends Fragment {
    private static final String ACTION_TO_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";

    private String url;

    @Override
    public void onResume() {
        super.onResume();

        if (getURL() == null) {
            startActivityForResult(new Intent(this.getContext(), UrlActivity.class), 1);
        } else {
            url = getURL();
        }

        Log.e("tales5", "url: " + url);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (receiver != null) {
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_ACTIVITY);
            context.registerReceiver(receiver, intentFilter);
        }
    }

    private String getURL() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UrlPref", Context.MODE_PRIVATE);
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
                .toString();
    }

    protected void requestFriends() {
        try {
            sendCommandToService(new JSONObject().put("command", "friends"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void requestChats() {
        try {
            sendCommandToService(new JSONObject().put("command", "rooms"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void requestNotifications() {
        try {
            sendCommandToService(new JSONObject().put("command", "notification"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void onFriendsResponse(JSONObject response) {
        //TODO: implementar funcionalidad por defecto
    }

    protected void onChatsResponse(JSONObject response) {
        //TODO: implementar funcionalidad por defecto
    }

    private void sendCommandToService(JSONObject command) {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_TO_SERVICE);
        new_intent.putExtra("command", command.toString());
        getActivity().sendBroadcast(new_intent);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseString = intent.getStringExtra("response");
            try {
                JSONObject response = new JSONObject(responseString);
                if (response.getString("type").equals("friends")) {
                    onFriendsResponse(response);
                } else if (response.getString("type").equals("rooms")) {
                    onChatsResponse(response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
}
