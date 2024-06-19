package com.hu.util;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisUtils {

    public RedisTemplate redisTemplate;



    public RedisUtils(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void set(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }


    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T get(final String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }


    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                List<String> keys = new ArrayList<>();
                for (int i = 0; i < key.length; i++) {
                    keys.add(key[i]);
                }
                redisTemplate.delete(keys);
            }
        }
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> long setForSet(final String key, List<T> dataSet) {
        Long count = redisTemplate.opsForSet().add(key, dataSet.toArray(new String[0]));
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getForSetAll(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public Boolean expireForSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }


    /**
     * 从set中移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long delForSet(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

}
