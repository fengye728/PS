package org.maple.profitsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("org.maple.profitsystem")
@EnableTransactionManagement
@MapperScan()
public class PSConfig {

}
