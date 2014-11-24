
package net.floodlightcontroller.core;

public interface IListener<T> {
    public enum Command {
        CONTINUE, STOP
    }
    
    /**
     * The name assigned to this listener
     */
    public String getName();

    /**
     * 名为name的module在处理这个消息的时候，要在这个module之前
     * Check if the module called name is a callback ordering prerequisite
     * for this module.  In other words, if this function returns true for 
     * the given name, then this message listener will be called after that
     * message listener.
     * @param type the message type to which this applies
     * @param name the name of the module
     * @return whether name is a prerequisite.
     */
    public boolean isCallbackOrderingPrereq(T type, String name);

    /**
     * 在处理type消息的时候，name_module 要在这个模块之后
     * Check if the module called name is a callback ordering post-requisite
     * for this module.  In other words, if this function returns true for 
     * the given name, then this message listener will be called before that
     * message listener.
     * @param type the message type to which this applies
     * @param name the name of the module
     * @return whether name is a post-requisite.
     */
    public boolean isCallbackOrderingPostreq(T type, String name);
}
