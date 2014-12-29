package net.floodlightcontroller.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a context object where floodlight listeners can register 
 * and later retrieve context information associated with an
 * event
 * @author readams
 */
public class FloodlightContext {
    protected ConcurrentHashMap<String, Object> storage =
            new ConcurrentHashMap<String, Object>();

    public ConcurrentHashMap<String, Object> getStorage() {
        return storage;
    }
}
