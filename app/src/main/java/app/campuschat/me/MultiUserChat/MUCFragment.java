package app.campuschat.me.MultiUserChat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import app.campuschat.me.MainActivity;
import app.campuschat.me.MainScreenFragment;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.UtilityClass;

public class MUCFragment extends ListFragment {

    private EditText mucMessageText;
    private Button mucSendButton;
    private String roomName = "";
    private String conferenceName = "CHAT_UNIVERSITY_OF_TEXAS" + Config.MUC_HOST_NAME;

    private String choseName = "Anonymous";

    String myNickname = "";

    public static MUCAdapter mucAdapter;

    private MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(MainScreenFragment.connectionManager.getConnection());

    protected static final String TAG = "CreatedMUC";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.muc_fragment_layout, container, false);

        mucMessageText = (EditText) view.findViewById(R.id.mucMessageText);
        mucMessageText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(150) });

        mucSendButton = (Button) view.findViewById(R.id.mucSendButton);
        mucSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mucMessageText.getText().length() > 0) {
                    sendMessage(mucMessageText.getText().toString());
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null) {
            mucAdapter = new MUCAdapter(getActivity(), MainActivity.mucChats);
            getListView().setAdapter(mucAdapter);
            joinRoom(conferenceName);
        }
    }

    private static MUCFragment inst;
    public static MUCFragment instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void updateMessage(String userJID, String userNickame, String messageBody) {
        MainActivity.mucChats.add(new MUCItem(userJID, userNickame, messageBody));
        mucAdapter.notifyDataSetChanged();
        getListView().setSelection(MainActivity.mucChats.size());
    }

    public void notifyMessages() {
        mucAdapter.notifyDataSetChanged();
        getListView().setSelection(MainActivity.mucChats.size());
    }

    boolean messageSent = false;
    public void sendMessage(final String message) {
        final MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(conferenceName);
        messageSent = false;

        final Message messageToSend = new Message();
        messageToSend.setBody(message);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    multiUserChat.sendMessage(messageToSend);
                    messageSent = true;
                } catch (SmackException.NotConnectedException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(messageSent) {
                    Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_LONG).show();
                    mucMessageText.setText("");
                } else {
                    Toast.makeText(getActivity(), "Unable to send message", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public static MUCMessageListener mucMessageListener;
    MultiUserChat multiUserChat;
    public void joinRoom(String fullRoomName) {
        multiUserChat = multiUserChatManager.getMultiUserChat(fullRoomName);

        if(multiUserChat != null) {
            try {
                multiUserChat.leave();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }

        multiUserChat = multiUserChatManager.getMultiUserChat(fullRoomName);

        if(mucMessageListener != null) {
            multiUserChat.removeMessageListener(mucMessageListener);
        }

        mucMessageListener = new MUCMessageListener();
        multiUserChat.addMessageListener(mucMessageListener);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    multiUserChat.join(MainActivity.myNickname);
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mucAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    public void disconnectMUC() {
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(conferenceName);
        // Message Listener removal can be done in MUCList onStart or here.
        // I decided to leave this here
        multiUserChat.removeMessageListener(mucMessageListener);
        try {
            multiUserChat.leave();
            MainActivity.mucChats.clear();
            if(Config.loggingOn) {
                Log.e(TAG, "Left room: " + conferenceName);
            }
        } catch (SmackException.NotConnectedException e) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnectMUC();
    }
}
