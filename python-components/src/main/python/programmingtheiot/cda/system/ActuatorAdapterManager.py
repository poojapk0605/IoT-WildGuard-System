#####
# 
# This class is part of the Programming the Internet of Things project.
# 
# It is provided as a simple shell to guide the student and assist with
# implementation for the Programming the Internet of Things exercises,
# and designed to be modified by the student as needed.
#

import logging

from importlib import import_module

import programmingtheiot.common.ConfigConst as ConfigConst
from programmingtheiot.common.ConfigUtil import ConfigUtil
from programmingtheiot.common.IDataMessageListener import IDataMessageListener

from programmingtheiot.data.ActuatorData import ActuatorData

from programmingtheiot.cda.sim.HvacActuatorSimTask import HvacActuatorSimTask
from programmingtheiot.cda.sim.HumidifierActuatorSimTask import HumidifierActuatorSimTask
from programmingtheiot.cda.emulated.LedDisplayEmulatorTask import LedDisplayEmulatorTask

class ActuatorAdapterManager(object):
	"""
	The `ActuatorAdapterManager` class manages environmental actuators like humidifiers and HVAC systems, processing commands and controlling their state based on incoming data. It supports both simulated and emulated actuators and integrates with a message listener to handle actuator responses.
	
	"""
	
	def __init__(self, dataMsgListener: IDataMessageListener = None):
		self.dataMsgListener = dataMsgListener
	
		self.configUtil = ConfigUtil()
		
		self.useSimulator = \
			self.configUtil.getBoolean( \
				section = ConfigConst.CONSTRAINED_DEVICE, key = ConfigConst.ENABLE_SIMULATOR_KEY)
		self.useEmulator  = \
			self.configUtil.getBoolean( \
				section = ConfigConst.CONSTRAINED_DEVICE, key = ConfigConst.ENABLE_EMULATOR_KEY)
		self.deviceID     = \
			self.configUtil.getProperty( \
				section = ConfigConst.CONSTRAINED_DEVICE, key = ConfigConst.DEVICE_LOCATION_ID_KEY, defaultVal = ConfigConst.NOT_SET)
		self.locationID   = \
			self.configUtil.getProperty( \
				section = ConfigConst.CONSTRAINED_DEVICE, key = ConfigConst.DEVICE_LOCATION_ID_KEY, defaultVal = ConfigConst.NOT_SET)
		
		self.humidifierActuator = None
		self.hvacActuator       = None
		self.ledDisplayActuator = None
		
		# see PIOT-CDA-03-007 description for thoughts on the next line of code
		self._initEnvironmentalActuationTasks()
  
	def _initEnvironmentalActuationTasks(self):
		if not self.useEmulator:
			# load the environmental tasks for simulated actuation
			self.humidifierActuator = HumidifierActuatorSimTask()
			
			# create the HVAC actuator
			self.hvacActuator = HvacActuatorSimTask()
			self.ledDisplayActuator = LedDisplayEmulatorTask()
		
		else:
				hueModule = import_module('programmingtheiot.cda.emulated.HumidifierEmulatorTask', 'HumidiferEmulatorTask')
				hueClazz = getattr(hueModule, 'HumidifierEmulatorTask')
				self.humidifierActuator = hueClazz()
				
				# create the HVAC actuator emulator
				hveModule = import_module('programmingtheiot.cda.emulated.HvacEmulatorTask', 'HvacEmulatorTask')
				hveClazz = getattr(hveModule, 'HvacEmulatorTask')
				self.hvacActuator = hveClazz()
				
				# create the LED display actuator emulator
				ledDisplayModule = import_module('programmingtheiot.cda.emulated.LedDisplayEmulatorTask', 'LedDisplayEmulatorTask')
				leClazz = getattr(ledDisplayModule, 'LedDisplayEmulatorTask')
				self.ledDisplayActuator = leClazz()
   
	def sendActuatorCommand(self, data: ActuatorData) -> bool:
		if data and not data.isResponseFlagEnabled():
			# first check if the actuation event is destined for this device
			if data.getLocationID() == self.locationID:
				logging.info("Actuator command received for location ID %s. Processing...", str(data.getLocationID()))
				
				aType = data.getTypeID()
				responseData = None
				success = False
				
				if aType == ConfigConst.HUMIDIFIER_ACTUATOR_TYPE and self.humidifierActuator:
					responseData = self.humidifierActuator.updateActuator(data)
					success = True
				elif aType == ConfigConst.HVAC_ACTUATOR_TYPE and self.hvacActuator:
					responseData = self.hvacActuator.updateActuator(data)
					success = True
				elif aType == ConfigConst.LED_DISPLAY_ACTUATOR_TYPE and self.ledDisplayActuator:
					responseData = self.ledDisplayActuator.updateActuator(data)
					success = True
				else:
					logging.warning("No valid actuator type. Ignoring actuation for type: %s", data.getTypeID())
					
				if success and responseData and self.dataMsgListener:
					self.dataMsgListener.handleActuatorCommandResponse(responseData)
					logging.debug("Actuator command processed. Response sent to listener.")
					return True
					
				return success
				
			else:
				logging.warning("Location ID doesn't match. Ignoring actuation: (me) %s != (you) %s", 
							str(self.locationID), str(data.getLocationID()))
		else:
			logging.warning("Actuator request received. Message is empty or response. Ignoring.")
		
		return False
	
	
	
	def setDataMessageListener(self, listener: IDataMessageListener) -> bool:
		if listener:
			self.dataMsgListener = listener
