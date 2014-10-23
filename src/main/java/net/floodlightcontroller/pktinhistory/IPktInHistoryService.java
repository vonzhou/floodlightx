package net.floodlightcontroller.pktinhistory;

import net.floodlightcontroller.core.module.IFloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.SwitchMessagePair;

public interface IPktInHistoryService extends IFloodlightService,
		IFloodlightModuleContext {
	public ConcurrentCircularBuffer<SwitchMessagePair> getBuffer();

}
