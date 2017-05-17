package co.com.exile.exile;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;

import co.com.exile.exile.task.TasksFragmetPagerAdapter;

public class MainActivity extends AppCompatActivity {

    TasksFragmetPagerAdapter tasksAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton fab;
    private FrameLayout fragmentContainer;
    private TabLayout tabs;

    private OnTabSelectListener tabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelected(@IdRes int tabId) {
            switch (tabId) {
                case R.id.navigation_tasks:
                    mViewPager.setVisibility(View.VISIBLE);
                    if (tasksAdapter == null) {
                        tasksAdapter = new TasksFragmetPagerAdapter(getSupportFragmentManager());
                    }
                    mViewPager.setAdapter(tasksAdapter);
                    fragmentContainer.setVisibility(View.GONE);
                    fab.hide();
                    tabs.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_report:
                    fragmentContainer.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.GONE);
                    fab.show();
                    tabs.setVisibility(View.GONE);
                    break;
                case R.id.navigation_chat:
                    fragmentContainer.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.GONE);
                    fab.show();
                    tabs.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(tabSelectListener);

        BottomBarTab nearby = bottomBar.getTabWithId(R.id.navigation_chat);
        nearby.setBadgeCount(5);

    }

}
