/**
* 
* This class is part of the Programming the Internet of Things
* project, and is available via the MIT License, which can be
* found in the LICENSE file at the top level of this repository.
* 
* Copyright (c) 2020 by Andrew D. King
*/
 
package programmingtheiot.part04.integration.connection;
 
import static org.junit.Assert.*;
 
import java.util.List;
import java.util.logging.Logger;
 
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.app.DeviceDataManager;
import programmingtheiot.gda.connection.*;
 
/**
* This test case class contains very basic integration tests for
* CloudClientConnector. It should not be considered complete,
* but serve as a starting point for the student implementing
* additional functionality within their Programming the IoT
* environment.
*
*/
public class CloudClientConnectorTest
{
	// static
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnectorTest.class.getName());

	// member var's
	private List<ICloudClient> cloudClientList = null;
	private ICloudClient cloudClient = null;

	// test setup methods
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.cloudClient = new CloudClientConnector();
	}
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		if (this.cloudClient != null) {
			this.cloudClient.disconnectClient();
			this.cloudClient = null;
		}
	}
	// test methods
	/**
	 * Test method for {@link programmingtheiot.gda.connection.UbidotsMqttCloudClientConnector#connectClient()}.
	 */
	@Test
	public void testCloudClientConnectAndDisconnect()
	{
		this.cloudClient.setDataMessageListener(new DefaultDataMessageListener());
	    try {
	        //assertTrue(this.cloudClient.connectClient());
	    	this.cloudClient.connectClient();
	        _Logger.info("Connected to the cloud.");
	        try {
	            // Sleep for a minute or so...
	            Thread.sleep(60000L);
	        } catch (InterruptedException e) {
	            // Ignore interrupted exception
	            _Logger.warning("Sleep interrupted: " + e.getMessage());
	        }
	        //assertTrue(this.cloudClient.disconnectClient());
	        this.cloudClient.disconnectClient();
	        _Logger.info("Disconnected from the cloud.");
	    } catch (Exception e) {
	        _Logger.warning("Exception during cloud client test: " + e.getMessage());
	        e.printStackTrace();
	        fail("Test failed: " + e.getMessage());
	    }
	    _Logger.info("Test complete.");
	}
	@Test
public void testPublishSensorDataToCloud() {
    CloudClientConnector cloudClient = new CloudClientConnector();
    cloudClient.connectClient();
 
    // Generate a sample SensorData message
    SensorData sensorData = new SensorData();
    sensorData.setName("Temperature");
    sensorData.setValue(25.5f);
    // sensorData.setTimeStamp(System.currentTimeMillis());
 
    // Publish the message
    boolean success = cloudClient.sendEdgeDataToCloud(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
 
    // Verify the message was published successfully
    assertTrue(success);
 
    // Verify within the cloud service (manual or automated verification)
    // Example: Check logs or cloud dashboard for received data
 
    cloudClient.disconnectClient();
}
 
@Test
public void testSubscribeToLedActuatorEvents() throws InterruptedException {
    CloudClientConnector cloudClient = new CloudClientConnector();
    try {
        assertTrue(cloudClient.connectClient());
 
        // Subscribe to LED actuator event topic
        boolean subscribed = cloudClient.subscribeToCloudEvents(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE);
        assertTrue(subscribed);
 
        // Run for 5 minutes (300 seconds)
        long endTime = System.currentTimeMillis() + 300000;
        while (System.currentTimeMillis() < endTime) {
            // Generate SensorData messages to trigger actuation events
            SensorData sensorData = new SensorData();
            sensorData.setName("Temperature");
            sensorData.setValue(75.0f); // Assume this value crosses a threshold
            // sensorData.setTimeStamp(System.currentTimeMillis());
 
            boolean success = cloudClient.sendEdgeDataToCloud(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensorData);
            assertTrue(success);
 
            // Sleep for 30 seconds before sending the next reading
            Thread.sleep(30000);
        }
 
        // NOTE: Verification steps
        // 1. Manually check logs or cloud dashboard for actuator commands
        // 2. Implement a mock IDataMessageListener to validate incoming messages
    } finally {
        cloudClient.disconnectClient();
    }
}
 
 
@Test
public void testEndToEndIntegration() throws InterruptedException {
	DeviceDataManager ddm = new DeviceDataManager();
	ddm.startManager();
	try {
		// sleep for a minute or so...
		Thread.sleep(60000L);
	} catch (Exception e) {
		// ignore
	}
	ddm.stopManager();
	_Logger.info("Test complete.");
}
 
}