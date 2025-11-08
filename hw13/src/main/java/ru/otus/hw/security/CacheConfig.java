package ru.otus.hw.security;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public javax.cache.CacheManager jcacheCacheManager() {

        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = provider.getCacheManager();
        createSimpleCache(cacheManager);
        return cacheManager;
    }

    private void createSimpleCache(javax.cache.CacheManager cacheManager) {

        if (cacheManager.getCache("aclCache") == null) {
            MutableConfiguration<Object, Object> config = new MutableConfiguration<>()
                    .setTypes(Object.class, Object.class)
                    .setStoreByValue(false)
                    .setStatisticsEnabled(true);

            cacheManager.createCache("aclCache", config);
        }
    }

    @Bean
    public CacheManager cacheManager() {

        return new JCacheCacheManager(jcacheCacheManager());
    }
}
