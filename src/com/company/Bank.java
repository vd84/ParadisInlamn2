// Peter Idestam-Almquist, 2020-01-31.

package com.company;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {
    // Instance variables.
    private final List<Account> accounts = new ArrayList<Account>();

    ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();

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


        int balance = account.getBalance();
        balance = balance + operation.getAmount();
        account.setBalance(balance);

    }

    void runTransaction(Transaction transaction) {
        List<Operation> currentOperations = transaction.getOperations();
        for (Operation operation : currentOperations) {

            runOperation(operation);

        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    private void lockTransactionLock() {
        transactionLock.writeLock();
        try {
            transactionLock.isWriteLocked();
        } finally {
            transactionLock.isWriteLocked();
        }

    }


    public static void main(String[] args) throws InterruptedException {
        Bank bank = new Bank();

        Account account1 = bank.getAccounts().get(bank.newAccount(0));
        Account account2 = bank.getAccounts().get(bank.newAccount(0));
        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());
        //OPERATION

        long startTime = System.nanoTime();

        //parralelize
        int numThreads = 9;
        int numTransactions = 1000;
        Transaction[] transactions = new Transaction[numTransactions];
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            transactions[i] = new Transaction(bank);

            for (int j = 0; j < numTransactions / numThreads; j++) {
                transactions[i].add(new Operation(bank, account1.getId(), 100));
            }

            threads[i] = new Thread(transactions[i]);
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }


        System.out.println(account1.getBalance());
        System.out.println(account2.getBalance());

        long end = System.nanoTime();


        System.out.println("It took time: " + (end - startTime) / 1000000000.0);


    }


}
