package com.example.zupder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允許所有來源
        config.addAllowedOriginPattern("*");
        // 允許所有請求頭
        config.addAllowedHeader("*");
        // 允許所有請求方法
        config.addAllowedMethod("*");
        // 允許攜帶憑證
        config.setAllowCredentials(true);
        // 預檢請求的有效期，單位為秒
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}



