package de.simonschuler.ba.obd2;

import de.simonschuler.ba.misc.GeneralCallback;

/**
 * interface for communicating with the MCU via OBD2
 */
public interface Obd2 {
    void getSupportedPIDs(GeneralCallback cb);

    void getRPM(GeneralCallback cb);

    void getSpeed(GeneralCallback cb);

    void getThrottlePosition(GeneralCallback cb);

    void getEngineLoad(GeneralCallback cb);
}
