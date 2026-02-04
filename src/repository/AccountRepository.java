package repository;

import entity.Account;

import java.util.*;

public class AccountRepository {
    private final Map<String, Account> accountsByNumber = new HashMap<>();

    public void save(Account account) {
        accountsByNumber.put(account.getAccountNumber(), account);
    }

    public List<Account> findAll() {
        return new ArrayList<>(accountsByNumber.values());
    }

    public Optional<Account> findByNumber(String accountNumber) {
        return Optional.ofNullable(accountsByNumber.get(accountNumber));
    }

    public List<Account> findByCustomerId(String customerId) {
        List<Account> result = new ArrayList<>();
        for(Account account : accountsByNumber.values()) {
            if(account.getCustomerId().equals(customerId)) {
                result.add(account);
            }
        }
        return result;
    }


}
