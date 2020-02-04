// Peter Idestam-Almquist, 2018-02-21.

package com.company;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Program {
	// Static variables.
	private static final int NUM_THREADS = 100;
	private static final int NUM_ACCOUNTS = 6;
	private static final int FACTOR = 100000;
	private static final int TIMEOUT = 60; // Seconds;
	private static final int NUM_TRANSACTIONS = NUM_ACCOUNTS * FACTOR;
	private static Integer[] accountIds = new Integer[NUM_ACCOUNTS];
	private static Operation[] withdrawals = new Operation[NUM_ACCOUNTS];
	private static Operation[] deposits = new Operation[NUM_ACCOUNTS];
	private static Bank bank = new Bank();

	// Static methods.

	private static void initiate() {
		for (int i = 0; i < NUM_ACCOUNTS; i++) {
			accountIds[i] = bank.newAccount(1000);
		}

		for (int i = 0; i < NUM_ACCOUNTS; i++) {
			withdrawals[i] = new Operation(bank, accountIds[i], -100);;
		}

		for (int i = 0; i < NUM_ACCOUNTS; i++) {
			deposits[i] = new Operation(bank, accountIds[i], +100);;
		}
	}

	// You may use this test to test thread-safety for operations.
	private static void runTestOperations() {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

		Operation[] operations = new Operation[NUM_TRANSACTIONS * 2];
		for (int i = 0; i < NUM_TRANSACTIONS; i++) {
			operations[i * 2] = withdrawals[i % NUM_ACCOUNTS];
			operations[(i * 2) + 1] = deposits[(i + 1) % NUM_ACCOUNTS];
		}

		try {
			long time = System.nanoTime();
			for (int i = 0; i < NUM_TRANSACTIONS * 2; i++) {
				executor.execute(operations[i]);
			}
			executor.shutdown();
			boolean completed = executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
			time = System.nanoTime() - time;

			System.out.println("Test operations finished.");
			System.out.println("Completed: " + completed);
			System.out.println("Time [ms]: " + time / 1000000);

			for (int i = 0; i < NUM_ACCOUNTS; i++) {
				int balance = bank.getAccountBalance(accountIds[i]);
				System.out.println("Account: " + accountIds[i] + "; Balance: " + balance);
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	// You may use this test to test thread-safety for transactions.
	private static void runTestTransactions() {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

		Transaction[] transactions = new Transaction[NUM_TRANSACTIONS];
		for (int i = 0; i < NUM_TRANSACTIONS; i++) {
			transactions[i] = new Transaction(bank);
			transactions[i].add(withdrawals[i % NUM_ACCOUNTS]);
			transactions[i].add(deposits[(i + 1) % NUM_ACCOUNTS]);
		}

		try {
			long time = System.nanoTime();
			for (int i = 0; i < NUM_TRANSACTIONS; i++) {
				executor.execute(transactions[i]);
			}
			executor.shutdown();
			boolean completed = executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
			time = System.nanoTime() - time;

			System.out.println("Test transactions finished.");
			System.out.println("Completed: " + completed);
			System.out.println("Time [ms]: " + time / 1000000);

			for (int i = 0; i < NUM_ACCOUNTS; i++) {
				int balance = bank.getAccountBalance(accountIds[i]);
				System.out.println("Account: " + accountIds[i] + "; Balance: " + balance);
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	// Entry point.
	public static void main(String[] args) {
		initiate();
		runTestOperations();
		runTestTransactions();
	}
}
