package de.simonschuler.ba.client;

import de.simonschuler.ba.misc.GeneralCallback;

/**
 * interface for communication with the blockchain node
 */
public interface Client {
    void addScore(long time, double score, GeneralCallback cb);

    void getTimes(GeneralCallback cb);

    void getScore(long time, GeneralCallback cb);

    void giveAccessRight(String address, GeneralCallback cb);

    void removeAccessRight(String address, GeneralCallback cb);

    void whoHasAccessRight(GeneralCallback cb);
}
