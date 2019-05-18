package de.simonschuler.ba.data;

public class DriverData {
    private int RPM;
    private int speed;
    private float throttlePosition;
    private float engineLoad;

    private boolean isRPM = false;
    private boolean isSpeed = false;
    private boolean isThrottlePosition = false;
    private boolean isEngineLoad = false;

    //data holder object for obd2 data at a point in time
    public DriverData() {
        //
    }

    /**
     * determines weather this object holds al necessary data
     *
     * @return true if all data is present else false
     */
    public boolean isComplete() {
        return isRPM && isSpeed && isThrottlePosition && isEngineLoad;
    }

    public int getRPM() {
        return RPM;
    }

    public void setRPM(int RPM) {
        isRPM = true;
        this.RPM = RPM;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        isSpeed = true;
        this.speed = speed;
    }

    public float getThrottlePosition() {
        return throttlePosition;
    }

    public void setThrottlePosition(float throttlePosition) {
        isThrottlePosition = true;
        this.throttlePosition = throttlePosition;
    }

    public float getEngineLoad() {
        return engineLoad;
    }

    public void setEngineLoad(float engineLoad) {
        isEngineLoad = true;
        this.engineLoad = engineLoad;
    }
}
