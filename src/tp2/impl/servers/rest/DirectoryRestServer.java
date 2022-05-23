package tp2.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tp2.api.service.java.Directory;
import tp2.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import util.Token;

public class DirectoryRestServer extends AbstractRestServer {
	
	public static final int PORT = 4567;
	
	private static Logger Log = Logger.getLogger(DirectoryRestServer.class.getName());

	DirectoryRestServer() {
		super(Log, Directory.SERVICE_NAME, PORT);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( DirectoryResources.class ); 
		config.register( GenericExceptionMapper.class );		
//		config.register( CustomLoggingFilter.class);
	}
	
	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.TP2);

		Token.set( args.length > 0 ? args[0] : "");

		new DirectoryRestServer().start();
	}	
}