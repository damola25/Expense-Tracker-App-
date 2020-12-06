package com.jadeapps.expensetracker;

import java.util.Date;

public class Expense {
    private Integer id, monthIncomeExpenseId;
    private double amount;
    private String paymentFor, madeOn;
    private int regular;

    public Expense(Integer id, Integer monthIncomeExpenseId, String paymentFor, double amount, String madeOn, int regular) {
        this.id = id;
        this.monthIncomeExpenseId = monthIncomeExpenseId;
        this.paymentFor = paymentFor;
        this.amount = amount;
        this.madeOn = madeOn;
        this.regular = regular;

    }

    public Expense() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMonthIncomeExpenseId() {
        return monthIncomeExpenseId;
    }

    public void setMonthIncomeExpenseId(Integer monthIncomeExpenseId) {
        this.monthIncomeExpenseId = monthIncomeExpenseId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentFor() {
        return paymentFor;
    }

    public void setPaymentFor(String paymentFor) {
        this.paymentFor = paymentFor;
    }

    public String getMadeOn() {
        return madeOn;
    }

    public void setMadeOn(String madeOn) {
        this.madeOn = madeOn;
    }

    public int getRegular() {
        return regular;
    }

    public void setRegular(int regular) {
        this.regular = regular;
    }
}
