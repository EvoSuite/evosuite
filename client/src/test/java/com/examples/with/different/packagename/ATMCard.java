/**
 * 
 */
package com.examples.with.different.packagename;

/**
 * @author Gordon Fraser
 * 
 */
public class ATMCard {

	private Owner owner;

	private BankAccount account;

	private int[] pin;

	private boolean enabled = true;

	/**
	 * @param owner
	 * @param account
	 * @param pin
	 */
	public ATMCard(Owner owner, BankAccount account, int[] pin) {
		this.owner = owner;
		this.account = account;
		this.pin = pin;
	}

	/**
	 * @return the owner
	 */
	public Owner getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	/**
	 * @return the account
	 */
	public BankAccount getAccount() {
		return account;
	}

	/**
	 * @param account
	 *            the account to set
	 */
	public void setAccount(BankAccount account) {
		this.account = account;
	}

	/**
	 * @return the pin
	 */
	public int[] getPin() {
		return pin;
	}

	/**
	 * @param pin
	 *            the pin to set
	 */
	public void setPin(int[] pin) {
		this.pin = pin;
	}

	public void invalidate() {
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
