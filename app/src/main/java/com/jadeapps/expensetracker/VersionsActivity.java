package com.jadeapps.expensetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.icu.util.Calendar.*;

public class VersionsActivity extends AppCompatActivity {

    public static List<MonthlyIncomeExpense> monthlyIncomeExpenses = new ArrayList<>();

    Toolbar toolbar;
    LinearLayout versionsActivityLinearLayout, appLoaderLinearLayout;
    ProgressBar appLoaderPregressbar;
    Spinner monthSelectionSpinner, yearSelectionSpinner;
    TextView monthIncomeExpenseListIsEmptyTextView;
    ListView monthIncomeExpenseListView;
    ScrollView monthIncomeExpenseScrollView;
    MonthIncomeExpenseListViewAdapter monthIncomeExpenseListViewAdapter;

    Calendar calendar;
    int year;
    int month;
    double douIncomeAmount;

    int intStartYear;
    int intLastYear;
    List<Integer> years;
    List<String> months;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versions);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        versionsActivityLinearLayout = (LinearLayout) findViewById(R.id.versionsActivityLinearLayout);
        appLoaderLinearLayout = (LinearLayout) findViewById(R.id.appLoaderLinearLayout);
        appLoaderPregressbar = (ProgressBar) findViewById(R.id.appLoaderPregressbar);
        monthIncomeExpenseListIsEmptyTextView = (TextView) findViewById(R.id.monthIncomeExpenseListIsEmptyTextView);
        monthIncomeExpenseListView = (ListView) findViewById(R.id.monthIncomeExpenseListView);
        monthIncomeExpenseScrollView = (ScrollView) findViewById(R.id.monthIncomeExpenseScrollView);


        calendar = getInstance();
        year = calendar.get(YEAR);
        month = calendar.get(MONTH);


        intStartYear = 1970;
        intLastYear = getSpinnerLastYear(year);
        years = new ArrayList<>();
        months = new ArrayList<>();

        int tempStartYear = intStartYear;
        for (int i=0; i<(intLastYear - intStartYear); i++) {
            tempStartYear++;
            years.add(tempStartYear);
        }

        months = Arrays.asList(MainActivity.calendarMonthsString);

        loadApplicationState();
    }

    private void loadApplicationState() {
        versionsActivityLinearLayout.setVisibility(View.GONE);
        appLoaderLinearLayout.setVisibility(View.VISIBLE);
        appLoaderPregressbar.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name).toUpperCase());
        toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        toolbar.setVisibility(View.VISIBLE);

        if (monthlyIncomeExpenses.size() <= 0) {
            monthIncomeExpenseListIsEmptyTextView.setVisibility(View.VISIBLE);
            monthIncomeExpenseListView.setVisibility(View.GONE);
        } else {
            updateListViewContent();
        }

        monthIncomeExpenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final MonthlyIncomeExpense monthlyIncomeExpense = VersionsActivity.monthlyIncomeExpenses.get(i);

                Intent intent = new Intent(VersionsActivity.this, MainActivity.class);
                intent.putExtra("MONTH_INDEX", monthlyIncomeExpense.getMonth());
                intent.putExtra("YEAR", monthlyIncomeExpense.getYear());
                startActivity(intent);
                finish();
            }
        });

        versionsActivityLinearLayout.setVisibility(View.VISIBLE);
        appLoaderLinearLayout.setVisibility(View.GONE);
        appLoaderPregressbar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.version_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newSheet:
                addNewSheetDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewSheetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VersionsActivity.this);
        View v = LayoutInflater.from(VersionsActivity.this).inflate(R.layout.create_month_income_expense, null);
        builder.setView(v);

        yearSelectionSpinner = (Spinner) v.findViewById(R.id.yearSelectionSpinner);
        monthSelectionSpinner = (Spinner) v.findViewById(R.id.monthSelectionSpinner);

        List<String> monthSpinnerList = new ArrayList<>();
        List<String> yearSpinnerList = new ArrayList<>();

        final String monthSelectString = "Select Month";
        final String yearSelectString = "Select Year";
        yearSpinnerList.add(yearSelectString);
        monthSpinnerList.add(monthSelectString);

        int tempStartYear = intStartYear;
        for (int i=0; i<(intLastYear - intStartYear); i++) {
            tempStartYear++;
            yearSpinnerList.add(""+tempStartYear);
        }

        for (int i=0; i<months.size(); i++) {
            monthSpinnerList.add(months.get(i));
        }

        ArrayAdapter spinnerAdapterMonth = new ArrayAdapter(VersionsActivity.this,android.R.layout.simple_spinner_item, monthSpinnerList);
        spinnerAdapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelectionSpinner.setAdapter(spinnerAdapterMonth);
        monthSelectionSpinner.setSelection((month+1), true);

        ArrayAdapter spinnerAdapterYear= new ArrayAdapter(VersionsActivity.this,android.R.layout.simple_spinner_item, yearSpinnerList);
        spinnerAdapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSelectionSpinner.setAdapter(spinnerAdapterYear);
        yearSelectionSpinner.setSelection((years.indexOf(year)+1), true);

        builder.setPositiveButton("Create Record", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strMonth = monthSelectionSpinner.getSelectedItem().toString().trim();
                String strYear = yearSelectionSpinner.getSelectedItem().toString().trim();

                if (!strMonth.equals(monthSelectString) && !strYear.equals(yearSelectString)) {
                    if (VersionsActivity.monthlyIncomeExpenses.size() <= 0) {
                        createMonthlyRecord(Integer.parseInt(strYear), months.indexOf(strMonth));
                        if (monthIncomeExpenseListViewAdapter != null) {
                            monthIncomeExpenseListViewAdapter.notifyDataSetChanged();
                        } else {
                            updateListViewContent();
                        }
                        System.out.println("==============================================>  CRETAED RECORD COUNT: " + VersionsActivity.monthlyIncomeExpenses.size());
                    }  else {
                        boolean canCreateRecord = true;
                        for (int i = 0; i< VersionsActivity.monthlyIncomeExpenses.size(); i++) {
                            if (
                                    VersionsActivity.monthlyIncomeExpenses.get(i).getYear() == Integer.parseInt(strYear) &&
                                    VersionsActivity.monthlyIncomeExpenses.get(i).getMonth() == months.indexOf(strMonth)
                            ) {
                                canCreateRecord = false;
                            }
                        }

                        if (canCreateRecord) {
                            createMonthlyRecord(Integer.parseInt(strYear), months.indexOf(strMonth));
                            if (monthIncomeExpenseListViewAdapter != null) {
                                monthIncomeExpenseListViewAdapter.notifyDataSetChanged();
                            } else {
                                updateListViewContent();
                            }
                            System.out.println("==============================================>  CRETAED RECORD COUNT: " + VersionsActivity.monthlyIncomeExpenses.size());
                        } else {
                            Toast.makeText(VersionsActivity.this, "Record already exist.", Toast.LENGTH_SHORT).show();
                            System.out.println("==============================================>  ALL RECORD COUNT: " + VersionsActivity.monthlyIncomeExpenses.size());
                        }
                    }
                } else {
                    Toast.makeText(VersionsActivity.this, "Invalid Inputs, Try again.", Toast.LENGTH_SHORT).show();
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

    private void createMonthlyRecord(int yearValue, int monthIndexValue) {
        List<Expense> newExpenses = new ArrayList<>();
        MonthlyIncomeExpense monthlyIncomeExpense = new MonthlyIncomeExpense(monthIndexValue, yearValue, 0.0, newExpenses);
        VersionsActivity.monthlyIncomeExpenses.add(monthlyIncomeExpense);
        Toast.makeText(VersionsActivity.this, "Record has been created.", Toast.LENGTH_SHORT).show();
    }

    private int getSpinnerLastYear(int currentYear) {
        return currentYear;
    }

    private void updateListViewContent() {
        monthIncomeExpenseListViewAdapter = new MonthIncomeExpenseListViewAdapter(VersionsActivity.this, VersionsActivity.monthlyIncomeExpenses, R.layout.monthly_income_expense_item_view, monthIncomeExpenseListView);
        monthIncomeExpenseListIsEmptyTextView.setVisibility(View.GONE);
        monthIncomeExpenseScrollView.setVisibility(View.VISIBLE);
        monthIncomeExpenseListView.setVisibility(View.VISIBLE);
        monthIncomeExpenseListView.setAdapter(monthIncomeExpenseListViewAdapter);
        monthIncomeExpenseListViewAdapter.notifyDataSetChanged();
    }
}