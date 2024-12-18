from programmingtheiot.cda.connection.RedisClientAdapter import RedisClientAdapter
from programmingtheiot.common.ResourceNameEnum import ResourceNameEnum
from programmingtheiot.data.SensorData import SensorData

# Create instance of RedisPersistenceAdapter
redis_adapter = RedisClientAdapter()

# Connect to Redis
if redis_adapter.connectClient():
    print("Connected to Redis successfully.")

    # Create sample SensorData
    sensor_data = SensorData(name="Temperature", value=25.5)

    # Store data in Redis
    if redis_adapter.storeData(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sensor_data):
        print("Sensor data stored successfully.")

    # Disconnect from Redis
    if redis_adapter.disconnectClient():
        print("Disconnected from Redis successfully.")
