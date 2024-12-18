/**
 * 
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 by Andrew D. King
 */ 

package programmingtheiot.part03.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.*;
import programmingtheiot.gda.connection.*;

/**
 The `MqttClientControlPacketTest` class contains integration tests for the `MqttClientConnector`, verifying its functionality for connecting to and disconnecting from an MQTT server, as well as publishing and subscribing to topics. It provides a structured approach for testing essential MQTT client operations, ensuring that the client behaves as expected in a simulated environment.
 *
 */
public class MqttClientControlPacketTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientControlPacketTest.class.getName());
	
	
	// member var's
	
	private MqttClientConnector mqttClient = null;
	
	
	// test setup methods
	
	@Before
	public void setUp() throws Exception
	{
		this.mqttClient = new MqttClientConnector();
		assertNotNull(this.mqttClient);
	}
	
	@After
	public void tearDown() throws Exception
	{
		if (this.mqttClient != null) {
            this.mqttClient.disconnectClient();
        }
	}

	
	// test methods
	
	@Test
	public void testConnectAndDisconnect()
	{
		_Logger.info("Testing connect and disconnect...");
        
        boolean connected = this.mqttClient.connectClient();
        assertTrue(connected);
        
        boolean disconnected = this.mqttClient.disconnectClient();
        assertTrue(disconnected);
        
        _Logger.info("Connect and disconnect test completed.");
	}
	
	@Test
	public void testServerPing()
	{
		_Logger.info("Testing server connection...");
	
		// Connect to the server
		boolean connected = this.mqttClient.connectClient();
		assertTrue(connected);
		
		// Log connection status
		_Logger.info("Connected to the server.");
	
		try
		{
			// Sleep for twice the keep-alive interval
			Thread.sleep(2 * this.mqttClient.getKeepAlive() * 1000); // Multiply by 1000 to convert to milliseconds
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	
		// Check if the client is still connected
		boolean isStillConnected = this.mqttClient.isConnected(); // Ensure this method exists
		assertTrue(isStillConnected);
	
		// Disconnect from the server
		boolean disconnected = this.mqttClient.disconnectClient();
		assertTrue(disconnected);
	
		_Logger.info("Server connection test completed.");
	}
	
	@Test
	public void testPubSub()
	{
		_Logger.info("Testing publish and subscribe...");
        
        boolean connected = this.mqttClient.connectClient();
        assertTrue(connected);
        
        // Subscribe to a topic with QoS 1
        boolean subscribed = this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, 1);
        assertTrue(subscribed);
        
        // Publish to a topic with QoS 2
        boolean published = this.mqttClient.publishMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, "Test message", 2);
        assertTrue(published);
        
        // Unsubscribe from the topic
        boolean unsubscribed = this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
        assertTrue(unsubscribed);
        
        boolean disconnected = this.mqttClient.disconnectClient();
        assertTrue(disconnected);
        
        _Logger.info("Publish and subscribe test completed.");
    }
	}
	

