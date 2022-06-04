package tp2.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tp2.api.service.java.Files;
import tp2.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import util.Token;

public class FilesProxyRestServer extends AbstractRestServer {
    public static final int PORT = 6785;

	public static String apiKey;
	public static String apiSecret;
	public static String accessTokenStr;
	public static boolean cleanDropbox;
	
	private static Logger Log = Logger.getLogger(FilesProxyRestServer.class.getName());

	
	FilesProxyRestServer() {
		super(Log, Files.SERVICE_NAME, PORT);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( new FilesResources(apiKey, apiSecret, accessTokenStr, cleanDropbox) ); 
		config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
	}
	
	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.TP2);

		if (args[0].equals("true")) {
			cleanDropbox = true;
		}
		else cleanDropbox = false;
		
		Token.set( args.length == 1 ? "" : args[1] );

		if (args.length > 1) {
			apiKey = args[2];
			apiSecret = args[3];
			accessTokenStr = args[4];
		}

		new FilesProxyRestServer().start();
	}	
}
