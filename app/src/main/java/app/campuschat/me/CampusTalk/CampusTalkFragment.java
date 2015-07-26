package app.campuschat.me.CampusTalk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import app.campuschat.me.MainActivity;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;

public class CampusTalkFragment extends ListFragment implements AdapterView.OnItemClickListener {

    protected static final String TAG = "CampusWallFragment";

    private ProgressDialog progressDialog;
    private SwipyRefreshLayout mSwipyRefreshLayout;

    private FloatingActionButton addWallItem;
    public static CampusTalkAdapter adapter;

    OnTalkClick mCallBack;
    public interface OnTalkClick {
        public void OnTalkClicked(String talkTrueId, String talkMessage);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallBack = (OnTalkClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTalkClick");
        }
    }

    private static CampusTalkFragment inst;
    public static CampusTalkFragment instance() {
        return inst;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.campus_talk_layout, container, false);

        addWallItem = (FloatingActionButton) view.findViewById(R.id.addWallItem);
        addWallItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance().sendTracker("CampusTalkFragment", "Post submission", "Clicked add post button", "Button click");
                MainActivity.mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        mSwipyRefreshLayout = (SwipyRefreshLayout) view.findViewById(R.id.swipyrefreshlayout);
        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                MainActivity.instance().sendTracker("CampusTalkFragment", "Refresh", "Swiped to refresh data", "Swipe");
                retrieveChats();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        if(MainActivity.loadOnce) {
            progressDialog.setMessage("Getting posts");
            progressDialog.show();
            retrieveChats();
            MainActivity.loadOnce = false;
        } else {
            retrievePosts();
        }

    }

    public void retrievePosts() {
        adapter = new CampusTalkAdapter(getActivity(), MainActivity.talkItems);
        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
        progressDialog.dismiss();
        mSwipyRefreshLayout.setRefreshing(false);
        addWallItem.attachToListView(getListView());
    }

    Response response;
    public void retrieveChats() {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(Config.queryPrefix + "gettalks.php").build();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(response.isSuccessful()) {

                    MainActivity.talkItems.clear();
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        JSONArray Jarray = object.getJSONArray("campustalk");
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                            String id = Jasonobject.optString("id");
                            String trueID = Jasonobject.optString("trueid");
                            String message = Jasonobject.optString("message");
                            String date = Jasonobject.optString("date");
                            int comments = Jasonobject.optInt("comments");
                            String rating = Jasonobject.optString("rating");

                            MainActivity.talkItems.add(new CampusTalkItem(id, trueID, message, date, comments, rating));
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "No posts", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Unable to retrieve posts", Toast.LENGTH_LONG).show();
                }
                retrievePosts();
            }
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String trueId = MainActivity.talkItems.get(position).trueID;
        String talkMessage = MainActivity.talkItems.get(position).message;
        mCallBack.OnTalkClicked(trueId, talkMessage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
