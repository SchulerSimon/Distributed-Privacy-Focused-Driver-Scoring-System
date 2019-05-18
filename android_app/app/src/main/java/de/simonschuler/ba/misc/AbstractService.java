package de.simonschuler.ba.misc;

/**
 * every service implements this.
 */
public abstract class AbstractService {
    public final String SERVICE_NAME;

    public AbstractService(String name) {
        this.SERVICE_NAME = name;
    }
}
