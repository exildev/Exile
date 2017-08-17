package co.com.exile.exile;

import android.app.Application;

import shortbread.Shortbread;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Shortbread.create(this);
    }
}
