package com.jadeapps.expensetracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExpenseListViewAdapter extends BaseAdapter {
    Context context;
    List<Expense> expenses;
    int layoutRes;
    ListView expenseListview;

    public ExpenseListViewAdapter(Context context, List<Expense> expenses, int layoutRes, ListView expenseListview) {
        this.context = context;
        this.expenses = expenses;
        this.layoutRes = layoutRes;
        this.expenseListview = expenseListview;
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

        final Expense expense = expenses.get(i);

        expenseTitleTextView.setText(expense.getLabel().length() > 40 ? expense.getLabel().substring(0, 40) + "..." : expense.getLabel());
        expenseDateTextView.setText(dateFormat.format(expense.getExpenseDate()));
        expenseAmountTextView.setText("Eur " + String.format("%.2f", expense.getAmount()));

        if (i % 2 == 0) {
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }

        return view;
    }
}
