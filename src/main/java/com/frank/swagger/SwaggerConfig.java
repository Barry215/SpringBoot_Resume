package com.frank.swagger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by frank on 17/4/11.
 */
@Configuration
@EnableSwagger2
//@ComponentScan(basePackages = {"com.frank.controller"})
public class SwaggerConfig {
    /**
     * 可以定义多个组，比如本类中定义把test和demo区分开了 （访问页面就可以看到效果了）
     * http://localhost:8888/swagger-ui.html
     */
    @Bean
    public Docket testApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.frank.controller"))
                .paths(PathSelectors.any())
                .build();
    }


    private ApiInfo apiInfo() {
        Contact contact=new Contact("frank",
                "https://barry215.github.io","frank315111@gmail.com");
        return new ApiInfoBuilder()
                .title("Spring Boot 后台API")
                .description("Swagger2 API")
                //.termsOfServiceUrl("http://www.jianshu.com/u/f192766abeab")
                //.contact("作者名")
                .contact(contact)
                .version("1.0")
                .build();
    }
}