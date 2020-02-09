/**
 * 
 */
package com.examples.with.different.packagename;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class BankAccount {

	protected int balance = 0;

	protected final Owner owner;

	public BankAccount(Owner owner) {
		this.owner = owner;
	}

	public int getBalance() {
		return balance;
	}

	public Owner getOwner() {
		return owner;
	}

	public abstract void deposit(int amount);

	public abstract boolean withdraw(int amount);

}
