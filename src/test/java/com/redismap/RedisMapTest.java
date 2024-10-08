package com.redismap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisMapTest {

    @Mock
    private Jedis jedisMock;

    private RedisMap redisMap;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        redisMap = new RedisMap(jedisMock);
    }

    @Test
    public void testPutAndGet() {
        when(jedisMock.get("key1")).thenReturn("value1");

        redisMap.put("key1", "value1");
        assertEquals("value1", redisMap.get("key1"));
        verify(jedisMock).set("key1", "value1");
    }

    @Test
    public void testContainsKey() {
        when(jedisMock.exists("key2")).thenReturn(true);
        when(jedisMock.exists("nonexistentKey")).thenReturn(false);

        assertTrue(redisMap.containsKey("key2"));
        assertFalse(redisMap.containsKey("nonexistentKey"));
    }

    @Test
    public void testContainsValue() {
        when(jedisMock.get("key3")).thenReturn("value3");
        when(jedisMock.get("key4")).thenReturn("value4");
        when(jedisMock.keys("*")).thenReturn(Set.of("key3", "key4"));

        assertTrue(redisMap.containsValue("value3"));
        assertFalse(redisMap.containsValue("nonexistentValue"));
    }

    @Test
    public void testSize() {
        when(jedisMock.dbSize()).thenReturn(2L);
        assertEquals(2, redisMap.size());
    }

    @Test
    public void testRemove() {
        when(jedisMock.get("key5")).thenReturn("value5");

        assertEquals("value5", redisMap.remove("key5"));
        verify(jedisMock).del("key5");  // Проверяем, что del был вызван

        when(jedisMock.get("key5")).thenReturn(null);
        assertNull(redisMap.get("key5"));
    }

    @Test
    public void testClear() {
        redisMap.clear();
        verify(jedisMock).flushDB();
    }

    @Test
    public void testIsEmpty() {
        when(jedisMock.dbSize()).thenReturn(0L);

        assertTrue(redisMap.isEmpty());

        when(jedisMock.dbSize()).thenReturn(1L);
        assertFalse(redisMap.isEmpty());
    }

    @Test
    public void testPutWithNullKey() {
        assertThrows(IllegalArgumentException.class, () -> redisMap.put(null, "value"));
        verify(jedisMock, never()).set(anyString(), anyString());  // Убедимся, что set не был вызван
    }

    @Test
    public void testPutWithNullValue() {
        assertThrows(IllegalArgumentException.class, () -> redisMap.put("key", null));
        verify(jedisMock, never()).set(anyString(), anyString());
    }

    @Test
    public void testGetKeyNotExists() {
        when(jedisMock.get("nonexistentKey")).thenReturn(null);

        assertNull(redisMap.get("nonexistentKey"));
        verify(jedisMock).get("nonexistentKey");
    }

    @Test
    public void testKeySet() {
        when(jedisMock.keys("*")).thenReturn(new HashSet<>(Arrays.asList("key1", "key2", "key3")));
        assertEquals(new HashSet<>(Arrays.asList("key1", "key2", "key3")), redisMap.keySet());
    }

    @Test
    public void testValues() {
        when(jedisMock.keys("*")).thenReturn(new HashSet<>(Arrays.asList("key1", "key2")));
        when(jedisMock.get("key1")).thenReturn("value1");
        when(jedisMock.get("key2")).thenReturn("value2");

        assertEquals(Arrays.asList("value1", "value2"), redisMap.values());
    }

    @Test
    public void testEntrySet() {
        when(jedisMock.keys("*")).thenReturn(new HashSet<>(Arrays.asList("key1", "key2")));
        when(jedisMock.get("key1")).thenReturn("value1");
        when(jedisMock.get("key2")).thenReturn("value2");

        Set<Map.Entry<String, String>> expected = new HashSet<>();
        expected.add(new AbstractMap.SimpleEntry<>("key1", "value1"));
        expected.add(new AbstractMap.SimpleEntry<>("key2", "value2"));

        assertEquals(expected, redisMap.entrySet());
    }
}
