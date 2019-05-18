package de.simonschuler.ba.obd2;

import android.bluetooth.BluetoothSocket;

import com.github.pires.obd.commands.ObdMultiCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_01_20;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_41_60;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;

import java.io.IOException;

import de.simonschuler.ba.misc.AbstractService;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;

public class Obd2Impl extends AbstractService implements Obd2 {
    private final GeneralCallback uicb;
    protected BluetoothStartup bluetooth;
    protected BluetoothSocket socket;

    public Obd2Impl(String deviceAddress, GeneralCallback uicb) {
        super("obd2_service");
        this.uicb = uicb;
        bluetooth = new BluetoothStartup();
        bluetooth.connect(deviceAddress, new BluetoothStartupCallback());
    }

    @Override
    public void getSupportedPIDs(GeneralCallback cb) {
        ObdMultiCommand c = new ObdMultiCommand();
        c.add(new AvailablePidsCommand_01_20());
        c.add(new AvailablePidsCommand_21_40());
        c.add(new AvailablePidsCommand_41_60());
        try {
            c.sendCommands(socket.getInputStream(), socket.getOutputStream());
        } catch (InterruptedException e) {
            uicb.fail("bluetooth connection lost");
        } catch (Exception e) {
            uicb.exception(e);
        }

        try {
            String available = c.getFormattedResult();
            cb.ok(available);
        } catch (Exception e) {
            uicb.exception(e);
        }
    }

    @Override
    public void getRPM(GeneralCallback cb) {
        try {
            RPMCommand command = new RPMCommand();
            command.run(socket.getInputStream(), socket.getOutputStream());
            cb.ok(command.getCalculatedResult());
        } catch (NoDataException e) {
            cb.fail("RPM not supported by ECU");
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void getSpeed(GeneralCallback cb) {
        try {
            SpeedCommand command = new SpeedCommand();
            command.run(socket.getInputStream(), socket.getOutputStream());
            cb.ok(command.getCalculatedResult());
        } catch (NoDataException e) {
            cb.fail("speed not supported by ECU");
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void getThrottlePosition(GeneralCallback cb) {
        try {
            ThrottlePositionCommand command = new ThrottlePositionCommand();
            command.run(socket.getInputStream(), socket.getOutputStream());
            cb.ok(command.getCalculatedResult());
        } catch (NoDataException e) {
            cb.fail("throttle position not supported by ECU");
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void getEngineLoad(GeneralCallback cb) {
        try {
            LoadCommand command = new LoadCommand();
            command.run(socket.getInputStream(), socket.getOutputStream());
            cb.ok(command.getCalculatedResult());
        } catch (NoDataException e) {
            cb.fail("engine load not supported by ECU");
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    public void disconnect() {
        try {
            this.socket.close();
        } catch (IOException e) {
            //ignore fail
        }
        ServiceManager.removeService(Obd2Impl.class);
    }

    private void setupObd2(GeneralCallback cb) {
        try {
            //PROTOCOL = auto, ECHO = off
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
            //new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    class BluetoothStartupCallback implements GeneralCallback {
        public void setSocket(BluetoothSocket btSocket) {
            socket = btSocket;

            //we can pass this to the setup, because setup only uses this.exception()
            //so we don't get into an infinite stack loop
            setupObd2(this);
        }

        @Override
        public void ok(String response) {
            uicb.ok(response);
        }

        @Override
        public void fail(String response) {
            uicb.fail(response);
        }

        @Override
        public void exception(Exception e) {
            uicb.exception(e);
        }
    }
}
