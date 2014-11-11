package net.floodlightcontroller.core.internal;

/**
 * Wrapper class to hold state for the OpenFlow switch connection
 * 南向接口连接状态，控制着下一步的动作
 * @author readams
 */
class OFChannelState {

    /**
     * State for handling the switch handshake
     */
    protected enum HandshakeState {
        /**
         * Beginning state
         */
        START,

        /**
         * Received HELLO from switch
         */
        HELLO,

        /**
         * We've received the features reply
         * Waiting for Config and Description reply
         */
        FEATURES_REPLY,

        /**
         * Switch is ready for processing messages
         */
        READY

    }

    protected volatile HandshakeState hsState = HandshakeState.START;
    protected boolean hasGetConfigReply = false;
    protected boolean hasDescription = false;
    
    // The firstRoleReplyRecevied flag indicates if we have received the
    // first role reply message on this connection (in response to the 
    // role request sent after the handshake). If role support is disabled
    // on the controller we also set this flag to true. 
    // The flag is used to decide if the flow table should be wiped
    // @see Controller.handleRoleReplyMessage()
    protected boolean firstRoleReplyReceived = false;
}