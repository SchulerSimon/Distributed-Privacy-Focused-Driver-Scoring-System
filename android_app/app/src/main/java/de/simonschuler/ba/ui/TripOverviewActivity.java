package de.simonschuler.ba.ui;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import de.simonschuler.ba.R;
import de.simonschuler.ba.client.ClientImpl;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;

public class TripOverviewActivity extends AbstractToolbarController {

    private ArrayAdapter<String> tripListAdapter;
    private ListView tripList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.trips_overview);
        setupActionBar();

        displayTripsOverview();
    }

    private void displayTripsOverview() {
        //setup list and adapter
        tripList = findViewById(R.id.list_times);
        tripList.requestFocus();
        tripListAdapter = new ArrayAdapter<>(this, R.layout.default_list_item);
        tripListAdapter.add("...");
        tripList.setAdapter(tripListAdapter);


        //ask server for trips
        ClientImpl client = ServiceManager.getService(ClientImpl.class);
        client.getTimes(new GetTripsClientCallback());
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu the menu to configure
     */
    @Override
    protected void setupMenuItems(Menu menu) {
        //nothing to do here
    }

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    @Override
    protected int getCurrentViewTitleId() {
        return R.string.trips;
    }


    private class GetTripsClientCallback implements GeneralCallback {

        @Override
        public void ok(final String response) {
            System.out.println(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tripListAdapter.clear();
                    LinkedList<Double> scores = new LinkedList<>();
                    try {
                        JSONObject data = new JSONObject(response);
                        String result = data.getString("get_times");
                        result = result.substring(2, result.length() - 1);
                        for (String item : result.split("],\\[")) {
                            String[] another = item.replace("\"", "")
                                    .replace("\\[", "")
                                    .replace("]", "").split(",");
                            long time = Long.parseLong(another[0]);
                            double score = Double.parseDouble(another[1]) / 1000000;
                            scores.add(score);
                            String date = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(new Date(time));
                            tripListAdapter.add(date + "\n" + String.format("%.2f", score));
                        }
                    } catch (JSONException e) {
                        exception(e);
                    }
                    double overallScore = 0;
                    for (double d : scores) {
                        overallScore += d;
                    }
                    overallScore /= scores.size();
                    TextView v = findViewById(R.id.overall_score);
                    v.setText(String.format("%.2f", overallScore));
                }

            });
        }

        @Override
        public void fail(final String response) {
            System.out.println(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tripListAdapter.clear();
                    tripListAdapter.add(response);
                }
            });
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tripListAdapter.clear();
                    tripListAdapter.add("fail");
                }
            });
        }
    }
}
