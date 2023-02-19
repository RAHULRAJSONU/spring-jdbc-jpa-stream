package io.github.rahulrajsonu.springdatajpademojava.domain;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Builder
public class CustomerDto {
    @Id
    private Long id;
    private String name;

    public static CustomerDto mapToDto(SqlRowSet row){
        return CustomerDto.builder()
                .id(row.getLong("id"))
                .name(row.getString("name"))
                .build();
    }

    public static CustomerDto rowMapper(ResultSet rs, int i) throws SQLException {
        return CustomerDto.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}
