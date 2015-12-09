package net.floodlightcontroller.chown;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

import org.openflow.protocol.OFError;
import org.openflow.protocol.OFMatch;
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

import redis.clients.jedis.Jedis;

public class RedundancyMonitor implements IOFMessageListener, IFloodlightModule {
	protected IFloodlightProviderService floodlightProvider;
	protected Set macAddresses;
	protected static Logger logger;
	// for dedu work
	public static final int UDP_SERVER_PORT = 5679;
	public static final int UDP_CMD_PORT = 4321;
	public static final int FILE_TYPE_MASK = 0x80000000;
	public static final int DELETE_OVS_MASK = 0x40000000;
	public static final int SIMILAR_MASK = 0x20000000;

	public static final String serverIP = "192.168.4.136";
	// server to its files map
	protected Map<Integer, Set<FingerPrint>> fileContainer;
	protected Set<FingerPrint> filefps;

	// key-value data base
	Jedis jedis;

	@Override
	// Need provide ID for OFMessageListener
	public String getName() {
		return "redundancy-monitor";
	}

	// TODO the callback orders ???
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
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
		logger = LoggerFactory.getLogger(RedundancyMonitor.class);
		filefps = new HashSet<FingerPrint>();
		jedis = new Jedis("localhost");
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		// we are interested in pktin msg
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch (msg.getType()) {
		case PACKET_IN:
			return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
		case ERROR:
			logger.info("received an error {} from switch {}", (OFError) msg,
					sw);
			return Command.CONTINUE;
		}
		logger.error("received an unexpected message {} from switch {}", msg,
				sw);
		return Command.CONTINUE;
	}

	private Command processPacketInMessage(IOFSwitch sw, OFPacketIn msg,
			FloodlightContext cntx) {
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		IPacket commandPkt = null;// udp packet to send
		OFPacketOut po = null; // packet out for this above udp pkt
		String srcMac = HexString.toHexString(eth.getSourceMACAddress());

		// logger.info("Packet-In from switch {},eth type {}",
		// srcMac,eth.getEtherType());
		/*
		 * OFMatch match = new OFMatch();
		 * match.loadFromPacket(msg.getPacketData(), msg.getInPort());
		 * logger.info("Packet-In host Port  {}", match.getTransportSource() +
		 * ":"+ match.getTransportDestination());
		 */
		// We filter what we need
		if (eth.getEtherType() == Ethernet.TYPE_IPv4) {
			IPv4 ipPkt = (IPv4) eth.getPayload();
			String srcHostIP = IPv4.fromIPv4Address(ipPkt.getSourceAddress());
			if (ipPkt.getProtocol() == IPv4.PROTOCOL_UDP) {
				UDP udpPkt = (UDP) ipPkt.getPayload();
				short srcPort = udpPkt.getSourcePort();
				short destPort = udpPkt.getDestinationPort();
				// logger.info("aPacket-In host IP {}", srcHostIP);
				// logger.info("Packet-In host Port  {}", srcPort + ":"+
				// destPort);
				if (destPort != UDP_SERVER_PORT)
					return Command.CONTINUE;

				Data dataPkt = (Data) udpPkt.getPayload();
				// printL7Data(dataPkt);
				// int destIP = ipPkt.getDestinationAddress();

				DeduHeader dh = getFpFromPacketData(dataPkt);
				boolean present = false; // filefps.contains(fp);
				logger.info("This file {}", present);
				String cmd = "NONE";
				int count = dh.getChunkID();
				if (count == 0) {
					if (isGeneralFile(dh)) {
						// doc file dedu logic
						if (deletedInOVS(dh)) {
							// tell the client stop
							cmd = "STOP";
						} else {
							// search the DB
							System.out.println("wulinqian2");
							boolean makesure = searchController(dh.getFp());
						//	boolean makesure=false;
							if (makesure)
								cmd = "STOP";
							else {
								// When to Update The DB in controller
								updateControllerDB(dh.getFp());
							}
						}

					} else {// picture dedu request
						if (deletedInOVS(dh)) {
							// tell the client stop
							cmd = "STOP";
						} else if (isPicSimilar(dh)) {
							cmd = "WAIT";
						} else {
							logger.info("Not a similar file sent !!");
						}

					}

					System.out.println(cmd);
					// send command to the udp client
					commandPkt = getCommandUDPPkt(srcMac, srcHostIP, cmd);
					po = getPacketOut(commandPkt, msg.getInPort());
					// logger.info("PacketOut:{}", po.toString());
					// and then send to the client..
					try {
						sw.write(po, cntx);
						logger.info("Packet Out of UDP sent !!");
					} catch (IOException e) {
						logger.error("Failure writing PacketOut", e);
					}

				}
			}
		}
		return null;
	}

	private boolean isPicSimilar(DeduHeader dh) {
		int first = dh.getFlags();
		first = first & SIMILAR_MASK;
		logger.info("is sim: " + Integer.toHexString(first));
		return first == 0x20000000;
	}

	private void updateControllerDB(FingerPrint fp) {
		jedis.set(fp + "", "1");
	}

	private boolean searchController(FingerPrint fp) {
		return jedis.get(fp + "") != null;
	}

	private boolean deletedInOVS(DeduHeader dh) {
		int first = dh.getFlags();
		first = first & DELETE_OVS_MASK;
		// System.out.println("is dedufile"+first);
		logger.info("is dedu: " + Integer.toHexString(first));
		return first == 0x40000000;
	}

	private boolean isGeneralFile(DeduHeader dh) {
		int first = dh.getFlags();
		// logger.info("All the pl: " + Integer.toHexString(first));
		// System.out.println("***********"+first);
		first = first & FILE_TYPE_MASK;
		// System.out.println("***********"+first);
		return first == 0;
	}

	private DeduHeader getFpFromPacketData(Data dataPkt) {
		byte[] datas = dataPkt.getData();
		// logger.info("Packet data len : {}", datas.length);
		byte[] fp = new byte[20];
		System.arraycopy(datas, 8, fp, 0, 20);
		FingerPrint tmp = new FingerPrint();
		tmp.setFp(fp);

		byte[] int1 = new byte[4];
		int1 = Arrays.copyOfRange(datas, 0, 4);
		ByteBuffer bb = ByteBuffer.wrap(int1);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int type = bb.getInt();
		String test = Arrays.toString(fp) + "";
		logger.info("All the pl: " + Integer.toHexString(type) + test);
		// TODO get chunk id ....

		byte[] count = new byte[4];
		count = Arrays.copyOfRange(datas, 4, 8);
		ByteBuffer cc = ByteBuffer.wrap(count);
		cc.order(ByteOrder.LITTLE_ENDIAN);
		int count1 = cc.getInt();
		logger.info("count: " + Integer.toHexString(count1));

		return new DeduHeader(type, count1, tmp);
	}

	private OFPacketOut getPacketOut(IPacket udpPkt, short outport) {
		OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory()
				.getMessage(OFType.PACKET_OUT);
		// po.setInPort(pi.getInPort());
		po.setInPort(OFPort.OFPP_NONE);
		po.setBufferId(OFPacketOut.BUFFER_ID_NONE);

		// set actions
		OFActionOutput action = new OFActionOutput().setPort(outport);
		// FIXME:packet out to the inport??

		po.setActions(Collections.singletonList((OFAction) action));
		po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

		byte[] data = udpPkt.serialize();
		po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH + po.getActionsLength()
				+ data.length));
		po.setPacketData(data);
		return po;
	}

	private IPacket getCommandUDPPkt(String dstMac, String srcHostIP, String cmd) {
		// construct a UDP
		IPacket testPacket = new Ethernet()
				.setDestinationMACAddress(dstMac)
				.setSourceMACAddress("00:11:22:33:44:55")
				.setEtherType(Ethernet.TYPE_IPv4)
				.setPayload(
						new IPv4()
								.setTtl((byte) 128)
								.setSourceAddress("192.168.4.136")
								.setDestinationAddress(srcHostIP)
								.setPayload(
										new UDP()
												.setSourcePort((short) 1234)
												.setDestinationPort(
														(short) UDP_CMD_PORT)
												.setPayload(
														new Data(cmd.getBytes()))));
		return testPacket;
	}

	private void printL7Data(Data dataPkt) {
		System.out.println(dataPkt.getData().length);

		byte[] arr = dataPkt.getData();
		for (int i = 0; i < dataPkt.getData().length; i++) {
			System.out.print((char) arr[i]);
		}
	}

}
