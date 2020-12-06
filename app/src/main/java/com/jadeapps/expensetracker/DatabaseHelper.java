package com.jadeapps.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "monthly_expense.db";
    public static final int VERSION = 1;

    public static final String TABLE_NAME_1 = "month_income_expense";
    public static final String TABLE_NAME_2 = "expenses";

    public static final String TABLE_2_COL_1 = "ID";
    public static final String TABLE_2_COL_2 = "MONTH_ID";
    public static final String TABLE_2_COL_3 = "PAYMENT_FOR";
    public static final String TABLE_2_COL_4 = "AMOUNT";
    public static final String TABLE_2_COL_5 = "DATE";
    public static final String TABLE_2_COL_6 = "REGULAR";

    public static final String TABLE_1_COL_1 = "ID";
    public static final String TABLE_1_COL_2 = "MONTH";
    public static final String TABLE_1_COL_3 = "YEAR";
    public static final String TABLE_1_COL_4 = "INCOME";

    SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        context.deleteDatabase("monthly_expense.db");
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_2);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_1 + " (" +
                TABLE_1_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
                TABLE_1_COL_2 + " INTEGER NOT NULL DEFAULT 0, \n" +
                TABLE_1_COL_3 + " INTEGER NOT NULL DEFAULT 0, \n" +
                TABLE_1_COL_4 + " DOUBLE NOT NULL DEFAULT 0" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_2 + " (" +
                TABLE_2_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
                TABLE_2_COL_2 + " INTEGER NOT NULL, \n" +
                TABLE_2_COL_3 + " VARCHAR(200) NOT NULL, \n" +
                TABLE_2_COL_4 + " DOUBLE NOT NULL DEFAULT 0, \n" +
                TABLE_2_COL_5 + " VARCHAR(30) NOT NULL, \n" +
                TABLE_2_COL_6 + " INTEGER NOT NULL DEFAULT 0" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_2);
        onCreate(db);
    }

    public List<MonthlyIncomeExpense> allMonthlyIncomeExpense() {
        List<MonthlyIncomeExpense> monthlyIncomeExpenses = new ArrayList<>();

        String MONTHLY_INCOME_EXPENSE_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_NAME_1);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(MONTHLY_INCOME_EXPENSE_SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            monthlyIncomeExpenses.clear();
            do {
                monthlyIncomeExpenses.add(new MonthlyIncomeExpense(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getDouble(3)
                ));
            } while (cursor.moveToNext());
        }

        return monthlyIncomeExpenses;
    }

    public MonthlyIncomeExpense findMonthlyIncomeExpenseById(Integer id) {
        MonthlyIncomeExpense monthlyIncomeExpense = null;

        String MONTHLY_INCOME_EXPENSE_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s='%s'", TABLE_NAME_1, TABLE_1_COL_1, id);

        db =  getReadableDatabase();
        Cursor cursor = db.rawQuery(MONTHLY_INCOME_EXPENSE_SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                monthlyIncomeExpense = new MonthlyIncomeExpense(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getDouble(3)
                );
            } while (cursor.moveToNext());
        }

        return monthlyIncomeExpense;
    }

    public List<Expense> allExpenses() {
        List<Expense> expenses = new ArrayList<>();

        String EXPENSE_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_NAME_2);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EXPENSE_SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            expenses.clear();
            do {
                expenses.add(new Expense(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4),
                        cursor.getInt(5)
                ));
            } while (cursor.moveToNext());
        }
        return expenses;
    }

    public List<Expense> getExpensesByMonthlyIncomeExpenseId(int monthlyIncomeExpenseId) {
        List<Expense> expenses = new ArrayList<>();

        String EXPENSE_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s='%s'", TABLE_NAME_2, TABLE_2_COL_2, monthlyIncomeExpenseId);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EXPENSE_SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            expenses.clear();
            do {
                expenses.add(new Expense(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4),
                        cursor.getInt(5)
                ));
            } while (cursor.moveToNext());
        }
        return expenses;
    }

    public Expense findExpenseById(Integer id) {
        Expense expense = null;

        String EXPENSE_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s='%s'", TABLE_NAME_2, TABLE_2_COL_1, id);

        db =  getReadableDatabase();
        Cursor cursor = db.rawQuery(EXPENSE_SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                expense = new Expense(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4),
                        cursor.getInt(5)
                );
            } while (cursor.moveToNext());
        }

        return expense;
    }

    public boolean addMonthlyIncomeExpense(Integer month, Integer year) {
        double income = 0.0;

        ContentValues addMonthlyIncomeExpenseContentValues = new ContentValues();
        addMonthlyIncomeExpenseContentValues.put(TABLE_1_COL_2, month);
        addMonthlyIncomeExpenseContentValues.put(TABLE_1_COL_3, year);
        addMonthlyIncomeExpenseContentValues.put(TABLE_1_COL_4, income);

        db = getWritableDatabase();

        try {
            db.insert(TABLE_NAME_1, null, addMonthlyIncomeExpenseContentValues);
        } catch(Exception ex) {
            Log.d("", "An error occured while trying to add monthly income/expense ...");
            return false;
        }
        return true;
    }

    public boolean addExpense(Expense expense) {

        ContentValues addExpenseContentValues = new ContentValues();
        addExpenseContentValues.put(TABLE_2_COL_2, expense.getMonthIncomeExpenseId());
        addExpenseContentValues.put(TABLE_2_COL_3, expense.getPaymentFor());
        addExpenseContentValues.put(TABLE_2_COL_4, expense.getAmount());
        addExpenseContentValues.put(TABLE_2_COL_5, expense.getMadeOn());
        addExpenseContentValues.put(TABLE_2_COL_6, expense.getRegular());

        db = getWritableDatabase();

        try {
            db.insert(TABLE_NAME_2, null, addExpenseContentValues);
        } catch(Exception ex) {
            Log.d("", "An error occured while trying to add expense ...");
            return false;
        }
        return true;
    }

    public boolean updateMonthlyIncomeExpense(MonthlyIncomeExpense monthlyIncomeExpense) {
        ContentValues updateMonthlyIncomeExpenseContentValues = new ContentValues();
        updateMonthlyIncomeExpenseContentValues.put(TABLE_1_COL_2, monthlyIncomeExpense.getMonth());
        updateMonthlyIncomeExpenseContentValues.put(TABLE_1_COL_3, monthlyIncomeExpense.getYear());
        updateMonthlyIncomeExpenseContentValues.put(TABLE_1_COL_4, monthlyIncomeExpense.getIncome());

        db = getWritableDatabase();

        String WHERE_CLAUSE = TABLE_1_COL_1 + " = ?";

        try {
            db.update(TABLE_NAME_1, updateMonthlyIncomeExpenseContentValues, WHERE_CLAUSE, new String[]{monthlyIncomeExpense.getId().toString()});
        } catch(Exception ex) {
            Log.d("", "An error occured while trying to update monthly income/expense ...");
            return false;
        }
        return true;
    }

    private Date getDateFromCursor(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        Date dateInstance;

        try {
            dateInstance = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return dateInstance;
    }

    public boolean deleteMonthlyIncomeExpenseFromDB(MonthlyIncomeExpense monthlyIncomeExpense) {

        String MONTHLY_INCOME_EXPENSE_DELETE_QUERY = String.format("DELETE FROM  %s WHERE %s = '%s'", TABLE_NAME_1, TABLE_1_COL_1, String.valueOf(monthlyIncomeExpense.getId()));

        db = getWritableDatabase();

        try {
            db.execSQL(MONTHLY_INCOME_EXPENSE_DELETE_QUERY);
        } catch(Exception ex) {
            Log.d("", "An error occured while trying to deleting expense ...");
            return false;
        }
        return true;
    }

    public boolean deleteExpenseFromDB(Expense expense) {
        String EXPENSE_DELETE_QUERY = String.format("DELETE FROM  %s WHERE %s = '%s'", TABLE_NAME_2, TABLE_2_COL_1, String.valueOf(expense.getId()));

        db = getWritableDatabase();

        try {
            db.execSQL(EXPENSE_DELETE_QUERY);
        } catch(Exception ex) {
            Log.d("", "An error occured while trying to deleting expense ...");
            return false;
        }
        return true;
    }

    public boolean updateExpense(Expense expense) {
        ContentValues updateExpenseContentValues = new ContentValues();
        updateExpenseContentValues.put(TABLE_2_COL_2, expense.getMonthIncomeExpenseId());
        updateExpenseContentValues.put(TABLE_2_COL_3, expense.getPaymentFor());
        updateExpenseContentValues.put(TABLE_2_COL_4, expense.getAmount());
        updateExpenseContentValues.put(TABLE_2_COL_5, expense.getMadeOn());
        updateExpenseContentValues.put(TABLE_2_COL_6, expense.getRegular());

        db = getWritableDatabase();

        String WHERE_CLAUSE = TABLE_2_COL_1 + " = ?";

        try {
            db.update(TABLE_NAME_2, updateExpenseContentValues, WHERE_CLAUSE, new String[]{expense.getId().toString()});
        } catch(Exception ex) {
            Log.d("", "An error occured while trying to update expense ...");
            return false;
        }
        return true;
    }
}
