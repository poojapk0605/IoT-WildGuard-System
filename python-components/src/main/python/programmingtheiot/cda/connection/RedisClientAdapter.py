import logging
import redis
from programmingtheiot.common.ConfigUtil import ConfigUtil
from programmingtheiot.common.ConfigConst import ConfigConst
from programmingtheiot.common.ResourceNameEnum import ResourceNameEnum
from programmingtheiot.data.SensorData import SensorData

class RedisPersistenceAdapter:
    DATA_GATEWAY_SERVICE = 'Data.GatewayService'

    def __init__(self):
        """
        Constructor for RedisPersistenceAdapter.
        """
        self._logger = logging.getLogger(self.__class__.__name__)
        self._config = ConfigUtil()
        self._redis_client = None

        # Retrieve host and port from the config file
        self._host = self._config.getProperty(self.DATA_GATEWAY_SERVICE, ConfigConst.HOST_KEY, defaultVal='localhost')
        self._port = self._config.getInteger(self.DATA_GATEWAY_SERVICE, ConfigConst.PORT_KEY, defaultVal=6379)

        self._logger.info(f"Configured Redis host: {self._host}, port: {self._port}")

    def connectClient(self) -> bool:
        """
        Connects to the Redis server.
        """
        if self._redis_client and self._redis_client.ping():
            self._logger.warning("Redis client is already connected.")
            return True

        try:
            self._redis_client = redis.Redis(host=self._host, port=self._port)
            if self._redis_client.ping():
                self._logger.info("Successfully connected to Redis server.")
                return True
        except Exception as e:
            self._logger.error(f"Failed to connect to Redis server: {e}")

        return False

    def disconnectClient(self) -> bool:
        """
        Disconnects from the Redis server.
        """
        if not self._redis_client:
            self._logger.warning("Redis client is already disconnected.")
            return True

        try:
            # Closing Redis connection
            self._redis_client = None
            self._logger.info("Successfully disconnected from Redis server.")
            return True
        except Exception as e:
            self._logger.error(f"Failed to disconnect from Redis server: {e}")

        return False

    def storeData(self, resource: ResourceNameEnum, data: SensorData) -> bool:
        """
        Stores SensorData in Redis.

        :param resource: ResourceNameEnum
        :param data: SensorData
        :return: True if successful, False otherwise
        """
        if not self._redis_client or not self._redis_client.ping():
            self._logger.warning("Redis client is not connected. Attempting to reconnect...")
            if not self.connectClient():
                return False

        try:
            topic = resource.getResourceName()
            payload = data.toJson()  # Assuming SensorData has a `toJson` method
            self._redis_client.set(topic, payload)
            self._logger.info(f"Data stored in Redis. Topic: {topic}, Payload: {payload}")
            return True
        except Exception as e:
            self._logger.error(f"Failed to store data in Redis: {e}")

        return False

