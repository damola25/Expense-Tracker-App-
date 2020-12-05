package com.jadeapps.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    Toolbar toolbar;
    LinearLayout mainActivityLinearLayout, appLoaderLinearLayout;
    ScrollView expenseScrollView;
    TextView expenseListIsEmptyTextView, monthIncomeAmount, monthlyIncomeLabel, monthlyExpenseLabel;
    ListView expenseListView;
    ProgressBar appLoaderPregressbar;
    EditText expenseTitleEditText, expenseAmountEditText;
    ExpenseListViewAdapter expenseListViewAdapter;

    List<Expense> expenses;

    Calendar calendar;
    int year;
    int month;
    double douIncomeAount;

    String[] calendarMonthsString;

    private int expenseCounter = 0;

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
        appLoaderPregressbar = (ProgressBar) findViewById(R.id.appLoaderPregressbar);

        expenses = new ArrayList<>();

        calendarMonthsString = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
        }

        douIncomeAount = 0.0;

        loadApplicationState();
    }

    private void loadApplicationState() {
        mainActivityLinearLayout.setVisibility(View.GONE);
        appLoaderLinearLayout.setVisibility(View.VISIBLE);
        appLoaderPregressbar.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name).toUpperCase());
        toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        toolbar.setVisibility(View.VISIBLE);

        monthlyIncomeLabel.setText(calendarMonthsString[month] + " " + year + " Income");
        monthlyExpenseLabel.setText(calendarMonthsString[month] + " " + year + " Expense");
        monthIncomeAmount.setText("Eur " + String.format("%.2f", douIncomeAount));

        if (expenses.size() <= 0) {
            expenseListIsEmptyTextView.setVisibility(View.VISIBLE);
            expenseListView.setVisibility(View.GONE);
        } else {
            expenseListViewAdapter = new ExpenseListViewAdapter(MainActivity.this, expenses, R.layout.expense_item_view, expenseListView);
            expenseListIsEmptyTextView.setVisibility(View.GONE);
            expenseScrollView.setVisibility(View.VISIBLE);
            expenseListView.setVisibility(View.VISIBLE);
            expenseListView.setAdapter(expenseListViewAdapter);
            expenseListViewAdapter.notifyDataSetChanged();
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
                    expenses.add(new Expense(expenseTitle, Double.parseDouble(strExpenseAmount), date));
                    if (expenseListViewAdapter == null) {
                        expenseListViewAdapter = new ExpenseListViewAdapter(MainActivity.this, expenses, R.layout.expense_item_view, expenseListView);
                        expenseListIsEmptyTextView.setVisibility(View.GONE);
                        expenseScrollView.setVisibility(View.VISIBLE);
                        expenseListView.setVisibility(View.VISIBLE);
                        expenseListView.setAdapter(expenseListViewAdapter);
                        expenseListViewAdapter.notifyDataSetChanged();
                    } else {
                        expenseListViewAdapter.notifyDataSetChanged();
                    }
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
}