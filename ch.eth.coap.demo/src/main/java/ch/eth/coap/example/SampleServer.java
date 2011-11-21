package ch.eth.coap.example;

import java.net.SocketException;

import ch.eth.coap.coap.Request;
import ch.eth.coap.demonstrationServer.resources.HelloWorldResource;
import ch.eth.coap.demo.resources.LargeResource;
import ch.eth.coap.demo.resources.SeparateResource;
import ch.eth.coap.demo.resources.StorageResource;
import ch.eth.coap.demo.resources.ToUpperResource;
import ch.eth.coap.endpoint.Endpoint;
import ch.eth.coap.endpoint.LocalEndpoint;

/*
 * This class implements a simple CoAP server for testing purposes.
 * 
 * Currently, it just provides some simple resources.
 *  
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class SampleServer extends LocalEndpoint {

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	
	/*
	 * Constructor for a new SampleServer
	 * 
	 */
	public SampleServer() throws SocketException {
		
		// add resources to the server
		addResource(new HelloWorldResource());
		addResource(new StorageResource());
		addResource(new ToUpperResource());
		addResource(new SeparateResource());
		addResource(new LargeResource());
	}

	// Logging /////////////////////////////////////////////////////////////////
	
	@Override
	public void handleRequest(Request request) {
		
		// output the request
		System.out.println("Incoming request:");
		request.log();
		
		// handle the request
		super.handleRequest(request);
	}

	
	// Application entry point /////////////////////////////////////////////////
	
	public static void main(String[] args) {
		
		// create server
		try {
			
			Endpoint server = new SampleServer();
			
			System.out.printf("SampleServer listening at port %d.\n", server.port());
			
		} catch (SocketException e) {

			System.err.printf("Failed to create SampleServer: %s\n", 
				e.getMessage());
			System.exit(ERR_INIT_FAILED);
		}
		
	}

}
