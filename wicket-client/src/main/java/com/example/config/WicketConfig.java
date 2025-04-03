package com.example.config;

import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WicketConfig {
    @Bean
    public FilterRegistrationBean<WicketFilter> wicketFilter() {
        FilterRegistrationBean<WicketFilter> registration = new FilterRegistrationBean<>();
        WicketFilter filter = new WicketFilter();
        registration.setFilter(filter);
        registration.addInitParameter(WicketFilter.APP_FACT_PARAM, 
            "com.example.config.WicketApplication");
        registration.addUrlPatterns("/*");
        return registration;
    }
}