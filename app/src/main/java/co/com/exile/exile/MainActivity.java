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
import co.com.exile.exile.report.ReportFragmetPagerAdapter;
import co.com.exile.exile.task.TasksFragmetPagerAdapter;

public class MainActivity extends AppCompatActivity {

    TasksFragmetPagerAdapter tasksAdapter;
    ReportFragmetPagerAdapter reportAdapter;
    private ViewPager mTaskPager;
    private ViewPager mReportPager;
    private FrameLayout fragmentContainer;
    private TabLayout tabs;
    private AppBarLayout appBar;
    private BottomBar bottomBar;
    private Menu menu;

    private OnTabSelectListener tabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelected(@IdRes int tabId) {
            appBar.setExpanded(true);
            switch (tabId) {
                case R.id.navigation_tasks:
                    showViewPager(R.id.navigation_tasks);
                    break;
                case R.id.navigation_report:
                    showViewPager(R.id.navigation_report);
                    break;
                case R.id.navigation_chat:
                    showFragment(R.id.navigation_chat);
                    break;
            }
        }
    };

    void showFragment(int fragment) {
        fragmentContainer.setVisibility(View.VISIBLE);
        mTaskPager.setVisibility(View.GONE);
        mReportPager.setVisibility(View.GONE);
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

    void showViewPager(int option) {
        if (option == R.id.navigation_tasks) {
            if (tasksAdapter == null) {
                tasksAdapter = new TasksFragmetPagerAdapter(getSupportFragmentManager());
                mTaskPager.setAdapter(tasksAdapter);
            }

            hideOption(R.id.nav_add);
            mTaskPager.setVisibility(View.VISIBLE);
            mReportPager.setVisibility(View.GONE);
            tabs.setupWithViewPager(mTaskPager);
        } else if (option == R.id.navigation_report) {
            if (reportAdapter == null) {
                reportAdapter = new ReportFragmetPagerAdapter(getSupportFragmentManager());
                mReportPager.setAdapter(reportAdapter);
            }

            showOption(R.id.nav_add);
            mTaskPager.setVisibility(View.GONE);
            mReportPager.setVisibility(View.VISIBLE);
            tabs.setupWithViewPager(mReportPager);
        }

        fragmentContainer.setVisibility(View.GONE);
        tabs.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBar = (AppBarLayout) findViewById(R.id.appbar);
        mTaskPager = (ViewPager) findViewById(R.id.task_pager);
        mReportPager = (ViewPager) findViewById(R.id.report_pager);
        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mTaskPager);

        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.nav_add) {

            switch (bottomBar.getCurrentTabId()) {
                case R.id.navigation_report:
                    reportAdapter.addReport(this);
                    break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
