package com.examples.with.different.packagename;

public class ATM {

    private ATMCard insertedCard = null;

    private enum Status {
        IDLE, CARD_INSERTED, AUTHORIZED
    }

    private Status status = Status.IDLE;

    private final Bank bank;

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public void insertCard(ATMCard card) {
        if (card.isEnabled()) {
            if (bank.hasBankAccount(card.getAccount())) {
                this.insertedCard = card;
                status = Status.CARD_INSERTED;
            } else {
                ejectCard();
            }
        }
    }

    public void ejectCard() {
        this.insertedCard = null;
        status = Status.IDLE;
    }

    public void enterPin(int[] pin) {
        if (status == Status.CARD_INSERTED) {
            int[] realPin = insertedCard.getPin();
            if (realPin.length != pin.length)
                return;

            for (int i = 0; i < realPin.length; i++) {
                if (realPin[i] != pin[i])
                    return;
            }
            status = Status.AUTHORIZED;
        }
    }

    public void deposit(int amount) {
        if (status == Status.AUTHORIZED) {
            insertedCard.getAccount().deposit(amount);
        }
    }

    public void withdraw(int amount) {
        if (status == Status.AUTHORIZED) {
            if (!insertedCard.getAccount().withdraw(amount)) {
                insertedCard.invalidate();
            }
            ejectCard();
        }
    }

}
