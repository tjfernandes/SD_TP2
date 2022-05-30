package tp2.impl.servers.soap;


import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import jakarta.xml.ws.Endpoint;
import tp2.api.service.java.Directory;
import util.Debug;
import util.Token;


public class DirectorySoapServer extends AbstractSoapServer {


	public static final int PORT = 14567;
	private static Logger Log = Logger.getLogger(DirectorySoapServer.class.getName());

	protected DirectorySoapServer() {
		super(false, Log, Directory.SERVICE_NAME, PORT, new SoapDirectoryWebService(), new DirectorySoapServer());
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.TP2);
		Token.set( args.length > 0 ? args[0] : "");
		
		new DirectorySoapServer().start();
	}
}
