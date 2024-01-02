package com.hdu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableTransactionManagement // 开启事务注解
public class TraintTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraintTicketApplication.class, args);
    }

}
