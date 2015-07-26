package app.campuschat.me;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.Settings;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import org.jivesoftware.smack.SmackConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import app.campuschat.me.CampusTalk.CampusTalkFragment;
import app.campuschat.me.CampusTalk.CampusTalkInfo;
import app.campuschat.me.CampusTalk.CampusTalkItem;
import app.campuschat.me.CampusWall.CampusWallFragment;
import app.campuschat.me.CampusWall.CampusWallInfo;
import app.campuschat.me.CampusWall.CampusWallItem;
import app.campuschat.me.MultiUserChat.MUCFragment;
import app.campuschat.me.MultiUserChat.MUCItem;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.UtilityClass;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
* CampusChat is a messaging platform that is based around communication.
* MultiUserChat (group chat) is a campus-wide chatting protocl based around XMPP.
* Posting discussions and pictures utilizes basic PHP
* The rating function is based on PHP as well. It's a rudimentary application since I did not create
* an algorithm to rank posts.
* */

public class MainActivity extends FragmentActivity implements FragmentManager.OnBackStackChangedListener, CampusWallFragment.OnImageClick,
        CampusTalkFragment.OnTalkClick {

    private final static String TAG = "MainActivity";
    public static ArrayList<MUCItem> mucChats = new ArrayList<>();

    public static EditText enterComment;
    Button sendComment;
    public static boolean isWall = false;

    public static TextView panelTitle;

    public static String trueId = "";
    public static String message = "";
    public static String name = "Anonymous";

    public static String myNickname = "";

    public static boolean tableCreated = false;

    public static boolean loadOnce = true;
    public static boolean loadOnce3 = true;

    public static ArrayList<CampusWallItem> wallItems = new ArrayList<>();
    public static ArrayList<CampusTalkItem> talkItems = new ArrayList<>();

    public static boolean viewingComments = false;

    private static MainActivity inst;
    public static MainActivity instance() {
        return inst;
    }

    Button cancelPost;
    public static SlidingUpPanelLayout mSlidingPanel;
    public static RelativeLayout slide_up_panel;
    public static ListView list_panel;
    public PanelAdapter panelAdapter;
    public static ArrayList<String> navList = new ArrayList<>();

    Response response;
    public static String phoneId;
    public static ArrayList<MyRatings> myRatings = new ArrayList<>();

    /*
    * This is a helpful method recommended by Google for improving performance.
    * */
    public void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmackConfiguration.DEBUG = true;

        enableStrictMode();

        // We initialize Google analytics here
        GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

        Tracker t = ((MainApplication) getApplication()).getTracker(MainApplication.TrackerName.APP_TRACKER);
            t.setScreenName("MainActivity");
            t.send(new HitBuilders.AppViewBuilder().build());

        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        // Phone ID is used as verification for anonymous posting. This way, we can deflect against
        // rating abuse
        phoneId = "c" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        shouldDisplayHomeUp();

        createBottomPanel();

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        getAnnouncements();

        initialize(savedInstanceState);

//        if(!myRatingsCreated()) {
//            // Do php table creation
//            createMyRatings();
//        } else {
//            // Retrieve comments from php/mysql
//            getMyRatings();
//        }

//        if(findViewById(R.id.fragment_container) != null) {
//            if(savedInstanceState != null) {
//                return;
//            }
//            MainScreenFragment mainScreenFragment = new MainScreenFragment();
//            mainScreenFragment.setArguments(getIntent().getExtras());
//            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainScreenFragment, "mainscreenfragment").commit();
//        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.change_campus:
                changeCampus();
                return true;
            case R.id.change_name:
                changeName();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void changeCampus() {
        Log.e(TAG, "Clicked campus change");
    }

    public void changeName() {
        Log.e(TAG, "Clicked name change");
    }

    /*
    * This method is called when first starting the application.
    * It retrieves which posts this user has voted on by finding the phoneID in the database.
    * If the MYSQL table does not exist, it is then created
    * */
    boolean ratingsCreated = false;
    boolean splashShown = false;
    public void initialize(final Bundle savedInstanceState) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
                ratingsCreated = sharedPrefs.getBoolean("myratingscreated", false);
                splashShown = sharedPrefs.getBoolean("splashshown", false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(ratingsCreated) {
                    // Do php table creation
                    createMyRatings();
                } else {
                    // Retrieve comments from php/mysql
                    getMyRatings();
                }

                if(findViewById(R.id.fragment_container) != null) {
                    if(savedInstanceState != null) {
                        return;
                    }
                    if(splashShown) {
                        MainScreenFragment mainScreenFragment = new MainScreenFragment();
                        mainScreenFragment.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainScreenFragment, "mainscreenfragment").commit();
                    } else {
                        SplashFragment splashFragment = new SplashFragment();
                        splashFragment.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, splashFragment, "splashfragment").commit();
                    }
                }

            }
        }.execute();
    }

    /*
    * Retrieves the current version of the application
    * */
    public int currentVersion() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionCode;
    }

    /*
    * Retrieves any announcements that the I may have issued out.
    * For example, a new version of the app is available.
    * */
    public void getAnnouncements() {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(Config.queryPrefix + "announcements.php").build();

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
                    String messages = null;
                    int version = 0;
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        JSONArray Jarray = object.getJSONArray("announcements");
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                            String id = Jasonobject.optString("id");
                            version = Jasonobject.optInt("version");
                            messages = Jasonobject.optString("message");
                        }
                        initiateAnnouncements(version, messages);
                    } catch (Exception e) { }
                }
            }
        }.execute();
    }

    public void initiateAnnouncements(int version, String messages) {
        if(Config.loggingOn) {
            Log.e(TAG, "Info: " + version + ", " + messages);
            Log.e(TAG, "Current v: " + currentVersion());
        }
        if(version > currentVersion()) {
            showDialog("This CampusChat version is outdated. Please visit the Google Play store to update.");
        }
    }

    public void showDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("CampusChat");
        dialog.setMessage(message);

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /*
    * Quick method for sending tracking information
    * */
    public void sendTracker(String screenName, String category, String message, String label) {
        Tracker t = ((MainApplication) getApplication()).getTracker(MainApplication.TrackerName.APP_TRACKER);
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(message)
                .setLabel(label)
                .build());
        t.setScreenName(null);
    }

    /*
    * Inserts the rating into the database if person has not rated the post
    * */
    public void insertMyRating(final String ratingTrueId, final boolean isWall) {
        if(Config.loggingOn) {
            Log.e(TAG, "Inserting my rating at " + phoneId + ": " + ratingTrueId);
        }
        final OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormEncodingBuilder().add("phoneid", phoneId).add("trueid", ratingTrueId).add("rating", "1").build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "insertmyrating.php").post(formBody).build();
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
                    if(Config.loggingOn) {
                        Log.e(TAG, "Rating inserted now increasing post rating");
                    }
                    increasePostRating(ratingTrueId, isWall);
                } else {
                    if(Config.loggingOn) {
                        Log.e(TAG, "Unable to insert rating");
                    }
                }
            }
        }.execute();
    }

    public void deleteMyRating(final String ratingTrueId, final boolean isWall) {
        final OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormEncodingBuilder().add("phoneid", phoneId).add("trueid", ratingTrueId).build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "deletemyrating.php").post(formBody).build();
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
                    Toast.makeText(MainActivity.this, "Rating saved", Toast.LENGTH_LONG).show();
                    decreasePostRating(ratingTrueId, isWall);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to save rating", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /*
    * Increases the post rating for the user
    * */
    public void increasePostRating(String ratingTrueId, boolean isWall) {
        final OkHttpClient client = new OkHttpClient();
        String uploadUrl = "";
        RequestBody formBody = new FormEncodingBuilder().add("trueid", ratingTrueId).build();

        if(isWall) {
            uploadUrl = Config.queryPrefix + "increasewallpost.php";
        } else {
            uploadUrl = Config.queryPrefix + "increasepost.php";
        }

        final Request request = new Request.Builder().url(uploadUrl).post(formBody).build();
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
                    if(Config.loggingOn) {
                        Log.e(TAG, "Rating increased");
                    }
                    Toast.makeText(MainActivity.this, "Rating saved", Toast.LENGTH_LONG).show();
                } else {
                    if(Config.loggingOn) {
                        Log.e(TAG, "Unable to increase rating");
                    }
                    Toast.makeText(MainActivity.this, "Unable to save rating", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /*
    * Decreases post rating for the user
    * */
    public void decreasePostRating(String ratingTrueId, final boolean isWall) {
        final OkHttpClient client = new OkHttpClient();

        String uploadUrl = "";

        RequestBody formBody = new FormEncodingBuilder().add("trueid", ratingTrueId).build();

        if(isWall) {
            uploadUrl = Config.queryPrefix + "decreasewallpost.php";
        } else {
            uploadUrl = Config.queryPrefix + "decreasepost.php";
        }

        final Request request = new Request.Builder().url(uploadUrl).post(formBody).build();
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
                    if(Config.loggingOn) {
                        Log.e(TAG, "Rating decreased");
                    }
                    Toast.makeText(MainActivity.this, "Rating saved", Toast.LENGTH_LONG).show();
                } else {
                    if(Config.loggingOn) {
                        Log.e(TAG, "Unable to decrease rating");
                    }
                    Toast.makeText(MainActivity.this, "Unable to save rating", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /*
    * Creates rating table for the user if it does not exist
    * */
    public void createMyRatings() {
        final OkHttpClient client = new OkHttpClient();
        trueId = UtilityClass.randomString(8);
        RequestBody formBody = new FormEncodingBuilder().add("phoneid", phoneId).build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "createmyratings.php").post(formBody).build();
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
                    saveTableCreation();
                    if(Config.loggingOn) {
                        Log.e(TAG, "MyRatings table created");
                    }
                }
            }
        }.execute();
    }

    /*
    * Gets the current post ratings for the user
    * */
    public void getMyRatings() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder().add("phoneid", phoneId).build();
        final Request request = new Request.Builder().url(Config.queryPrefix + "getmyratings.php").post(formBody).build();

        if(Config.loggingOn) {
            Log.e(TAG, "Getting myRatings");
        }

        myRatings.clear();

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
                        JSONArray Jarray = object.getJSONArray("myratings");
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                            String ratingTrueId = Jasonobject.optString("trueid");
                            String rating = Jasonobject.optString("rating");

                            myRatings.add(new MyRatings(ratingTrueId, rating));
                            if(Config.loggingOn) {
                                Log.e(TAG, "Got myRatings");
                            }
                        }
                    } catch (Exception e) {
                        if(Config.loggingOn) {
                            Log.e(TAG, "Unable to get table or no comments");
                        }
                    }
                } else {
                    if(Config.loggingOn) {
                        Log.e(TAG, "Connection error");
                    }
                }
            }
        }.execute();
    }

    public boolean myRatingsCreated() {
        SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
        boolean ratingsCreated = sharedPrefs.getBoolean("myratingscreated", false);
        boolean splashShown = sharedPrefs.getBoolean("splashshown", false);
        return sharedPrefs.getBoolean("myratingscreated", false);
    }

    public void saveTableCreation() {
        SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("myratingscreated", true);
        editor.apply();
    }

    /*
    * Creates the bottom slide-up panel. This is where you can view comments
    * or write messages.
    * */
    public void createBottomPanel() {
        mSlidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        slide_up_panel = (RelativeLayout) findViewById(R.id.slide_up_panel);

        panelAdapter = new PanelAdapter(this, navList);

        list_panel = (ListView) findViewById(R.id.list_panel);
        list_panel.setAdapter(panelAdapter);

        panelTitle = (TextView) findViewById(R.id.panelTitle);

        enterComment = (EditText) findViewById(R.id.enterComment);
        enterComment.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(200)});

        sendComment = (Button) findViewById(R.id.sendComment);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = enterComment.getText().toString().trim();
                if(message.length() > 0) {
                    if (viewingComments) {
                        sendTracker("MainActivity", "Sending something", "Sending a comment", "Button click");
                        if (tableCreated) {
                            if(Config.loggingOn) {
                                Log.e(TAG, "Table created");
                            }
                            sendComment(isWall);
                        } else {
                            if(Config.loggingOn) {
                                Log.e(TAG, "Table NOT created");
                            }
                            createTableAndSend();
                        }
                        if(Config.loggingOn) {
                            Log.e(TAG, "Viewing comments");
                        }
                    } else {
                        sendTracker("MainActivity", "Sending something", "Sending a campustalk", "Button click");
                        sendCampusTalk();
                        if(Config.loggingOn) {
                            Log.e(TAG, "Not viewing comments");
                        }
                    }
                }
                UtilityClass.hideKeyboard(MainActivity.this);
            }
        });

        cancelPost = (Button) findViewById(R.id.cancelPost);
        cancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                UtilityClass.hideKeyboard(MainActivity.this);
            }
        });
    }

    public void sendTalkInfoComment() {
        if (viewingComments) {
            sendTracker("MainActivity", "Sending something", "Sending a comment", "Button click");
            if (tableCreated) {
                sendComment(isWall);
            } else {
                createTableAndSend();
            }
        } else {
            sendTracker("MainActivity", "Sending something", "Sending a campustalk", "Button click");
            sendCampusTalk();
        }
    }

    /*
    * Adds a discussion to the Campus Talk section
    * */
    public void sendCampusTalk() {
        final OkHttpClient client = new OkHttpClient();
        trueId = UtilityClass.randomString(8);
        String nowDate = UtilityClass.getCurrentDateAndTime();
        RequestBody formBody = new FormEncodingBuilder()
                .add("trueid", trueId)
                .add("message", message)
                .add("date", nowDate)
                .build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "addcampustalk.php").post(formBody).build();
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
                    Toast.makeText(MainActivity.this, "Post submitted", Toast.LENGTH_LONG).show();
                    enterComment.setText("");
                    talkItems.add(0, new CampusTalkItem("" + talkItems.size() + 1, trueId, message, UtilityClass.getCurrentDateAndTime(), 0, "0"));
                    CampusTalkFragment.instance().retrieveChats();
                    mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to submit post", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /*
    * Writes a comment to a posting
    * */
    public void sendComment(final boolean isWall) {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder().add("trueid", trueId).add("name", name).add("message", message).build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "addcomment.php").post(formBody).build();
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
                    Toast.makeText(MainActivity.this, "Comment sent", Toast.LENGTH_LONG).show();
                    increaseCommentCount(isWall);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to send comment", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /*
    * Increases the count of comments for a particular post
    * */
    public void increaseCommentCount(final boolean isWall) {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder().add("trueid", trueId).build();

        String uploadUrl = "";

        if(isWall) {
            uploadUrl = Config.queryPrefix + "increasetalkcomment.php";
        } else {
            uploadUrl = Config.queryPrefix + "increasewallcomment.php";
        }

        final Request request = new Request.Builder().url(uploadUrl).post(formBody).build();
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
                    enterComment.setText("");
                    if(isWall) {
                        CampusTalkInfo.talkInfoCommentList.add(0, message);
                        CampusTalkInfo.adapter.notifyDataSetChanged();
                    } else {
                        navList.add(0, message);
                        panelAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Unable to increase talk comment", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void createTableAndSend() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder().add("trueid", trueId).build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "createtable.php").post(formBody).build();
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
                    sendComment(isWall);
                }
            }
        }.execute();
    }

    @Override
    public void OnImageClicked(String imageUrl, String trueId, String date) {
        CampusWallInfo campusWallInfo = new CampusWallInfo();
        Bundle args = new Bundle();
        args.putString("imageurl", imageUrl);
        args.putString("trueid", trueId);
        args.putString("date", date);
        campusWallInfo.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, campusWallInfo, "campuswallinfo");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void OnTalkClicked(String talkTrueId, String talkMessage) {
        CampusTalkInfo campusTalkInfo = new CampusTalkInfo();
        Bundle args = new Bundle();
        args.putString("talktrueid", talkTrueId);
        args.putString("talkmessage", talkMessage);
        campusTalkInfo.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, campusTalkInfo, "campustalkinfo");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackStackChanged() {
        mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(canback);
        }
    }

    @Override
    public boolean onNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainScreenFragment.connectionManager.disconnect();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}