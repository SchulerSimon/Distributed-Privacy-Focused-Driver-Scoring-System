package de.simonschuler.ba.obd2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

import de.simonschuler.ba.ui.OverviewActivity;

/**
 * this class represents a service for establishing a bluetooth connection
 * it uses a separate thread to connect and then hands the socket and device to the {@link Obd2Service}
 */
public class BluetoothStartup {
    /**
     * this UUID (00001101-0000-1000-8000-00805F9B34FB) is the representative BASE_UUID defined in the Bluetooth ISO standard
     *
     * @see <a href="here">https://www.bluetooth.com/specifications/assigned-numbers/service-discovery</a>
     */
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    /**
     * thread for establishing a connection
     */
    private ConnectThread connectThread;
    /**
     * standard android bluetooth adapter
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * callback from {@link OverviewActivity} to display/handle errors
     */
    private Obd2Impl.BluetoothStartupCallback scb;

    /**
     * sets up and establishes a connection to the specified device and hands the connection on to the {@link Obd2Service}
     *
     * @param device          bluetooth device to connect to
     * @param startupCallback callback from the ui
     */
    public synchronized void connect(String device,
                                     Obd2Impl.BluetoothStartupCallback startupCallback) {
        this.scb = startupCallback;
        // Start ConnectThread to connect to given device
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    /**
     * this class connects to the socket
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectThread(String device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device);
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                scb.exception(e);
            }
            bluetoothSocket = tmp;
        }

        /**
         * dose the work
         */
        public void run() {
            // because bluetooth-adapter-discovery is heavy weight procedure, cancel any on
            // going discovery before attempting to connect
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            // make a connection to the bluetooth socket
            try {
                // connect the device through the socket. this is blocking call so it
                // will return on a successful connection or an exception
                bluetoothSocket.connect();
            } catch (IOException e) {
                scb.exception(e);
                try {
                    // unable to connect, close the socket and get out
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    //we already know that it failed, nothing to do here
                }
            }
            // reset the connect thread because we're done
            synchronized (BluetoothStartup.this) {
                connectThread = null;
            }
            // do work to manage the connection (in a separate thread)
            if (bluetoothSocket.isConnected()) {
                scb.ok("connected");
                scb.setSocket(bluetoothSocket);
            }
        }

        /**
         * this method will cancel an in-progress connection and close the socket
         */
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                scb.fail("lost connection");
            }
        }
    }
}