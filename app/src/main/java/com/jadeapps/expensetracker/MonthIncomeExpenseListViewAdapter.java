package com.jadeapps.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MonthIncomeExpenseListViewAdapter extends BaseAdapter {
    Context context;
    List<MonthlyIncomeExpense> monthlyIncomeExpenses;
    int layoutRes;
    ListView monthIncomeExpenseListView;

    public MonthIncomeExpenseListViewAdapter(Context context, List<MonthlyIncomeExpense> monthlyIncomeExpenses, int layoutRes, ListView monthIncomeExpenseListView) {
        this.context = context;
        this.monthlyIncomeExpenses = monthlyIncomeExpenses;
        this.layoutRes = layoutRes;
        this.monthIncomeExpenseListView = monthIncomeExpenseListView;
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

        final MonthlyIncomeExpense monthlyIncomeExpense = monthlyIncomeExpenses.get(i);
        double totalExpenses = computeExpensesTotal(monthlyIncomeExpense.getExpenses());

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

        return view;
    }

    private double computeExpensesTotal(List<Expense> expenses) {
        double sum = 0.0;

        for (int i=0; i<expenses.size(); i++) {
            sum =+ expenses.get(i).getAmount();
        }
        return sum;
    }
}
