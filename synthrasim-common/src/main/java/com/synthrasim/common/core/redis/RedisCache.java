package com.synthrasim.common.core.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存操作工具类
 * 
 * 封装RedisTemplate的常用操作，简化业务层对Redis的调用。
 * 主要用于：
 * - 登录Token的缓存与刷新
 * - 验证码的存储与校验
 * - 防重复提交的标记
 */
@Component
public class RedisCache {

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存基本对象（String/Integer/实体类等），并设置过期时间
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 缓存基本对象（无过期时间）
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置键的过期时间
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获取缓存的基本对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, Object> operation = redisTemplate.opsForValue();
        return (T) operation.get(key);
    }

    /**
     * 删除单个缓存对象
     */
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 批量删除缓存对象
     */
    public long deleteObject(final Collection<String> collection) {
        Long count = redisTemplate.delete(collection);
        return count != null ? count : 0;
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
