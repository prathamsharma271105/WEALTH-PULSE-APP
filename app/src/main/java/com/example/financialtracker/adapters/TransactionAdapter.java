package com.example.financialtracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;
import com.example.financialtracker.models.Expense;
import com.example.financialtracker.models.Income;
import com.example.financialtracker.utils.FormatUtils;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public static class TransactionItem {
        public String id;
        public String title;
        public String subtitle;
        public double amount;
        public boolean isIncome;
        public String date;
        public Income incomeObj;
        public Expense expenseObj;

        public static TransactionItem fromIncome(Income inc) {
            TransactionItem item = new TransactionItem();
            item.id = inc.getId();
            item.title = inc.getSource();
            String note = (inc.getNote() != null && !inc.getNote().isEmpty()) ? " • " + inc.getNote() : "";
            item.subtitle = FormatUtils.formatDate(inc.getDate()) + note;
            item.amount = inc.getAmount();
            item.isIncome = true;
            item.date = inc.getDate();
            item.incomeObj = inc;
            return item;
        }

        public static TransactionItem fromExpense(Expense exp) {
            TransactionItem item = new TransactionItem();
            item.id = exp.getId();
            item.title = exp.getCategory();
            String note = (exp.getNote() != null && !exp.getNote().isEmpty()) ? " • " + exp.getNote() : "";
            item.subtitle = FormatUtils.formatDate(exp.getDate()) + note;
            item.amount = exp.getAmount();
            item.isIncome = false;
            item.date = exp.getDate();
            item.expenseObj = exp;
            return item;
        }
    }

    public interface OnDeleteClickListener {
        void onDelete(TransactionItem item);
    }

    private final Context context;
    private final List<TransactionItem> items = new ArrayList<>();
    private final String currency;
    private final OnDeleteClickListener listener;

    public TransactionAdapter(Context context, String currency, OnDeleteClickListener listener) {
        this.context = context;
        this.currency = currency;
        this.listener = listener;
    }

    public void setItems(List<TransactionItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = items.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);

        if (item.isIncome) {
            holder.tvAmount.setText("+" + FormatUtils.formatCurrency(item.amount, currency));
            holder.tvAmount.setTextColor(Color.parseColor("#10B981")); // Emerald
            holder.ivTypeIcon.setImageResource(R.drawable.ic_trending_up);
            holder.ivTypeIcon.setColorFilter(Color.parseColor("#10B981"));
            holder.iconContainer.setBackgroundResource(R.drawable.badge_green);
        } else {
            holder.tvAmount.setText("-" + FormatUtils.formatCurrency(item.amount, currency));
            holder.tvAmount.setTextColor(Color.parseColor("#F43F5E")); // Crimson Rose
            holder.ivTypeIcon.setImageResource(R.drawable.ic_trending_down);
            holder.ivTypeIcon.setColorFilter(Color.parseColor("#F43F5E"));
            holder.iconContainer.setBackgroundResource(R.drawable.badge_red);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View iconContainer;
        ImageView ivTypeIcon;
        TextView tvTitle;
        TextView tvSubtitle;
        TextView tvAmount;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivTypeIcon = itemView.findViewById(R.id.iv_type_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
