// Peter Idestam-Almquist, 2020-01-31.

package com.company;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {
    // Instance variables.
    private final List<Account> accounts = new ArrayList<Account>();
    volatile Hashtable<Integer, ReentrantReadWriteLock> accountAndLocks = new Hashtable<>(

    );
    ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();
    Object lock = new Object();

    // Instance methods.

    int newAccount(int balance) {
        int accountId;
        accountId = accounts.size(); // FIX ORIGINAL
        Account accountToAdd = new Account(accountId, balance);
        accounts.add(accountToAdd);
        accountAndLocks.put(accountId, new ReentrantReadWriteLock());
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
        synchronized (account) {
            int balance = account.getBalance();
            balance = balance + operation.getAmount();
            account.setBalance(balance);
        }
    }


    private boolean getCurrentLock(int accountId) {

        if (!accountAndLocks.get(accountId).writeLock().tryLock()) {
            return false;
        }
        return true;
    }

    private void releaseCurrentLock(int accountId) {
        accountAndLocks.get(accountId).writeLock().unlock();
    }

    private void releaseCurrentLockOperation(int accountId) {

        if (accountAndLocks.get(accountId).writeLock().isHeldByCurrentThread())
            accountAndLocks.get(accountId).writeLock().unlock();

    }

    private boolean lockOperation(int accountId) {


        if (!accountAndLocks.get(accountId).writeLock().tryLock()) {
            return false;
        }

        return true;

    }

    private boolean lockNecessaryAccounts(List<Operation> operations) {

        for (Operation operation : operations) {
            if (!accountAndLocks.get(operation.getAccountId()).writeLock().tryLock()) {
                return false;
            }

        }
        return true;

    }

    private void releaseNecessaryLocks(List<Operation> operations) {

        for (Operation operation : operations) {
            if (accountAndLocks.get(operation.getAccountId()).writeLock().isHeldByCurrentThread())
                accountAndLocks.get(operation.getAccountId()).writeLock().unlock();


        }
    }


    void runTransaction(Transaction transaction) {
        Random random = new Random();
        List<Operation> currentOperations = transaction.getOperations();

        for (Operation operation : currentOperations) {
            for (int i = 0; i < 100; i++) {
                if (lockNecessaryAccounts(currentOperations)) {
                    try {
                        runOperation(operation);
                    } finally {
                        releaseNecessaryLocks(currentOperations);
                    }
                    break;
                } else {
                    releaseNecessaryLocks(currentOperations);
                }
                try {
                    Thread.sleep(random.nextInt(50)); // Random wait before retry.
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }


    }


    public static void main(String[] args) throws InterruptedException {
/*
        Bank bank = new Bank();

        Account account1 = bank.getAccounts().get(bank.newAccount(0));
        //OPERATION

        long startTime = System.nanoTime();

        //parralelize
        int numThreads = 4;
        int numTransactions = 20000000;
        Transaction[] transactions = new Transaction[numTransactions];
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            transactions[i] = new Transaction(bank);

            for (int j = 0; j < numTransactions / numThreads; j++) {
                transactions[i].add(new Operation(bank, account1.getId(), 1));
            }

            threads[i] = new Thread(transactions[i]);
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }


        long end = System.nanoTime();


        System.out.println("It took time: " + (end - startTime) / 1000000000.0);

    }

*/

    }
}