// Peter Idestam-Almquist, 2020-01-31.

package com.company;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {
    // Instance variables.
    private final List<Account> accounts = new ArrayList<Account>();

    final ReentrantLock lock = new ReentrantLock();
    //ReentrantLock transactionLock = new ReentrantLock();

    // Instance methods.

    int newAccount(int balance) {
        int accountId;
        accountId = accounts.size(); // FIX ORIGINAL
        accounts.add(new Account(accountId, balance));
        return accountId;
    }

    int getAccountBalance(int accountId) {
        Account account = null;
        account = accounts.get(accountId);
        return account.getBalance();
    }

    void runOperation(Operation operation) {
        Account account = null;
        account = accounts.get(operation.getAccountId());

        synchronized (lock) {
            int balance = account.getBalance();
            balance = balance + operation.getAmount();
            account.setBalance(balance);
        }
    }

    void runTransaction(Transaction transaction) {
        List<Operation> currentOperations = transaction.getOperations();
        for (Operation operation : currentOperations) {
            synchronized (lock) {
                runOperation(operation);
            }
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    private void lockTransactionLock() {
        lock.lock();
        try {
            lock.tryLock();
        } finally {
            lock.unlock();
        }

    }


    public static void main(String[] args) throws InterruptedException {
        Bank bank = new Bank();

        Account account1 = bank.getAccounts().get(bank.newAccount(100));
        Account account2 = bank.getAccounts().get(bank.newAccount(0));
        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());
        //OPERATION
        Operation operation1 = new Operation(bank, account1.getId(), 100);
        Operation operation2 = new Operation(bank, account1.getId(), -100);
        Operation operation3 = new Operation(bank, account1.getId(), 100);
        Operation operation4 = new Operation(bank, account1.getId(), -100);
        Operation operation5 = new Operation(bank, account1.getId(), 100);

        operation1.run();
        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());

        //TRANSACTION
        Transaction transaction = new Transaction(bank);
        Transaction transaction2 = new Transaction(bank);


        transaction.add(operation1);
        transaction.add(operation2);
        transaction2.add(operation3);
        transaction2.add(operation4);
        transaction2.add(operation5);


        Thread thread1 = new Thread(transaction);
        Thread thread2 = new Thread(transaction2);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());


    }


}
