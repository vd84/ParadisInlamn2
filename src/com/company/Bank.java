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
    private List<ReentrantLock> reentrantLocks = new ArrayList<>();
    HashMap<Account, ReentrantLock> accountAndLocks = new HashMap<>();
    ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();

    // Instance methods.

    int newAccount(int balance) {
        int accountId;
        accountId = accounts.size(); // FIX ORIGINAL
        Account accountToAdd = new Account(accountId, balance);
        accounts.add(accountToAdd);
        accountAndLocks.put(accountToAdd, new ReentrantLock());
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


        accountAndLocks.get(accounts.get(operation.getAccountId())).lock();
            try {
                int balance = account.getBalance();
                balance = balance + operation.getAmount();
                account.setBalance(balance);
            } finally {
                accountAndLocks.get(accounts.get(operation.getAccountId())).unlock();
            }


    }

    private boolean getAllLocks() {
        for (Map.Entry<Account, ReentrantLock> accountReentrantLockEntry : accountAndLocks.entrySet()) {
            if (!accountReentrantLockEntry.getValue().tryLock()) {
                return false;
            }
        }
        return true;
    }

    private void releaseAllLocks() {



        for (Map.Entry<Account, ReentrantLock> accountReentrantLockEntry : accountAndLocks.entrySet()) {
            accountReentrantLockEntry.getValue().unlock();

        }

    }


    void runTransaction(Transaction transaction) {
        Random random = new Random();
        List<Operation> currentOperations = transaction.getOperations();
        for (Operation operation : currentOperations) {

            for (int i = 0; i < 100; i++) {
                if (getAllLocks()) {
                    try {
                        runOperation(operation);
                    } finally {
                        releaseAllLocks();

                        }

                    }
                    break;
                } else {
                    try {
                        Thread.sleep(random.nextInt(100)); // Random wait before retry.
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }


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
        //OPERATION

        long startTime = System.nanoTime();

        //parralelize
        int numThreads = 4;
        int numTransactions = 20;
        Transaction[] transactions = new Transaction[numTransactions];
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            transactions[i] = new Transaction(bank);

            for (int j = 0; j < numTransactions / numThreads; j++) {
                transactions[i].add(new Operation(bank, account1.getId(), 1));
            }
            System.out.println("There are " + transactions[i].getOperations().size() + " of operations to be done");

            threads[i] = new Thread(transactions[i]);
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }


        System.out.println(account1.getBalance());

        long end = System.nanoTime();


        System.out.println("It took time: " + (end - startTime) / 1000000000.0);

    }


}
