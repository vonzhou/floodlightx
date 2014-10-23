package org.openflow.protocol.action;

import org.jboss.netty.buffer.ChannelBuffer;

public class OFActionFPUpdate extends OFAction implements Cloneable {

	public static int MINIMUM_LENGTH = 8;

    protected int vector;

    public OFActionFPUpdate() {
        super.setType(OFActionType.FP_UPDATE);
        super.setLength((short) MINIMUM_LENGTH);
    }
    
    /**
     * Create an FP_Update Action updating the fingerprint table
     * in the of switch.
     *
     * This is the most common creation pattern for OFActions.
     *
     * @param vector
     */

    public OFActionFPUpdate(int vector) {
    	super();
        super.setType(OFActionType.FP_UPDATE);
        super.setLength((short) MINIMUM_LENGTH);
        this.vector = vector;
    }
    
    
    /**
     * Get the output port
     * @return
     */
    public int getVector() {
        return this.vector;
    }

    /**
     * Set the output port
     * @param port
     */
    public OFActionFPUpdate setVector(int vector) {
        this.vector = vector;
        return this;
    }

    
    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        this.vector = data.readInt();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        data.writeInt(vector);
    }

}












