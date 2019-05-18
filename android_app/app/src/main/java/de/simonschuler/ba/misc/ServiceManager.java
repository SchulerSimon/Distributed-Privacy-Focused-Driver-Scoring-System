package de.simonschuler.ba.misc;

import java.util.HashMap;

/**
 * this class represents a holder object for all the different services of the System
 * Singleton architecture is enforced, so that static calls to ServiceManager are possible
 */
public class ServiceManager {
    private static ServiceManager instance;

    /**
     * The Static initializer constructs the instance at class
     * loading time; this is to simulate a more involved
     * construction process (it it were really simple, you'd just
     * use an initializer)
     */
    static {
        instance = new ServiceManager();
    }

    private HashMap<Class, AbstractService> services = new HashMap<>();

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private ServiceManager() {
        //this is used so that we have a singleton instance of this manager.
    }

    /**
     * grant access to the instance with static getter
     *
     * @return instance of this object
     */
    private static ServiceManager getInstance() {
        return instance;
    }

    /**
     * static wrapper for addService
     *
     * @param abstractService the abstractService to add
     */
    public synchronized static void registerService(AbstractService abstractService) {
        getInstance().addService(abstractService);
    }

    /**
     * static wrapper for removeService
     *
     * @param cls
     */
    public synchronized static void removeService(Class cls) {
        getInstance().remove(cls);
    }

    /**
     * static wrapper for getService
     *
     * @param cls class of the AbstractService
     * @return
     */
    public synchronized static <T extends AbstractService> T getService(Class cls) {
        return getInstance().get(cls);
    }

    /**
     * adds/replaces a abstractService to/in the list
     *
     * @param abstractService the abstractService
     */
    private synchronized void addService(AbstractService abstractService) {
        this.services.put(abstractService.getClass(), abstractService);
    }

    /**
     * returns a service from the list
     *
     * @param cls class of the AbstractService
     * @return
     */
    private synchronized <T extends AbstractService> T get(Class cls) {
        return (T) this.services.get(cls);
    }

    /**
     * removes a service
     *
     * @param cls
     */
    private synchronized void remove(Class cls) {
        this.services.remove(cls);
    }
}
