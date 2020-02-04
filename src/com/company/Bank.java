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
    volatile Hashtable<Account, ReentrantLock> accountAndLocks = new Hashtable<>(

    );
    ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();
    Object lock = new Object();

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

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            if (getCurrentLock(account)) {
                try {
                    int balance = account.getBalance();
                    balance = balance + operation.getAmount();
                    account.setBalance(balance);
                } finally {
                    releaseCurrentLock(account);
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

    private boolean getCurrentLock(Account account) {

        return accountAndLocks.get(account).tryLock();
    }

    private void releaseCurrentLock(Account account) {
        accountAndLocks.get(account).unlock();
    }

    private void releaseCurrentLockOperation(List<Operation> operations) {
        for (Operation o : operations){
        if (accountAndLocks.get(accounts.get(o.getAccountId())).isHeldByCurrentThread())
            accountAndLocks.get(accounts.get(o.getAccountId())).unlock();
    }
}

    private boolean getAllLocks(List<Operation> operations) {

        for (Operation o : operations) {
            if (!accountAndLocks.get(accounts.get(o.getAccountId())).tryLock()) {
                return false;
            }

        }
        return true;


    }

    private void releaseAllLocks(List<Operation> operations) {

        for (Operation o : operations) {
            if (accountAndLocks.get(accounts.get(o.getAccountId())).isHeldByCurrentThread())
                accountAndLocks.get(accounts.get(o.getAccountId())).unlock();


        }
    }

    private boolean lockOperation(List<Operation> operations) {


        for (Operation o : operations) {
            if (!accountAndLocks.get(accounts.get(o.getAccountId())).tryLock()) {
                return false;
            }
        }
        return true;

    }


    void runTransaction(Transaction transaction) {
        Random random = new Random();
        List<Operation> currentOperations = transaction.getOperations();

        for (Operation operation : currentOperations) {
            for (int i = 0; i < 100; i++) {
                if (lockOperation(currentOperations)) {
                    try {
                        runOperation(operation);
                    } finally {
                        releaseCurrentLockOperation(currentOperations);
                    }
                    break;
                } else {
                    releaseCurrentLockOperation(currentOperations);
                }
                try {
                    Thread.sleep(random.nextInt(100)); // Random wait before retry.
                } catch (InterruptedException e) {
                    System.out.println(e);
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
        int numThreads = 20;
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
