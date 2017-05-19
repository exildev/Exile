package co.com.exile.exile;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;

import co.com.exile.exile.chat.ChatFragment;
import co.com.exile.exile.task.TasksFragmetPagerAdapter;

public class MainActivity extends AppCompatActivity {

    TasksFragmetPagerAdapter tasksAdapter;
    private ViewPager mViewPager;
    private FrameLayout fragmentContainer;
    private TabLayout tabs;
    private AppBarLayout appBar;
    private Menu menu;

    private OnTabSelectListener tabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelected(@IdRes int tabId) {
            appBar.setExpanded(true);
            switch (tabId) {
                case R.id.navigation_tasks:
                    mViewPager.setVisibility(View.VISIBLE);
                    if (tasksAdapter == null) {
                        tasksAdapter = new TasksFragmetPagerAdapter(getSupportFragmentManager());
                    }
                    mViewPager.setAdapter(tasksAdapter);
                    fragmentContainer.setVisibility(View.GONE);
                    hideOption(R.id.nav_add);
                    tabs.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_report:
                    showFragment(-1);
                    break;
                case R.id.navigation_chat:
                    showFragment(R.id.navigation_chat);
                    break;
            }
        }
    };

    void showFragment(int fragment) {
        fragmentContainer.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);
        showOption(R.id.nav_add);
        tabs.setVisibility(View.GONE);

        Fragment f;

        if (fragment == R.id.navigation_chat) {
            f = new ChatFragment();
        } else {
            f = new ChatFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, f)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBar = (AppBarLayout) findViewById(R.id.appbar);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);

        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(tabSelectListener);

        BottomBarTab nearby = bottomBar.getTabWithId(R.id.navigation_chat);
        nearby.setBadgeCount(5);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void hideOption(int id) {
        if (menu != null) {
            MenuItem item = menu.findItem(id);
            item.setVisible(false);
        }
    }

    private void showOption(int id) {
        if (menu != null) {
            MenuItem item = menu.findItem(id);
            item.setVisible(true);
        }
    }
}
