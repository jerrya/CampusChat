package app.campuschat.me;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;

import java.util.ArrayList;

import app.campuschat.me.Interfaces.OnLoggedIn;
import app.campuschat.me.MainContainer.MainTabContainer;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.UtilityClass;
import mehdi.sakout.fancybuttons.FancyButton;

public class MainScreenFragment extends Fragment implements OnLoggedIn{

    FancyButton joinButton;
    EditText enterNickname;

    public static ConnectionManager connectionManager = new ConnectionManager();

    protected static final String TAG = "MainScreenFragment";

    private String blockCharacterSet = "@)(~#^|$%&*!/";
    private InputFilter inputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_screen_layout, container, false);

        joinButton = (FancyButton) view.findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterNickname.getText().toString().trim().length() > 0) {
                    MainActivity.myNickname = enterNickname.getText().toString().trim() + "@" + UtilityClass.randomString(3);
                    connectionManager.startLogin(MainActivity.phoneId);
                    MainActivity.instance().sendTracker("MainScreenFragment", "Enter button", "Enter button clicked WITHIN length", "Button click");
                } else {
                    MainActivity.instance().sendTracker("MainScreenFragment", "Enter button", "Enter button clicked outside length", "Button click");
                }
            }
        });

        enterNickname = (EditText) view.findViewById(R.id.enterNickname);
        enterNickname.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(15)});

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connectionManager.onLoggedIn(this);

        // This is intended to show an instructional splash screen on startup
//        if(!splashNotShown()) {
//            SplashFragment splashFragment = new SplashFragment();
//            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, splashFragment, "splashfragment");
//            transaction.commit();
//        }
    }

    public boolean splashNotShown() {
        SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean("splashshown", false);
    }

    /*
    * This is mostly used for admin purposes. A new room has to be created.
    * */
    public void createRoom(final String campusName) {
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connectionManager.getConnection());
        final MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(campusName);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    multiUserChat.createOrJoin(connectionManager.getConnection().getUser());
                    Log.e(TAG, "Room created");
                } catch (Exception e) {
                    Log.e(TAG, "Unable to create " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getConfigForm(campusName, multiUserChat);
            }
        }.execute();
    }

    /*
    * Form must be retrieved, set and sent to configure the room
    * */
    Form form;
    public void getConfigForm(final String fullRoomName, final MultiUserChat multiUserChat) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    form = multiUserChat.getConfigurationForm();
                    Log.e(TAG, "Got config form");
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                sendForm(fullRoomName, form, multiUserChat);
            }
        }.execute();
    }

    public void sendForm(final String campusName, final Form form, final MultiUserChat multiUserChat) {
        final Form submitForm = form.createAnswerForm();

        submitForm.setAnswer("muc#roomconfig_roomname", "CHAT");
        submitForm.setAnswer("muc#roomconfig_roomdesc", "UTD chat");
        submitForm.setAnswer("muc#roomconfig_persistentroom", true);
        submitForm.setAnswer("muc#roomconfig_publicroom", true);
        submitForm.setAnswer("muc#roomconfig_changesubject", true);
        ArrayList<String> whois = new ArrayList<>();
        whois.add("anyone");
        submitForm.setAnswer("muc#roomconfig_whois", whois);
        submitForm.setAnswer("muc#roomconfig_moderatedroom", false);
        submitForm.setAnswer("muc#roomconfig_membersonly", false);
        submitForm.setAnswer("muc#roomconfig_historylength", "20");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    multiUserChat.sendConfigurationForm(submitForm);
                    Log.e(TAG, "Config sent");
                } catch (Exception e) {
                    Log.e(TAG, "Config not sent: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    /*
    * Interface for a successful login
    * */
    @Override
    public void onLoggedIn(boolean successful) {
        if(successful) {
            if(Config.loggingOn) {
                Log.e(TAG, "Logged in");
            }

            MainTabContainer mainTabContainer = new MainTabContainer();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, mainTabContainer);
            transaction.addToBackStack(null);
            transaction.commit();

        } else {
            Toast.makeText(getActivity(), "Unable to enter", Toast.LENGTH_LONG).show();
        }
    }
}
