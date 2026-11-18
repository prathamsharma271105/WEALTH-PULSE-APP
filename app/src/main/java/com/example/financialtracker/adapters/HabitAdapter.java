package com.example.financialtracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;
import com.example.financialtracker.models.Habit;
import com.example.financialtracker.utils.FormatUtils;
import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    public interface HabitActionListener {
        void onToggle(Habit habit, boolean isChecked);
        void onDelete(Habit habit);
    }

    private final Context context;
    private final List<Habit> habitList = new ArrayList<>();
    private final String currency;
    private final HabitActionListener listener;

    public HabitAdapter(Context context, String currency, HabitActionListener listener) {
        this.context = context;
        this.currency = currency;
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits) {
        this.habitList.clear();
        if (habits != null) {
            this.habitList.addAll(habits);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.tvName.setText(habit.getName());

        String freq = habit.getFrequency();
        if (freq != null && !freq.isEmpty()) {
            freq = Character.toUpperCase(freq.charAt(0)) + freq.substring(1);
        }
        holder.tvFrequency.setText(freq);

        holder.tvStreak.setText("🔥 " + habit.getStreak() + " Days");

        if (habit.getTargetAmount() > 0) {
            holder.tvTarget.setVisibility(View.VISIBLE);
            holder.tvTarget.setText("Target: " + FormatUtils.formatCurrency(habit.getTargetAmount(), currency));
        } else {
            holder.tvTarget.setVisibility(View.GONE);
        }

        // Avoid triggering listener during bind
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(habit.isCompletedToday());

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggle(habit, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(habit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvName;
        TextView tvFrequency;
        TextView tvStreak;
        TextView tvTarget;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cb_completed);
            tvName = itemView.findViewById(R.id.tv_habit_name);
            tvFrequency = itemView.findViewById(R.id.tv_frequency_badge);
            tvStreak = itemView.findViewById(R.id.tv_streak_badge);
            tvTarget = itemView.findViewById(R.id.tv_target_amount);
            btnDelete = itemView.findViewById(R.id.btn_delete_habit);
        }
    }
}
