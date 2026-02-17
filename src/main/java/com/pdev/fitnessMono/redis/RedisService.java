package com.pdev.fitnessMono.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis cache operations with JSON serialization.
 *
 * <p>Supports generic types via {@link TypeReference} and Java 8 date/time.</p>
 *
 */

@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /** Pre-configured ObjectMapper with Java 8 date/time support. */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // Register JSR310 module
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Write as ISO-8601 strings

    /**
     * Retrieves and deserializes value from cache.
     *
     * <p>Use {@link TypeReference} for generic types (e.g., {@code List<T>})
     * to preserve type information.</p>
     *
     * @param key cache key
     * @param typeReference type with generic info
     * @return deserialized object or null if not found
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) return null;
            return objectMapper.readValue(jsonValue, typeReference);
        } catch (Exception e) {
            log.error("Redis get error for key {}: {}", key, e.getMessage(), e);
            return null;
        }
    }
    /**
     * Serializes and stores value in cache with TTL.
     *
     * @param key cache key
     * @param obj object to cache
     * @param ttl seconds until expiration
     */

    public void set(String key, Object obj, long ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(obj);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis set error for key {}: {}", key, e.getMessage(), e);
        }
    }

    /**
     * Removes key from cache.
     *
     * @param key cache key
     * @return true if deleted
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}