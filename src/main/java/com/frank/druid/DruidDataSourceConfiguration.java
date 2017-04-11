package com.frank.druid;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;


/**
 * Created by frank on 17/4/10.
 */
@Configuration
public class DruidDataSourceConfiguration {
    private static final Logger logger = Logger.getLogger(DruidDataSourceConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource(){
        DataSource druidDataSource = new DruidDataSource();
        logger.info("druidDataSource:"+druidDataSource.toString());
        return druidDataSource;
    }
}
