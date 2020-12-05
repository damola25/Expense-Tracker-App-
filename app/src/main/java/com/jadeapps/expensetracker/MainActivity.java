package com.jadeapps.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String euroSymbol= "Eur ";
    public static final String[] calendarMonthsString = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    Toolbar toolbar;
    LinearLayout mainActivityLinearLayout, appLoaderLinearLayout, surplusDeficitLinearLayout;
    ScrollView expenseScrollView;
    TextView expenseListIsEmptyTextView, monthIncomeAmount, monthlyIncomeLabel, monthlyExpenseLabel, surplusDeficitTextView;
    ListView expenseListView;
    ProgressBar appLoaderPregressbar;
    EditText expenseTitleEditText, expenseAmountEditText, incomeEditText;
    ExpenseListViewAdapter expenseListViewAdapter;
    MonthlyIncomeExpense monthlyIncomeExpense;


    Calendar calendar;
    int year;
    int month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainActivityLinearLayout = (LinearLayout) findViewById(R.id.mainActivityLinearLayout);
        appLoaderLinearLayout = (LinearLayout) findViewById(R.id.appLoaderLinearLayout);
        expenseScrollView = (ScrollView) findViewById(R.id.expenseScrollView);
        expenseListIsEmptyTextView = (TextView) findViewById(R.id.expenseListIsEmptyTextView);
        monthIncomeAmount = (TextView) findViewById(R.id.monthIncomeAmount);
        monthlyIncomeLabel = (TextView) findViewById(R.id.monthlyIncomeLabel);
        monthlyExpenseLabel = (TextView) findViewById(R.id.monthlyExpenseLabel);
        expenseListView = (ListView) findViewById(R.id.expenseListView);
        surplusDeficitLinearLayout = (LinearLayout) findViewById(R.id.surplusDeficitLinearLayout);
        surplusDeficitTextView = (TextView) findViewById(R.id.surplusDeficitTextView);
        appLoaderPregressbar = (ProgressBar) findViewById(R.id.appLoaderPregressbar);

        int selectedMonthIndex = getIntent().getIntExtra("MONTH_INDEX", -1);
        int selectedYear = getIntent().getIntExtra("YEAR", -1);

        if (selectedMonthIndex >= 0 && selectedYear >= 0) {
            year = selectedYear;
            month = selectedMonthIndex;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
            }
        }

        loadApplicationState();
    }

    private void loadApplicationState() {
        mainActivityLinearLayout.setVisibility(View.GONE);
        appLoaderLinearLayout.setVisibility(View.VISIBLE);
        appLoaderPregressbar.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle((calendarMonthsString[month] + " " + year).toUpperCase());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, VersionsActivity.class));
            }
        });
        toolbar.setVisibility(View.VISIBLE);

        initializeSelectedMonthIncomeExpense();

        monthlyIncomeLabel.setText("Income".toUpperCase());
        monthlyExpenseLabel.setText("Expenses".toUpperCase());
        monthIncomeAmount.setText(euroSymbol + String.format("%.2f", monthlyIncomeExpense.getIncome()));

        computeSurplusDeficitIncome();

        if (monthlyIncomeExpense.getExpenses().size() <= 0) {
            expenseListIsEmptyTextView.setVisibility(View.VISIBLE);
            expenseListView.setVisibility(View.GONE);
        } else {
            updateListViewComponent();
        }

        mainActivityLinearLayout.setVisibility(View.VISIBLE);
        appLoaderLinearLayout.setVisibility(View.GONE);
        appLoaderPregressbar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateIncome:
                updateIncome();
                return true;

            case R.id.addExpense:
                addExpense();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addExpense() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_expense_dialog_view, null);
        builder.setView(v);

        expenseTitleEditText = (EditText) v.findViewById(R.id.expenseTitleEditText);
        expenseAmountEditText = (EditText) v.findViewById(R.id.expenseAmountEditText);

        builder.setPositiveButton("Add Expense", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String expenseTitle = expenseTitleEditText.getText().toString().trim();
                String strExpenseAmount = expenseAmountEditText.getText().toString().trim();
                Date date = new Date();

                if (!TextUtils.isEmpty(expenseTitle) && !TextUtils.isEmpty(strExpenseAmount)) {
                    updateRootMonthlyIncomeExpenseObjectExpenses(new Expense(expenseTitle, Double.parseDouble(strExpenseAmount), date));
                    initializeSelectedMonthIncomeExpense();
                    if (expenseListViewAdapter == null) {
                        updateListViewComponent();
                    } else {
                        expenseListViewAdapter.notifyDataSetChanged();
                    }
                    computeSurplusDeficitIncome();
                    Toast.makeText(MainActivity.this, "Expense item created..", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateIncome() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.income_dialog_view, null);
        builder.setView(v);
        incomeEditText = (EditText) v.findViewById(R.id.incomeEditText);
        builder.setPositiveButton("Update Income", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strIncomeForTheMonth = incomeEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(strIncomeForTheMonth)) {
                    updateRootMonthlyIncomeExpenseObjectIncome(Double.parseDouble(strIncomeForTheMonth));
                    initializeSelectedMonthIncomeExpense();
                    monthIncomeAmount.setText(euroSymbol + String.format("%.2f", monthlyIncomeExpense.getIncome()));
                    computeSurplusDeficitIncome();
                    Toast.makeText(MainActivity.this, "Income value has been updated.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid operation, please insert income amount to update income value", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void computeSurplusDeficitIncome() {
        double surplusDeficit = 0.0;
        double totalExpenses = 0.0;

        for (int i=0; i<monthlyIncomeExpense.getExpenses().size(); i++) {
            totalExpenses += monthlyIncomeExpense.getExpenses().get(i).getAmount();
        }

        surplusDeficit = monthlyIncomeExpense.getIncome() - totalExpenses;
        String surplusDeficitType = surplusDeficit >= 0 ? "Income Surplus:  " : "Income Deficit:  ";
        surplusDeficitTextView.setText(surplusDeficitType + euroSymbol + String.format("%.2f", Math.abs(surplusDeficit)));

        if (surplusDeficit < 0) {
            surplusDeficitLinearLayout.setBackgroundColor(getResources().getColor(R.color.deficitBackground));
            surplusDeficitTextView.setTextColor(getResources().getColor(R.color.deficitText));
        } else if (surplusDeficit == 0) {
            surplusDeficitLinearLayout.setBackgroundColor(getResources().getColor(R.color.warningBackground));
            surplusDeficitTextView.setTextColor(getResources().getColor(R.color.warningText));
        } else {
            surplusDeficitLinearLayout.setBackgroundColor(getResources().getColor(R.color.surplusBackground));
            surplusDeficitTextView.setTextColor(getResources().getColor(R.color.surplusText));
        }
    }

    private void initializeSelectedMonthIncomeExpense() {
        if (VersionsActivity.monthlyIncomeExpenses.size() <= 0) {
            List<Expense> expenseList = new ArrayList<>();
            MonthlyIncomeExpense tempMonthlyIncomeExpense = new MonthlyIncomeExpense(month, year, 0.0, expenseList);
            VersionsActivity.monthlyIncomeExpenses.add(tempMonthlyIncomeExpense);
        }

        for (int i=0; i<VersionsActivity.monthlyIncomeExpenses.size(); i++) {
            if (
                    VersionsActivity.monthlyIncomeExpenses.get(i).getYear() == year &&
                            VersionsActivity.monthlyIncomeExpenses.get(i).getMonth() == month
            ) {
                monthlyIncomeExpense = VersionsActivity.monthlyIncomeExpenses.get(i);
            }
        }
    }

    private void updateListViewComponent() {
        expenseListViewAdapter = new ExpenseListViewAdapter(MainActivity.this, monthlyIncomeExpense.getExpenses(), R.layout.expense_item_view, expenseListView);
        expenseListIsEmptyTextView.setVisibility(View.GONE);
        expenseScrollView.setVisibility(View.VISIBLE);
        expenseListView.setVisibility(View.VISIBLE);
        expenseListView.setAdapter(expenseListViewAdapter);
        expenseListViewAdapter.notifyDataSetChanged();
    }

    private void updateRootMonthlyIncomeExpenseObjectExpenses(Expense newExpense) {
        for (int i=0; i<VersionsActivity.monthlyIncomeExpenses.size(); i++) {
            if (
                    VersionsActivity.monthlyIncomeExpenses.get(i).getYear() == year && VersionsActivity.monthlyIncomeExpenses.get(i).getMonth() == month
            ) {
                VersionsActivity.monthlyIncomeExpenses.get(i).getExpenses().add(newExpense);
            }
        }
    }

    private void updateRootMonthlyIncomeExpenseObjectIncome(double newIncome) {
        for (int i=0; i<VersionsActivity.monthlyIncomeExpenses.size(); i++) {
            if (
                    VersionsActivity.monthlyIncomeExpenses.get(i).getYear() == year && VersionsActivity.monthlyIncomeExpenses.get(i).getMonth() == month
            ) {
                VersionsActivity.monthlyIncomeExpenses.get(i).setIncome(newIncome);
            }
        }
    }
}