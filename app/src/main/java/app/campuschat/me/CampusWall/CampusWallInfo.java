package app.campuschat.me.CampusWall;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import app.campuschat.me.MainActivity;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.TouchImageView;
import app.campuschat.me.Utility.UtilityClass;

public class CampusWallInfo extends Fragment {

    private TouchImageView imageView;
    private TextView imageInfoComments;
    private TextView wallInfoDate;
    private String trueId, imageUrl;
    protected static final String TAG = "CampusWallInfo";
    private ArrayList<CommentWallItem> commentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.campus_wall_info, container, false);

        imageView = (TouchImageView) view.findViewById(R.id.imageView);

        imageInfoComments = (TextView) view.findViewById(R.id.imageInfoComments);
        imageInfoComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance().sendTracker("CampusWallInfo", "Comment view clicked", "Clicked to view wall image comments", "Button click");
                MainActivity.mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        wallInfoDate = (TextView) view.findViewById(R.id.wallInfoDate);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        MainActivity.enterComment.setLayoutParams(params);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(UtilityClass.notConnected()) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        MainActivity.viewingComments = true;
        MainActivity.isWall = false;

        Bundle args = getArguments();
        if(args != null) {
            imageUrl = Config.WALL_PREFIX + args.getString("imageurl").trim();
            trueId = args.getString("trueid", trueId).trim();
            String date = args.getString("date");
            date = UtilityClass.getTimeAgo(UtilityClass.getDateInMillis(date));
            wallInfoDate.setText(date);

            MainActivity.trueId = trueId;

            Picasso.with(getActivity()).load(imageUrl).into(imageView);
            retrieveComments();
        }
    }

    Response response;
    public void retrieveComments() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder().add("trueid", trueId).build();
        final Request request = new Request.Builder().url(Config.queryPrefix + "getcomments.php").post(formBody).build();

        commentList.clear();
        MainActivity.navList.clear();

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
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        JSONArray Jarray = object.getJSONArray("comments");
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                            String id = Jasonobject.optString("id");
                            String username = Jasonobject.optString("username");
                            String message = Jasonobject.optString("message");

                            MainActivity.navList.add(message);
                            MainActivity.tableCreated = true;
                        }
                    } catch (Exception e) {
                        MainActivity.tableCreated = false;
                    }
                }
                MainActivity.instance().panelAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
