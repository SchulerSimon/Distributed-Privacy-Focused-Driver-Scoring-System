package de.simonschuler.ba.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import de.simonschuler.ba.R;
import de.simonschuler.ba.client.ClientImpl;
import de.simonschuler.ba.data.DataCollectorImpl;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;

public class DrivingActivity extends AbstractToolbarController {
    private TextView scoreView;
    private Timer timer;
    private boolean driving;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driving);
        setupActionBar();

        scoreView = findViewById(R.id.score_text_view);
        timer = new Timer();

        startTrip();
    }

    private void updateScore() {
        if (driving) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateScore();
                }
            }, 500);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DataCollectorImpl service = ServiceManager.getService(DataCollectorImpl.class);
                    scoreView.setText(String.format("%.2f", service.getOverallScore()));
                }
            });
        }
    }

    private void startTrip() {
        driving = true;
        //start all services needed for driving
        DataCollectorImpl dataCollector = new DataCollectorImpl(new DataCollectorUiCallback());
        ServiceManager.registerService(dataCollector);
        //start
        dataCollector.startTrip();

        //start the timer loop to refresh the view
        updateScore();
    }

    public void stopTrip(View v) {
        //stop the Loop
        driving = false;
        timer.purge();
        timer.cancel();

        //tell the datacollector
        DataCollectorImpl service = ServiceManager.getService(DataCollectorImpl.class);
        service.stopTrip();

        //get the final trip score
        double score = service.getOverallScore();
        scoreView.setText(String.format("%.2f", score));

        //push score to BC
        ClientImpl client = ServiceManager.getService(ClientImpl.class);
        client.addScore(System.currentTimeMillis(), score, new ClientUiCallback());

        //let the user navigate freely now
        enableMenuItems();
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu
     */
    @Override
    protected void setupMenuItems(Menu menu) {
        this.menu = menu;
        menu.findItem(R.id.nav_acm).setEnabled(false);
        menu.findItem(R.id.nav_new_trip).setEnabled(false);
        menu.findItem(R.id.nav_trips).setEnabled(false);
        menu.findItem(R.id.nav_settings).setEnabled(false);
        menu.findItem(R.id.nav_exit).setEnabled(false);
    }

    private void enableMenuItems() {
        menu.findItem(R.id.nav_acm).setEnabled(true);
        menu.findItem(R.id.nav_new_trip).setEnabled(true);
        menu.findItem(R.id.nav_trips).setEnabled(true);
        menu.findItem(R.id.nav_settings).setEnabled(true);
        menu.findItem(R.id.nav_exit).setEnabled(true);
    }

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    @Override
    protected int getCurrentViewTitleId() {
        return R.string.driving;
    }


    @Override
    public void finish() {
        if (driving) {
            //we dont want the ui to end when we are driving
            return;
        } else {
            super.finish();
        }
    }

    private class DataCollectorUiCallback implements GeneralCallback {

        @Override
        public void ok(String response) {
            //this class is only here to make sure the user knows about errors
        }

        @Override
        public void fail(final String response) {
            System.out.println(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView v = findViewById(R.id.driving_error_text_view);
                    v.setText(response);
                    v.setTextColor(Color.RED);
                }
            });
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
        }
    }

    private class ClientUiCallback implements GeneralCallback {

        @Override
        public void ok(String response) {
            System.out.println(response);
        }

        @Override
        public void fail(final String response) {
            System.out.println(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView v = findViewById(R.id.driving_error_text_view);
                    v.setText(response);
                    v.setTextColor(Color.RED);
                }
            });
        }

        @Override
        public void exception(Exception e) {

        }
    }
}
