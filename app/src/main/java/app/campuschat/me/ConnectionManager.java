package app.campuschat.me;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import app.campuschat.me.Interfaces.OnLoggedIn;
import app.campuschat.me.Utility.Config;

/*
* This is the main class that handles connections for XMPP (Smack)
* */

public class ConnectionManager {

    protected static final String TAG = "ConnectionManagerTAG";

    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration connectionConfiguration;

    private OnLoggedIn mLoggedIn;
    public void onLoggedIn(OnLoggedIn mCallBack) {
        this.mLoggedIn = mCallBack;
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    boolean startConnected = false;
    public void startLogin(final String username) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                        .setServiceName(Config.SERVICE_NAME)
                        .setHost(Config.HOST_NAME)
                        .setPort(5222)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .build();

                connection = new XMPPTCPConnection(connectionConfiguration);

                try {
                    connection.connect();
                    startConnected = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.e(TAG, "Connected");
                if(startConnected) {
                    connectionLogin();
                } else {
                    MainActivity.instance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.instance(), "Unable to connect", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.execute();
    }

    boolean loggedIn = true;
    public void connectionLogin() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    connection.login();
                } catch (Exception e) {
                    loggedIn = false;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(!loggedIn) {
                    Toast.makeText(MainActivity.instance(), "Unable to join", Toast.LENGTH_LONG).show();
                    disconnect();
                    loggedIn = true;
                } else {
                    MainActivity.instance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.instance(), "Joined", Toast.LENGTH_LONG).show();
                        }
                    });
                    mLoggedIn.onLoggedIn(true);
                }
            }
        }.execute();
    }
    public void disconnect() {
        if(connection != null && connection.isConnected()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    connection.disconnect();
                    return null;
                }
            }.execute();
        }
    }
}
