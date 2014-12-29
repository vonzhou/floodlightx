package net.floodlightcontroller.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.internal.Controller;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.counter.ICounterStoreService;
import net.floodlightcontroller.perfmon.IPktInProcessingTimeService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

/*
 * The FloodlightProvider provides two main pieces of functionality. 
 * It handles the connections to switches and turns OpenFlow messages into events 
 * that other modules can listen for. The second big function that it provides is 
 * decides the order in which specific OpenFlow messages (i.e. PacketIn, FlowRemoved, 
 * PortStatus, etc) are dispatched to the modules that listen for the messages. 
 * Modules can then decide to allow the processing of the message to go onto the next 
 * listener or to stop processing the message.
 * 
 * The FloodlightProvider uses the Netty library to handle threading and connections to switches. 
 * Each OpenFlow message will be processed by a Netty thread and will execute all logic associated 
 * with with the message across all modules. Other modules can also register for specific events 
 * like switches connecting or disconnecting and port status notifications. The FloodlightProvider 
 * will turn these wire protocol notifications into java based messages that other modules can handle. 
 * In order for modules to register for OpenFlow messages they must implement the IOFMessageListener interface.
 */
public class FloodlightProvider implements IFloodlightModule {
    Controller controller;
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> services =
                new ArrayList<Class<? extends IFloodlightService>>(1);
        services.add(IFloodlightProviderService.class);
        return services;
    }

    @Override
    public Map<Class<? extends IFloodlightService>,
               IFloodlightService> getServiceImpls() {
        controller = new Controller();
        
        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m = 
                new HashMap<Class<? extends IFloodlightService>,
                            IFloodlightService>();
        m.put(IFloodlightProviderService.class, controller);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> dependencies =
            new ArrayList<Class<? extends IFloodlightService>>(4);
        dependencies.add(IStorageSourceService.class);
        dependencies.add(IPktInProcessingTimeService.class);
        dependencies.add(IRestApiService.class);
        dependencies.add(ICounterStoreService.class);
        dependencies.add(IThreadPoolService.class);
        return dependencies;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
       controller.setStorageSourceService(
           context.getServiceImpl(IStorageSourceService.class));
       controller.setPktInProcessingService(
           context.getServiceImpl(IPktInProcessingTimeService.class));
       controller.setCounterStore(
           context.getServiceImpl(ICounterStoreService.class));
       controller.setRestApiService(
           context.getServiceImpl(IRestApiService.class));
       controller.setThreadPoolService(
           context.getServiceImpl(IThreadPoolService.class));
       controller.init(context.getConfigParams(this));
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        controller.startupComponents();
    }
}
