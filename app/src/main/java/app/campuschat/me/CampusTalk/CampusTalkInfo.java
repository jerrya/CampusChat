package app.campuschat.me.CampusTalk;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import app.campuschat.me.MainActivity;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.UtilityClass;

public class CampusTalkInfo extends ListFragment {

    private TextView talkInfoMessage;
    private EditText talkCommentMessage;
    private Button talkInfoCommentSubmit;

    String trueId, talkMessage;
    private Response response;

    protected static final String TAG = "CampusTalkInfo";

    public static CampusTalkInfoAdapter adapter;
    public static ArrayList<String> talkInfoCommentList = new ArrayList<>();

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.campus_talk_info_layout2, container, false);

        talkInfoMessage = (TextView) view.findViewById(R.id.talkInfoMessage);

        talkCommentMessage = (EditText) view.findViewById(R.id.talkCommentMessage);
        talkCommentMessage.setFilters(new InputFilter[] { new InputFilter.LengthFilter(150) });

        talkInfoCommentSubmit = (Button) view.findViewById(R.id.talkInfoCommentSubmit);
        talkInfoCommentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = talkCommentMessage.getText().toString().trim();
                if(message.length() > 0) {
                    MainActivity.instance().sendTracker("CampusTalkInfo", "Sending something", "Sending a comment", "Button click");
                    MainActivity.message = message;
                    MainActivity.instance().sendTalkInfoComment();
                    UtilityClass.hideKeyboard(getActivity());
                    talkCommentMessage.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            adapter = new CampusTalkInfoAdapter(getActivity(), talkInfoCommentList);
            getListView().setAdapter(adapter);
        }
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
        MainActivity.loadOnce = true;
        MainActivity.isWall = true;

        Bundle args = getArguments();
        if(args != null) {
            trueId = args.getString("talktrueid");
            MainActivity.trueId = trueId;

            talkMessage = args.getString("talkmessage");

            talkInfoMessage.setText(talkMessage);

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(trueId);

            if(view != null) {
                view.setBackgroundColor(color);
            }

            retrieveComments();
        }
    }

    public void retrieveComments() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder().add("trueid", trueId).build();
        final Request request = new Request.Builder().url(Config.queryPrefix + "getcomments.php").post(formBody).build();

        MainActivity.navList.clear();
        talkInfoCommentList.clear();

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
                            String message = Jasonobject.optString("message");

                            talkInfoCommentList.add(message);
                            MainActivity.tableCreated = true;
                        }
                    } catch (Exception e) {
                        MainActivity.tableCreated = false;
                    }
                } else {
                    Log.e(TAG, "Connection error");
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
