package com.example.user.myapplication;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by hu on 2018/2/27.
 */

public class MainActivity extends AppCompatActivity {
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private customFragmentPagerAdapter mFragmentPagerAdapter;

    private TabLayout.Tab one;
    private TabLayout.Tab two;
    private TabLayout.Tab three;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getSupportActionBar().hide();//隐藏掉整个ActionBar
        setContentView(R.layout.activity_main);
        initToolBar();
        //初始化视图
        initViews();
    }

    private void initToolBar() {
                        toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle(R.string.toolbarTitle);
                        setSupportActionBar(toolbar);
                        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow);
                        toolbar.setNavigationOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    }
                }
        );
    }

    private void initViews() {

        //使用适配器将ViewPager与Fragment绑定在一起
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mFragmentPagerAdapter = new customFragmentPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentPagerAdapter);

        //将TabLayout与ViewPager绑定在一起
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        //指定Tab的位置
        one = mTabLayout.getTabAt(0);
        two = mTabLayout.getTabAt(1);
        three = mTabLayout.getTabAt(2);

        //设置Tab的图标
        one.setIcon(R.mipmap.ic_launcher);
        two.setIcon(R.mipmap.ic_launcher);
        three.setIcon(R.mipmap.ic_launcher);
    }
}
