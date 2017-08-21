package com.yzx.test.cache;

import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;

/**
 * Created by yezhaoxiangabc@163.com on 2017-08-14 15:58:07.
 */
public class MemcachedCacheManager extends AbstractCacheManager {
// 注入memcachedClient（后面会有配置）
private MemcachedClient client;
private Collection<Cache> caches;

    public MemcachedClient getClient() {
    return client;
    }

    public Collection<? extends Cache> getCaches() {
    return caches;
    }

    public void setCaches(Collection<Cache> caches) {
        this.caches = caches;
        }

        public MemcachedCacheManager() {}

        public MemcachedCacheManager(MemcachedClient client) {
        this.client = client;
        }

        public void setClient(MemcachedClient client) {
        this.client = client;
        }

        protected Collection<Cache> loadCaches() {
            return this.caches;
            }

            // 根据名称获取cache，对应注解里的value如notice_cache，没有就创建并加入cache管理
            public Cache getCache(String name) {
            Cache cache = super.getCache(name);
            if (cache == null) {
            cache = new MemcachedCache(name, client);
            this.caches.add(cache);
            }
            return cache;
            }
            }