package com.socialmedia.socialmedia;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.socialmedia.socialmedia.fragments.MenuBFragment;
import com.socialmedia.socialmedia.fragments.MenuFragment;
import com.socialmedia.socialmedia.fragments.PostBFragment;
import com.socialmedia.socialmedia.fragments.PostFragment;

public class PagerAdapterB extends FragmentPagerAdapter {

    public PagerAdapterB(@NonNull FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return new PostBFragment();
        }
        else{
            return new MenuBFragment();
        }
    }

    @Override
    public int getCount() {
        return 2; // no. of tabs
    }


    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0){
            return "المنشورات";
        }else {
            return "الخدمات";
        }
    }
}
