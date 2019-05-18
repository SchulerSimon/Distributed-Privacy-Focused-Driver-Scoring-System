package de.simonschuler.ba.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import de.simonschuler.ba.R;
import de.simonschuler.ba.user.LoginData;
import de.simonschuler.ba.user.UserPreferencesConstants;

public class ConnectToServerActivity extends AbstractToolbarController {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_server);
        setupActionBar();

        //get the saved host
        SharedPreferences prefs = getSharedPreferences(UserPreferencesConstants.PREFS, 0);
        EditText host = findViewById(R.id.host);
        host.setText(prefs.getString(UserPreferencesConstants.HOST, ""));
        //select pwEditText
        if (!host.getText().toString().equals("")) {
            EditText pw = findViewById(R.id.pw);
            pw.requestFocus();
        }
    }


    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu
     */
    @Override
    protected void setupMenuItems(Menu menu) {
        menu.findItem(R.id.nav_acm).setEnabled(false);
        menu.findItem(R.id.nav_new_trip).setEnabled(false);
        menu.findItem(R.id.nav_trips).setEnabled(false);
        menu.findItem(R.id.nav_settings).setEnabled(false);
    }

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    @Override
    protected int getCurrentViewTitleId() {
        return R.string.connect_to_server_view_title;
    }

    public void connectClicked(View view) {
        //make sure the user entered something
        EditText pw = findViewById(R.id.pw);
        if (pw.getText().toString().equals("")) {
            return;
        }
        EditText host = findViewById(R.id.host);
        if (host.getText().toString().equals("")) {
            return;
        }
        //put host in shared prefs, so that the user dose not need to enter it every time
        SharedPreferences.Editor editor = getSharedPreferences(UserPreferencesConstants.PREFS, 0).edit();
        editor.clear();
        editor.putString(UserPreferencesConstants.HOST, host.getText().toString());
        editor.commit();

        //tell the OverviewActivity everything went as expected
        Intent i = new Intent();
        LoginData data = new LoginData(host.getText().toString(), pw.getText().toString());
        i.putExtra(UserPreferencesConstants.LOGIN_DATA, data);
        setResult(RESULT_OK, i);
        finish();
    }
}
