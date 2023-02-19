package io.github.rahulrajsonu.springdatajpademojava.controllers;

import io.github.rahulrajsonu.springdatajpademojava.domain.Customer;
import io.github.rahulrajsonu.springdatajpademojava.domain.CustomerDto;
import io.github.rahulrajsonu.springdatajpademojava.repositories.CustomRepository;
import io.github.rahulrajsonu.springdatajpademojava.repositories.CustomerRepository;
import io.github.rahulrajsonu.springdatajpademojava.specifications.CustomerSpecification;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomRepository customRepository;

    private String getCsvRowFromCustomer(Customer customer) {
        return String.format("%d,%s\n", customer.getId(), customer.getName());
    }

    private String getCsvRowFromCustomerDto(CustomerDto customer) {
        return String.format("%d,%s\n", customer.getId(), customer.getName());
    }

    private void setCsvParams(final HttpServletResponse response) {
        // not important; basically sets csv params so clients can understand it's a csv
        response.setContentType("application/csv");
        response.setHeader("Content-Disposition", "attachment;filename=customers.csv");
    }

    private void writeCustomersToResponseAsCsv(Stream<Customer> customerStream,
                                final HttpServletResponse response) throws IOException {
        setCsvParams(response);
        PrintWriter printWriter = response.getWriter();
        printWriter.write("id,name\n"); // the CSV column header, not really important here
        customerStream.peek(customer -> printWriter.write(getCsvRowFromCustomer(customer)))
            .forEach(entityManager::detach); // optional, but objects _may_ not be GC'd if you don't detach them first.
        printWriter.flush();
        printWriter.close();
    }

    @Transactional(readOnly = true)
    public void writeCustomerDtoToResponseAsCsv(Stream<CustomerDto> customerStream,
                                               final HttpServletResponse response) throws IOException {
        setCsvParams(response);
        PrintWriter printWriter = response.getWriter();
        try {
            printWriter.write("id,name\n"); // the CSV column header, not really important here

            customerStream.forEach(customer -> {
                String str = getCsvRowFromCustomerDto(customer);
                printWriter.write(str);
            });
        }catch (Exception ex){
            log.info("Error streaming the customer, Error: {}",ex.getMessage());
            throw ex;
        }finally {
            customerStream.close();
            printWriter.flush();
            printWriter.close();
        }
    }

    @GetMapping("/customers_fragment.csv")
    @Transactional(readOnly = true) // this is important, because Streams can only be opened in a transaction
    public void getCustomersCsv(
            final HttpServletResponse response,
            @RequestParam(required = false) final String name
    ) throws IOException {
        Specification<Customer> specification = CustomerSpecification.hasName(name);
        writeCustomersToResponseAsCsv(customerRepository.stream(specification, Customer.class), response);
    }

    @GetMapping("/customers_page_by_page.csv")
    public void getCustomersCsvPageByPage(
            final HttpServletResponse response,
            @RequestParam(required = false) final String name
    ) throws IOException {
        setCsvParams(response);
        PrintWriter printWriter = response.getWriter();
        Specification<Customer> specification = CustomerSpecification.hasName(name);
        Page<Customer> customerPage;
        int page = 0;
        do {
            customerPage = customerRepository.findAll(specification, PageRequest.of(page, 10));
            customerPage.getContent()
                    .forEach(customer -> printWriter.write(getCsvRowFromCustomer(customer)));
            page++;
        } while (customerPage.hasNext());
    }

    @GetMapping("/customer_findall")
    public String getCustomersCsvPageByPage(final HttpServletResponse response) {
        setCsvParams(response);
        Iterable<Customer> customers = customerRepository.findAll(CustomerSpecification.hasName(""));
        return StreamSupport.stream(customers.spliterator(), false)
                .map(customer -> String.format("%d,%s", customer.getId(), customer.getName()))
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    @GetMapping("/customer_findall_stream")
    public void getCustomersCsv(final HttpServletResponse response) throws IOException {
        writeCustomerDtoToResponseAsCsv(this.customRepository.streamAllCustomer(),response);
    }
}
