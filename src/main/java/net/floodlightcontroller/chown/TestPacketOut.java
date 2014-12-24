package net.floodlightcontroller.chown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.HexString;
import org.openflow.util.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * use it in VonzhouLearningSwitch module
 * 
 */
public class TestPacketOut implements IOFMessageListener, IFloodlightModule {
	protected IFloodlightProviderService floodlightProvider;
	protected Set macAddresses;
	protected static Logger logger;

	@Override
	// Need provide ID for OFMessageListener
	public String getName() {
		return TestPacketOut.class.getSimpleName();
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
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);
		macAddresses = new ConcurrentSkipListSet<Long>();
		logger = LoggerFactory.getLogger(TestPacketOut.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		// we are interested in pktin msg
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		// We ignore IPv6
		if (eth.getEtherType() == Ethernet.TYPE_IPv4) {
			/*
			 * // cannot work !! NullPointerException Forwarding forward = new
			 * Forwarding(); OFPacketIn pi = (OFPacketIn) msg; OFMatch match =
			 * new OFMatch(); match.loadFromPacket(pi.getPacketData(),
			 * pi.getInPort()); forward.pushPacket(sw, match, pi, (short) 2,
			 * cntx);
			 */

			// Simulate The Hub impl and its test
			
			String srcMac  = HexString.toHexString(eth.getSourceMACAddress());
			logger.info("Packet-In from switch {}", srcMac);
			// construct a UDP
			IPacket testPacket = new Ethernet()
				.setDestinationMACAddress(srcMac)
				.setSourceMACAddress("00:11:22:33:44:55")
				.setEtherType(Ethernet.TYPE_IPv4)
				.setPayload(new IPv4()
					.setTtl((byte)128)
					.setSourceAddress("10.0.0.1")
					.setDestinationAddress("10.0.0.2")
					.setPayload(new UDP()
							.setSourcePort((short)1234)
							.setDestinationPort((short)5001)
							.setPayload(new Data("ACK".getBytes()))));
							
			//construct the openflow msg
			OFPacketIn pi = (OFPacketIn) msg;
			
			
			OFPacketOut po = (OFPacketOut) floodlightProvider
					.getOFMessageFactory().getMessage(OFType.PACKET_OUT);
			//po.setInPort(pi.getInPort());
			po.setInPort(OFPort.OFPP_NONE);
			po.setBufferId(OFPacketOut.BUFFER_ID_NONE); 

			// set actions
			OFActionOutput action = new OFActionOutput()
					.setPort((short) OFPort.OFPP_FLOOD.getValue());
			// FIXME:packet out to the inport??

			po.setActions(Collections.singletonList((OFAction) action));
			po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

			byte[] data = testPacket.serialize();
			po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
					+ po.getActionsLength() + data.length));
			po.setPacketData(data);
			try {
				sw.write(po, null);
				logger.info("Packet Out of UDP sent !!");
			} catch (IOException e) {
				logger.error("[TestPacketOut]Failure writing PacketOut", e);
			}

		}

		return Command.CONTINUE;// other handlers in this pipeline to process
								// this packetin ;
	}

}
