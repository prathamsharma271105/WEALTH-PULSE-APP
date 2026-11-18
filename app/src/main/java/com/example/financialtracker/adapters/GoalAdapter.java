package com.example.financialtracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;
import com.example.financialtracker.models.Goal;
import com.example.financialtracker.utils.FormatUtils;
import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    public interface GoalActionListener {
        void onContribute(Goal goal);
        void onDelete(Goal goal);
    }

    private final Context context;
    private final List<Goal> goalList = new ArrayList<>();
    private final String currency;
    private final GoalActionListener listener;

    public GoalAdapter(Context context, String currency, GoalActionListener listener) {
        this.context = context;
        this.currency = currency;
        this.listener = listener;
    }

    public void setGoals(List<Goal> goals) {
        this.goalList.clear();
        if (goals != null) {
            this.goalList.addAll(goals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal goal = goalList.get(position);

        holder.tvTitle.setText(goal.getTitle());

        double pct = goal.getProgressPercentageDouble();
        String pctStr;
        if (pct >= 100.0) {
            pctStr = "100%";
        } else if (pct == (long) pct) {
            pctStr = ((long) pct) + "%";
        } else {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#0.#");
            pctStr = df.format(pct) + "%";
        }
        holder.tvPercent.setText(pctStr);
        holder.progressBar.setProgress((int) Math.min(100, Math.round(pct)));

        String savedStr = FormatUtils.formatCurrency(goal.getSavedAmount(), currency);
        String targetStr = FormatUtils.formatCurrency(goal.getTargetAmount(), currency);
        holder.tvAmounts.setText(savedStr + " / " + targetStr);

        if (goal.getDeadline() != null && !goal.getDeadline().trim().isEmpty()) {
            holder.tvDeadline.setVisibility(View.VISIBLE);
            holder.tvDeadline.setText("Target: " + FormatUtils.formatDate(goal.getDeadline()));
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }

        holder.btnContribute.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContribute(goal);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(goal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvPercent;
        ProgressBar progressBar;
        TextView tvAmounts;
        TextView tvDeadline;
        Button btnContribute;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_goal_title);
            tvPercent = itemView.findViewById(R.id.tv_progress_percent);
            progressBar = itemView.findViewById(R.id.pb_goal_progress);
            tvAmounts = itemView.findViewById(R.id.tv_amounts_summary);
            tvDeadline = itemView.findViewById(R.id.tv_goal_deadline);
            btnContribute = itemView.findViewById(R.id.btn_contribute);
            btnDelete = itemView.findViewById(R.id.btn_delete_goal);
        }
    }
}
