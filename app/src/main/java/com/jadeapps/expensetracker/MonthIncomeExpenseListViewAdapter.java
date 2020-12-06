package com.jadeapps.expensetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MonthIncomeExpenseListViewAdapter extends BaseAdapter {
    Context context;
    List<MonthlyIncomeExpense> monthlyIncomeExpenses;
    int layoutRes, selectedMonthIncomeExpenseId;
    ListView monthIncomeExpenseListView;
    DatabaseHelper monthlyIncomeExpenseDB;

    public MonthIncomeExpenseListViewAdapter(Context context, List<MonthlyIncomeExpense> monthlyIncomeExpenses, int layoutRes, ListView monthIncomeExpenseListView, DatabaseHelper monthlyIncomeExpenseDB) {
        this.context = context;
        this.monthlyIncomeExpenses = monthlyIncomeExpenses;
        this.layoutRes = layoutRes;
        this.monthIncomeExpenseListView = monthIncomeExpenseListView;
        this.monthlyIncomeExpenseDB = monthlyIncomeExpenseDB;
        this.selectedMonthIncomeExpenseId = -1;
    }

    @Override
    public int getCount() {
        return monthlyIncomeExpenses.size();
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
            view = inflater.inflate(R.layout.monthly_income_expense_item_view, null);
        }

        TextView monthYearTextView = (TextView) view.findViewById(R.id.monthYearTextView);
        TextView monthIncomeTextView = (TextView) view.findViewById(R.id.monthIncomeTextView);
        TextView monthExpenseTextView = (TextView) view.findViewById(R.id.monthExpenseTextView);
        ImageButton monthYearEditButton = (ImageButton) view.findViewById(R.id.monthYearEditButton);
        ImageButton monthYearDeleteButton = (ImageButton) view.findViewById(R.id.monthYearDeleteButton);

        monthYearEditButton.setFocusable(false);
        monthYearEditButton.setFocusableInTouchMode(false);
        monthYearEditButton.setClickable(true);

        monthYearDeleteButton.setFocusable(false);
        monthYearDeleteButton.setFocusableInTouchMode(false);
        monthYearDeleteButton.setClickable(true);

        final MonthlyIncomeExpense monthlyIncomeExpense = monthlyIncomeExpenses.get(i);
        final List<Expense> expenses = monthlyIncomeExpenseDB.getExpensesByMonthlyIncomeExpenseId(monthlyIncomeExpense.getId());

        double totalExpenses = computeExpensesTotal(expenses);

        selectedMonthIncomeExpenseId = monthlyIncomeExpense.getId();

        monthYearTextView.setText(MainActivity.calendarMonthsString[monthlyIncomeExpense.getMonth()] + "(" + monthlyIncomeExpense.getYear() + ")");
        monthIncomeTextView.setText(MainActivity.euroSymbol + String.format("%.2f", monthlyIncomeExpense.getIncome()));
        monthExpenseTextView.setText(MainActivity.euroSymbol + String.format("%.2f", totalExpenses));

        if (monthlyIncomeExpense.getIncome() > totalExpenses) {
            view.setBackgroundColor(context.getResources().getColor(R.color.surplusBackground));
        } else if (monthlyIncomeExpense.getIncome() < totalExpenses) {
            view.setBackgroundColor(context.getResources().getColor(R.color.deficitBackground));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.warningBackground));
        }

        monthYearEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMonthlyIncomeExpenseSheetDialog(monthlyIncomeExpense);
            }
        });

        monthYearDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View delView = v;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Are you sure?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int viewPos = monthIncomeExpenseListView.getPositionForView(delView);
                        final MonthlyIncomeExpense monthlyIncomeExpenseTemp = monthlyIncomeExpenses.get(computeCurrentPosition(viewPos, monthlyIncomeExpenses.size()));
                        if (deleteMonthlyIncomeExpenseFromDB(monthlyIncomeExpenseTemp, monthlyIncomeExpenseDB)) {
                            Toast.makeText(context, "Deleted month income/expense item", Toast.LENGTH_SHORT).show();
                            loadApplicationState(monthlyIncomeExpenseTemp);
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

        return view;
    }

    private void loadApplicationState(MonthlyIncomeExpense monthlyIncomeExpense) {
        if (monthlyIncomeExpense != null) {
            monthlyIncomeExpenses.remove(monthlyIncomeExpense);
        }
        monthlyIncomeExpenses = monthlyIncomeExpenseDB.allMonthlyIncomeExpense();
        this.notifyDataSetChanged();
    }

    private boolean deleteMonthlyIncomeExpenseFromDB(MonthlyIncomeExpense monthlyIncomeExpenseTemp, DatabaseHelper monthlyIncomeExpenseDB) {
        return monthlyIncomeExpenseDB.deleteMonthlyIncomeExpenseFromDB(monthlyIncomeExpenseTemp);
    }

    private int computeCurrentPosition(int pos, int size) {
        if (pos >= size) {
            return size - 1;
        }
        return pos;
    }

    private double computeExpensesTotal(List<Expense> expenses) {
        double sum = 0.0;

        for (int i=0; i<expenses.size(); i++) {
            sum =+ expenses.get(i).getAmount();
        }
        return sum;
    }

    private void editMonthlyIncomeExpenseSheetDialog(final MonthlyIncomeExpense monthlyIncomeExpense) {
        int intStartYear = 1970;
        int intLastYear = getSpinnerLastYear(monthlyIncomeExpense.getYear());

        final List<Integer> years = new ArrayList<>();
        final List<String> months = Arrays.asList(MainActivity.calendarMonthsString);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.update_month_income_expense, null);
        builder.setView(v);

        final Spinner yearSelectionSpinner = (Spinner) v.findViewById(R.id.yearSelectionSpinner);
        final Spinner monthSelectionSpinner = (Spinner) v.findViewById(R.id.monthSelectionSpinner);

        List<String> monthSpinnerList = new ArrayList<>();
        List<String> yearSpinnerList = new ArrayList<>();

        final String monthSelectString = "Select Month";
        final String yearSelectString = "Select Year";
        yearSpinnerList.add(yearSelectString);
        monthSpinnerList.add(monthSelectString);

        int yearIndex = -1;
        int tempStartYear = intStartYear;
        for (int i=0; i<(intLastYear - intStartYear); i++) {
            tempStartYear++;
            yearSpinnerList.add(""+tempStartYear);
            if (monthlyIncomeExpense.getYear() == tempStartYear) {
                yearIndex = i;
            }
        }

        for (int i=0; i<months.size(); i++) {
            monthSpinnerList.add(months.get(i));
        }

        ArrayAdapter spinnerAdapterMonth = new ArrayAdapter(context,android.R.layout.simple_spinner_item, monthSpinnerList);
        spinnerAdapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelectionSpinner.setAdapter(spinnerAdapterMonth);
        monthSelectionSpinner.setSelection((monthlyIncomeExpense.getMonth()+1), true);

        ArrayAdapter spinnerAdapterYear= new ArrayAdapter(context,android.R.layout.simple_spinner_item, yearSpinnerList);
        spinnerAdapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSelectionSpinner.setAdapter(spinnerAdapterYear);
        yearSelectionSpinner.setSelection((yearIndex+1), true);


        builder.setPositiveButton("Update Record", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strMonth = monthSelectionSpinner.getSelectedItem().toString().trim();
                String strYear = yearSelectionSpinner.getSelectedItem().toString().trim();

                if (!strMonth.equals(monthSelectString) && !strYear.equals(yearSelectString)) {
                    int intMonth = months.indexOf(strMonth);
                    int intYear = Integer.parseInt(strYear);
                    monthlyIncomeExpense.setIncome(0);
                    monthlyIncomeExpense.setMonth(intMonth);
                    monthlyIncomeExpense.setYear(intYear);
                    if (updateMonthlyIncomeExpense(monthlyIncomeExpense)) {
                        Toast.makeText(context, "Income/Expense record was successfully updated.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Update attempt for Income/Expense record failed, Try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Invalid Inputs, Try again.", Toast.LENGTH_SHORT).show();
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

    private boolean updateMonthlyIncomeExpense(MonthlyIncomeExpense monthlyIncomeExpense) {
        boolean result = monthlyIncomeExpenseDB.updateMonthlyIncomeExpense(monthlyIncomeExpense);
        if (result) {
            loadApplicationState(null);
        }
        return result;
    }

    private int getSpinnerLastYear(int currentYear) {
        return currentYear+100;
    }
}
