#####
# 
# This class is part of the Programming the Internet of Things project.
# 
# It is provided as a simple shell to guide the student and assist with
# implementation for the Programming the Internet of Things exercises,
# and designed to be modified by the student as needed.
#

import programmingtheiot.common.ConfigConst as ConfigConst

from programmingtheiot.data.BaseIotData import BaseIotData

class SensorData(BaseIotData):
	"""
	The `SensorData` class extends `BaseIotData` to represent sensor-specific data, storing the sensor type and value. It provides methods to retrieve, update, and manage sensor values, and can update its data from another `SensorData` instance.
	
	"""
		
	def __init__(self, typeID: int = ConfigConst.DEFAULT_SENSOR_TYPE, name = ConfigConst.NOT_SET, d = None):
		super(SensorData, self).__init__(name = name, typeID = typeID, d = d)
		self.value = ConfigConst.DEFAULT_VAL
	
	def getSensorType(self) -> int:
		"""
		Returns the sensor type to the caller.
		
		@return int
		"""
		return self.sensorType
	
	def getValue(self) -> float:
		return self.value
	
	def setValue(self, newVal: float):
		self.value = newVal
		self.updateTimeStamp()
		
	def _handleUpdateData(self, data):
		if data and isinstance(data, SensorData):
			self.value = data.getValue()