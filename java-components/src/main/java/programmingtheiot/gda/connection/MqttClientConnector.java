/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

 package programmingtheiot.gda.connection;

 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
 import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
 import org.eclipse.paho.client.mqttv3.MqttClient;
 import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
 import org.eclipse.paho.client.mqttv3.MqttException;
 import org.eclipse.paho.client.mqttv3.MqttMessage;
 import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
 import org.eclipse.paho.client.mqttv3.MqttSecurityException;
 import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

 import programmingtheiot.gda.connection.RedisAdapter;

import programmingtheiot.common.ConfigConst;
 import programmingtheiot.common.ConfigUtil;
 import programmingtheiot.common.IDataMessageListener;
 import programmingtheiot.common.ResourceNameEnum;

 import java.io.File;
 import javax.net.ssl.SSLSocketFactory;
 import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;

import programmingtheiot.common.SimpleCertManagementUtil;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
 
 /**The `MqttClientConnector` class serves as a client for connecting to an MQTT broker, allowing for operations such as publishing, subscribing, and managing messages with specified Quality of Service (QoS) levels. It includes features for handling connection events and callbacks, as well as logging relevant actions and errors during communication with the broker.
  * 
  */
 public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
 {
	 // static
 
	 private boolean useAsyncClient = true;
	 private MqttAsyncClient mqttClient = null;
	//  private MqttClient           mqttClient = null;
	 private MqttConnectOptions   connOpts = null;
	 private MemoryPersistence    persistence = null;
	 private IDataMessageListener dataMsgListener = null;
 
	 private String  clientID = null;
	 private String  brokerAddr = null;
	 private String  host = ConfigConst.DEFAULT_HOST;
	 private String  protocol = ConfigConst.DEFAULT_MQTT_PROTOCOL;
	 private int     port = ConfigConst.DEFAULT_MQTT_PORT;
	 private int     brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
	 
	 private String pemFileName = null;
	 private boolean enableEncryption = false;
	 private boolean useCleanSession = false;
	 private boolean enableAutoReconnect = true;
	 private RedisAdapter redisAdapter;
	 private IConnectionListener connListener = null;
	 private boolean useCloudGatewayConfig = false;
	 private static final Logger _Logger =
		 Logger.getLogger(MqttClientConnector.class.getName());
	 
	 // params
	 
	 
	 // constructors
	 
	 /**
	  * Default.
	  * 
	  */
	 public MqttClientConnector()
	 {this(false);
		//  super();
		 
		 ConfigUtil configUtil = ConfigUtil.getInstance();
		 initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
	 
	 this.host =
		 configUtil.getProperty(
			 ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
	 
	 this.port =
		 configUtil.getInteger(
			 ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
	 
	 this.brokerKeepAlive =
		 configUtil.getInteger(
			 ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
	 
	 // This next config file boolean property is optional; it can be
	 // set within the [Mqtt.GatewayService] and [Cloud.GatewayService]
	 // sections of PiotConfig.props. You can use it to create a logical
	 // flow within this class to determine whether to use MqttClient
	 // or MqttAsyncClient, or simply choose one of the two classes based
	 // on your usage needs. Generally speaking, MqttAsyncClient will
	 // be necessary when running the GDA as an application, as it will
	 // need to handle incoming and outgoing messages using MQTT
	 // simultaneously. For GDA-only testing using the test cases
	 // specified in this lab module and others, it's generally best -
	 // and likely required - to use MqttClient.
	 // 
	 // IMPORTANT: If you're using an older version of ConfigConst.java,
	 // you'll need to add the following line of code to ConfigConst.java:
	 // public static final String USE_ASYNC_CLIENT_KEY = "useAsyncClient";
	 this.useAsyncClient =
		 configUtil.getBoolean(
			 ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.USE_ASYNC_CLIENT_KEY);
	 
	 // NOTE: paho Java client requires a client ID - for now, you
	 // can use the generated client ID; for later exercises, you
	 // should define your own and load it from the config file
	 this.clientID = MqttClient.generateClientId();
	 
	 // these are specific to the MQTT connection which will be used during connect
	 this.persistence = new MemoryPersistence();
	 this.connOpts = new MqttConnectOptions();
	 
	 this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
	 
	 // NOTE: If using a random clientID for each new connection,
	 // clean session should be 'true'; see MQTT spec for details
	 this.connOpts.setCleanSession(true);
	 
	 // NOTE: Auto-reconnect can be a useful connection recovery feature
	 this.connOpts.setAutomaticReconnect(true);
	 // NOTE: URL does not have a protocol handler for "tcp",
	 // so we need to construct the URL manually
	//  this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;
 
	 }
	 public MqttClientConnector(boolean useCloudGatewayConfig)
{
	this(useCloudGatewayConfig ? ConfigConst.CLOUD_GATEWAY_SERVICE : null);
}
public MqttClientConnector(String cloudGatewayConfigSectionName)
{
	super();
	
	if (cloudGatewayConfigSectionName != null && cloudGatewayConfigSectionName.trim().length() > 0) {
		this.useCloudGatewayConfig = true;
		
		initClientParameters(cloudGatewayConfigSectionName);
	} else {
		this.useCloudGatewayConfig = false;
		
		// NOTE: This next method call should have already been created
		// in Lab Module 10. It is simply a delegate to handle parsing
		// of the appropriate configuration file section
		initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
	}
}
	 
	 // public methods
	 
	 @Override
	 public boolean connectClient()
	 {
		try {
			if (this.mqttClient == null) {
				// Initialize the MQTT client
				this.mqttClient = new MqttAsyncClient(this.brokerAddr, this.clientID, this.persistence);
				this.mqttClient.setCallback(this);
			}
	
			if (!this.mqttClient.isConnected()) {
				_Logger.info("MQTT client connecting to broker: " + this.brokerAddr);
				this.mqttClient.connect(this.connOpts);
	
				// Add artificial delay to ensure connection is established
				Thread.sleep(20); // Wait for 1 second
	
				if (this.mqttClient.isConnected()) {
					_Logger.info("MQTT client connected successfully to broker.");
				// 	if (this.redisAdapter == null) {
                //     this.redisAdapter = new RedisAdapter();
                // }
                // if (!this.redisAdapter.connectClient()) {
                //     _Logger.warning("Failed to connect RedisAdapter after MQTT client connection.");
                // }
					

					return true;
				} else {
					_Logger.warning("MQTT client connection to broker is not established yet.");
				}
			} else {
				_Logger.warning("MQTT client already connected to broker: " + this.brokerAddr);
			}
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Failed to connect MQTT client to broker.", e);
		} catch (InterruptedException e) {
			_Logger.log(Level.WARNING, "Thread interrupted during artificial delay.", e);
		}
	
		return false;
	 }
 
	 @Override
	 public boolean disconnectClient()
	 {
		 
		try {
			if (this.mqttClient != null) {
				if (this.mqttClient.isConnected()) {
					_Logger.info("Disconnecting MQTT client from broker: " + this.brokerAddr);
					this.mqttClient.disconnect();
	
					// Add artificial delay to ensure disconnection is complete
					Thread.sleep(20); // Wait for 1 second
	
					if (!this.mqttClient.isConnected()) {
						_Logger.info("MQTT client disconnected successfully from broker.");
						return true;
					} else {
						_Logger.warning("MQTT client is still connected after attempting to disconnect.");
					}
				} else {
					_Logger.warning("MQTT client not connected to broker: " + this.brokerAddr);
				}
			}
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Failed to disconnect MQTT client from broker: " + this.brokerAddr, e);
		} catch (InterruptedException e) {
			_Logger.log(Level.WARNING, "Thread interrupted during artificial delay.", e);
		}
	
		return false;
	 }
 
	 public boolean isConnected()
	 {
		 // TODO: this logic for use with the synchronous `MqttClient` instance only
	 return (this.mqttClient != null && this.mqttClient.isConnected());
		 
	 }
	 
	 



	 @Override
	 public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	 {
		 _Logger.info("publishMessage called with topic: " + topicName + ", message: " + msg + ", QoS: " + qos);
		// TODO: determine how verbose your logging should be, especially if this method is called often
	 if (topicName == null) {
		 _Logger.warning("Resource is null. Unable to publish message: " + this.brokerAddr);
		 return false;
	 }
	 
	 if (msg == null || msg.length() == 0) {
		 _Logger.warning("Message is null or empty. Unable to publish message: " + this.brokerAddr);
		 return false;
	 }
	 
	//  if (qos < 0 || qos > 2) {
	// 	 qos = ConfigConst.DEFAULT_QOS;
	//  }
	 
	//  try {
	// 	 byte[] payload = msg.getBytes();
	// 	 MqttMessage mqttMsg = new MqttMessage(payload);
	// 	 mqttMsg.setQos(qos);
	// 	 this.mqttClient.publish(topicName.getResourceName(), mqttMsg);
	// 	 return true;
	//  } catch (Exception e) {
	// 	 _Logger.log(Level.SEVERE, "Failed to publish message to topic: " + topicName, e);
	//  }
	 
	return publishMessage(topicName.getResourceName(), msg.getBytes(), qos);
		 
	 }
 
	 @Override
	 public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	 { 
		//  _Logger.info("subscribeToTopic called with topic: " + topicName + ", QoS: " + qos);
		 if (topicName == null) {
			 _Logger.warning("Resource is null. Unable to subscribe to topic: " + this.brokerAddr);
			 return false;
		 }
		 return subscribeToTopic(topicName.getResourceName(), qos);
		 
		//  if (qos < 0 || qos > 2) {
		// 	 qos = ConfigConst.DEFAULT_QOS;
		//  }
		 
		//  try {
		// 	 this.mqttClient.subscribe(topicName.getResourceName(), qos);
		// 	 _Logger.info("Successfully subscribed to topic: " + topicName.getResourceName());
		// 	 return true;
		//  } catch (Exception e) {
		// 	 _Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName, e);
		//  }
		//  return false;
		// try {
		// 	if (this.mqttClient != null && this.mqttClient.isConnected()) {
		// 		_Logger.info("Subscribing to topic: " + topicName.getResourceName() + " with QoS " + qos);
		// 		this.mqttClient.subscribe(topicName.getResourceName(), qos);
		// 		return true; // Indicate success
		// 	} else {
		// 		_Logger.warning("Unable to subscribe to topic - MQTT client is not connected.");
		// 	}
		// } catch (Exception e) {
		// 	_Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName, e);
		// }
		// return false; 
	 }
 
	 @Override
	 public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	 { 
		 _Logger.info("unsubscribeFromTopic called with topic: " + topicName);
		 if (topicName == null) {
			 _Logger.warning("Resource is null. Unable to unsubscribe from topic: " + this.brokerAddr);
			 return false;
		 }
		 
		//  try {
		// 	 this.mqttClient.unsubscribe(topicName.getResourceName());
		// 	 _Logger.info("Successfully unsubscribed from topic: " + topicName.getResourceName());
		// 	 return true;
		//  } catch (Exception e) {
		// 	 _Logger.log(Level.SEVERE, "Failed to unsubscribe from topic: " + topicName, e);
		//  }
		 return  unsubscribeFromTopic(topicName.getResourceName());
	 }
 
	 @Override
	 public boolean setConnectionListener(IConnectionListener listener)
	 {
		if (listener != null) {
			_Logger.info("Setting connection listener.");
			this.connListener = listener;
			return true;
		} else {
			_Logger.warning("No connection listener specified. Ignoring.");
		}
		 return false;
	 }
	 
	 @Override
	 public boolean setDataMessageListener(IDataMessageListener listener)
	 {
		 if (listener != null) {
			 this.dataMsgListener = listener;
			 return true;
		 }
		 
		 return false;
		 
	 }
	 
	 // callbacks
	 
	 @Override
	 public void connectComplete(boolean reconnect, String serverURI)
	 
	 {
	// 	_Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " + serverURI);
    
    // int qos = 1;
    
    // // Option 1: Conditional subscription based on useCloudGatewayConfig
    // if (!this.useCloudGatewayConfig) {
    //     // Subscribe to specific topics
    //     if (!this.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos)) {
    //         _Logger.warning("Failed to subscribe to CDA_ACTUATOR_RESPONSE_RESOURCE.");
    //     }
    //     if (!this.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos)) {
    //         _Logger.warning("Failed to subscribe to CDA_SENSOR_MSG_RESOURCE.");
    //     }
    //     if (!this.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos)) {
    //         _Logger.warning("Failed to subscribe to CDA_SYSTEM_PERF_MSG_RESOURCE.");
    //     }

    //     // Important: Message parsing will be handled in the `messageArrived` method
    // }

    // // Notify listener about successful connection
    // if (this.connListener != null) {
    //     this.connListener.onConnect();
    // }

// _Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " + serverURI);

//     int qos = ConfigConst.DEFAULT_QOS;

//     // Option 1: Subscribe to topics based on useCloudGatewayConfig
//     if (!this.useCloudGatewayConfig) {
//         try {
//             _Logger.info("Subscribing to local MQTT topics...");

//             // Subscribe to CDA Actuator Response topic
//             this.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);

//             // Subscribe to Sensor Data topic
//             this.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);

//             // Subscribe to System Performance Data topic
//             this.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);

//         } catch (Exception e) {
//             _Logger.severe("Failed to subscribe to local MQTT topics: " + e.getMessage());
//         }
//     } else {
//         try {
//             _Logger.info("Subscribing to cloud-specific topics...");

//             // // Add cloud-specific topic subscriptions here
//             //  this.subscribeToTopic(ResourceNameEnum.CLOUD_ACTUATOR_RESPONSE_RESOURCE, qos);

//         } catch (Exception e) {
//             _Logger.severe("Failed to subscribe to cloud MQTT topics: " + e.getMessage());
//         }
//     }

//     // Notify listener that connection is complete
//     if (this.connListener != null) {
//         this.connListener.onConnect();
	
// 	}
_Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " +serverURI);
 
        int qos = ConfigConst.DEFAULT_QOS;
 
        this.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
        this.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
        this.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
 
        try {
            if(!this.useCloudGatewayConfig){
                _Logger.info("Subscribing to topic: " + ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE.getResourceName());
 
                this.mqttClient.subscribe(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE.getResourceName(), qos);
            }
        } catch (Exception e) {
            _Logger.warning("Failed to connect to CDA Actuator response topic");
        }
 
        if(this.connListener != null)
        {
			
            this.connListener.onConnect();
		}
 }
		 
	 
 
	 @Override
	 public void connectionLost(Throwable cause)
	 {
		_Logger.warning("Lost connection to MQTT broker: " + brokerAddr);
		_Logger.log(Level.WARNING, "Reason for connection lost: ", cause);
		
		// Reconnect logic
		try {
			_Logger.info("Attempting to reconnect to broker...");
			this.connectClient();
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Reconnection attempt failed.", e);
		}
	 }
	 
	 @Override
	 public void deliveryComplete(IMqttDeliveryToken token)
	 {
		 // TODO: Logging level may need to be adjusted to see output in log file / console
	 _Logger.fine("Delivered MQTT message with ID: " + token.getMessageId());
	 }
	 
	 @Override
public void messageArrived(String topic, MqttMessage msg) throws Exception {
    _Logger.info("MQTT message arrived on topic: '" + topic + "'");

    if (this.dataMsgListener != null) {
        try {
            String payload = new String(msg.getPayload());

            // Handle ActuatorData messages
            if (ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE.getResourceName().equals(topic)) {
                ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(payload);
                if (actuatorData != null) {
                    _Logger.info("Received ActuatorData response: " + actuatorData.getValue());
                    this.dataMsgListener.handleActuatorCommandResponse(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, actuatorData);
					
                } else {
                    _Logger.warning("ActuatorData conversion returned null for topic: " + topic);
                }
            }
            // Handle SystemPerformanceData messages
            if (ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE.getResourceName().equals(topic)) {
                SystemPerformanceData systemPerformanceData = DataUtil.getInstance().jsonToSystemPerformanceData(payload);
                if (systemPerformanceData != null) {
                    _Logger.info("Received SystemPerformanceData response: " + systemPerformanceData.getName());
                    this.dataMsgListener.handleSystemPerformanceMessage(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, systemPerformanceData);
                } else {
                    _Logger.warning("SystemPerformanceData conversion returned null for topic: " + topic);
                }

            }
            // Handle SensorData messages
        	 if (ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE.getResourceName().equals(topic)) {
                SensorData sensorData = DataUtil.getInstance().jsonToSensorData(payload);
                if (sensorData != null) {
                    if (sensorData.getTypeID() == ConfigConst.TEMP_SENSOR_TYPE) {
                        _Logger.info("Received Temperature SensorData: " + sensorData.getValue());
                    }
                    this.dataMsgListener.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
					// redisAdapter.storeData(topic, 0, sensorData);
                } else {
                    _Logger.warning("SensorData conversion returned null for topic: " + topic);
                }
            }
            // Handle ActuatorCommand messages (new handling for your use case)
            if (ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE.getResourceName(). equals(topic)) {
                ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(payload);
                if (actuatorData != null) {
                    _Logger.info("Received Actuator Command: " + actuatorData.getValue());
                    this.dataMsgListener.handleActuatorCommandRequest(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, actuatorData);
                } else {
                    _Logger.warning("ActuatorCommand conversion returned null for topic: " + topic);
                }
            }
            // Unknown topic
            else {
                _Logger.warning("Received message for an unknown topic: " + topic + ". Payload: " + payload);
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to process message for topic: " + topic, e);
        }
    } else {
        _Logger.warning("No DataMessageListener registered. Ignoring the message.");
    }



}

	 
	
 
	 
	 // private methods
	 
	 /**
	  * Called by the constructor to set the MQTT client parameters to be used for the connection.
	  * 
	  * @param configSectionName The name of the configuration section to use for
	  * the MQTT client configuration parameters.
	  */
	 private void initClientParameters(String configSectionName)
	 {
		ConfigUtil configUtil = ConfigUtil.getInstance();
	
		this.host =
			configUtil.getProperty(
				configSectionName, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
		this.port =
			configUtil.getInteger(
				configSectionName, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
		this.brokerKeepAlive =
			configUtil.getInteger(
				configSectionName, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
		this.enableEncryption =
			configUtil.getBoolean(
				configSectionName, ConfigConst.ENABLE_CRYPT_KEY);
		this.pemFileName =
			configUtil.getProperty(
				configSectionName, ConfigConst.CERT_FILE_KEY);
		
		// This next config file boolean property is optional; it can be
		// set within the [Mqtt.GatewayService] and [Cloud.GatewayService]
		// sections of PiotConfig.props. You can use it to create a logical
		// flow within this class to determine whether to use MqttClient
		// or MqttAsyncClient, or simply choose one of the two classes based
		// on your usage needs. Generally speaking, MqttAsyncClient will
		// be necessary when running the GDA as an application, as it will
		// need to handle incoming and outgoing messages using MQTT
		// simultaneously. For GDA-only testing using the test cases
		// specified in this lab module and others, it's generally best -
		// and likely required - to use MqttClient.
		// 
		// IMPORTANT: If you're using an older version of ConfigConst.java,
		// you'll need to add the following line of code to ConfigConst.java:
		// public static final String USE_ASYNC_CLIENT_KEY = "useAsyncClient";
		this.useAsyncClient =
			configUtil.getBoolean(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.USE_ASYNC_CLIENT_KEY);
	
		// NOTE: updated from Lab Module 07 - attempt to load clientID from configuration file
		this.clientID =
			configUtil.getProperty(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.DEVICE_LOCATION_ID_KEY, MqttClient.generateClientId());
		
		// these are specific to the MQTT connection which will be used during connect
		this.persistence = new MemoryPersistence();
		this.connOpts    = new MqttConnectOptions();
		
		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		this.connOpts.setCleanSession(this.useCleanSession);
		this.connOpts.setAutomaticReconnect(this.enableAutoReconnect);
		
		// if encryption is enabled, try to load and apply the cert(s)
		if (this.enableEncryption) {
			initSecureConnectionParameters(configSectionName);
		
		// this.protocol = ConfigConst.DEFAULT_MQTT_SECURE_PROTOCOL;
        // this.port = configUtil.getInteger(
        //     configSectionName, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_MQTT_SECURE_PORT);
    }

    // Update brokerAddr after all parameters are set
    this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;
	_Logger.info("MQTT client connecting to broker: " + this.brokerAddr);

		
		// if there's a credential file, try to load and apply them
		if (configUtil.hasProperty(configSectionName, ConfigConst.CRED_FILE_KEY)) {
			initCredentialConnectionParameters(configSectionName);
		}
		
		// NOTE: URL does not have a protocol handler for "tcp" or "ssl",
		// so construct the URL manually
		// this.brokerAddr  = this.protocol + "://" + this.host + ":" + this.port;
		
		_Logger.info("Using URL for broker conn: " + this.brokerAddr);
	 }
	 
	 /**
	  * Called by {@link #initClientParameters(String)} to load credentials.
	  * 
	  * @param configSectionName The name of the configuration section to use for
	  * the MQTT client configuration parameters.
	  */
	 private void initCredentialConnectionParameters(String configSectionName)
	 {
		ConfigUtil configUtil = ConfigUtil.getInstance();
	
		try {
			_Logger.info("Checking if credentials file exists and is loadable...");
			
			Properties props = configUtil.getCredentials(configSectionName);
			
			if (props != null) {
				this.connOpts.setUserName(props.getProperty(ConfigConst.USER_NAME_TOKEN_KEY, ""));
				this.connOpts.setPassword(props.getProperty(ConfigConst.USER_AUTH_TOKEN_KEY, "").toCharArray());
				
				_Logger.info("Credentials now set.");
			} else {
				_Logger.warning("No credentials are set.");
			}
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "Credential file non-existent. Disabling auth requirement.");
		}
	 }
	 
	 /**
	  * Called by {@link #initClientParameters(String)} to enable encryption.
	  * 
	  * @param configSectionName The name of the configuration section to use for
	  * the MQTT client configuration parameters.
	  */
	 private void initSecureConnectionParameters(String configSectionName)
	 {
		ConfigUtil configUtil = ConfigUtil.getInstance();
	
	try {
		_Logger.info("Configuring TLS...");
		
		if (this.pemFileName != null) {
			File file = new File(this.pemFileName);
			
			if (file.exists()) {
				_Logger.info("PEM file valid. Using secure connection: " + this.pemFileName);
			} else {
				this.enableEncryption = false;
				
				_Logger.log(Level.WARNING, "PEM file invalid. Using insecure connection: " + this.pemFileName, new Exception());
				
				return;
			}
		}
		
		SSLSocketFactory sslFactory =
			SimpleCertManagementUtil.getInstance().loadCertificate(this.pemFileName);
		
		this.connOpts.setSocketFactory(sslFactory);
		
		// override current config parameters
		this.port =
			configUtil.getInteger(
				configSectionName, ConfigConst.SECURE_PORT_KEY);
		
		this.protocol = ConfigConst.DEFAULT_MQTT_SECURE_PROTOCOL;
		
		_Logger.info("TLS enabled.");
	} catch (Exception e) {
		_Logger.log(Level.SEVERE, "Failed to initialize secure MQTT connection. Using insecure connection.", e);
		
		this.enableEncryption = false;
	}	 
	}
 
	 public int getKeepAlive() {
		 ConfigUtil configUtil = ConfigUtil.getInstance();
		 return configUtil.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
	 }

	 //Protected methods for cloud integration
	 protected boolean publishMessage(String topicName, byte[] payload, int qos)
{
	if (topicName == null) {
		_Logger.warning("Resource is null. Unable to publish message: " + this.brokerAddr);
		
		return false;
	}
	
	if (payload == null || payload.length == 0) {
		_Logger.warning("Message is null or empty. Unable to publish message: " + this.brokerAddr);
		
		return false;
	}
	
	if (qos < 0 || qos > 2) {
		_Logger.warning("Invalid QoS. Using default. QoS requested: " + qos);
		
		// TODO: retrieve default QoS from config file
		qos = ConfigConst.DEFAULT_QOS;
	}
	
	try {
		MqttMessage mqttMsg = new MqttMessage();
		mqttMsg.setQos(qos);
		mqttMsg.setPayload(payload);
		
		this.mqttClient.publish(topicName, mqttMsg);
		
		return true;
	} catch (Exception e) {
		_Logger.log(Level.SEVERE, "Failed to publish message to topic: " + topicName, e);
	}
	
	return false;
}

protected boolean subscribeToTopic(String topicName, int qos)
{
	return subscribeToTopic(topicName, qos, null);
}

protected boolean subscribeToTopic(String topicName, int qos, IMqttMessageListener listener)
{
	// NOTE: This is the preferred method for subscribing to a given topic,
	// as it allows the use of an IMqttMessageListener to be defined and
	// registered as the handler for incoming messages pertaining to the
	// given topic 'topicName'.

	if (topicName == null) {
		_Logger.warning("Resource is null. Unable to subscribe to topic: " + this.brokerAddr);
		
		return false;
	}
	
	if (qos < 0 || qos > 2) {
		_Logger.warning("Invalid QoS. Using default. QoS requested: " + qos);
		
		// TODO: retrieve default QoS from config file
		qos = ConfigConst.DEFAULT_QOS;
	}
	
	try {
		if (listener != null) {
			this.mqttClient.subscribe(topicName, qos, listener);
			
			_Logger.info("Successfully subscribed to topic with listener: " + topicName);
		} else {
			this.mqttClient.subscribe(topicName, qos);
			
			_Logger.info("Successfully subscribed to topic: " + topicName);
		}
		
		return true;
	} catch (Exception e) {
		_Logger.log(Level.SEVERE, "Failed to subscribe to topic: " + topicName, e);
	}
	
	return false;
}

protected boolean unsubscribeFromTopic(String topicName)
{
	if (topicName == null) {
		_Logger.warning("Resource is null. Unable to unsubscribe from topic: " + this.brokerAddr);
		
		return false;
	}
	
	try {
		this.mqttClient.unsubscribe(topicName);
		
		_Logger.info("Successfully unsubscribed from topic: " + topicName);
		
		return true;
	} catch (Exception e) {
		_Logger.log(Level.SEVERE, "Failed to unsubscribe from topic: " + topicName, e);
	}
	
	return false;
}


	
}