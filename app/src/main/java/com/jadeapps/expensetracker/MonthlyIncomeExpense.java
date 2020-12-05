package com.jadeapps.expensetracker;

import java.util.List;

public class MonthlyIncomeExpense {
    private int month;
    private int year;
    private double income;
    private List<Expense> expenses;

    public MonthlyIncomeExpense(int month, int year, double income, List<Expense> expenses) {
        this.month = month;
        this.year = year;
        this.income = income;
        this.expenses = expenses;
    }

    public MonthlyIncomeExpense() {
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
