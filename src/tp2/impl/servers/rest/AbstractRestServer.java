package tp2.impl.servers.rest;

import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tp2.impl.discovery.Discovery;
import tp2.impl.servers.common.AbstractServer;
import util.IP;

public abstract class AbstractRestServer extends AbstractServer {
	
	protected static String SERVER_BASE_URI = "http://%s:%s/rest";
	
	protected AbstractRestServer(Logger log, String service, int port) {
		super(log, service, port);
	}


	protected void start() {
		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, port);
		
		ResourceConfig config = new ResourceConfig();
		
		registerResources( config );
		
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(ip, INETADDR_ANY)), config);

		Log.info(String.format("%s Server ready @ %s\n",  service, serverURI));
		
		Discovery.getInstance().announce(service, serverURI);
	}
	
	abstract void registerResources( ResourceConfig config );
}
