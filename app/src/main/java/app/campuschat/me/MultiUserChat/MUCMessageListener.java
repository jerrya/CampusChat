package app.campuschat.me.MultiUserChat;

import android.util.Log;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import app.campuschat.me.MainActivity;
import app.campuschat.me.MainContainer.MainTabContainer;
import app.campuschat.me.Utility.Config;

public class MUCMessageListener implements MessageListener {

    protected static final String TAG = "MUCMessageListener";

    @Override
    public void processMessage(Message message) {
        final String messageBody = message.getBody();
        final String userNickame = message.getFrom().replace("chat_university_of_texas" + Config.MUC_HOST_NAME + "/", "");
        final String newNickame = userNickame.split("@")[0];

        if(Config.loggingOn) {
            Log.e(TAG, "Msg from " + newNickame + ": " + message.getBody() + " subj: " + message.getSubject());
        }

        MainActivity.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MUCFragment.instance().updateMessage(userNickame, newNickame, messageBody);
            }
        });
    }
}
