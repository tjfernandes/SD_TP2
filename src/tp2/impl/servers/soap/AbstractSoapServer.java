package tp2.impl.servers.soap;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import tp2.impl.discovery.Discovery;
import tp2.impl.servers.common.AbstractServer;
import util.IP;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import jakarta.xml.ws.Endpoint;

public class AbstractSoapServer extends AbstractServer{
	private static String SERVER_BASE_URI = "https://%s:%s/soap";

	final Object implementor;
	final Object soapServer;
	
	protected AbstractSoapServer( boolean enableSoapDebug, Logger log, String service, int port, Object implementor, Object soapServer) {
		super( log, service, port);
		this.implementor = implementor;
		this.soapServer = soapServer;
		
		if(enableSoapDebug ) {
			System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
		}
	}
	
	protected void start() {
		try {

			var ip = IP.hostAddress();
			var serverURI = String.format(SERVER_BASE_URI, ip, port);

			var server = HttpsServer.create(new InetSocketAddress(ip, port), 0);

			server.setExecutor(Executors.newCachedThreadPool());
			server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));

			var endpoint = Endpoint.create(soapServer);
			endpoint.publish(server.createContext("/soap"));

			Discovery.getInstance().announce(service, serverURI);

			Log.info(String.format("%s Soap Server ready @ %s\n", service, serverURI));

		} catch (Exception e) {
			e.printStackTrace();
		}


		
	}
}
