package io.github.rahulrajsonu.springdatajpademojava.repositories;

import io.github.rahulrajsonu.springdatajpademojava.domain.CustomerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static org.springframework.orm.hibernate5.SessionFactoryUtils.getDataSource;

@Component
public class CustomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Stream<CustomerDto> streamAllCustomer() {
        return jdbcTemplate.queryForStream("SELECT * FROM customer",CustomerDto::rowMapper);
    }

}
