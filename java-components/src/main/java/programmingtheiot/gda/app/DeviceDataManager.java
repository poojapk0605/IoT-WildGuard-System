/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

 package programmingtheiot.gda.app;

 import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.BaseIotData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;
 /**
Here’s the updated description with the added line regarding MQTT:

The `DeviceDataManager` class manages connections to various components like MQTT, CoAP, cloud clients, and system performance monitoring. It handles incoming data, such as sensor and system performance messages, and starts/stops these components based on configuration settings, including managing the MQTT client's connection and subscription to relevant topics.  *
  */
 public class DeviceDataManager implements IDataMessageListener
 {
	 // static
	 
	 private static final Logger _Logger =
		 Logger.getLogger(DeviceDataManager.class.getName());
	 
	 // private var's
	 
	 private boolean enableMqttClient = true;
	 private boolean enableCoapServer = false;
	 private boolean enableCloudClient = false;
	 private boolean enableSmtpClient = false;
	 private boolean enablePersistenceClient = false;
	 private boolean enableSystemPerf = false;
	 private ICloudClient cloudClient;

	 private IActuatorDataListener actuatorDataListener = null;
	 private IPubSubClient mqttClient = null;
	//  private IPubSubCLient cloudClient = null;
	 private IPersistenceClient persistenceClient = null;
	 private IRequestResponseClient smtpClient = null;
	 private CoapServerGateway coapServer = null;
	 private SystemPerformanceManager sysPerfMgr = null;

	 private ActuatorData   latestHumidifierActuatorData = null;
	 private ActuatorData   latestHumidifierActuatorResponse = null;
	 private SensorData     latestHumiditySensorData = null;
	 private OffsetDateTime latestHumiditySensorTimeStamp = null;

	 private boolean handleHumidityChangeOnDevice = false; // optional
	 private int     lastKnownHumidifierCommand   = ConfigConst.OFF_COMMAND;

	// TODO: Load these from PiotConfig.props
	 private long    humidityMaxTimePastThreshold = 300; // seconds
	 private float   nominalHumiditySetting   = 40.0f;
	 private float   triggerHumidifierFloor   = 30.0f;
	 private float   triggerHumidifierCeiling = 50.0f;
	 
	 // constructors
	 
	 public DeviceDataManager()
	 {
		 super();
		 ConfigUtil configUtil = ConfigUtil.getInstance();
	
	this.enableMqttClient =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
	
	this.enableCoapServer =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
	
	this.enableCloudClient =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
	
	this.enablePersistenceClient =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
	
	// TODO: add these to ConfigConst
	this.handleHumidityChangeOnDevice =
	configUtil.getBoolean(
		ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

	this.humidityMaxTimePastThreshold =
	configUtil.getInteger(
		ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

	this.nominalHumiditySetting =
	configUtil.getFloat(
		ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");

	this.triggerHumidifierFloor =
	configUtil.getFloat(
		ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");

	this.triggerHumidifierCeiling =
	configUtil.getFloat(
		ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

	// TODO: basic validation for timing - add other validators for remaining values
	if (this.humidityMaxTimePastThreshold < 10 || this.humidityMaxTimePastThreshold > 7200) {
	this.humidityMaxTimePastThreshold = 300;
	}	
	initManager();
		 
		//  initConnections();
	 }
	 
	 public DeviceDataManager(
		 boolean enableMqttClient,
		 boolean enableCoapClient,
		 boolean enableCloudClient,
		 boolean enableSmtpClient,
		 boolean enablePersistenceClient)
	 {
		 super();
		 initManager();
		//  initConnections();
	 }
	 
	 
	 // public methods
	 
	 @Override
	 public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	 {
		if (data != null) {
			_Logger.info("Handling actuator response: " + data.getName());
			
			// this next call is optional for now
			//this.handleIncomingDataAnalysis(resourceName, data);
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}
			
			return true;
		} else {
			return false;
		}
	 }
 
	 @Override
	 public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
	 {
		if (data != null) {
			// NOTE: Feel free to update this log message for debugging and monitoring
			_Logger.log(
				Level.INFO,
				"Actuator request received: {0}. Message: {1}",
				new Object[] {resourceName.getResourceName(), Integer.valueOf((data.getCommand()))});
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}
			
			// TODO: retrieve this from config file
			// 
			
			// TODO: you may want to implement some analysis logic here or
			// in a separate method to determine how best to handle incoming
			// ActuatorData before calling this.sendActuatorCommandtoCda()
			
			// Recall that this private method was implement in Lab Module 10
			// See PIOT-GDA-10-003 for details
			this.sendActuatorCommandtoCda(resourceName, data);
			
			return true;
		} else {
			_Logger.warning("Received null ActuatorData. Ignoring request.");
			return false;
		}
	 }
 
	 @Override
	 public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
		
		if (resourceName != null && msg != null) {
			try {
				if (resourceName == ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE) {
					_Logger.info("Handling incoming ActuatorData message: " + msg);
					
					// NOTE: it may seem wasteful to convert to ActuatorData and back while
					// the JSON data is already available; however, this provides a validation
					// scheme to ensure the data is actually an 'ActuatorData' instance
					// prior to sending off to the CDA
					ActuatorData ad = DataUtil.getInstance().jsonToActuatorData(msg);
					String jsonData = DataUtil.getInstance().actuatorDataToJson(ad);
					
					if (this.mqttClient != null) {
						// TODO: retrieve the QoS level from the configuration file
						_Logger.fine("Publishing data to MQTT broker: " + jsonData);
						return this.mqttClient.publishMessage(resourceName, jsonData, 0);
					}
					
					// TODO: If the GDA is hosting a CoAP server (or a CoAP client that
					// will connect to the CDA's CoAP server), you can add that logic here
					// in place of the MQTT client or in addition
					
				} else {
					_Logger.warning("Failed to parse incoming message. Unknown type: " + msg);
					
					return false;
				}
			} catch (Exception e) {
				_Logger.log(Level.WARNING, "Failed to process incoming message for resource: " + resourceName, e);
			}
		} else {
			_Logger.warning("Incoming message has no data. Ignoring for resource: " + resourceName);
		}
		
		return false;
	 }
	 
         
					
	
 
	 @Override
	 public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	 {
		if (data != null) {
			_Logger.info("Handling sensor message: " + data.getName());
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}
			String jsonData = DataUtil.getInstance().sensorDataToJson(data);
		
		_Logger.fine("JSON [SensorData] -> " + jsonData);
		
		// TODO: retrieve this from config file
		int qos = ConfigConst.DEFAULT_QOS;
		
		if (this.enablePersistenceClient && this.persistenceClient != null) {
			this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
		}
		
		this.handleIncomingDataAnalysis(resourceName, data);
		
		this.handleUpstreamTransmission(resourceName, jsonData, qos);
			
			return true;
		} else {
			return false;
		}
	
	 }

	 private boolean handleUpstreamTransmission(ResourceNameEnum resource, String jsonData, int qos)
	 {
		if (this.cloudClient != null) {
			try {
				boolean success = false;
	
				// Use a switch-case to handle different resource types
				switch (resource) {
					case CDA_SENSOR_MSG_RESOURCE:
						SensorData sensorData = DataUtil.getInstance().jsonToSensorData(jsonData);
						success = this.cloudClient.sendEdgeDataToCloud(resource, sensorData);
						break;
	
					case CDA_SYSTEM_PERF_MSG_RESOURCE:
						SystemPerformanceData systemPerfData = DataUtil.getInstance().jsonToSystemPerformanceData(jsonData);
						success = this.cloudClient.sendEdgeDataToCloud(resource, systemPerfData);
						break;
					
					case GDA_SYSTEM_PERF_MSG_RESOURCE:
						SystemPerformanceData systemPerfData1 = DataUtil.getInstance().jsonToSystemPerformanceData(jsonData);
						success = this.cloudClient.sendEdgeDataToCloud(resource, systemPerfData1);
						break;
	
					default:
						_Logger.warning("Unhandled resource type: " + resource);
				}
	
				if (success) {
					_Logger.fine("Successfully sent data upstream to CSP. Resource: " + resource);
				} else {
					_Logger.warning("Failed to send data to CSP. Resource: " + resource + ", Data: " + jsonData);
				}
			} catch (Exception e) {
				_Logger.log(Level.SEVERE, "Exception occurred while sending data to cloud service.", e);
			}
		} else {
			_Logger.warning("Cloud client is not initialized. Cannot send data to cloud service.");
		}
		_Logger.info("handling upstream transmission");
		       return true;
			}
		
	 

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, SensorData data)
{
	// check either resource or SensorData for type
	if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {
		handleHumiditySensorAnalysis(resource, data);
	}
	
}

private void handleHumiditySensorAnalysis(ResourceNameEnum resource, SensorData data)
{
	//
	// NOTE: INCOMPLETE and VERY BASIC CODE SAMPLE. Not intended to provide a solution.
	//
	
	_Logger.fine("Analyzing humidity data from CDA: " + data.getLocationID() + ". Value: " + data.getValue());
	
	boolean isLow  = data.getValue() < this.triggerHumidifierFloor;
	boolean isHigh = data.getValue() > this.triggerHumidifierCeiling;
	
	if (isLow || isHigh) {
		_Logger.fine("Humidity data from CDA exceeds nominal range.");
		
		if (this.latestHumiditySensorData == null) {
			// set properties then exit - nothing more to do until the next sample
			this.latestHumiditySensorData = data;
			this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);
			
			_Logger.fine(
				"Starting humidity nominal exception timer. Waiting for seconds: " +
				this.humidityMaxTimePastThreshold);
			
			return;
		} else {
			OffsetDateTime curHumiditySensorTimeStamp = getDateTimeFromData(data);
			
			long diffSeconds =
				ChronoUnit.SECONDS.between(
					this.latestHumiditySensorTimeStamp, curHumiditySensorTimeStamp);
			
			_Logger.fine("Checking Humidity value exception time delta: " + diffSeconds);
			
			if (diffSeconds >= this.humidityMaxTimePastThreshold) {
				ActuatorData ad = new ActuatorData();
				ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
				ad.setLocationID(data.getLocationID());
				ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
				ad.setValue(this.nominalHumiditySetting);
				
				if (isLow) {
					ad.setCommand(ConfigConst.ON_COMMAND);
				} else if (isHigh) {
					ad.setCommand(ConfigConst.OFF_COMMAND);
				}
				
				_Logger.info(
					"Humidity exceptional value reached. Sending actuation event to CDA: " +
					ad);
				
				this.lastKnownHumidifierCommand = ad.getCommand();
				sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ad);
				
				// set ActuatorData and reset SensorData (and timestamp)
				this.latestHumidifierActuatorData = ad;
				this.latestHumiditySensorData = null;
				this.latestHumiditySensorTimeStamp = null;
			}
		}
	} else if (this.lastKnownHumidifierCommand == ConfigConst.ON_COMMAND) {
		// check if we need to turn off the humidifier
		if (this.latestHumidifierActuatorData != null) {
			// check the value - if the humidifier is on, but not yet at nominal, keep it on
			if (this.latestHumidifierActuatorData.getValue() >= this.nominalHumiditySetting) {
				this.latestHumidifierActuatorData.setCommand(ConfigConst.OFF_COMMAND);
				
				_Logger.info(
					"Humidity nominal value reached. Sending OFF actuation event to CDA: " +
					this.latestHumidifierActuatorData);
				
				sendActuatorCommandtoCda(
					ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, this.latestHumidifierActuatorData);
				
				// reset ActuatorData and SensorData (and timestamp)
				this.lastKnownHumidifierCommand = this.latestHumidifierActuatorData.getCommand();
				this.latestHumidifierActuatorData = null;
				this.latestHumiditySensorData = null;
				this.latestHumiditySensorTimeStamp = null;
			} else {
				_Logger.fine("Humidifier is still on. Not yet at nominal levels (OK).");
			}
		} else {
			// shouldn't happen, unless some other logic
			// nullifies the class-scoped ActuatorData instance
			_Logger.warning(
				"ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");
		}
	}
}

