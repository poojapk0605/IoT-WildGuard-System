package programmingtheiot.gda.connection;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisAdapter implements IPersistenceClient {
    // Logger instance for logging messages
    private static final Logger _Logger = Logger.getLogger(RedisAdapter.class.getName());
    
    // Jedis client instance
    private Jedis jedis;

    // Constructor
    public RedisAdapter() {
        super();
    }

    /**
     * Connects to the Redis server.
     */
    @Override
    public boolean connectClient() {
        try {
            jedis = new Jedis("localhost", 6379); // Update host/port if Redis is on a different server
            _Logger.log(Level.INFO, "Connected to Redis server successfully.");
            return true;
        } catch (JedisConnectionException e) {
            _Logger.log(Level.SEVERE, "Failed to connect to Redis server.", e);
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Unexpected error during Redis connection.", e);
        }
        return false;
    }

    /**
     * Disconnects from the Redis server.
     */
    @Override
    public boolean disconnectClient() {
        try {
            if (jedis != null) {
                jedis.close();
                _Logger.log(Level.INFO, "Disconnected from Redis server successfully.");
                return true;
            } else {
                _Logger.log(Level.WARNING, "Jedis client is already null. Nothing to disconnect.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Unexpected error during Redis disconnection.", e);
        }
        return false;
    }

    /**
     * Stores SensorData into Redis.
     */
    @Override
    public boolean storeData(String topic, int qos, SensorData... data) {
        if (data == null || data.length == 0) {
            return false;
        }

        try {
            for (SensorData sensorData : data) {
                // Convert SensorData to JSON
                String jsonData = DataUtil.getInstance().sensorDataToJson(sensorData);

                // Construct Redis key and store the data
                String redisKey = topic + ":" + sensorData.getName();
                jedis.set(redisKey, jsonData);
            }
            return true;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Error while storing SensorData to Redis.", e);
        }

        return false;
    }

    /**
     * Retrieves ActuatorData (Not implemented).
     */
    @Override
    public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate) {
        return null;
    }

    /**
     * Retrieves SensorData (Not implemented).
     */
    @Override
    public SensorData[] getSensorData(String topic, Date startDate, Date endDate) {
        return null;
    }

    /**
     * Stores ActuatorData (Not implemented).
     */
    @Override
    public boolean storeData(String topic, int qos, ActuatorData... data) {
        return false;
    }

    /**
     * Stores SystemPerformanceData (Not implemented).
     */
    @Override
    public boolean storeData(String topic, int qos, SystemPerformanceData... data) {
        return false;
    }

    /**
     * Registers a data storage listener (Not implemented).
     */
    @Override
    public void registerDataStorageListener(Class cType, IPersistenceListener listener, String... topics) {
        // Not implemented
    }
}
