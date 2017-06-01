package com.frank.config;

import com.frank.filter.AdminFilter;
import com.frank.filter.CORSFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank on 17/4/20.
 */
@Configuration
public class FilterConfig {

//    @Bean
//    public FilterRegistrationBean greetingFilterRegistrationBean() {
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setName("adminFilter");
//        AdminFilter adminFilter = new AdminFilter();
//        registrationBean.setFilter(adminFilter);
//        registrationBean.setOrder(10);
//        List<String> urlList = new ArrayList<>();
//        urlList.add("/admin/*");
//        registrationBean.setUrlPatterns(urlList);
//        return registrationBean;
//    }

    @Bean
    public FilterRegistrationBean corsFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setName("corsFilter");
        CORSFilter corsFilter = new CORSFilter();
        registrationBean.setFilter(corsFilter);
        registrationBean.setOrder(20);
        List<String> urlList = new ArrayList<>();
        urlList.add("/*");
        registrationBean.setUrlPatterns(urlList);
        return registrationBean;
    }

    /*

    更简便的写法，但是缺少配置

    @Bean
    @Order(1)
    Filter adminFilter() {
        return new AdminFilter();
    }*/

//    @Bean
//    @Order(2)
//    public Filter corsFilter() {
//        return new CORSFilter();
//    }

}
