/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.list.UnmodifiableList;

/**
 * @author Gordon Fraser
 * 
 */
public class Bank {

	private final Set<BankAccount> accounts = new HashSet<BankAccount>();

	// For the sake of having a dependency
        public void addAccounts(UnmodifiableList accountList) {
        	accounts.addAll(accountList);
        }

	public void addAccount(BankAccount account) {
		accounts.add(account);
	}

	public void removeAccount(BankAccount account) {
		accounts.remove(account);
	}

	public boolean hasBankAccount(BankAccount account) {
		return accounts.contains(account);
	}

}
