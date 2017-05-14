package co.com.exile.exile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import co.com.exile.exile.task.TasksFragmetPagerAdapter;

public class MainActivity extends AppCompatActivity {

    TasksFragmetPagerAdapter tasksAdapter;
    private ViewPager mViewPager;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_tasks:
                    mViewPager.setVisibility(View.VISIBLE);
                    if (tasksAdapter == null) {
                        tasksAdapter = new TasksFragmetPagerAdapter(getSupportFragmentManager());
                    }
                    mViewPager.setAdapter(tasksAdapter);
                    return true;
                case R.id.navigation_report:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_tasks);
    }

}
