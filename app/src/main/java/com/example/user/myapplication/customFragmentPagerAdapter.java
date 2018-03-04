package com.example.user.myapplication;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class customFragmentPagerAdapter extends FragmentPagerAdapter {

    private  Context mContext = null;
    private String[] mTitles;

    public customFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mTitles = new String[]{mContext.getResources().getString(R.string.text_import_tab),
                mContext.getResources().getString(R.string.text_import_tab),
                mContext.getResources().getString(R.string.text_setting_tab)};
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new ExportContactsFragment();
        } else if (position == 2) {
            return new SettingFragment();
        }
//        return new ImportContactsFragment();
        return new NewImportContactsFragment();
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    //ViewPager与TabLayout绑定后，这里获取到PageTitle就是Tab的Text
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
