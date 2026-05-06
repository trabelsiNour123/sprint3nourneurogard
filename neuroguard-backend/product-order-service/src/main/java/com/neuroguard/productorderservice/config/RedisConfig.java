package com.neuroguard.productorderservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager() {
        // Utilisation du cache en mémoire ultra-rapide par défaut de Java
        // Cela évite de faire crasher l'application si un serveur Redis n'est pas installé sur la machine.
        return new ConcurrentMapCacheManager("products");
    }
}
