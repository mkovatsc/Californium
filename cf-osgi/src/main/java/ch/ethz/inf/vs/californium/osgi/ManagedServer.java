package ch.ethz.inf.vs.californium.osgi;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.EndpointManager;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.network.config.NetworkConfigDefaults;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.ServerInterface;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.elements.Connector;

/**
 * A managed Californium {@code ServerInterface} instance that can be configured using the OSGi
 * <i>Configuration Admin</i> service.
 * 
 * The service understands all network configuration properties defined
 * by {@link NetworkConfigDefaults}.
 * Additionally, it understands the following properties:
 * <ul>
 * <li>DEFAULT_COAPS_PORT - Adds a secure (DTLS) endpoint to the server and binds it to the given port.
 * In order for this to work, the <i>secureConnectorFactory</i> property needs to be set.
 * </li>
 * </ul>
 * 
 * This managed service uses the <i>white board</i> pattern for registering resources,
 * i.e. the service tracks Californium {@code Resource} instances being added to the OSGi service registry
 * and automatically adds them to the managed Californium {@code ServerInterface} instance.
 *  
 * @author Kai Hudalla
 */
public class ManagedServer implements ManagedService, ServiceTrackerCustomizer<Resource, Resource> {

	private final static Logger LOGGER = Logger.getLogger(ManagedServer.class.getCanonicalName());
	
	public final static String DEFAULT_COAPS_PORT = "DEFAULT_COAPS_PORT";
	private ServerInterface server;
	private boolean running = false;
	private BundleContext context;
	private ServiceTracker<Resource, Resource> resourceTracker;
	private ServerInterfaceFactory serverFactory;
	private ConnectorFactory secureConnectorFactory;
	
	/**
	 * Sets all required collaborators.
	 * 
	 * Invoking this constructor is equivalent to invoking {@link #ManagedServer(BundleContext, ServerInterfaceFactory)
	 * with <code>null</code> as the server factory.
	 * 
	 * @param bundleContext the bundle context to be used for tracking {@code Resource}s
	 * @throws NullPointerException if the bundle context is <code>null</code>
	 */
	public ManagedServer(BundleContext bundleContext) {
		this(bundleContext, null);
	}

	/**
	 * Sets all required collaborators.
	 * 
	 * @param bundleContext the bundle context to be used for tracking {@code Resource}s
	 * @param serverFactory the factory to use for creating new server instances
	 * @throws NullPointerException if the bundle context is <code>null</code>
	 */
	public ManagedServer(BundleContext bundleContext, ServerInterfaceFactory serverFactory) {
		if (bundleContext == null) {
			throw new NullPointerException("BundleContext must not be null");
		}
		this.context = bundleContext;
		if (serverFactory != null) {
			this.serverFactory = serverFactory;
		} else {
			this.serverFactory= new ServerInterfaceFactory() {
				
				@Override
				public ServerInterface newServer(NetworkConfig config) {
					return new Server(config);
				}

				@Override
				public ServerInterface newServer(NetworkConfig config, int... ports) {
					return new Server(config, ports);
				}
			};
		}
	}

