package repository;

import entity.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerRepository {
    private final Map<String, Customer> customersById = new HashMap<>();

    public List<Customer> findAll() {
        return new ArrayList<>(customersById.values());
    }

    public void save(Customer customer) {
        customersById.put(customer.getId(), customer);
    }

}
