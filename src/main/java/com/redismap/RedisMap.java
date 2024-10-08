package com.redismap;

import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

public class RedisMap implements Map<String, String> {

    private final Jedis jedis;

    public RedisMap(String redisHost, int redisPort) {
        this.jedis = new Jedis(redisHost, redisPort);
    }

    public RedisMap(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public int size() {
        return Math.toIntExact(jedis.dbSize());
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return jedis.exists((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        Set<String> keys = keySet();
        for (String key : keys) {
            if (get(key).equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String get(Object key) {
        return jedis.get((String) key);
    }

    @Override
    public String put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key can not  be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value can not  be null");
        }
        jedis.set(key, value);
        return value;
    }

    @Override
    public String remove(Object key) {
        String value = jedis.get((String) key);
        jedis.del((String) key);
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        jedis.flushDB();
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(jedis.keys("*"));
    }


    @Override
    public Collection<String> values() {
        return jedis.keys("*").stream()
                .map(jedis::get)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<String> keys = jedis.keys("*");
        Set<Map.Entry<String, String>> entries = new HashSet<>();
        for (String key : keys) {
            String value = jedis.get(key);
            entries.add(new AbstractMap.SimpleEntry<>(key, value));
        }
        return entries;
    }
}
