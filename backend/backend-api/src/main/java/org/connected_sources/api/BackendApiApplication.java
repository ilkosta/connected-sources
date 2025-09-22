package org.connected_sources.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

//import javax.sql.DataSource;
import java.util.Arrays;

@ConfigurationPropertiesScan
@SpringBootApplication
@ComponentScan(
    basePackages = {
        "org.connected_sources",
        "org.connected_sources.tenant.spi.db"
    })
public class BackendApiApplication  {

    public static void main(String[] args) {
        SpringApplication.run(BackendApiApplication.class, args);
    }

//    // DEBUG!!
//    @Bean
//    public CommandLineRunner checkBeans(ApplicationContext ctx) {
//        return args -> {
//            System.out.println("Checking required beans...");
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//        };
//    }
}