	/**
	 * Updates the configuration properties of the wrapped Californium server.
	 * 
	 * If the server is running when this method is called by ConfigAdmin, the server
	 * is destroyed, a new instance is created using the given properties and finally
	 * started.
	 *  
	 * @param properties the properties to set on the server
	 */
	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {

		LOGGER.fine("Updating configuration of managed server instance");
		
		if (isRunning()) {
			stop();
		}
		
		NetworkConfig networkConfig = NetworkConfig.createStandardWithoutFile();
		if (properties != null) {
			for (Enumeration<String> allKeys = properties.keys(); allKeys.hasMoreElements(); ) {
				String key = allKeys.nextElement();
				networkConfig.set(key, properties.get(key));
			}
		}
		
		int defaultPort = networkConfig.getInt(NetworkConfigDefaults.DEFAULT_COAP_PORT);		
		if ( defaultPort == 0 )
		{
			defaultPort = EndpointManager.DEFAULT_COAP_PORT;
		}
		
		// create server instance with default CoAP endpoint on specified port
		server = serverFactory.newServer(networkConfig, defaultPort);

		// add secure endpoint if configured
		int securePort = networkConfig.getInt(DEFAULT_COAPS_PORT);
		if ( securePort > 0 ) {
			if (getSecureConnectorFactory() != null) {
				LOGGER.fine(String.format("Adding secure endpoint on port %d", securePort));
				server.addEndpoint(new CoAPEndpoint(
						getSecureConnectorFactory().newConnector(new InetSocketAddress((InetAddress) null, securePort)),
						networkConfig));
			} else {
				LOGGER.warning("Secure endpoint has been configured in server properties but no secure ConnectorFactory has been set");
			}

		}
		
		server.setExecutor(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
		server.start();
		running = true;

		// start tracking resources registered by arbitrary bundles
		resourceTracker = new ServiceTracker<Resource, Resource>(context, Resource.class.getName(), this);
		resourceTracker.open();
	}
	
	private boolean isRunning() {
		return running;
	}
	
	/**
	 * Stops and destroys the managed server instance.
	 * 
	 * This method should be called by the {@code BundleActivator} that registered
	 * this managed service when the bundle is stopped.
	 */
	public void stop() {
		if (server != null) {
			LOGGER.fine("Destroying managed server instance");
			if (resourceTracker != null) {
				// stop tracking Resources
				resourceTracker.close();
			}
			
			server.destroy();
			running = false;
		}
	}

	/**
	 * Adds a Californium {@code Resource} to the managed Californium {@code Server}.
	 * 
	 * This method is invoked automatically by the {@code ServiceTracker} whenever
	 * a {@code Resource} is added to the OSGi service registry.
	 * 
	 * @param reference the {@code Resource} service that has been added
	 * @return the unmodified {@code Resource}
	 */
	@Override
	public Resource addingService(ServiceReference<Resource> reference) {
		Resource resource = context.getService(reference);
		LOGGER.fine(String.format("Adding resource [%s]", resource.getName()));
		if (resource != null) {
			server.add(resource);
		}
		return resource;
	}
	
	/**
	 * Removes a Californium {@code Resource} from the managed Californium {@code Server}.
	 * 
	 * This method is invoked automatically by the {@code ServiceTracker} whenever
	 * a {@code Resource} is removed from the OSGi service registry.
	 * 
	 * @param reference the reference to the {@code Resource} service that has been removed
	 * @param service the service object
	 */
	@Override
	public void removedService(ServiceReference<Resource> reference,
			Resource service) {
		LOGGER.fine(String.format("Removing resource [%s]", service.getName()));
		server.remove(service);
		context.ungetService(reference);
	}
	
	/**
	 * Does nothing as the Californium server does not need to be informed about
	 * updated service registration properties of a {@code Resource}.
	 * 
	 * @param reference the updated {@code Resource} service reference
	 * @param service the corresponding {@code Resource} instance
	 */
	@Override
	public void modifiedService(ServiceReference<Resource> reference,
			Resource service) {
		// nothing to do
	}

	/**
	 * Gets the factory to use for creating {@link Connector} instances for secure endpoints.
	 * 
	 * @return the factory or <code>null</code> if no factory has been configured 
	 */
	public ConnectorFactory getSecureConnectorFactory() {
		return secureConnectorFactory;
	}

	/**
	 * Sets the factory to use for creating {@link Connector} instances for secure endpoints.
	 * 
	 * @param the factory or <code>null</code> if secure endpoints are not to be supported 
	 */
	public void setSecureConnectorFactory(ConnectorFactory secureConnectorFactory) {
		this.secureConnectorFactory = secureConnectorFactory;
	}
}
