package me.wonwoo.springrecords;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootApplication(proxyBeanMethods = false)
@EnableConfigurationProperties(CustomerProperties.class)
public class SpringRecordsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRecordsApplication.class, args);
    }

}

record Product(String id, String productName) {

}


interface ProductRepository extends MongoRepository<Product, String> {

}


@RestController
@RequestMapping("/products")
class ProductController {

    private final ProductRepository productRepository;

    ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> findByProduct() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product saveProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }
}


@ConfigurationProperties(prefix = "customer")
record CustomerProperties(String message) {

}


@RestController
@RequestMapping("/customers")
class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerProperties customerProperties;

    CustomerController(CustomerRepository customerRepository, CustomerProperties customerProperties) {
        this.customerRepository = customerRepository;
        this.customerProperties = customerProperties;
    }

    @GetMapping("/hi")
    public String hi() {
        return customerProperties.message();
    }

    @GetMapping("/find-first-name")
    public List<Customer> findByCustomers(@ModelAttribute Customer customer) {
        return customerRepository.findByFirstName(customer);
    }

    @GetMapping
    public List<Customer> findByCustomers() {
        return customerRepository.findByCustomers();
    }

    @GetMapping("/{id}")
    public Customer findByCustomer(@PathVariable Long id) {
        return customerRepository.findByCustomer(id);
    }

    @PostMapping
    public int saveCustomer(@RequestBody Customer customer) {
        return customerRepository.saveCustomer(customer);
    }
}


@Repository
class CustomerRepository {

    private final NamedParameterJdbcTemplate template;

    CustomerRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public int saveCustomer(Customer customer) {
        return template.update("""
                         insert 
                             into
                         customer
                             (first_name, last_name)
                        values
                             (:firstName, :lastName)
                        """,
                new BeanPropertySqlParameterSource(customer)
        );
    }

    public List<Customer> findByCustomers() {
        return template.query("""
                        select 
                            id, 
                            first_name, 
                            last_name 
                        from
                            customer 
                        """,
                new DataClassRowMapper<>(Customer.class));
    }

    public Customer findByCustomer(Long id) {
        return DataAccessUtils.uniqueResult(template.query("""
                        select 
                            id, 
                            first_name, 
                            last_name 
                        from
                            customer 
                        where 
                            id = :id
                        """,
                new MapSqlParameterSource("id", id),
                new DataClassRowMapper<>(Customer.class)));
    }

    public List<Customer> findByFirstName(Customer customer) {
        return template.query("""
                        select 
                            id, 
                            first_name, 
                            last_name 
                        from
                            customer
                        where 
                            first_name = :firstName
                        """,
                new BeanPropertySqlParameterSource(customer),
                new DataClassRowMapper<>(Customer.class));

    }
}

record Customer(Long id, String firstName, String lastName) {
}
