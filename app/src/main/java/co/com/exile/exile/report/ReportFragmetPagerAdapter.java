package co.com.exile.exile.report;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class ReportFragmetPagerAdapter extends FragmentPagerAdapter {
    public static final int ADD_NEW_REPORT = 120;

    public ReportFragmetPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ReportFragment.newInstance(ReportFragment.TYPE_OPEN);
        }
        return ReportFragment.newInstance(ReportFragment.TYPE_CLOSED);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Abiertos";
        } else {
            return "Cerrados";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public void addReport(Activity activity) {
        Intent intent = new Intent(activity, ReportActivity.class);
        activity.startActivityForResult(intent, ADD_NEW_REPORT);
    }
}
