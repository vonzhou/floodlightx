
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
     * ��Ϊname��module�ڴ��������Ϣ��ʱ��Ҫ�����module֮ǰ
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
     * �ڴ���type��Ϣ��ʱ��name_module Ҫ�����ģ��֮��
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
