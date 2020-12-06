package com.jadeapps.expensetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.icu.util.Calendar.getInstance;

public class ExpenseListViewAdapter extends BaseAdapter {
    Context context;
    List<Expense> expenses;
    int layoutRes;
    ListView expenseListview;
    DatabaseHelper monthlyIncomeExpenseDB;

    EditText expenseTitleEditText, expenseAmountEditText;
    Spinner yearSelectionSpinner, monthSelectionSpinner, daySelectionSpinner;

    int tempYear;
    int tempMonth;
    int tempDay;

    int intStartYear;
    int intLastYear;
    List<Integer> years;
    List<String> months;

    Calendar calendar;
    int year;
    int month;
    int day;


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ExpenseListViewAdapter(Context context, List<Expense> expenses, int layoutRes, ListView expenseListview, DatabaseHelper monthlyIncomeExpenseDB) {
        this.context = context;
        this.expenses = expenses;
        this.layoutRes = layoutRes;
        this.expenseListview = expenseListview;
        this.monthlyIncomeExpenseDB = monthlyIncomeExpenseDB;

        calendar = getInstance();

        intStartYear = 1970;
        intLastYear = getSpinnerLastYear();
        years = new ArrayList<>();
        months = Arrays.asList(MainActivity.calendarMonthsString);

        int tempStartYear = intStartYear;
        for (int i=0; i<(intLastYear - intStartYear); i++) {
            tempStartYear++;
            years.add(tempStartYear);
        }


    }


    @Override
    public int getCount() {
        return expenses.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.expense_item_view, null);
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        TextView expenseTitleTextView = (TextView) view.findViewById(R.id.expenseTitleTextView);
        TextView expenseDateTextView = (TextView) view.findViewById(R.id.expenseDateTextView);
        TextView expenseAmountTextView = (TextView) view.findViewById(R.id.expenseAmountTextView);
        ImageButton expenseEditButton = (ImageButton) view.findViewById(R.id.expenseEditButton);
        ImageButton expenseDeleteButton = (ImageButton) view.findViewById(R.id.expenseDeleteButton);

        expenseEditButton.setFocusable(false);
        expenseEditButton.setFocusableInTouchMode(false);
        expenseEditButton.setClickable(true);

        expenseDeleteButton.setFocusable(false);
        expenseDeleteButton.setFocusableInTouchMode(false);
        expenseDeleteButton.setClickable(true);

        final Expense expense = expenses.get(i);

        expenseTitleTextView.setText(expense.getPaymentFor().length() > 40 ? expense.getPaymentFor().substring(0, 40) + "..." : expense.getPaymentFor());
        expenseDateTextView.setText("Expense Date: " + expense.getMadeOn());
        expenseAmountTextView.setText(MainActivity.euroSymbol + String.format("%.2f", expense.getAmount()));

        expenseEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editExpense(expense);
            }
        });

        expenseDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View delView = v;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Are you sure?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int viewPos = expenseListview.getPositionForView(delView);
                        final Expense expenseTemp = expenses.get(computeCurrentPosition(viewPos, expenses.size()));
                        if (deleteExpenseFromDB(expenseTemp, monthlyIncomeExpenseDB)) {
                            Toast.makeText(context, "Deleted expense item", Toast.LENGTH_SHORT).show();
                            loadApplicationState(expenseTemp);
                            ((MainActivity)context).computeSurplusDeficitIncome();
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
        });

        if (i % 2 == 0) {
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }

        return view;
    }

    private boolean deleteExpenseFromDB(Expense expense, DatabaseHelper monthlyIncomeExpenseDB) {
        return monthlyIncomeExpenseDB.deleteExpenseFromDB(expense);
    }

    private int computeCurrentPosition(int pos, int size) {
        if (pos >= size) {
            return size - 1;
        }
        return pos;
    }

    private void loadApplicationState(Expense expense) {
        if (expense != null) {
            expenses.remove(expense);
        }
        expenses = monthlyIncomeExpenseDB.allExpenses();
        this.notifyDataSetChanged();
    }

    private void editExpense(final Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.edit_expense_dialog_view, null);
        builder.setView(v);

        expenseTitleEditText = (EditText) v.findViewById(R.id.expenseTitleEditText);
        expenseAmountEditText = (EditText) v.findViewById(R.id.expenseAmountEditText);

          expenseTitleEditText.setText(expense.getPaymentFor());
          expenseAmountEditText.setText(expense.getAmount()+"");


        builder.setPositiveButton("Update Expense", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String expenseTitle = expenseTitleEditText.getText().toString().trim();
                String strExpenseAmount = expenseAmountEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(expenseTitle) && !TextUtils.isEmpty(strExpenseAmount) ) {
                    expense.setMadeOn("2020-08-12");
                    expense.setAmount(Double.parseDouble(strExpenseAmount));
                    expense.setPaymentFor(expenseTitle);
                    boolean result =  monthlyIncomeExpenseDB.updateExpense(expense);
                    if (result) {
                        Toast.makeText(context, "Expense updated successfully.", Toast.LENGTH_SHORT).show();
                        loadApplicationState(null);
                        ((MainActivity)context).computeSurplusDeficitIncome();
                    }
                } else {
                    Toast.makeText(context, "Invalid operation, please try again...", Toast.LENGTH_SHORT).show();
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

        ArrayAdapter spinnerAdapterYear= new ArrayAdapter(context,android.R.layout.simple_spinner_item, yearSpinnerList);
        spinnerAdapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSelectionSpinner.setAdapter(spinnerAdapterYear);
        yearSelectionSpinner.setSelection(yearIndex, true);
    }

    private void setupMonthSpinnerSelector(int targetMonth) {
        List<String> monthSpinnerList = new ArrayList<>();

        final String monthSelectString = "Month";

        for (int i=0; i<months.size(); i++) {
            monthSpinnerList.add(months.get(i));
        }

        ArrayAdapter spinnerAdapterMonth = new ArrayAdapter(context,android.R.layout.simple_spinner_item, monthSpinnerList);
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

        ArrayAdapter spinnerAdapterYear= new ArrayAdapter(context, android.R.layout.simple_spinner_item, daySpinnerList);
        spinnerAdapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySelectionSpinner.setAdapter(spinnerAdapterYear);
        daySelectionSpinner.setSelection((dayIndex), true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int getSpinnerLastYear() {
        return calendar.get(Calendar.YEAR);
    }

    private int getDayOfTheMonth(int targetMonth, int targetYear) {
        int monthLength = 0;
        switch (targetMonth-1) {
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
}

