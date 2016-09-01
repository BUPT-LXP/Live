package com.lue.live.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import com.lue.live.R;
import com.lue.live.fragment.LiveFragment;
import com.lue.live.fragment.LocalFragment;
import com.lue.live.fragment.MineFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";

    private LocalFragment localFragment;
    private LiveFragment liveFragment;
    private MineFragment mineFragment;
    private FragmentManager fragmentManager;

    private RadioGroup radioGroup;
    private ViewPager viewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.mipmap.v);
            actionBar.setDisplayUseLogoEnabled(true);
        }

        initView();


    }

    /**
     * 初始化布局
     */
    public void initView()
    {
        fragmentManager = getSupportFragmentManager();
        radioGroup = (RadioGroup) findViewById(R.id.tab_menu);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.button_local:
                        viewPager.setCurrentItem(0, true);
                        break;
                    case R.id.button_live:
                        viewPager.setCurrentItem(1, true);
                        break;
                    case R.id.button_mine:
                        viewPager.setCurrentItem(2, true);
                        break;
                    default:
                        break;
                }
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        localFragment = new LocalFragment();
        liveFragment = new LiveFragment();
        mineFragment = new MineFragment();

        List<Fragment> allFragments = new ArrayList<>();
        allFragments.add(localFragment);
        allFragments.add(liveFragment);
        allFragments.add(mineFragment);

        viewPager.setAdapter(new MyFragmentPagerAdapter(fragmentManager, allFragments));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                switch (position)
                {
                    case 0:
                        radioGroup.check(R.id.button_local);
                        break;
                    case 1:
                        radioGroup.check(R.id.button_live);
                        break;
                    case 2:
                        radioGroup.check(R.id.button_mine);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }


    /**
     * 重写按下返回键的响应
     */
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);//true对任何Activity都适用
    }


    class MyFragmentPagerAdapter extends FragmentPagerAdapter
    {
        private List<Fragment> list;

        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
