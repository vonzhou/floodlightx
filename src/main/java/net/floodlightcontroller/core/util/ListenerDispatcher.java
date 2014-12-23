package net.floodlightcontroller.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IListener;

/**
 * 保持各个消息的观察者，这个listener 是根据依赖关系排序的！
 * Maintain lists of listeners ordered by dependency.  
 * 
 * @author readams
 */
public class ListenerDispatcher<U, T extends IListener<U>> {
    protected static Logger logger = LoggerFactory.getLogger(ListenerDispatcher.class);
    List<T> listeners = null;
    
    private void visit(List<T> newlisteners, U type, HashSet<T> visited, 
                       List<T> ordering, T listener) {
        if (!visited.contains(listener)) {
            visited.add(listener);
            
            for (T i : newlisteners) {
                if (ispre(type, i, listener)) {
                    visit(newlisteners, type, visited, ordering, i);
                }
            }
            ordering.add(listener);
        }
    }
    
    private boolean ispre(U type, T l1, T l2) {
        return (l2.isCallbackOrderingPrereq(type, l1.getName()) ||
                l1.isCallbackOrderingPostreq(type, l2.getName()));
    }
    
    /**
     * 给某个消息增加一个listener，并且维护其中的order；
     * Add a listener to the list of listeners
     * @param listener
     */
    public void addListener(U type, T listener) {
        List<T> newlisteners = new ArrayList<T>();
        if (listeners != null)
            newlisteners.addAll(listeners);

        newlisteners.add(listener);
        // Find nodes without outgoing edges
        List<T> terminals = new ArrayList<T>(); 
        for (T i : newlisteners) {
            boolean isterm = true;
            for (T j : newlisteners) {
                if (ispre(type, i, j)) {
                    isterm = false;
                    break;
                }
            }
            if (isterm) {
                terminals.add(i);
            }
        }
        
        if (terminals.size() == 0) {
            logger.error("No listener dependency solution: " +
            		     "No listeners without incoming dependencies");
            listeners = newlisteners;
            return;
        }
        
        // visit depth-first traversing in the opposite order from
        // the dependencies.  Note we will not generally detect cycles
        HashSet<T> visited = new HashSet<T>();
        List<T> ordering = new ArrayList<T>(); 
        for (T term : terminals) {
            visit(newlisteners, type, visited, ordering, term);
        }
        listeners = ordering;
    }

    /**
     * Remove the given listener
     * @param listener the listener to remove
     */
    public void removeListener(T listener) {
        if (listeners != null) {
            List<T> newlisteners = new ArrayList<T>();
            newlisteners.addAll(listeners);
            newlisteners.remove(listener);
            listeners = newlisteners;
        }
    }
    
    /**
     * Clear all listeners
     */
    public void clearListeners() {
        listeners = new ArrayList<T>();
    }
    
    /** 
     * Get the ordered list of listeners ordered by dependencies 
     * @return
     */
    public List<T> getOrderedListeners() {
        return listeners;
    }
}
