package app.campuschat.me.CampusWall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.melnykov.fab.FloatingActionButton;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import app.campuschat.me.MainActivity;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;

public class CampusWallFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ProgressDialog progressDialog;
    private FloatingActionButton fab;
    protected static final String TAG = "CampusWallFragment";

    private CampusWallAdapter adapter;

    private GridView gridView;
    private SwipyRefreshLayout mSwipyRefreshLayout_grid;

    OnImageClick mCallBack;
    public interface OnImageClick {
        public void OnImageClicked(String imageUrl, String trueId, String date);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallBack = (OnImageClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnImageClick");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.campus_wall_layout, container, false);

        gridView = (GridView) view.findViewById(R.id.gridview);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance().sendTracker("CampusWallFragment", "Wall addition", "Clicked to add image to wall", "Button click");
                CampusWallAdd campusWallAdd = new CampusWallAdd();
                android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, campusWallAdd);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        mSwipyRefreshLayout_grid = (SwipyRefreshLayout) view.findViewById(R.id.swipyrefreshlayout_grid);
        mSwipyRefreshLayout_grid.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                MainActivity.instance().sendTracker("CampusWallFragment", "Swipe", "Swipe to refresh WALL data", "Swipe");
                retrieveChats();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        if(MainActivity.loadOnce3) {
            progressDialog.setMessage("Getting posts");
            progressDialog.show();

            retrieveChats();
            MainActivity.loadOnce3 = false;
        } else {
            retrieveWall();
        }
    }

    Response response;
    public void retrieveChats() {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(Config.queryPrefix + "getwall.php").build();

        MainActivity.wallItems.clear();

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
                try {
                    JSONObject object = new JSONObject(response.body().string());
                    JSONArray Jarray = object.getJSONArray("campuswall");
                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject Jasonobject = Jarray.getJSONObject(i);
                        String id = Jasonobject.optString("id");
                        String trueID = Jasonobject.optString("trueid");
                        String date = Jasonobject.optString("date");
                        int comments = Jasonobject.optInt("comments");
                        String rating = Jasonobject.optString("rating");
                        String imageUrl = trueID + ".jpg";

                        MainActivity.wallItems.add(new CampusWallItem(id, trueID, imageUrl, date, comments, rating));
                    }
                } catch (Exception e) {

                }
                retrieveWall();
            }
        }.execute();
    }

    public void retrieveWall() {
        if(getActivity() != null) {
            adapter = new CampusWallAdapter(getActivity(), MainActivity.wallItems);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
        }
        progressDialog.dismiss();
        mSwipyRefreshLayout_grid.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CampusWallItem wallItem = adapter.getItem(position);
        String imageUrl= wallItem.imageurl;
        String trueId = wallItem.trueID;
        String date = wallItem.date;
        mCallBack.OnImageClicked(imageUrl, trueId, date);
    }
}
