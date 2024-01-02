package com.hdu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableTransactionManagement // 开启事务注解
@EnableOpenApi // http://localhost:8080/swagger-ui/index.html#/
public class TraintTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraintTicketApplication.class, args);
    }

}
