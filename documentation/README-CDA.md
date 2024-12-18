### Description for labModule 1 & 2 
What does your implementation do?
In **labModule1**, it is important to ensure the **PYTHONPATH** is set correctly for the project to function as expected. First, copy the file path for **PiotConfig.props** and then set the **Default_Config_Name** in the **ConfigConstant.py** file. After making these changes, run both unit tests and integration tests to confirm that everything is working properly and that the Python environment is correctly configured.

In **labModule2**, my implementation sets a delay time for the **ConstrainedDeviceApp**. It schedules the **System Performance Manager** to start when the app begins and stop when the **ConstrainedDeviceApp Test** is about to finish. It also sets up **SystemCPUtil** and **SystemMemUtil** to measure the CPU and memory usage while the app is running.

The implementation for labModule3 simulates sensor data collection and actuator control for IoT devices. It emulates sensors like humidity, pressure, and temperature, generating telemetry data packaged in SensorData objects. It also emulates actuators like humidifiers, HVAC systems, and LED displays, responding to commands for activation and deactivation, and updating their states through feedback on a SenseHAT LED display. The DeviceDataManager, SensorAdapterManager, and ActuatorAdapterManager classes manage the flow of data between these components, ensuring smooth communication and device control.

In labModule4, my implementation simulates an IoT environment with both sensors (humidity, pressure, and temperature) and actuators (humidifiers, HVAC systems, and LED displays). It handles the generation of sensor telemetry data, either via simulation or SenseHAT emulation, and manages actuator commands to control device states like activation or deactivation, providing feedback on the SenseHAT display.

The implementation is designed to facilitate data management and communication in an IoT system by converting data objects into JSON format for easy transmission and storage. The DataUtil class enables this conversion, allowing for seamless integration with other components and services. The DeviceDataManager enhances the system's capability to handle real-time data from sensors and system performance metrics, ensuring that the application can effectively manage resources and respond to changes in the environment. Overall, it supports robust data handling and monitoring within the IoT architecture, enhancing system functionality and performance.

The MqttClientConnector class manages MQTT connections, enabling the publishing and subscribing to topics while handling Quality of Service (QoS) levels and connection events. The MqttClientControlPacketTest class conducts unit tests on the MqttClientConnector, validating its connectivity, publish/subscribe functionality, and server ping responsiveness.

The implementation provides a CoAP client connector that facilitates communication with a CoAP server in an IoT application. It enables sending requests (GET, POST, PUT, DELETE), processing responses, and managing resource observations. It is designed to handle resource discovery, interact with server-side resources, and notify listeners about received messages

How does your implementation work?
My implementation works by first ensuring that the "PYTHONPATH" is set up correctly in **labModule1**. Then, I copy the path of the configuration file, "PiotConfig.props," and set the "Default_Config_Name" in the **ConfigConstant.py** file. After that, I run both unit and integration tests to verify that everything is working correctly and the Python environment is properly configured.

My implementation works by first setting a delay time for the **ConstrainedDeviceApp**. Then, I schedule the **System Performance Manager** to start when the test begins and stop when the **ConstrainedDeviceApp Test** is about to finish. During the test, I also set up **SystemCPUtil** and **SystemMemUtil** to measure the CPU and memory usage while the app is running.

The SensorAdapterManager schedules sensor tasks like humidity, pressure, and temperature generation through classes such as HumiditySensorSimTask, PressureSensorSimTask, and TemperatureSensorSimTask, which simulate sensor data either randomly or from predefined ranges. Actuator tasks like HumidifierEmulatorTask and HvacEmulatorTask receive commands to activate or deactivate, displaying feedback on the SenseHAT LED screen. The DeviceDataManager manages communication between sensors and actuators, processing sensor data, comparing it to thresholds, and triggering appropriate actuator responses. This framework provides a full simulation of an IoT system, handling both environmental data collection and actuator control.

The implementation works by using the SensorAdapterManager to schedule sensor data generation and telemetry. The sensor tasks (simulated or emulated) generate readings, which are relayed to a listener for processing. The ActuatorAdapterManager receives commands for actuators, processes them based on the incoming data, and updates their state, with feedback provided on the SenseHAT LED display.


The implementation works by utilizing the DataUtil class as a Singleton to handle the serialization and deserialization of various data objects, such as ActuatorData, SensorData, and SystemPerformanceData, to and from JSON format using the Gson library. This ensures consistent data representation and communication across different components. Additionally, the DeviceDataManager class manages connections to various protocols (like MQTT and CoAP) and cloud clients, processing incoming data related to sensor readings and system performance. It dynamically starts and stops these components based on configuration settings, enabling efficient data flow and integration within the IoT system.


The MqttClientConnector class initializes with configuration settings for the MQTT broker and manages client connections using the Paho MQTT library. It provides methods for connecting, disconnecting, and handling message callbacks while logging key events. The MqttClientControlPacketTest class uses the unittest framework to test various functionalities of the MqttClientConnector, including establishing connections, subscribing to topics, publishing messages, and ensuring proper handling of different QoS levels.


The implementation works by initializing the client with host and port configurations using the CoAPthon library's `HelperClient` to establish the communication channel. It enables sending CoAP requests such as `GET` to retrieve resource data, `POST` to send new data, `PUT` to update existing data, and `DELETE` to remove resources. Responses are processed through callback methods that extract payload data and notify listeners as needed, supporting both synchronous and asynchronous operations. The client also manages resource observation using CoAP's observe mechanism, providing real-time updates for changes in resources and passing observed data to registered listeners. Additionally, it supports resource discovery by querying the `.well-known/core` endpoint to identify available resources on the CoAP server. Robust error handling is implemented, logging warnings and errors for failed operations to ensure reliability. Overall, the client integrates seamlessly into an IoT application, simplifying the management and interaction with CoAP-based resources.

### Repository URL
https://github.com/Tele6530-Connected-Devices/python-components

### UML DIAGRAM 
Added a jpeg on document repository

### Unit Testing for labModule 1 & 2 
ConfigUtilTest
SystemCpuUtilTaskTest
SystemMemUtilTaskTest
HumidityEmulatorTaskTest.py
PressureEmulatorTaskTest.py
TemperatureEmulatorTaskTest.py
SenseHatEmulatorQuickTest.py
HumidifierEmulatorTaskTest.py
HvacEmulatorTaskTest.py
LedDisplayEmulatorTaskTest.py
SensorEmulatorManagerTest.py
ActuatorEmulatorManagerTest.py
ActuatorDataTest
SensorDataTest
SystemPerformanceDataTest
HumiditySensorSimTaskTest
PressureSensorSimTaskTest
TemperatureSensorSimTaskTest
HumidifierActuatorSimTaskTest
HvacActuatorSimTaskTest
DataUtilTest.py
### Integration Testing for labModule 1 & 2 
ConstrainedDeviceAppTest
SystemPerformanceManagerTest
SensorAdapterManagerTest
ActuatorAdapterManagerTest
DeviceDataManagerNoCommsTest
DataIntegrationTest
MqttClientConnectorTest
CoapClientConnectorTest