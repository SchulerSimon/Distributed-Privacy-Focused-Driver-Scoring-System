package de.simonschuler.ba.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.simonschuler.ba.R;

/**
 * every view with a Navigation bar extends this class
 * it handles all the callback and navigation functionality
 */
public abstract class AbstractToolbarController extends AppCompatActivity {
    public static final String call_method = "call_method";
    public static final int default_value = -1;
    public static final int acm_ui = 1;
    public static final int new_trip_ui = 2;
    public static final int trips_ui = 3;
    public static final int settings_ui = 4;
    public static final int exit_app = 5;
    public static final int connect_server_ui = 6;
    public static final int connect_bluetooth_ui = 7;
    public static final int driving_ui = 8;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    /**
     * sets up the Action bar for all the different Views
     * also registers a new class as navigationItemSelectedListener
     */
    protected void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.new_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = this.getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_acm:
                        startAcmUi();
                        break;
                    case R.id.nav_new_trip:
                        startNewTripUi();
                        break;
                    case R.id.nav_trips:
                        startTripsUi();
                        break;
                    case R.id.nav_settings:
                        startSettingsUi();
                        break;
                    case R.id.nav_exit:
                        exitApp();
                        break;
                    default:
                        break;
                }
                // close drawer when item is tapped
                drawerLayout.closeDrawers();
                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                return true;
            }
        });

        //let every activity decide which menu items are active
        setupMenuItems(navigationView.getMenu());
        //let every activity choose its own title
        actionbar.setTitle("DriveChain - " + getResources().getString(getCurrentViewTitleId()));
    }

    /**
     * determines weather the calling activity is OverviewActivity or not, based on that it
     * either:  starts the UI for the selected Option
     * or:      ends itself and tells OverviewActivity to call_method the selected Option
     */
    protected void startAcmUi() {
        if (this instanceof OverviewActivity) {
            //call_method new Activity from here
            Intent i = new Intent(this, AcmActivity.class);
            startActivityForResult(i, acm_ui);
        } else {
            //end current Activity and tell OverviewActivity to call_method new Activity
            Intent i = new Intent();
            i.putExtra(call_method, acm_ui);
            //the UI didn't complete its task (it would have ended itself by now) so the status is canceled
            setResult(RESULT_CANCELED, i);
            finish();
        }
    }

    /**
     * determines weather the calling activity is OverviewActivity or not, based on that it
     * either:  starts the UI for the selected Option
     * or:      ends itself and tells OverviewActivity to call_method the selected Option
     */
    protected void startNewTripUi() {
        if (this instanceof OverviewActivity) {
            this.recreate();
        } else {
            Intent i = new Intent();
            setResult(RESULT_CANCELED, i);
            finish();
        }
    }

    /**
     * determines weather the calling activity is OverviewActivity or not, based on that it
     * either:  starts the UI for the selected Option
     * or:      ends itself and tells OverviewActivity to call_method the selected Option
     */
    protected void startSettingsUi() {
        if (this instanceof OverviewActivity) {
            //call_method new Activity from here
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, settings_ui);
        } else {
            //end current Activity and tell OverviewActivity to call_method new Activity
            Intent i = new Intent();
            i.putExtra(call_method, settings_ui);
            //the UI didn't complete its task (it would have ended itself by now) so the status is canceled
            setResult(RESULT_CANCELED, i);
            finish();
        }
    }

    /**
     * determines weather the calling activity is OverviewActivity or not, based on that it
     * either:  starts the UI for the selected Option
     * or:      ends itself and tells OverviewActivity to call_method the selected Option
     */
    protected void startTripsUi() {
        if (this instanceof OverviewActivity) {
            //call_method new Activity from here
            Intent i = new Intent(this, TripOverviewActivity.class);
            startActivityForResult(i, trips_ui);
        } else {
            //end current Activity and tell OverviewActivity to call_method new Activity
            Intent i = new Intent();
            i.putExtra(call_method, trips_ui);
            //the UI didn't complete its task (it would have ended itself by now) so the status is canceled
            setResult(RESULT_CANCELED, i);
            finish();
        }
    }

    /**
     * hook method for exiting the app
     */
    protected void exitApp() {
        if (this instanceof OverviewActivity) {
            finish();
        } else {
            Intent i = new Intent();
            i.putExtra(call_method, exit_app);
            setResult(RESULT_CANCELED, i);
            finish();
        }
    }

    /**
     * checks if the returned result from and activity was to start another activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            switch (data.getIntExtra(call_method, default_value)) {
                case acm_ui:
                    startAcmUi();
                    break;
                case new_trip_ui:
                    startNewTripUi();
                    break;
                case trips_ui:
                    startTripsUi();
                    break;
                case settings_ui:
                    startSettingsUi();
                    break;
                case exit_app:
                    exitApp();
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * click listener for menu button on the top left of the screen. Just opens the nav drawer
     *
     * @param item the menu button
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int size = navigationView.getMenu().size();
        //make sure that no item is preselected
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
        //open the drawer
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu the menu to configure
     */
    protected abstract void setupMenuItems(Menu menu);

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    protected abstract int getCurrentViewTitleId();
}
