package com.jadeapps.expensetracker;

import androidx.annotation.RequiresApi;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String euroSymbol= "Eur ";
    public static final String[] calendarMonthsString = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    Toolbar toolbar;
    LinearLayout mainActivityLinearLayout, appLoaderLinearLayout, surplusDeficitLinearLayout, regularNonRegularExpenseLayout;
    ScrollView expenseScrollView;
    TextView expenseListIsEmptyTextView, monthIncomeAmount, monthlyIncomeLabel, monthlyExpenseLabel, surplusDeficitTextView, nonRegularExpenseTotal, nonRegularExpenseStats, regularExpenseStats, regularExpenseTotal;
    ListView expenseListView;
    ProgressBar appLoaderPregressbar;
    EditText expenseTitleEditText, expenseAmountEditText, incomeEditText;
    Spinner yearSelectionSpinner, monthSelectionSpinner, daySelectionSpinner;
    ExpenseListViewAdapter expenseListViewAdapter;
    MonthlyIncomeExpense monthlyIncomeExpense;
    CheckBox markRegularExpenseCheckbox;

    DatabaseHelper monthlyIncomeExpenseDB;
    List<MonthlyIncomeExpense> monthlyIncomeExpenses;
    List<Expense> expenses;


    Calendar calendar;
    int year;
    int month;
    int day;

    int intStartYear;
    int intLastYear;
    List<Integer> years;
    List<String> months;

    int tempYear;
    int tempMonth;
    int tempDay;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monthlyIncomeExpenseDB = new DatabaseHelper(MainActivity.this);
        calendar = Calendar.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainActivityLinearLayout = (LinearLayout) findViewById(R.id.mainActivityLinearLayout);
        regularNonRegularExpenseLayout = (LinearLayout) findViewById(R.id.regularNonRegularExpenseLayout);
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
        nonRegularExpenseTotal  = (TextView) findViewById(R.id.nonRegularExpenseTotal);
        nonRegularExpenseStats  = (TextView) findViewById(R.id.nonRegularExpenseStats);
        regularExpenseStats  = (TextView) findViewById(R.id.regularExpenseStats);
        regularExpenseTotal = (TextView) findViewById(R.id.regularExpenseTotal);

        monthlyIncomeExpenses = new ArrayList<>();
        expenses = new ArrayList<>();

        intStartYear = 1970;
        intLastYear = getSpinnerLastYear();
        years = new ArrayList<>();
        months = Arrays.asList(MainActivity.calendarMonthsString);

        int tempStartYear = intStartYear;
        for (int i=0; i<(intLastYear - intStartYear); i++) {
            tempStartYear++;
            years.add(tempStartYear);
        }

        int selectedMonthIndex = getIntent().getIntExtra("MONTH_INDEX", -1);
        int selectedYear = getIntent().getIntExtra("YEAR", -1);

        if (selectedMonthIndex >= 0 && selectedYear >= 0) {
            year = selectedYear;
            month = selectedMonthIndex;
            day = 1;
        } else {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        loadApplicationState();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

        updateListViewComponent();

        mainActivityLinearLayout.setVisibility(View.VISIBLE);
        appLoaderLinearLayout.setVisibility(View.GONE);
        appLoaderPregressbar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        return true;
    }

    public void updateRegularNonRegularStats() {
        int regularExpensesCount = 0, nonRegularExpensesCount = 0;
        double regularExpensesTotal = 0.0, nonRegularExpensesTotal = 0.0;

        for (int i=0; i < expenses.size(); i++) {
            if (expenses.get(i).getRegular() == 1) {
                regularExpensesCount++;
                regularExpensesTotal+=expenses.get(i).getAmount();
            } else {
                nonRegularExpensesCount++;
                nonRegularExpensesTotal+=expenses.get(i).getAmount();
            }
        }

        nonRegularExpenseTotal.setText(String.format("%.2f", nonRegularExpensesTotal));
        nonRegularExpenseStats.setText(nonRegularExpensesCount+"");
        regularExpenseStats.setText(regularExpensesCount+"");
        regularExpenseTotal.setText(String.format("%.2f", regularExpensesTotal));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addExpense() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_expense_dialog_view, null);
        builder.setView(v);

        expenseTitleEditText = (EditText) v.findViewById(R.id.expenseTitleEditText);
        expenseAmountEditText = (EditText) v.findViewById(R.id.expenseAmountEditText);
        yearSelectionSpinner = (Spinner) v.findViewById(R.id.yearSelectionSpinner);
        monthSelectionSpinner = (Spinner) v.findViewById(R.id.monthSelectionSpinner);
        daySelectionSpinner = (Spinner) v.findViewById(R.id.daySelectionSpinner);
        markRegularExpenseCheckbox = (CheckBox) v.findViewById(R.id.markRegularExpenseCheckbox);

        tempYear = calendar.get(Calendar.YEAR);
        tempMonth = (calendar.get(Calendar.MONTH)+1);
        tempDay = calendar.get(Calendar.DAY_OF_MONTH);

        setupYearSpinnerSelector(tempYear);
        setupMonthSpinnerSelector(tempMonth);
        setupDaySpinnerSelector(tempDay, tempMonth, tempYear);

        monthSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tempMonth = months.indexOf(monthSelectionSpinner.getSelectedItem().toString().trim());
                setupDaySpinnerSelector(tempDay, tempMonth, tempYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        builder.setPositiveButton("Add Expense", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String expenseTitle = expenseTitleEditText.getText().toString().trim();
                String strExpenseAmount = expenseAmountEditText.getText().toString().trim();
                String strYear = yearSelectionSpinner.getSelectedItem().toString().trim();
                String strMonth = monthSelectionSpinner.getSelectedItem().toString().trim();
                String strDay = daySelectionSpinner.getSelectedItem().toString().trim();

                if (!TextUtils.isEmpty(expenseTitle) && !TextUtils.isEmpty(strExpenseAmount)  && !strYear.equals("Year") && !strMonth.equals("Month") && !strDay.equals("Day")) {
                    String dateString = strYear + "-" + (months.indexOf(strMonth)+1) + "-" + strDay;
                    Expense expense = new Expense();
                    expense.setMonthIncomeExpenseId(monthlyIncomeExpense.getId());
                    expense.setPaymentFor(expenseTitle);
                    expense.setAmount(Double.parseDouble(strExpenseAmount));
                    expense.setMadeOn(dateString);
                    expense.setRegular(markRegularExpenseCheckbox.isChecked() ? 1 : 0);
                    boolean result =  monthlyIncomeExpenseDB.addExpense(expense);
                    if (result) {
                        initializeSelectedMonthIncomeExpense();
                        updateListViewComponent();
                        computeSurplusDeficitIncome();
                        Toast.makeText(MainActivity.this, "Expense item created..", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid operation, please try again...", Toast.LENGTH_SHORT).show();
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

    private void setupYearSpinnerSelector(int targetYear) {
        List<String> yearSpinnerList = new ArrayList<>();

        final String yearSelectString = "Year";
        yearSpinnerList.add(yearSelectString);

        int tempStartYear = intStartYear;
        int yearIndex = -1;
        for (int i=0; i<(intLastYear - intStartYear); i++) {
            tempStartYear++;
            yearSpinnerList.add(""+tempStartYear);
            if (targetYear == tempStartYear) {
                yearIndex = i;
            }
        }

        ArrayAdapter spinnerAdapterYear= new ArrayAdapter(MainActivity.this,android.R.layout.simple_spinner_item, yearSpinnerList);
        spinnerAdapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSelectionSpinner.setAdapter(spinnerAdapterYear);
        yearSelectionSpinner.setSelection((yearIndex+1), true);
    }

    private void setupMonthSpinnerSelector(int targetMonth) {
        List<String> monthSpinnerList = new ArrayList<>();

        final String monthSelectString = "Month";

        for (int i=0; i<months.size(); i++) {
            monthSpinnerList.add(months.get(i));
        }

        ArrayAdapter spinnerAdapterMonth = new ArrayAdapter(MainActivity.this,android.R.layout.simple_spinner_item, monthSpinnerList);
        spinnerAdapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelectionSpinner.setAdapter(spinnerAdapterMonth);
        monthSelectionSpinner.setSelection((targetMonth-1), true);
    }

    private void setupDaySpinnerSelector(int targetDay, int targetMonth, int targetYear) {
        List<String> daySpinnerList = new ArrayList<>();

        final String daySelectString = "Day";
        daySpinnerList.add(daySelectString);

        int intLastDayOfTheMonth = getDayOfTheMonth(targetMonth, targetYear);

        int dayIndex = -1;
        for (int i=1; i<=(intLastDayOfTheMonth); i++) {
            daySpinnerList.add(""+i);
            if (targetDay == i) {
                dayIndex = i;
            }
        }

        ArrayAdapter spinnerAdapterYear= new ArrayAdapter(MainActivity.this,android.R.layout.simple_spinner_item, daySpinnerList);
        spinnerAdapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySelectionSpinner.setAdapter(spinnerAdapterYear);
        daySelectionSpinner.setSelection((dayIndex), true);
    }

    private int getDayOfTheMonth(int targetMonth, int targetYear) {
        int monthLength = 0;
        switch (targetMonth) {
            case 1:
                monthLength = targetYear % 4 == 0 ? 29 : 28;
                break;

            case 3:
            case 5:
            case 8:
            case 10:
                monthLength = 30;
                break;

            default:
                monthLength = 31;
                break;
        }
        return monthLength;
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
                    MonthlyIncomeExpense temp = monthlyIncomeExpense;
                    temp.setIncome(Double.parseDouble(strIncomeForTheMonth));
                    boolean result = monthlyIncomeExpenseDB.updateMonthlyIncomeExpense(temp);
                    if (result)  {
                        initializeSelectedMonthIncomeExpense();
                        monthIncomeAmount.setText(euroSymbol + String.format("%.2f", monthlyIncomeExpense.getIncome()));
                        computeSurplusDeficitIncome();
                        Toast.makeText(MainActivity.this, "Income value has been updated.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid operation, please insert income amount to update income value", Toast.LENGTH_LONG).show();
                    }
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

    public void computeSurplusDeficitIncome() {
        double surplusDeficit = 0.0;
        double totalExpenses = 0.0;

        expenses = monthlyIncomeExpenseDB.getExpensesByMonthlyIncomeExpenseId(monthlyIncomeExpense.getId());

        for (int i=0; i<expenses.size(); i++) {
            totalExpenses += expenses.get(i).getAmount();
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

        updateRegularNonRegularStats();
    }


    private void initializeSelectedMonthIncomeExpense() {
        monthlyIncomeExpenses = monthlyIncomeExpenseDB.allMonthlyIncomeExpense();
        if (monthlyIncomeExpenses.size() <= 0 || !hasCurrentMonthRecord(month, year)) {
            boolean result = monthlyIncomeExpenseDB.addMonthlyIncomeExpense(month, year);
            if (result) {
                monthlyIncomeExpenses = monthlyIncomeExpenseDB.allMonthlyIncomeExpense();
            }
        }

        for (int i=0; i<monthlyIncomeExpenses.size(); i++) {
            if (
                    monthlyIncomeExpenses.get(i).getYear() == year &&
                            monthlyIncomeExpenses.get(i).getMonth() == month
            ) {
                monthlyIncomeExpense = monthlyIncomeExpenses.get(i);
                expenses = monthlyIncomeExpenseDB.getExpensesByMonthlyIncomeExpenseId(monthlyIncomeExpense.getId());
            }
        }
    }

    private boolean hasCurrentMonthRecord(int month, int year) {
        boolean hasRecord = false;
        for (int i=0; i<monthlyIncomeExpenses.size(); i++) {
            if (
                    monthlyIncomeExpenses.get(i).getYear() == year &&
                            monthlyIncomeExpenses.get(i).getMonth() == month
            ) {
                hasRecord = true;
            }
        }
        return hasRecord;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateListViewComponent() {
        if (expenses.size() <= 0) {
            expenseListIsEmptyTextView.setVisibility(View.VISIBLE);
            expenseListView.setVisibility(View.GONE);
            regularNonRegularExpenseLayout.setVisibility(View.GONE);
        } else {
            expenses = monthlyIncomeExpenseDB.getExpensesByMonthlyIncomeExpenseId(monthlyIncomeExpense.getId());
            expenseListViewAdapter = new ExpenseListViewAdapter(MainActivity.this, expenses, R.layout.expense_item_view, expenseListView, monthlyIncomeExpenseDB);
            expenseListIsEmptyTextView.setVisibility(View.GONE);
            expenseScrollView.setVisibility(View.VISIBLE);
            expenseListView.setVisibility(View.VISIBLE);
            regularNonRegularExpenseLayout.setVisibility(View.VISIBLE);
            expenseListView.setAdapter(expenseListViewAdapter);
            expenseListViewAdapter.notifyDataSetChanged();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int getSpinnerLastYear() {
        return calendar.get(Calendar.YEAR);
    }
}