private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data)
{
	if (this.actuatorDataListener != null) {
		this.actuatorDataListener.onActuatorDataUpdate(data);
	}
	if (this.enableMqttClient && this.mqttClient != null) {
		String jsonData = DataUtil.getInstance().actuatorDataToJson(data);
		if (this.mqttClient.publishMessage(resource, jsonData, ConfigConst.DEFAULT_QOS)) {
			_Logger.info(
				"Published ActuatorData humidifier command from GDA to CDA: " + data.getCommand());
		} else {
			_Logger.warning(
				"Failed to publish ActuatorData humidifier command from GDA to CDA: " + data.getCommand());
		}
	}
}
	
private OffsetDateTime getDateTimeFromData(BaseIotData data)
{
	OffsetDateTime odt = null;
	
	try {
		odt = OffsetDateTime.parse(data.getTimeStamp());
	} catch (Exception e) {
		_Logger.warning(
			"Failed to extract ISO 8601 timestamp from IoT data. Using local current time.");
		
		// TODO: this won't be accurate, but should be reasonably close, as the CDA will
		// most likely have recently sent the data to the GDA
		odt = OffsetDateTime.now();
	}
	
	return odt;
}
 
	 @Override
	 public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	 {
		if (data != null) {
			_Logger.info("Handling system performance message: " + data.getName());
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for SystemPerformanceData instance.");
			}
			int qos = ConfigConst.DEFAULT_QOS;
			String jsonData = DataUtil.getInstance().systemPerformanceDataToJson(data);
			_Logger.fine("JSON [SystemPerformanceData] -> " + jsonData);
		
						this.handleUpstreamTransmission(resourceName, jsonData, qos);
			
			return true;
		} else {
			return false;
		}
	 }
	 
	 public void setActuatorDataListener(String name, IActuatorDataListener listener)
	 {
		if (listener != null) {
			// for now, just ignore 'name' - if you need more than one listener,
			// you can use 'name' to create a map of listener instances
			this.actuatorDataListener = listener;
		}
	 }
	 
	 public void startManager()
	 {
		_Logger.info("DeviceDataManager is starting...");

		if (this.mqttClient != null) {
			if (this.mqttClient.connectClient()) {
				_Logger.info("Successfully connected MQTT client to broker.");
				
				// add necessary subscriptions
				
				// TODO: read this from the configuration file
				// int qos = ConfigConst.DEFAULT_QOS;
				
				// // TODO: check the return value for each and take appropriate action
				
				// // IMPORTANT NOTE: The 'subscribeToTopic()' method calls shown
				// // below will be moved to MqttClientConnector.connectComplete()
				// // in Lab Module 10. For now, they can remain here.
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
				this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, ConfigConst.DEFAULT_QOS);
				this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ConfigConst.DEFAULT_QOS);
				_Logger.info("Subscribed to CDA sensor and actuator topics.");
			} else {
				_Logger.severe("Failed to connect MQTT client to broker.");
				
				// TODO: take appropriate action
			}
		}
        // Start System Performance Manager if enabled
        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.startManager();
            _Logger.info("SystemPerformanceManager started.");
		}

		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.startServer()) {
				_Logger.info("CoAP server started.");
			} else {
				_Logger.severe("Failed to start CoAP server. Check log file for details.");
			}
		}
		if (this.cloudClient != null) {
			_Logger.info("Starting CloudClientConnector...");
			this.cloudClient.connectClient();
				}
	 }
	 
	 public void stopManager()
	 {
		_Logger.info("Stopping DeviceDataManager...");

        // Stop System Performance Manager if it was started
        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.stopManager();
            _Logger.info("SystemPerformanceManager stopped.");
		}
		if (this.mqttClient != null) {
			// add necessary un-subscribes
			
			// TODO: check the return value for each and take appropriate action
			
			// NOTE: The unsubscribeFromTopic() method calls below should match with
			// the subscribeToTopic() method calls from startManager(). Also, the
			// unsubscribe logic below can be moved to MqttClientConnector's
			// disconnectClient() call PRIOR to actually disconnecting from
			// the MQTT broker.
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
			
			if (this.mqttClient.disconnectClient()) {
				_Logger.info("Successfully disconnected MQTT client from broker.");
			} else {
				_Logger.severe("Failed to disconnect MQTT client from broker.");
				
				// TODO: take appropriate action
			}
		}
		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.stopServer()) {
				_Logger.info("CoAP server stopped.");
			} else {
				_Logger.severe("Failed to stop CoAP server. Check log file for details.");
			}
		}

		if (this.cloudClient != null) {
			_Logger.info("Stopping CloudClientConnector...");
			if (this.cloudClient.disconnectClient()) {
				_Logger.info("CloudClientConnector disconnected successfully.");
			} else {
				_Logger.warning("Failed to disconnect CloudClientConnector.");
			}
		}
	 }
 
	 
	 // private methods
	 
	 /**
	  * Initializes the enabled connections. This will NOT start them, but only create the
	  * instances that will be used in the {@link #startManager() and #stopManager()) methods.
	  * 
	  */
	//  private void initConnections()
	//  {
	//  }
	private void initManager()
	{
		ConfigUtil configUtil = ConfigUtil.getInstance();
		
		this.enableSystemPerf =
			configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,  ConfigConst.ENABLE_SYSTEM_PERF_KEY);
		
		if (this.enableSystemPerf) {
			this.sysPerfMgr = new SystemPerformanceManager();
			this.sysPerfMgr.setDataMessageListener(this);
		}
		
		if (this.enableMqttClient) {
			this.mqttClient = new MqttClientConnector();
		
		// NOTE: The next line isn't technically needed until Lab Module 10
		this.mqttClient.setDataMessageListener(this);
		}
		
		if (this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
		}
		
		if (this.enableCloudClient) {
			this.cloudClient = new CloudClientConnector();
        _Logger.info("CloudClientConnector initialized.");
		this.cloudClient.setDataMessageListener(this);
		}
		
		if (this.enablePersistenceClient) {
			// TODO: implement this as an optional exercise in Lab Module 5
		}
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, ActuatorData data)
{
	_Logger.info("Analyzing incoming actuator data: " + data.getName());
	
	if (data.isResponseFlagEnabled()) {
		// TODO: implement this
	} else {
		if (this.actuatorDataListener != null) {
			this.actuatorDataListener.onActuatorDataUpdate(data);
		}
	}
}

public boolean sendActuationCommandToCDA(ResourceNameEnum resourceName, ActuatorData actuatorData) {
	try {
		if (actuatorData != null) {
			String jsonData = DataUtil.getInstance().actuatorDataToJson(actuatorData);

			_Logger.info("Forwarding ActuatorData to CDA: " + jsonData);

			// Publish ActuatorData to CDA via MQTT
			if (this.mqttClient != null) {
				int qos = ConfigUtil.getInstance()
					.getInteger(ConfigConst.GATEWAY_DEVICE, ConfigConst.DEFAULT_QOS_KEY); // Default QoS = 1

				boolean result = this.mqttClient.publishMessage(resourceName, jsonData, qos);
				if (result) {
					_Logger.info("ActuatorData successfully published to CDA.");
				} else {
					_Logger.warning("Failed to publish ActuatorData to CDA.");
				}
				return result;
			} else {
				_Logger.warning("MQTT client is not initialized. Cannot publish ActuatorData to CDA.");
			}
		} else {
			_Logger.warning("ActuatorData is null. Cannot forward to CDA.");
		}
	} catch (Exception e) {
		_Logger.log(Level.SEVERE, "Failed to forward ActuatorData to CDA.", e);
	}
	return false;
}
}
 