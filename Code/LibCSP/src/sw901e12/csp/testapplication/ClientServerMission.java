package sw901e12.csp.testapplication;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import sw901e12.csp.CSPManager;

public class ClientServerMission extends Mission implements Safelet<Mission> {

	public static final int NODE_ADDRESS = 0xA;
	private CSPManager manager;
	
	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public MissionSequencer<Mission> getSequencer() {
		return new LinearMissionSequencer<Mission>(new PriorityParameters(20), 
				new StorageParameters(100000, null), 
				this);
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public long immortalMemorySize() {
		return 100000;
	}
	
	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void initialize() {
		super.peHandlerCount = 4;
		
		manager = new CSPManager();
		
		manager.init((byte)ClientServerMission.NODE_ADDRESS);
		manager.initPools();
		manager.startRouteHandler();
		
		initializeFirstClientHandler();
		initializeSecondClientHandler();
		initializeServerHandler();
	}
	
	
	private void initializeFirstClientHandler() {
		final int CLIENT_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 2048;
		final int CLIENT_HANDLER_SCOPE_SIZE_IN_BYTES = 512;
		final int CLIENT_HANDLER_RELEASE_PERIOD_IN_MS = 400;
		final int CLIENT_HANDLER_PRIORITY = 5;
		
		PriorityParameters clientHandlerPriorityParameters = new PriorityParameters(CLIENT_HANDLER_PRIORITY);
		PeriodicParameters clientHandlerPeriodicParameters = new PeriodicParameters(new RelativeTime(200, 0), new RelativeTime(CLIENT_HANDLER_RELEASE_PERIOD_IN_MS, 0));
		StorageParameters clientHandlerStorageParameters = new StorageParameters(CLIENT_HANDLER_BACKING_STORE_SIZE_IN_BYTES, new long[] { CLIENT_HANDLER_SCOPE_SIZE_IN_BYTES }, 0, 0);
		
		ClientHandler client = new ClientHandler(clientHandlerPriorityParameters,
			clientHandlerPeriodicParameters,
			clientHandlerStorageParameters,
			0,
			manager);
		
		client.register();
	}
	
	private void initializeSecondClientHandler() {
		final int CLIENT_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 2048;
		final int CLIENT_HANDLER_SCOPE_SIZE_IN_BYTES = 512;
		final int CLIENT_HANDLER_RELEASE_PERIOD_IN_MS = 400;
		final int CLIENT_HANDLER_PRIORITY = 7;
		
		PriorityParameters clientHandlerPriorityParameters = new PriorityParameters(CLIENT_HANDLER_PRIORITY);
		PeriodicParameters clientHandlerPeriodicParameters = new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(CLIENT_HANDLER_RELEASE_PERIOD_IN_MS, 0));
		StorageParameters clientHandlerStorageParameters = new StorageParameters(CLIENT_HANDLER_BACKING_STORE_SIZE_IN_BYTES, new long[] { CLIENT_HANDLER_SCOPE_SIZE_IN_BYTES }, 0, 0);
		
		SecondClientHandler client = new SecondClientHandler(clientHandlerPriorityParameters,
			clientHandlerPeriodicParameters,
			clientHandlerStorageParameters,
			0,
			manager);
		
		client.register();
	}
	
	private void initializeServerHandler() {
		final int SERVER_HANDLER_BACKING_STORE_SIZE_IN_BYTES = 2048;
		final int SERVER_HANDLER_SCOPE_SIZE_IN_BYTES = 512;
		final int SERVER_HANDLER_RELEASE_PERIOD_IN_MS = 15;
		final int SERVER_HANDLER_PRIORITY = 15;
		
		PriorityParameters serverHandlerPriorityParameters = new PriorityParameters(SERVER_HANDLER_PRIORITY);
		PeriodicParameters serverHandlerPeriodicParameters = new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(SERVER_HANDLER_RELEASE_PERIOD_IN_MS, 0));
		StorageParameters serverHandlerStorageParameters = new StorageParameters(SERVER_HANDLER_BACKING_STORE_SIZE_IN_BYTES, new long[] { SERVER_HANDLER_SCOPE_SIZE_IN_BYTES }, 0, 0);
		
		ServerHandler server = new ServerHandler(serverHandlerPriorityParameters,
				serverHandlerPeriodicParameters,
				serverHandlerStorageParameters,
				0,
				manager);
		
		server.register();
	}

	@Override
	@SCJAllowed
	public long missionMemorySize() {
		return 100000;
	}

}
