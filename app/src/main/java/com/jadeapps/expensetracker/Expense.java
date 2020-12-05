package com.jadeapps.expensetracker;

import java.util.Date;

public class Expense {
    private String label;
    private double amount;
    private Date expenseDate;

    public Expense(String label, double amount, Date expenseDate) {
        this.label = label;
        this.amount = amount;
        this.expenseDate = expenseDate;
    }

    public Expense() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }
}
