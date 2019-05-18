package de.simonschuler.ba.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;

import de.simonschuler.ba.R;

public class ConnectToBluetoothActivity extends AbstractToolbarController {
    public static final String BLUETOOTH_DEVICE = "bluetooth_device";

    /**
     * bluetooth
     */
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ListView pairedListView;
    private String selectedDevice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.connect_bluetooth);
        setupActionBar();
        //set the connect button disabled as long as no item was selected
        setConnectButtonEnabled(true);
        displayPairedDevices();
    }

    private void setConnectButtonEnabled(boolean enabled) {
        Button button = findViewById(R.id.connection_bluetooth_connect_button);
        button.setEnabled(enabled);
    }

    /**
     * displays a list of bluetooth devices
     * the getResources().getColor is deprecated, dose not matter for this prototype
     *
     * @see <a href="here">https://stackoverflow.com/questions/31842983/getresources-getcolor-is-deprecated</a> for a solution
     */
    @SuppressWarnings("deprecation")
    private void displayPairedDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        while (!bluetoothAdapter.isEnabled()) {
            //TODO display spinner
        }
        // Initialize ArrayAdapter for paired devices
        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.default_list_item);
        pairedListView = findViewById(R.id.list_paired_bluetooth_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    parent.getChildAt(i).setBackgroundColor(android.R.drawable.list_selector_background);
                }
                parent.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.selected));

                //remember which item was selected
                selectedDevice = pairedDevicesArrayAdapter.getItem(position);
                //set the connect button enabled, because an Item is selected
                setConnectButtonEnabled(true);
            }
        });
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // Add paired devices to ArrayAdapter
                // ArrayAdapter will be connected to LisView
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            //TODO maybe find a better solution? another day...
            String noDevices = getResources().getText(R.string.title_none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu the menu to configure
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
        return R.string.connect_to_bluetooth_view_title;
    }

    /**
     * send the selected device back to the main Activity and destroy this activity
     *
     * @param view
     */
    public void bluetoothConnectClicked(View view) {
        if (selectedDevice == "") {
            //we should never be here!!
            return;
        }
        Intent i = new Intent();
        System.out.println(selectedDevice);
        i.putExtra(BLUETOOTH_DEVICE, selectedDevice.substring(selectedDevice.indexOf("\n") + 1));
        setResult(RESULT_OK, i);
        finish();
    }
}
