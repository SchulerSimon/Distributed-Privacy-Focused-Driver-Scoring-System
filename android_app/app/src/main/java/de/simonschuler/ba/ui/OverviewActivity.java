package de.simonschuler.ba.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.simonschuler.ba.R;
import de.simonschuler.ba.client.ClientImpl;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;
import de.simonschuler.ba.obd2.Obd2Impl;
import de.simonschuler.ba.user.LoginData;
import de.simonschuler.ba.user.UserPreferencesConstants;

import static de.simonschuler.ba.R.drawable.text_view_border_green;
import static de.simonschuler.ba.R.drawable.text_view_border_red;

public class OverviewActivity extends AbstractToolbarController {

    //is server and bluetooth connected?
    private boolean serverConnected;
    private boolean btConnected;
    private Menu menu;
    private Button startTripButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.overview);
        setupActionBar();

        //if the connections have been established just let the user know on startup
        if (ServiceManager.getService(ClientImpl.class) != null) {
            new ClientCallback().ok("connected");
        }
        if (ServiceManager.getService(Obd2Impl.class) != null) {
            new Obd2Callback().ok("connected");
        }

        this.startTripButton = findViewById(R.id.start_trip_button);
        startTripButton.setEnabled(false);
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu
     */
    @Override
    protected void setupMenuItems(Menu menu) {
        this.menu = menu;
        menu.findItem(R.id.nav_new_trip).setEnabled(false);
        menu.findItem(R.id.nav_acm).setEnabled(false);
        menu.findItem(R.id.nav_trips).setEnabled(false);
    }

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    @Override
    protected int getCurrentViewTitleId() {
        return R.string.overview;
    }


    public void startTripClicked(View v) {
        if (serverConnected && btConnected) {
            //start driving_ui
            Intent i = new Intent(this, DrivingActivity.class);
            startActivityForResult(i, driving_ui);
        }
    }

    public void connectServerClicked(View v) {
        Intent i = new Intent(this, ConnectToServerActivity.class);
        startActivityForResult(i, connect_server_ui);
        TextView cv = findViewById(R.id.connection_server_text_view);
        cv.setText("...");
    }

    public void connectBluetoothClicked(View v) {
        Intent i = new Intent(this, ConnectToBluetoothActivity.class);
        startActivityForResult(i, connect_bluetooth_ui);
        TextView cv = findViewById(R.id.connection_bluetooth_text_view);
        cv.setText("...");
    }

    /**
     * descides what is to do when an activity returns a result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //when a ui finishes we land here, then we decide what to do.
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case connect_server_ui:
                    //login screen terminated successfully, lets start the client-service
                    ClientImpl client = new ClientImpl((LoginData) data.getSerializableExtra(
                            UserPreferencesConstants.LOGIN_DATA), new ClientCallback());
                    //register service with serviceManager
                    ServiceManager.registerService(client);
                    serverConnected = true;
                    break;
                case connect_bluetooth_ui:
                    //the connect_bluetooth_ui terminated successfully, lets start the bluetooth-service
                    Obd2Impl obd2 = new Obd2Impl(data.getStringExtra(
                            ConnectToBluetoothActivity.BLUETOOTH_DEVICE), new Obd2Callback());
                    //register service with serviceManager
                    ServiceManager.registerService(obd2);
                    btConnected = true;
                    break;
                case settings_ui:
                    //TODO restart all services with respective settings
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        try {
            Obd2Impl obd2 = ServiceManager.getService(Obd2Impl.class);
            obd2.disconnect();
            ClientImpl client = ServiceManager.getService(ClientImpl.class);
            client.disconnect();
        } catch (Exception e) {
            //nothing to do if it fails
        }

        super.finish();
    }

    private void enableStartTripButton() {
        if (serverConnected && btConnected) {
            startTripButton.setEnabled(true);
        }
    }

    /**
     * client callback, just used for logging in with the client and in case of a connection drop
     */
    private class ClientCallback implements GeneralCallback {
        @Override
        public void ok(final String response) {
            serverConnected = true;
            System.out.println("OK" + response);
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    TextView textView = findViewById(R.id.connection_server_text_view);
                    textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), text_view_border_green));
                    textView.setText(response);

                    // let the user use all the functionality that needs server connection
                    menu.findItem(R.id.nav_acm).setEnabled(true);
                    menu.findItem(R.id.nav_trips).setEnabled(true);
                    enableStartTripButton();
                }
            });
        }

        @Override
        public void fail(final String response) {
            serverConnected = false;
            System.out.println("FAIL" + response);
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    TextView textView = findViewById(R.id.connection_server_text_view);
                    textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), text_view_border_red));
                    textView.setText(response);
                }
            });
        }

        @Override
        public void exception(Exception e) {
            serverConnected = false;
            System.out.println("EXCEPTION" + e);
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    TextView textView = findViewById(R.id.connection_server_text_view);
                    textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), text_view_border_red));
                    textView.setText("fail");
                }
            });
        }
    }

    /**
     * obd2 callback, just used for establishing connection and getting errors displayed
     */
    private class Obd2Callback implements GeneralCallback {

        @Override
        public void ok(final String response) {
            btConnected = true;
            System.out.println("OK" + response);
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    TextView textView = findViewById(R.id.connection_bluetooth_text_view);
                    textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), text_view_border_green));
                    textView.setText(response);

                    enableStartTripButton();
                }
            });
        }

        @Override
        public void fail(final String response) {
            btConnected = false;
            System.out.println("FAIL" + response);
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    TextView textView = findViewById(R.id.connection_bluetooth_text_view);
                    textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), text_view_border_red));
                    textView.setText(response);
                }
            });
        }

        @Override
        public void exception(Exception e) {
            btConnected = false;
            System.out.println("EXCEPTION" + e);
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    TextView textView = findViewById(R.id.connection_bluetooth_text_view);
                    textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), text_view_border_red));
                    textView.setText("fail");
                }
            });
        }
    }
}