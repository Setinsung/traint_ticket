package com.hdu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;

@Service
@Slf4j
public class TrainCacheService {

    @Resource(name = "shardedJedisPoll")
    private ShardedJedisPool shardedJedisPool;

    private ShardedJedis instance() {
        return shardedJedisPool.getResource();
    }

    public void safeClose(ShardedJedis shardedJedis) {
        try {
            if (shardedJedis != null)
                shardedJedis.close();
        } catch (Exception e) {
            log.error("jedis close exception", e);
        }
    }

    public void set(String key, String value) {
        if (value == null)
            return;
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.set(key, value);
        } catch (Exception e) {
            log.error("jedis.set exception, key:{},value:{}", key, value, e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public String get(String key) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            return shardedJedis.get(key);
        } catch (Exception e) {
            log.error("jedis.set exception, key:{}", key, e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public String hget(String cacheKey, String field) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            return shardedJedis.hget(cacheKey, field);
        } catch (Exception e) {
            log.error("jedis.hget exception, cacheKey:{},field:{}", cacheKey, field, e);
            throw e;
        }finally {
            safeClose(shardedJedis);
        }
    }

    public void hset(String key, String field, String value) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.hset(key, field, value);
        } catch (Exception e) {
            log.error("jedis.set exception, key:{},field:{},value:{}", key, field, value, e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public void hincrBy(String key, String field, Long value) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.hincrBy(key, field, value);
        } catch (Exception e) {
            log.error("jedis.hincrBy exception, key:{},field:{},value:{}", key, field, value, e);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }
}
