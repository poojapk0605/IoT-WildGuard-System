package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;


/**
 *This code defines a **CoAP resource handler** for managing actuator command data in an IoT application, allowing clients to interact with actuator data via CoAP methods (`GET`, `POST`, `PUT`, `DELETE`), while supporting data updates and client notifications through observability.
 *
 */
public class GetActuatorCommandResourceHandler extends CoapResource implements IActuatorDataListener
{
	// static
private ActuatorData actuatorData = null;
	private static final Logger _Logger =
		Logger.getLogger(GetActuatorCommandResourceHandler.class.getName());
	
	// params
	
	
	// constructors
	
	/**
	 * Constructor.
	 * 
	 * @param resource Basically, the path (or topic)
	 */

	
	/**
	 * Constructor.
	 * 
	 * @param resourceName The name of the resource.
	 */
	public GetActuatorCommandResourceHandler(String resourceName)
	{
		super(resourceName);
        super.setObservable(true);
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
       
	}
	public boolean onActuatorDataUpdate(ActuatorData data)
{
	if (data != null && this.actuatorData != null) {
		this.actuatorData.updateData(data);
		
		// notify all connected clients
		super.changed();
		
		_Logger.fine("Actuator data updated for URI: " + super.getURI() + ": Data value = " + this.actuatorData.getValue());
		
		return true;
	}
	
	return false;
}

}
