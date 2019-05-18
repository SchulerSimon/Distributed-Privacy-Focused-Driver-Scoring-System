package de.simonschuler.ba.scoring;

import de.simonschuler.ba.data.DriverData;

public interface ScoringEngine {
    double getScore(DriverData data);
}
