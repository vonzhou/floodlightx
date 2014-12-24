package net.floodlightcontroller.chown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.mactracker.MACTracker1;
import net.floodlightcontroller.packet.Ethernet;

import org.openflow.protocol.OFFPUpdate;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionFPUpdate;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MACTracker1 implements IOFMessageListener, IFloodlightModule {
	protected IFloodlightProviderService floodlightProvider;
	protected Set macAddresses;
	protected static Logger logger;

	@Override
	//Need provide ID for OFMessageListener
	public String getName() {
		return MACTracker1.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l=
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		macAddresses = new ConcurrentSkipListSet<Long>();
		logger = LoggerFactory.getLogger(MACTracker1.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		// we are interested in pktin msg
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		//i want to see the packet in data,to see if get such as the file name
		
		OFPacketIn pi = (OFPacketIn)msg;
		logger.info("PacketIn buffer id :" + pi.getBufferId());
		 
		OFMatch match = new OFMatch();
	    match.loadFromPacket(pi.getPacketData(), pi.getInPort());
	    logger.info("destination port num: " + (short)match.getTransportDestination());
	    if(match.getNetworkProtocol() == 0x11 && match.getTransportDestination() == 2500){
	    	// UDP
	    	logger.info("packet_in len: " + pi.getLengthU());
	    	StringBuffer buff = new StringBuffer();
	        for (int i = 0; i < pi.getPacketData().length; i++)	            {
	        	 buff.append((char)pi.getPacketData()[i]);
	        }
	    	String filename = buff.substring(54, buff.length());
	    	logger.info("filename : " + filename);
	    	 
	    }
	    
	    /*After got the file name from the packet_in,
	     * we push the appropriate fp vector to the SW
	     * (TODO:the switches along the path)
	     */
	    
	    OFFPUpdate fu =
                (OFFPUpdate) floodlightProvider.getOFMessageFactory()
                                              .getMessage(OFType.FP_UPDATE);
        OFActionFPUpdate action = new OFActionFPUpdate();
        action.setVector(0xffffffff); // just for testing
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(action);

        fu.setBufferId(OFFPUpdate.BUFFER_ID_NONE)
        	.setInPort((short)0)
            .setActions(actions)
            .setLengthU(OFFPUpdate.MINIMUM_LENGTH+OFActionFPUpdate.MINIMUM_LENGTH);
        try {
			sw.write(fu, cntx);
		} catch (IOException e) {
            logger.error("Failure writing fp update", e);
        }
	    
	    /*
		 // get mac 
		Ethernet eth = floodlightProvider.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		Long ethMacHash  = Ethernet.toLong(eth.getSourceMACAddress());
		if(!macAddresses.contains(ethMacHash)){
			macAddresses.add(ethMacHash);
			logger.info("MAC Address:{} seen on switch {}",
					HexString.toHexString(ethMacHash),sw.getId());
			
		}
		*/
		return Command.CONTINUE;// other handlers in this pipeline to process this packetin ;
	}

}
