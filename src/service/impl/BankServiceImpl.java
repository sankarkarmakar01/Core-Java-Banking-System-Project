package service.impl;

import domain.Type;
import entity.Account;
import entity.Customer;
import entity.Transaction;
import exception.AccountNotFoundException;
import exception.InsufficientFundsException;
import exception.ValidationException;
import repository.AccountRepository;
import repository.CustomerRepository;
import repository.TransactionRepository;
import service.BankService;
import util.Validation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankServiceImpl implements BankService {

    private final AccountRepository accountRepository = new AccountRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();

    private final Validation<String> validateName = name -> {
        if(name == null || name.isBlank()) {
            throw new ValidationException("Name is requires");
        }
    };

    private final Validation<String> validateEmail = email -> {
        if(email == null || !email.contains("@")) {
            throw new ValidationException("Valid email is required");
        }
    };

    private final Validation<String> validateType = type -> {
        if(type == null || !(type.equalsIgnoreCase("SAVINGS") || type.contains("CURRENT"))) {
            throw new ValidationException("Type must be SAVINGS or CURRENT");
        }
    };

    private final Validation<Double> validateAmountPositive = amount -> {
        if(amount == null || amount < 0) {
            throw new ValidationException("Please enter valid amount");
        }
    };

    @Override
    public String openAccount(String name, String email, String accountType) {
        validateName.validate(name);
        validateEmail.validate(email);
        validateType.validate(accountType);

        String customerId = UUID.randomUUID().toString();

        // Create customer
        Customer customer = new Customer(customerId, name, email);
        customerRepository.save(customer);

        // CHANGE LATER --> 10 + 1 = AC000011 --> AC<06>
        // String accountNumber = UUID.randomUUID().toString();

        String accountNumber = getAccountNumber();
        Account account = new Account(accountNumber, customerId, (double) 0, accountType);
        accountRepository.save(account);
        return accountNumber;
    }

    @Override
    public List<Account> listAccounts() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .collect(Collectors.toList());
    }

    @Override
    public void deposit(String accountNumber, Double amount, String note) {
        validateAmountPositive.validate(amount);
        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        account.setBalance(account.getBalance() + amount);
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), Type.DEPOSIT, account.getAccountNumber(), amount, LocalDateTime.now(), note);
        transactionRepository.add(transaction);
    }

    @Override
    public void withdraw(String accountNumber, Double amount, String note) {
        validateAmountPositive.validate(amount);
        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        if (account.getBalance().compareTo(amount) < 0)
            throw new InsufficientFundsException("Insufficient Balance");
        account.setBalance(account.getBalance() - amount);
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), Type.WITHDRAW, account.getAccountNumber(), amount, LocalDateTime.now(), note);
        transactionRepository.add(transaction);
    }

    @Override
    public void transfer(String fromAcc, String toAcc, Double amount, String note) {
        validateAmountPositive.validate(amount);
        if (fromAcc.equals(toAcc))
            throw new ValidationException("Cannot transfer to your own account");
        Account from = accountRepository.findByNumber(fromAcc)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + fromAcc));
        Account to = accountRepository.findByNumber(toAcc)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + toAcc));
        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientFundsException("Insufficient Balance");

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        transactionRepository.add(new Transaction(UUID.randomUUID().toString(), Type.TRANSFER_OUT, from.getAccountNumber(), amount, LocalDateTime.now(), note));

        transactionRepository.add(new Transaction(UUID.randomUUID().toString(), Type.TRANSFER_IN, to.getAccountNumber(), amount, LocalDateTime.now(), note));

    }

    @Override
    public List<Transaction> getStatement(String account) {
        return transactionRepository.findByAccount(account).stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> searchAccountByCustomerName(String q) {
        String query = (q == null) ? "" : q.toLowerCase();

        return customerRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(query))
                .flatMap(c -> accountRepository.findByCustomerId(c.getId()).stream())
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .collect(Collectors.toList());
    }

    private String getAccountNumber() {
        int size = accountRepository.findAll().size() + 1;
        return String.format("AC%06d", size);
    }

}
