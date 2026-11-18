package com.example.financialtracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;
import com.example.financialtracker.models.AdminUserItem;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface AdminUserActionListener {
        void onToggleRole(AdminUserItem user);
        void onDelete(AdminUserItem user);
    }

    private final Context context;
    private final List<AdminUserItem> userList = new ArrayList<>();
    private final String currentUserId;
    private final AdminUserActionListener listener;

    public AdminUserAdapter(Context context, String currentUserId, AdminUserActionListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setUsers(List<AdminUserItem> users) {
        this.userList.clear();
        if (users != null) {
            this.userList.addAll(users);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUserItem user = userList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        if (user.isAdmin()) {
            holder.tvRole.setText("ADMIN");
            holder.tvRole.setTextColor(Color.parseColor("#8B5CF6")); // Purple
            holder.tvRole.setBackgroundResource(R.drawable.badge_purple);
            holder.btnToggleRole.setText("Demote to User");
        } else {
            holder.tvRole.setText("USER");
            holder.tvRole.setTextColor(Color.parseColor("#3B82F6")); // Blue
            holder.tvRole.setBackgroundResource(R.drawable.badge_blue);
            holder.btnToggleRole.setText("Promote to Admin");
        }

        String stats = "Inflows: " + user.getIncomeCount() + " • Outflows: " + user.getExpenseCount() +
                " • Habits: " + user.getHabitCount() + " • Goals: " + user.getGoalCount();
        holder.tvStats.setText(stats);

        boolean isSelf = (currentUserId != null && currentUserId.equals(user.getId()));
        if (isSelf) {
            holder.btnToggleRole.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnToggleRole.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        holder.btnToggleRole.setOnClickListener(v -> {
            if (listener != null) listener.onToggleRole(user);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        TextView tvRole;
        TextView tvStats;
        Button btnToggleRole;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_admin_user_name);
            tvEmail = itemView.findViewById(R.id.tv_admin_user_email);
            tvRole = itemView.findViewById(R.id.tv_role_badge);
            tvStats = itemView.findViewById(R.id.tv_user_stats);
            btnToggleRole = itemView.findViewById(R.id.btn_toggle_role);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}
