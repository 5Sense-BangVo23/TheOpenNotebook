package com.dev.notebook.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean(name={"userLoginCache"})
    @Primary
    public CacheStore<String, Integer> userCache(){
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }

    @Bean(name={"registrationCache"})
    public CacheStore<String, Integer> anotherCache(){
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }
}
