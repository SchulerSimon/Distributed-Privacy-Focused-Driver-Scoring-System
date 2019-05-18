package de.simonschuler.ba.callback;

public interface BaseCallback {
    void failure(String msg);

    void except(Throwable e);
}
