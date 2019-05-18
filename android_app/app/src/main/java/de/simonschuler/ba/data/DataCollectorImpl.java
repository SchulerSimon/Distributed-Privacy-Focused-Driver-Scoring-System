package de.simonschuler.ba.data;

import java.util.Random;

import de.simonschuler.ba.misc.AbstractService;
import de.simonschuler.ba.misc.GeneralCallback;
import de.simonschuler.ba.misc.ServiceManager;
import de.simonschuler.ba.obd2.Obd2Impl;
import de.simonschuler.ba.scoring.ScoringEngineImpl;

public class DataCollectorImpl extends AbstractService implements DataCollector, Runnable {

    private double currentOverallScore = 10.0;
    private double alpha = 0.2;
    private boolean running;
    private DriverData data;

    public DataCollectorImpl(GeneralCallback cb) {
        super("data_service");

        //start scoring engine
        ScoringEngineImpl scoringEngine = new ScoringEngineImpl();
        ServiceManager.registerService(scoringEngine);
    }

    @Override
    public void startTrip() {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void stopTrip() {
        running = false;
    }

    @Override
    public double getOverallScore() {
        return currentOverallScore;
    }

    @Override
    public void run() {
        //run for as long as trip is "running"
        while (running) {
            data = new DriverData();
            //get all the Data from the OBD2
            Obd2Impl obd2Service = ServiceManager.getService(Obd2Impl.class);
            obd2Service.getRPM(new RPMCallback());
            obd2Service.getSpeed(new SpeedCallback());
            obd2Service.getThrottlePosition(new ThrottlePositionCallback());
            obd2Service.getEngineLoad(new EngineLoadCallback());
            //the data comes with the callbacks
            try {
                //and w8 for 100ms
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            while (!data.isComplete()) {
                //w8 for all the data
            }
            updateScore();
        }
    }

    private void updateScore() {
        ScoringEngineImpl service = ServiceManager.getService(ScoringEngineImpl.class);
        double score = service.getScore(data);

        //if we could not calculate a score we just return
        if (score == -1) {
            return;
        }
        //using complementary filter for low pass filtration of scores
        currentOverallScore = (1 - alpha) * currentOverallScore + alpha * score;
    }

    private class RPMCallback implements GeneralCallback {

        @Override
        public void ok(String response) {
            System.out.println("RPM: " + response);
            data.setRPM(Integer.parseInt(response));
        }

        @Override
        public void fail(String response) {
            System.out.println(response);
            data.setRPM(-1);
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private class SpeedCallback implements GeneralCallback {

        @Override
        public void ok(String response) {
            System.out.println("Speed: " + response);
            data.setSpeed(Integer.parseInt(response));
        }

        @Override
        public void fail(String response) {
            System.out.println(response);
            data.setSpeed(-1);
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private class ThrottlePositionCallback implements GeneralCallback {

        @Override
        public void ok(String response) {
            System.out.println("Throttle Position: " + response);
            data.setThrottlePosition(Float.parseFloat(response));
        }

        @Override
        public void fail(String response) {
            System.out.println(response);
            data.setThrottlePosition(-1f);
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private class EngineLoadCallback implements GeneralCallback {

        @Override
        public void ok(String response) {
            System.out.println("Engine Load: " + response);
            data.setEngineLoad(Float.parseFloat(response));
        }

        @Override
        public void fail(String response) {
            System.out.println(response);
            data.setEngineLoad(-1f);
        }

        @Override
        public void exception(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
