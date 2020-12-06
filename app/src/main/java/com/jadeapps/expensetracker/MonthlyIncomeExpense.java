package com.jadeapps.expensetracker;

import java.util.List;

public class MonthlyIncomeExpense {
    private Integer id, month, year;
    private double income;

    public MonthlyIncomeExpense() {}

    public MonthlyIncomeExpense(Integer id, Integer month, Integer year, double income) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.income = income;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }
}
