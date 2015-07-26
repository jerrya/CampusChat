package app.campuschat.me.MainContainer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import app.campuschat.me.CampusTalk.CampusTalkFragment;
import app.campuschat.me.CampusWall.CampusWallFragment;
import app.campuschat.me.MultiUserChat.MUCFragment;
import app.campuschat.me.R;

public class MainTabAdapter extends FragmentStatePagerAdapter {

    private final String TAG = "MainTabAdapter";

    final int PAGE_COUNT = 3;
    protected Context context;

    public MainTabAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "POSTS";
        } else if(position == 1) {
            return "CHAT";
        } else {
            return "WALL";
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CampusTalkFragment();
            case 1:
                return new MUCFragment();
            case 2:
                return new CampusWallFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

}