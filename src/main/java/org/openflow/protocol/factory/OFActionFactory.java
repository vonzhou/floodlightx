package org.openflow.protocol.factory;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;


/**
 * The interface to factories used for retrieving OFAction instances. All
 * methods are expected to be thread-safe.
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFActionFactory {
    /**
     * Retrieves an OFAction instance corresponding to the specified
     * OFActionType
     * @param t the type of the OFAction to be retrieved
     * @return an OFAction instance
     */
    public OFAction getAction(OFActionType t);

    /**
     * Attempts to parse and return all OFActions contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ChannelBuffer to parse for OpenFlow actions
     * @param length the number of Bytes to examine for OpenFlow actions
     * @return a list of OFAction instances
     */
    public List<OFAction> parseActions(ChannelBuffer data, int length);

    /**
     * Attempts to parse and return all OFActions contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ChannelBuffer to parse for OpenFlow actions
     * @param length the number of Bytes to examine for OpenFlow actions
     * @param limit the maximum number of messages to return, 0 means no limit
     * @return a list of OFAction instances
     */
    public List<OFAction> parseActions(ChannelBuffer data, int length, int limit);
}
