package ch.eth.coap.demo;

import java.net.SocketException;

import ch.eth.coap.util.Properties;

import java.util.logging.*;
import java.io.*;

import ch.eth.coap.coap.Option;
import ch.eth.coap.coap.OptionNumberRegistry;
import ch.eth.coap.coap.Request;
import ch.eth.coap.demo.resources.CarelessResource;
import ch.eth.coap.demo.resources.FeedbackResource;
import ch.eth.coap.demo.resources.HelloWorldResource;
import ch.eth.coap.demo.resources.LargeResource;
import ch.eth.coap.demo.resources.MirrorResource;
import ch.eth.coap.demo.resources.SeparateResource;
import ch.eth.coap.demo.resources.StorageResource;
import ch.eth.coap.demo.resources.TimeResource;
import ch.eth.coap.demo.resources.ToUpperResource;
import ch.eth.coap.demo.resources.ZurichWeatherResource;
import ch.eth.coap.endpoint.Endpoint;
import ch.eth.coap.endpoint.LocalEndpoint;

/*
 * This class implements a simple CoAP server for demonstration purposes.
 * 
 * Currently, it just provides some simple resources.
 *  
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class DemonstrationServer extends LocalEndpoint {

	public static Logger logger;

	/*
	 * Constructor for a new DemonstrationServer
	 */
	public DemonstrationServer(int port, int defaultBlockSize) throws SocketException {

		super(port, defaultBlockSize);
		
		// add resources to the server
		addResource(new HelloWorldResource());
		addResource(new StorageResource());
		addResource(new ToUpperResource());
		addResource(new SeparateResource());
		addResource(new TimeResource());
		addResource(new ZurichWeatherResource());
		addResource(new FeedbackResource());
		addResource(new MirrorResource());
		addResource(new LargeResource());
		addResource(new CarelessResource());
	}

	// Logging /////////////////////////////////////////////////////////////////

	@Override
	public void handleRequest(Request request) {

		// output the request
		System.out.println("Incoming request:");
		request.log();
		
		if (request.getURI()!=null) {
		    logger.info(request.getURI() + "\t" + Option.join(request.getOptions(OptionNumberRegistry.URI_PATH), "/"));
		}

		// handle the request
		super.handleRequest(request);
	}

	// Application entry point /////////////////////////////////////////////////

	public static void main(String[] args) {

		int port = Properties.std.getInt("DEFAULT_PORT");
		int defaultBlockSize = Properties.std.getInt("DEFAULT_BLOCK_SIZE");
		
		// input parameters
		// syntax: DemonstrationServer.jar [PORT] [BLOCKSIZE]
		
		for (String arg : args) {
			System.out.println(arg);
		}
		
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		if (args.length > 1) {
			defaultBlockSize = Integer.parseInt(args[1]);
		}
		
		try {
			boolean append = true;
			FileHandler fh = new FileHandler("clients.log", append);
			fh.setFormatter(new SimpleFormatter());
			logger = Logger.getLogger("ServerLog");
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// create server
		try {
			Endpoint server = new DemonstrationServer(port, defaultBlockSize);

			System.out.printf("Californium DemonstrationServer listening at port %d.\n",
					server.port());
			
			if (defaultBlockSize < 0) {
				System.out.println("Outgoing block-wise transfer disabled");
			}

		} catch (SocketException se) {
			System.err.printf("Failed to create DemonstrationServer: %s\n",
					se.getMessage());
			return;
		}
	}
}
