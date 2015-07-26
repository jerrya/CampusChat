package app.campuschat.me.MainContainer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import app.campuschat.me.MainActivity;
import app.campuschat.me.MultiUserChat.MUCFragment;
import app.campuschat.me.R;
import app.campuschat.me.Utility.SlidingTabLayout;
import app.campuschat.me.Utility.UtilityClass;

public class MainTabContainer extends Fragment {

    private final String TAG = "MainTabContainer";

    public static boolean viewingGroupChat = false;

    MainTabAdapter mMainTabAdapter;
    ViewPager mViewPager;
    SlidingTabLayout mSlidingTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_layout, container, false);

        mMainTabAdapter = new MainTabAdapter(getChildFragmentManager(), getActivity());

        MainActivity.enterComment.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mMainTabAdapter);

        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setBackgroundResource(R.color.blue_main);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });

        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MainActivity.mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                if (position == 0) {
                    MainActivity.instance().sendTracker("MainTabContainer", "Tab viewing", "Viewing POSTS tab", "Button click");
                    viewingGroupChat = false;
                } else if (position == 1) {
                    MainActivity.instance().sendTracker("MainTabContainer", "Tab viewing", "Viewing GROUP CHAT tab", "Button click");
                    MUCFragment.instance().notifyMessages();
                    viewingGroupChat = true;
                } else if (position == 2) {
                    MainActivity.instance().sendTracker("MainTabContainer", "Tab viewing", "Viewing WALL tab", "Button click");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(UtilityClass.notConnected()) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }
        MainActivity.viewingComments = false;
        MainActivity.isWall = false;
        MainActivity.panelTitle.setText("ADD POST");
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.panelTitle.setText("COMMENTS");
    }
}
