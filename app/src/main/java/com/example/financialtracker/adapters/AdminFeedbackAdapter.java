package com.example.financialtracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;
import com.example.financialtracker.models.FeedbackItem;
import com.example.financialtracker.utils.FormatUtils;
import java.util.ArrayList;
import java.util.List;

public class AdminFeedbackAdapter extends RecyclerView.Adapter<AdminFeedbackAdapter.ViewHolder> {

    public interface OnFeedbackResolveListener {
        void onResolve(FeedbackItem item);
    }

    private final Context context;
    private final List<FeedbackItem> feedbackList = new ArrayList<>();
    private final OnFeedbackResolveListener listener;

    public AdminFeedbackAdapter(Context context, OnFeedbackResolveListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFeedbacks(List<FeedbackItem> list) {
        this.feedbackList.clear();
        if (list != null) {
            this.feedbackList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackItem item = feedbackList.get(position);

        String userDisplay = item.getUserName();
        if (item.getUserEmail() != null && !item.getUserEmail().isEmpty()) {
            userDisplay += " (" + item.getUserEmail() + ")";
        }
        holder.tvUser.setText(userDisplay);
        holder.tvDate.setText(FormatUtils.formatDate(item.getCreatedAt()));
        holder.tvMessage.setText(item.getMessage());

        if (item.isResolved()) {
            holder.tvStatus.setText("RESOLVED");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            holder.tvStatus.setBackgroundResource(R.drawable.badge_green);
            holder.btnResolve.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("OPEN");
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvStatus.setBackgroundResource(R.drawable.badge_amber);
            holder.btnResolve.setVisibility(View.VISIBLE);
        }

        holder.btnResolve.setOnClickListener(v -> {
            if (listener != null) listener.onResolve(item);
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser;
        TextView tvDate;
        TextView tvStatus;
        TextView tvMessage;
        Button btnResolve;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_feedback_user);
            tvDate = itemView.findViewById(R.id.tv_feedback_date);
            tvStatus = itemView.findViewById(R.id.tv_feedback_status);
            tvMessage = itemView.findViewById(R.id.tv_feedback_message);
            btnResolve = itemView.findViewById(R.id.btn_resolve_feedback);
        }
    }
}
