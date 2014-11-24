
package net.floodlightcontroller.core;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;


public interface IOFMessageListener extends IListener<OFType> {
  /**
   * Floodlight利用这个方法呼叫这些listeners来处理这个OF MSG；
   * This is the method Floodlight uses to call listeners with OpenFlow messages
   * @param sw the OpenFlow switch that sent this message
   * @param msg the message
   * @param cntx a floodlight message context object you can use to pass 
   * information between listeners
   * @return the command to continue or stop the execution
   */
  public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx);
}
