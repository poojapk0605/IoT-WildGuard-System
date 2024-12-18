### Description for labModule 1 & 2 
What does your implementation do?
In **labModule1**, it is important to ensure the environment is set correctly for the project to function as expected.  After installing all jar files and maven, run both unit tests and integration tests to confirm that everything is working properly and that the Python environment is correctly configured.

In **labModule2**, my implementation sets a delay time for the **GatewayDeviceApp**. It schedules the **System Performance Manager** to start when the app begins and stop when the **GatewayDeviceApp Test** is about to finish. It also sets up **SystemCPUtil** and **SystemMemUtil** by setting a **telementry function** to measure the CPU and memory usage while the app is running.

The implementation is designed to monitor and manage system performance metrics, such as CPU, memory, and disk usage, within an IoT context. It collects and processes telemetry data from various system components and sensors, facilitating effective resource management and performance monitoring. The data is serialized for easy communication and storage, enabling integration with other systems and applications. Overall, it supports the development of robust IoT applications by providing essential monitoring and data management capabilities.

The implementation includes the MqttClientConnector, which facilitates connecting to an MQTT broker for publishing and subscribing to topics while managing message Quality of Service (QoS) levels. It also features the DeviceDataManager class, which oversees connections to various components, such as MQTT and system performance monitoring, and manages incoming data and component lifecycle based on configuration settings


The implementation provides a CoAP server gateway and a set of CoAP resource handlers for managing IoT-related data, such as telemetry data, system performance metrics, and actuator commands. The server gateway facilitates hosting, initializing, and managing CoAP resources dynamically, while the resource handlers define how clients can interact with these resources using CoAP methods (GET, POST, PUT, DELETE). The handlers also enable data processing by delegating received data to a listener (IDataMessageListener) and notifying clients about updates through CoAP's observability.


How does your implementation work?
My implementation works by first ensuring that the enviroment is set up correctly in **labModule1**.  I run both unit and integration tests to verify that everything is working correctly and the Python environment is properly configured.

My implementation works by first setting a delay time for the **GatewayDeviceApp**. Then, I schedule the **System Performance Manager** to start when the test begins and stop when the **GatewayDeviceApp Test** is about to finish. During the test, I also set up **SystemCPUtil** and **SystemMemUtil** by setting a **telementry function**to measure the CPU and memory usage while the app is running.

The implementation utilizes a combination of classes that communicate and perform specific tasks related to system performance monitoring and IoT data management. The SystemPerformanceManager periodically executes tasks to gather CPU, memory, and disk utilization data, which is then sent to a listener for processing. Each specific utilization task (SystemCpuUtilTask, SystemMemUtilTask, SystemDiskUtilTask) retrieves the relevant metrics and returns the data in a standardized format. Additionally, classes like SensorData and ActuatorData encapsulate the relevant sensor and actuator information, allowing for easy data manipulation and logging.

The MqttClientControlPacketTest class provides integration tests to verify the functionality of the MqttClientConnector in a simulated environment, ensuring expected behavior for essential operations like connecting, disconnecting, publishing, and subscribing to topics. The DeviceDataManager class manages these connections, starting and stopping components based on configuration settings while handling incoming sensor and system performance messages effectively.


The CoAP server gateway initializes the server and sets up default resources or dynamically adds new ones as required. It starts/stops the server and manages client interactions through linked resource handlers. Each resource handler defines CoAP methods like PUT for telemetry and system performance updates or GET/POST/DELETE for actuator commands. These methods process client requests, delegate data to an IDataMessageListener for further handling, and respond with appropriate CoAP status codes. For actuator commands, the handler supports observability, notifying subscribed clients about data changes in real time. This structure ensures efficient data management and interaction for IoT applications.



### Repository URL
https://github.com/Tele6530-Connected-Devices/java-components

### UML DIAGRAM 
Added a jpeg on document repository

### Unit Testing for labModule 1 & 2 & 5 
ConfigUtilTest
SystemCpuUtilTaskTest
SystemMemUtilTaskTest
ActuatorDataTest
SensorDataTest
SystemPerformanceDataTest
DataUtilTest

### Integration Testing for labModule 1 & 2 & 5
GatewayDeviceAppTest
DeviceDataManagerNoCommsTest
SystemPerformanceManagerTest
DataIntegrationTest
MqttClientConnectorTest
CoapServerGatewayTest