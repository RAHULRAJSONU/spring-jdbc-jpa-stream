package io.github.rahulrajsonu.springdatajpademojava.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
public class Customer {
    @Id
    private Long id;
    private String name;
}
