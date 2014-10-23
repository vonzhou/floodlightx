package org.openflow.protocol;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.U16;

public class OFFPUpdate extends OFMessage implements OFActionFactoryAware{
	
	 public static int MINIMUM_LENGTH = 16;
	 public static int BUFFER_ID_NONE = 0xffffffff;

	 protected OFActionFactory actionFactory;
	 protected int bufferId;
	 protected short inPort = 0x0000; // not used yet
	 protected short actionsLength;
	 protected List<OFAction> actions;
	 //protected byte[] packetData;
	 
	 public OFFPUpdate(){
		 super();
		 this.type = OFType.FP_UPDATE;
		 this.length = U16.t(MINIMUM_LENGTH);
	 }
	 
	 /**
	     * Get buffer_id
	     * @return
	     */
	    public int getBufferId() {
	        return this.bufferId;
	    }

	    /**
	     * Set buffer_id
	     * @param bufferId
	     */
	    public OFFPUpdate setBufferId(int bufferId) {
	        this.bufferId = bufferId;
	        return this;
	    }

	    /**
	     * Get in_port
	     * @return
	     */
	    public short getInPort() {
	        return this.inPort;
	    }

	    /**
	     * Set in_port
	     * @param inPort
	     */
	    public OFFPUpdate setInPort(short inPort) {
	        this.inPort = inPort;
	        return this;
	    }

	    /**
	     * Set in_port. Convenience method using OFPort enum.
	     * @param inPort
	     */
	    public OFFPUpdate setInPort(OFPort inPort) {
	        this.inPort = inPort.getValue();
	        return this;
	    }

	    /**
	     * Get actions_len
	     * @return
	     */
	    public short getActionsLength() {
	        return this.actionsLength;
	    }

	    /**
	     * Get actions_len, unsigned
	     * @return
	     */
	    public int getActionsLengthU() {
	        return U16.f(this.actionsLength);
	    }

	    /**
	     * Set actions_len
	     * @param actionsLength
	     */
	    public OFFPUpdate setActionsLength(short actionsLength) {
	        this.actionsLength = actionsLength;
	        return this;
	    }

	    /**
	     * Returns the actions contained in this message
	     * @return a list of ordered OFAction objects
	     */
	    public List<OFAction> getActions() {
	        return this.actions;
	    }

	    /**
	     * Sets the list of actions on this message
	     * @param actions a list of ordered OFAction objects
	     */
	    public OFFPUpdate setActions(List<OFAction> actions) {
	        this.actions = actions;
	        return this;
	    }

	    @Override
	    public void setActionFactory(OFActionFactory actionFactory) {
	        this.actionFactory = actionFactory;
	    }

	    @Override
	    public void readFrom(ChannelBuffer data) {
	        super.readFrom(data);
	        this.bufferId = data.readInt();
	        this.inPort = data.readShort();
	        this.actionsLength = data.readShort();
	        if ( this.actionFactory == null)
	            throw new RuntimeException("ActionFactory not set");
	        this.actions = this.actionFactory.parseActions(data, getActionsLengthU());
	        
	    }

	    @Override
	    public void writeTo(ChannelBuffer data) {
	        super.writeTo(data);
	        data.writeInt(bufferId);
	        data.writeShort(inPort);
	        data.writeShort(actionsLength);
	        for (OFAction action : actions) {
	            action.writeTo(data);
	        }
	    }
	 

}
