import logging
import unittest

from time import sleep

import programmingtheiot.common.ConfigConst as ConfigConst

from programmingtheiot.cda.connection.MqttClientConnector import MqttClientConnector
from programmingtheiot.common.ConfigUtil import ConfigUtil
from programmingtheiot.common.ResourceNameEnum import ResourceNameEnum
from programmingtheiot.common.DefaultDataMessageListener import DefaultDataMessageListener
from programmingtheiot.data.ActuatorData import ActuatorData
from programmingtheiot.data.SensorData import SensorData 
from programmingtheiot.data.SystemPerformanceData import SystemPerformanceData 
from programmingtheiot.data.DataUtil import DataUtil

# The `MqttClientControlPacketTest` class performs unit tests on the `MqttClientConnector`, verifying connectivity, publish/subscribe functionality, and server ping responsiveness for MQTT communication. It ensures the client operates correctly with different QoS levels and handles connections as expected.The `MqttClientControlPacketTest` class performs unit tests on the `MqttClientConnector`, verifying connectivity, publish/subscribe functionality, and server ping responsiveness for MQTT communication. It ensures the client operates correctly with different QoS levels and handles connections as expected.
class MqttClientControlPacketTest(unittest.TestCase):
	@classmethod
	def setUpClass(cls):
		logging.basicConfig(format = '%(asctime)s:%(module)s:%(levelname)s:%(message)s', level = logging.DEBUG)
		logging.info("Executing the MqttClientControlPacketTest class...")
		
		cls.cfg = ConfigUtil()
		
		# Use a unique clientID for this test
		cls.mcc = MqttClientConnector(clientID = "CDAMqttCLient")
		
	def setUp(self):
		pass

	def tearDown(self):
		pass

	def testConnectAndDisconnect(self):
		"""
		Test case to connect to the MQTT broker and disconnect.
		"""
		logging.info("Testing connection and disconnection to the broker...")
		
		# Connect to the broker
		isConnected = self.mcc.connectClient()
		self.assertTrue(isConnected, "MQTT client should be connected.")
		
		# Sleep for a few seconds to ensure stable connection
		sleep(2)
		
		# Disconnect from the broker
		isDisconnected = self.mcc.disconnectClient()
		self.assertTrue(isDisconnected, "MQTT client should be disconnected.")
		
	def testServerPing(self):
		"""
		Test case to check the server ping (PINGREQ and PINGRESP).
		"""
		logging.info("Testing server ping...")
		
		# Connect to the broker
		isConnected = self.mcc.connectClient()
		self.assertTrue(isConnected, "MQTT client should be connected.")
		
		# # Sleep for a few seconds to ensure stable connection
		# keepAliveInterval = self.cfg.keepAlive()
		# sleep(2 * keepAliveInterval)
		keepAliveInterval = self.cfg.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE)
		logging.info(f"Using keep-alive interval: {keepAliveInterval} seconds")
		sleep(2 * keepAliveInterval)
		
		# Send a PINGREQ and expect a PINGRESP (paho-mqtt handles this internally)
		# Sleep for another few seconds to see the PINGREQ/RESP in the logs
		# sleep(5)
		
  
		# Disconnect from the broker
		isDisconnected = self.mcc.disconnectClient()
		self.assertTrue(isDisconnected, "MQTT client should be disconnected.")
		
	def testPubSub(self):
		"""
		Test case to test publishing and subscribing to a topic.
		"""
		logging.info("Testing publish and subscribe functionality...")
		
		# Connect to the broker
		isConnected = self.mcc.connectClient()
		self.assertTrue(isConnected, "MQTT client should be connected.")
		
		# Set up QoS 1 and QoS 2 to test control packets
		qosList = [1, 2]
		resource = ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE
		
		for qos in qosList:
			# Subscribe to a topic
			isSubscribed = self.mcc.subscribeToTopic(resource=resource, qos=qos)
			self.assertTrue(isSubscribed, f"Client should be subscribed with QoS {qos}.")
			
			# Publish a message
			message = "Test message with QoS " + str(qos)
			isPublished = self.mcc.publishMessage(resource=resource, msg=message, qos=qos)
			self.assertTrue(isPublished, f"Client should have published the message with QoS {qos}.")
			
			# Sleep for a few seconds to let the message be processed
			sleep(2)
   
			isUnsubscribed = self.mcc.unsubscribeFromTopic(resource=resource)
			self.assertTrue(isUnsubscribed, f"Client should have unsubscribed from the topic with QoS {qos}.")
    
		
		# Disconnect from the broker
		isDisconnected = self.mcc.disconnectClient()
		self.assertTrue(isDisconnected, "MQTT client should be disconnected.")

if __name__ == '__main__':
	unittest.main()
