package com.github.ltprc.gamepal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling // 开启定时任务功能
public class ConfigApplication {

    public static void main(String[] args) {
//        SpringApplication app = new SpringApplication(SpringBootProjectApplication.class);
//        app.setBannerMode(Banner.Mode.OFF);
//        app.run(args);
        SpringApplication.run(ConfigApplication.class, args);
    }
    //或者启动如下方式，servlet上不需要添加注解
//    @Bean
//    public ServletRegistrationBean<MyServlet> getServletRegistrationBean(){
//        ServletRegistrationBean<MyServlet> bean = new ServletRegistrationBean<>(new MyServlet());
//        ArrayList<String> url = new ArrayList<>();
//        url.add("/srv");
//        bean.setUrlMappings(url);
//        bean.setLoadOnStartup(1);
//        return bean;
//    }
//    @Bean
//    public ServletListenerRegistrationBean listenerRegist() {
//        ServletListenerRegistrationBean srb = new ServletListenerRegistrationBean();
//        srb.setListener(new MyHttpSessionListener());
//        System.out.println("listener");
//        return srb;
//    }
}
