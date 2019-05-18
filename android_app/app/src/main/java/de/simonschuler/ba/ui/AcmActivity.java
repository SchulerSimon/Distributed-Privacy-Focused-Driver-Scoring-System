package de.simonschuler.ba.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import de.simonschuler.ba.R;
import de.simonschuler.ba.client.ClientImpl;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;

public class AcmActivity extends AbstractToolbarController {

    private ArrayAdapter<String> accessListAdapter;
    private ListView accessListView;
    private String selectedAddress;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.acm);
        setupActionBar();


        displayAddressesWithAccess();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void displayAddressesWithAccess() {
        accessListView = findViewById(R.id.list_access);
        accessListView.requestFocus();
        accessListAdapter = new ArrayAdapter<>(this, R.layout.default_list_item);
        accessListAdapter.add("...");
        accessListView.setAdapter(accessListAdapter);
        //background color of selected item and remember selection
        accessListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(parent.getCount());
                System.out.println(position);
                for (int i = 0; i < parent.getChildCount(); i++) {
                    System.out.println(i);
                    parent.getChildAt(i).setBackgroundColor(android.R.drawable.list_selector_background);
                }
                //parent.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.selected));
                TextView tv = (TextView) view;
                tv.setBackgroundColor(getResources().getColor(R.color.selected));

                selectedAddress = accessListAdapter.getItem(position);
            }
        });
        accessListView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                for (int i = 0; i < accessListView.getChildCount(); i++) {
                    accessListView.getChildAt(i).setBackgroundColor(android.R.drawable.list_selector_background);
                }
                selectedAddress = "";
            }
        });

        ClientImpl client = ServiceManager.getService(ClientImpl.class);
        if (client == null) {
            accessListAdapter.add(getResources().getString(R.string.not_connected));
            return;
        }
        client.whoHasAccessRight(new WhoHasAccessClientCallback());
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu the menu to configure
     */
    @Override
    protected void setupMenuItems(Menu menu) {
        //TODO
    }

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    @Override
    protected int getCurrentViewTitleId() {
        return R.string.acm;
    }

    public void removeAccessClicked(View v) {
        if (selectedAddress != null && !selectedAddress.equals("")) {
            ClientImpl client = ServiceManager.getService(ClientImpl.class);
            if (client != null) {
                client.removeAccessRight(selectedAddress, new RemoveAccessClientCallback());
            }
        }
    }

    public void grantAccessClicked(View v) {
        EditText addr = findViewById(R.id.give_access_edit_text);
        String address = addr.getText().toString();
        if (!address.isEmpty()) {
            ClientImpl client = ServiceManager.getService(ClientImpl.class);
            client.giveAccessRight(address, new GiveAccessClientCallback());
        }
    }

    private class WhoHasAccessClientCallback implements GeneralCallback {
        @Override
        public void ok(final String response) {
            System.out.println(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    accessListAdapter.clear();
                    try {
                        JSONObject data = new JSONObject(response);
                        String addresses = String.valueOf(
                                new JSONObject(
                                        String.valueOf(data.get("who_has_access_right"))
                                ).get("_who_has_access_right"));
                        addresses = addresses.substring(1, addresses.length() - 1);
                        for (String s : addresses.split(",")) {
                            accessListAdapter.add(s);
                        }
                    } catch (JSONException e) {
                        exception(e);
                    }
                }
            });
        }

        @Override
        public void fail(final String response) {
            System.out.println(response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    accessListAdapter.clear();
                    accessListAdapter.add(response);
                }
            });
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    accessListAdapter.clear();
                    accessListAdapter.add("fail");
                }
            });
        }
    }

    private class RemoveAccessClientCallback implements GeneralCallback {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void ok(String response) {
            System.out.println(response);
            displayAddressesWithAccess();
        }

        @Override
        public void fail(String response) {
            System.out.println(response);
            //nothing to do here
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            //nothing to do here
        }
    }

    private class GiveAccessClientCallback implements GeneralCallback {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void ok(String response) {
            System.out.println(response);
            displayAddressesWithAccess();
        }

        @Override
        public void fail(String response) {
            System.out.println(response);
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
        }
    }
}
