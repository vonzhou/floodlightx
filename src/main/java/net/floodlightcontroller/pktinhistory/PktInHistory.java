package net.floodlightcontroller.pktinhistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.SwitchMessagePair;
import net.floodlightcontroller.restserver.IRestApiService;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

public class PktInHistory implements IPktInHistoryService,IOFMessageListener, IFloodlightModule {
	protected IFloodlightProviderService floodlightProvider;
	protected IRestApiService restApi;
	protected ConcurrentCircularBuffer<SwitchMessagePair> buffer;

	@Override
	public String getName() {
		return "PktInHistory";
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
		Collection<Class<? extends IFloodlightService>> l=
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IPktInHistoryService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>,IFloodlightService> map=
				new HashMap<Class<? extends IFloodlightService>,IFloodlightService>();
		map.put(IPktInHistoryService.class, this);
		return map;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = 
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApi = context.getServiceImpl(IRestApiService.class);
		buffer = new ConcurrentCircularBuffer<SwitchMessagePair>(SwitchMessagePair.class,100);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		restApi.addRestletRoutable(new PktInHistoryWebRoutable());

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch(msg.getType()){
		case PACKET_IN:
			buffer.add(new SwitchMessagePair(sw,msg));
			break;
		default:
			break;
		}
		return Command.CONTINUE;
	}

	@Override
	public <T extends IFloodlightService> T getServiceImpl(Class<T> service) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getAllServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IFloodlightModule> getAllModules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getConfigParams(IFloodlightModule module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConcurrentCircularBuffer<SwitchMessagePair> getBuffer() {
		return buffer;
	}

}
