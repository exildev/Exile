package co.com.exile.exile.task;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class TasksFragmetPagerAdapter extends FragmentPagerAdapter {

    public TasksFragmetPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return TodayFragment.newInstance();
        }
        return ScheduleFragment.newInstance();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Hoy";
        } else {
            return "Proximos";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
