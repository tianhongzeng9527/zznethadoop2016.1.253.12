package tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
    private static JedisPool pool = null;
    private static Logger logger = Logger.getLogger(RedisClient.class);
    private static String redisServerIp = null;
    private static Integer redisServerPort = null;

    private static void initialPool() {
        try {
            //Properties p = new Properties();
            //p.load(ClassLoader.getSystemResourceAsStream("params.properties"));
            redisServerIp = "192.168.101.12";
            redisServerPort = 6379;
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(5);
            config.setMinIdle(2);
            config.setMaxWaitMillis(1000);
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, redisServerIp, redisServerPort, 5000);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("create JedisPool error : " + e);
        }
    }


    public static String get(String key) {
        String value = null;

        Jedis jedis = null;
        try {
            if (pool == null) {
                initialPool();
            }
            jedis = pool.getResource();
            value = jedis.get(key);
            logger.info("check the key: " + key + " is stored in redis or not, " + (value == null ? false : true));
        } catch (Exception e) {

            logger.error("Redis get key error : " + e);
            jedis.close();
            e.printStackTrace();
        } finally {

            jedis.close();
        }

        return value;
    }

    public static void set(String key, String value) {

        Jedis jedis = null;
        try {
            if (pool == null) {
                initialPool();
            }
            jedis = pool.getResource();
            jedis.set(key, value);
        } catch (Exception e) {

            logger.error("Redis set key error : " + e);
            jedis.close();
            e.printStackTrace();
        } finally {
            jedis.close();
        }

    }

    public static void setBatch(Map<String, String> batches) {

        Jedis jedis = null;
        try {
            if (pool == null) {
                initialPool();
            }
            jedis = pool.getResource();
            Iterator<Entry<String, String>> iter = batches.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, String> entry = iter.next();
                String key = entry.getKey();
                String val = entry.getValue();
                jedis.set(key, val);
                logger.info("set key:" + key + " to redis!");
            }
        } catch (Exception e) {
            logger.error("Redis setBatch error : " + e);
            jedis.close();
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }


    public static void setExpire(String key) {

        Jedis jedis = null;
        try {
            if (pool == null) {
                initialPool();
            }
            jedis = pool.getResource();
            jedis.expire(key, addDay(1));
        } catch (Exception e) {
            logger.error("Redis expire error : " + e);
            jedis.close();
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    public static void flush() {

        Jedis jedis = null;
        try {
            if (pool == null) {
                initialPool();
            }
            jedis = pool.getResource();
            jedis.flushAll();
        } catch (Exception e) {
            logger.error("Redis flush error : " + e);
            jedis.close();
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    public static int addDay(int n) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(sdf.format(new Date())));
            cd.add(Calendar.DATE, n);
            String addOne = sdf.format(cd.getTime()) + " 00:00:00";
            int lingdian = (int)sdf1.parse(addOne).getTime();
            int now = (int)new Date().getTime();
            return (lingdian - now) / 1000;

        } catch (Exception e) {
            logger.error("Error:",e);
            return 0;
        }

    }
}