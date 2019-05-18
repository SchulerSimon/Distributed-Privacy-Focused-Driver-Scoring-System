package de.simonschuler.ba.obd2;

import android.bluetooth.BluetoothSocket;

import com.github.pires.obd.commands.ObdMultiCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;

import de.simonschuler.ba.misc.AbstractService;
import de.simonschuler.ba.misc.GeneralCallback;

/**
 * this class implements {@link Runnable} and dose all the bluetooth and OBD2 things in the background
 */
public abstract class Obd2Service extends AbstractService implements Obd2 {

    private final GeneralCallback uicb;
    private boolean keepRunning = true;
    /**
     * the open bluetooth socket
     */
    private BluetoothSocket socket;

    /**
     * initializes the Obd2Service with the uiCallback
     *
     * @param uicb
     */
    public Obd2Service(GeneralCallback uicb) {
        super("obd2_service");
        this.uicb = uicb;
    }

    /**
     * connects to a bluetooth device
     */
    public void run() {
        if (!socket.isConnected()) {
            uicb.fail("not connected");
            return;
        }

        try {
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            ObdMultiCommand c = new ObdMultiCommand();
            //c.add(new AvailablePidsCommand_01_20());
            //c.add(new AvailablePidsCommand_21_40());
            //c.add(new AvailablePidsCommand_41_60());
            //c.sendCommands(socket.getInputStream(), socket.getOutputStream());
            //String available = c.getFormattedResult();
            //uiCallback.success(available);

            while (keepRunning) {
                SpeedCommand sc = new SpeedCommand();
                sc.run(socket.getInputStream(), socket.getOutputStream());
                RPMCommand rc = new RPMCommand();
                rc.run(socket.getInputStream(), socket.getOutputStream());

                //uiCallback.displayForTest("Speed: " + sc.getMetricSpeed());
                //uiCallback.displayForTest("RPM:" + rc.getCalculatedResult());
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            uicb.exception(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                //nothing to do here
            }
            uicb.fail("lost connection");
        }

    }


    /**
     * asks the MCU for its supported PIDs
     *
     * @param callback used to return the supported PIDs, returns int[]
     */
    @Override
    public void getSupportedPIDs(GeneralCallback callback) {

    }


    public void disconnect() {
        keepRunning = false;
    }

}
