package com.yzx.test.cache;

import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by yezhaoxiangabc@163.com on 2017-08-14 15:58:07.
 */
public class MemcachedCache implements Cache {
private static final String PRESENT = new String();
// 单个cache存储的key最大数量
private static final int maxElements = 10000;
// 默认过期时间10天
private static int expire = 10 * 24 * 60 * 60;
private String name;
private MemcachedClient client;

public static int getExpire() {
return expire;
}

public static void setExpire(int expire) {
MemcachedCache.expire = expire;
}

public void setName(String name) {
this.name = name;
}

// 存储key的集合，使用LinkedHashMap实现
private KeySet keys;

public MemcachedCache() {
this.keys = new KeySet(maxElements);
}

public MemcachedCache(String name, MemcachedClient client) {
this.name = name;
this.client = client;
this.keys = new KeySet(maxElements);
}

public String getName() {
return this.name;
}

public Object getNativeCache() {
return this.client;
}

// ckey是key+cacheName作为前缀，也是最终存入缓存的key
public ValueWrapper get(Object key) {
String ckey = toStringWithCacheName(key);
if (keys.containsKey(ckey)) {
Object value = client.get(ckey);
return value != null ? new SimpleValueWrapper(value) : null;
} else {
return null;
}
}

@Override
public <T> T get(Object key, Class<T> aClass) {

    String ckey = toStringWithCacheName(key);
    if (keys.containsKey(ckey)) {
    Object value = client.get(ckey);
    if (value != null && aClass.isInstance(value)) {
    return (T) value;
    }
    return null;
    } else {
    return null;
    }
    }

    @Override
    public <T> T get(Object o, Callable<T> callable) {
        return null;
        }

        // 将ckey加入key集合并将ckey-value存入缓存
        public void put(Object key, Object value) {
        String ckey = toStringWithCacheName(key);
        keys.put(ckey, PRESENT);
        client.set(ckey, expire, value);
        }

        @Override
        public ValueWrapper putIfAbsent(Object o, Object o1) {
        return null;
        }

        // 从keys集合清除ckey，并从缓存清除
        public void evict(Object key) {
        String ckey = toStringWithCacheName(key);
        keys.remove(ckey);
        client.delete(ckey);
        }

        private String toStringWithCacheName(Object obj) {
        return name + "." + String.valueOf(obj);
        }

        // 遍历清除
        public void clear() {
        for (String ckey : keys.keySet()) {
        client.delete(ckey);
        }
        keys.clear();
        }

        public MemcachedClient getClient() {
        return this.client;
        }

        public void setClient(MemcachedClient client) {
        this.client = client;
        }

        public KeySet getKeys() {
        return this.keys;
        }


        class KeySet extends LinkedHashMap<String, String> {
        private static final long serialVersionUID = 1L;
        private int maxSize;

        public KeySet(int initSize) {
        super(initSize, 0.75F, true);
        this.maxSize = initSize;
        }

        public boolean removeEldestEntry(Map.Entry<String, String> eldest) {
        boolean overflow = size() > this.maxSize;
        if (overflow) {
        MemcachedCache.this.client.delete(eldest.getKey());
        }
        return overflow;
        }
        }
        }