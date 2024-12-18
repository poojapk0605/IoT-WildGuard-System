
package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;


/**
 * This code defines a **CoAP resource handler** for managing telemetry data updates, allowing clients to interact with the resource through CoAP methods (currently unimplemented), and providing a mechanism to set a data message listener for processing telemetry data.
 *
 */
public class UpdateTelemetryResourceHandler extends CoapResource
{
	// static
	
    private IDataMessageListener dataMsgListener = null;
	private static final Logger _Logger =
		Logger.getLogger(UpdateTelemetryResourceHandler.class.getName());
	
	// params
	
	
	// constructors
	
	/**
	 * Constructor.
	 * 
	 * @param resource Basically, the path (or topic)
	
	
	/**
	 * Constructor.
	 * 
	 * @param resourceName The name of the resource.
	 */
	public UpdateTelemetryResourceHandler(String resourceName)
	{
		super(resourceName);
	}
	
	
	// public methods
	
	@Override
	public void handleDELETE(CoapExchange context)
	{
	}
	
	@Override
	public void handleGET(CoapExchange context)
	{
	}
	
	@Override
	public void handlePOST(CoapExchange context)
	{
	}
	
	@Override
	public void handlePUT(CoapExchange context)
	{
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
            this.dataMsgListener = listener;
        }
	}
}
	

