package org.openflow.protocol;

import org.openflow.util.U16;

/**
 * Represents an OFPT_GET_CONFIG_REQUEST type message
 */
public class OFGetConfigRequest extends OFMessage {
    public OFGetConfigRequest() {
        super();
        this.type = OFType.GET_CONFIG_REQUEST;
        this.length = U16.t(OFMessage.MINIMUM_LENGTH);
    }
}
