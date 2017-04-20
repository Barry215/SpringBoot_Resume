package com.frank.config;

import com.frank.filter.AdminFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank on 17/4/20.
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean greetingFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setName("greeting");
        AdminFilter adminFilter = new AdminFilter();
        registrationBean.setFilter(adminFilter);
        registrationBean.setOrder(1);
        List<String> urlList = new ArrayList<>();
        urlList.add("/p/new");
        urlList.add("/p/edit/*");
        urlList.add("/p/*/check/*");
        registrationBean.setUrlPatterns(urlList);
        return registrationBean;
    }

    /*

    更简便的写法，但是缺少配置

    @Bean
    @Order(1)
    Filter adminFilter() {
        return new AdminFilter();
    }

    @Bean
    @Order(2)
    public Filter helloFilter() {
        return new HelloFilter();
    }*/

}
