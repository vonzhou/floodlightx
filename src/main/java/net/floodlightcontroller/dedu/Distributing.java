package net.floodlightcontroller.dedu;
/*
 * This is the fingerprint distributing module
 * vonzhou  
 */
import java.util.ArrayList;
import java.util.Collection;
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
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Distributing implements IOFMessageListener, IFloodlightModule {
	protected IFloodlightProviderService floodlightProvider;
	protected Set macAddresses;
	protected static Logger logger;

	@Override
	//Need provide ID for OFMessageListener
	public String getName() {
		return Distributing.class.getSimpleName();
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
		logger = LoggerFactory.getLogger(Distributing.class);
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
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                                            IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		/* Need to FIXME
		 * net.floodlightcontroller.packet.DHCP cannot be cast to net.floodlightcontroller.packet.Data
		 */
		if(eth.getEtherType() == Ethernet.TYPE_IPv4){
                IPv4 ipPkt = (IPv4)eth.getPayload();
                if(ipPkt.getProtocol() == IPv4.PROTOCOL_UDP){
                         UDP udpPkt = (UDP)ipPkt.getPayload();
                         Data dataPkt = (Data)udpPkt.getPayload();
               
                        System.out.println(dataPkt.getData().length);  
                        
                        byte[] arr = dataPkt.getData();
                        for (int i = 0; i < dataPkt.getData().length; i++){
                        		System.out.print((char)arr[i]);
                        }
                 }
		}
	    /*After got the file name from the packet_in,
	     * we push the appropriate fp vector to the SW
	     * (TODO:the switches along the path)
	     */
	    /*FIXME  extract a method
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
	    */
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
