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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;

import co.com.exile.exile.chat.ChatFragment;
import co.com.exile.exile.profile.ProfileFragment;
import co.com.exile.exile.report.ReportFragmetPagerAdapter;
import co.com.exile.exile.task.TasksFragmetPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private TasksFragmetPagerAdapter tasksAdapter;
    private ReportFragmetPagerAdapter reportAdapter;
    private ViewPager mTaskPager;
    private ViewPager mReportPager;
    private FrameLayout fragmentContainer;
    private TabLayout tabs;
    private AppBarLayout appBar;
    private Toolbar toolbar;
    private BottomBar bottomBar;
    private Menu menu;

    private OnTabSelectListener tabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelected(@IdRes int tabId) {
            getSupportActionBar().show();
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
                case R.id.navigation_profile:
                    getSupportActionBar().hide();
                    showFragment(R.id.navigation_profile);
                    break;
            }
        }
    };

    void showFragment(int fragment) {
        tabs.setVisibility(View.GONE);

        Fragment f;

        if (fragment == R.id.navigation_chat) {
            f = new ChatFragment();
            showOption(R.id.nav_add);
        } else {
            f = new ProfileFragment();
        }

        if (mTaskPager.getVisibility() == View.VISIBLE) {
            mTaskPager.bringToFront();
            fragmentContainer.setVisibility(View.VISIBLE);

            Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

            fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mTaskPager.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mTaskPager.startAnimation(fade_out);
            fragmentContainer.startAnimation(fade_in);

        } else if (mReportPager.getVisibility() == View.VISIBLE) {
            mReportPager.bringToFront();
            fragmentContainer.setVisibility(View.VISIBLE);

            Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

            fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mReportPager.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mReportPager.startAnimation(fade_out);
            fragmentContainer.startAnimation(fade_in);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
