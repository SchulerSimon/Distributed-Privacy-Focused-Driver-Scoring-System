package de.simonschuler.ba.misc;

public interface GeneralCallback {
    void ok(String response);

    void fail(String response);

    void exception(Exception e);
}
