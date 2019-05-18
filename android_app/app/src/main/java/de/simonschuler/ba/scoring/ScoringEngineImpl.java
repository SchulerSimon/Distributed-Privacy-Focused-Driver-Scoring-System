package de.simonschuler.ba.scoring;

import de.simonschuler.ba.data.DriverData;
import de.simonschuler.ba.misc.AbstractService;

/**
 * This class implements a prototype driver scoring engine just for the purpose of having something.
 * This is by no means a good driver scoring/evaluation system on its own. And it is not supposed
 * to be, this prototype has its focus on preserving-privacy using Blockchains.
 * Not on driver scoring.
 * <p>
 * I use part of the Paper
 * "Driving Behavior Analysis Based on Vehicle OBD Information and AdaBoost Algorithms"
 * by Shi-Huang Chen, Jeng-Shyang Pan, and Kaixuan Lu
 * to implement this.
 * <p>
 * The Paper
 * "Driver Behavior Analysis using Vehicular Data"
 * by K.A.De Zoysa Karunathilaka, P.U.Jayaweera, K.G. Kalani, R.W.P.M. Meththananda
 * from the university of Moratuwa
 * has a more detailed approach including GPS, Accelerometer and Gyrometer data into the scoring
 * process.
 * <p>
 * input:
 * RPM
 * Speed
 * Throttle Position
 * engine load
 * <p>
 * formulas:
 * vehicle speed at time t
 * cs(t) <= 220
 * <p>
 * engine speed at time t
 * zs(t) <= 8000
 * <p>
 * relative ratio of vehicle speed and engine speed
 * Rcz (t) = ( cs(t) / 220 ) / ( zs(t) / 8000)
 * <p>
 * change rate of throttle position at time t
 * jq'(t)
 * <p>
 * change rate of engine speed at time t
 * zs'(t)
 * <p>
 * maximum change rate of values of throttle position
 * max(jq')
 * <p>
 * maximum change rate of engine speed
 * max(zs')
 * <p>
 * relative ratio of throttle position and engine speed
 * Rjz (t) = (jq'(t) / max (jq')) / (zs'(t) / max (zs'))
 * <p>
 * d/dt where t2 -t1 = 1
 * d(t) = (data(t2) - data(t1))/(t2 - t1)
 * <p>
 * scoring:
 * a = 1 if 0.9 <= Rcz <= 1.3 else 0
 * b = 1 if 0.9 <= Rjz <= 1.3 else 0
 * c = 1 if 20% <= engine_load <= 50% else 0
 * <p>
 * score(t) = (max_score)*((a+b+c)/3)
 * <p>
 * basically a low pass filter (complementary filter)
 * overall_score(t) = (1-alpha)*score(t) + alpha*overall_score(t-1)
 */
public class ScoringEngineImpl extends AbstractService implements ScoringEngine {

    private DriverData tMinus1;

    //the max-starting-values of 20 are just randomly chosen, further research needs to be done to
    //ensure these values are ok.
    private double maxjq = 20;
    private double maxzs = 20;

    public ScoringEngineImpl() {
        super("scoring_service");
    }

    @Override
    public double getScore(DriverData data) {
        // if this is not the first time we call this, than calculate a score
        // if rpm == 0 or throttle Position == 0 we are currently not driving, so we just return -1
        if (tMinus1 != null && data.getRPM() != 0 && data.getThrottlePosition() != 0) {
            double rcz = calcRcz(data);
            double rjz = calcRjz(data);
            double el = data.getEngineLoad();

            //this is from the paper
            //a = 1 if 0.9 <= Rcz <= 1.3 else 0
            //b = 1 if 0.9 <= Rjz <= 1.3 else 0
            //c = 1 if 20% <= engine_load <= 50% else 0
            int a = rcz >= 0.9 && rcz <= 1.3 ? 1 : 0;
            int b = rjz >= 0.9 && rjz <= 1.3 ? 1 : 0;
            int c = el >= 20d && el <= 50d ? 1 : 0;

            //this is just an idea of how to scramble 3 scores together
            //score(t) = (max_score)*((a+b+c)/3)
            double score = 10d * ((a + b + c) / 3);

            return score;
        }
        //else store t-1 data and return -1
        tMinus1 = data;
        return -1d;
    }

    /**
     * relative ratio of vehicle speed and engine speed
     * Rcz (t) = ( cs(t) / 220 ) / ( zs(t) / 8000)
     *
     * @param data data at point t
     * @return relative ratio of vehicle speed and engine speed
     */
    private double calcRjz(DriverData data) {
        return (data.getSpeed() / 220) / ((double)data.getRPM() / 8000);
    }

    /**
     * relative ratio of throttle position and engine speed
     * Rjz (t) = (jq'(t) / max (jq')) / (zs'(t) / max (zs'))
     *
     * @param data
     * @return
     */
    private double calcRcz(DriverData data) {
        //calculate max
        double jq = dt(data.getThrottlePosition(), tMinus1.getThrottlePosition());
        if (maxjq < jq) {
            maxjq = jq;
        }

        double zs = dt(data.getRPM(), tMinus1.getRPM());
        if (maxzs < zs) {
            maxzs = zs;
        }

        return (jq / maxjq) / (zs / maxzs);
    }

    /**
     * d/dt where t2 -t1 = 1
     * d(t) = (data(t2) - data(t1))/(t2 - t1)
     *
     * @param t1 current data
     * @param t2 t-1 data
     * @return (data ( t2) - data(t1))/(t2 - t1) for t2-t1 = 1
     */
    private double dt(double t1, double t2) {
        return t2 - t1;
    }
}


