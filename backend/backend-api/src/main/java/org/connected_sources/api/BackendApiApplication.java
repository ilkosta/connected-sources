package org.connected_sources.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan
@SpringBootApplication
@ComponentScan(
    basePackages = {
        "org.connected_sources",
//        "org.connected_sources.tenant.spi.db"
    })
@EnableJpaRepositories(
        basePackages = "org.connected_sources.core"
)
@EntityScan(
        basePackages = "org.connected_sources.core"
)
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
