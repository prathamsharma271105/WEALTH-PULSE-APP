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
import com.example.financialtracker.models.Investment;
import com.example.financialtracker.utils.FormatUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder> {

    public interface InvestmentActionListener {
        void onEditValue(Investment investment);
        void onDelete(Investment investment);
    }

    private final Context context;
    private final List<Investment> investmentList = new ArrayList<>();
    private final String currency;
    private final InvestmentActionListener listener;

    public InvestmentAdapter(Context context, String currency, InvestmentActionListener listener) {
        this.context = context;
        this.currency = currency;
        this.listener = listener;
    }

    public void setInvestments(List<Investment> list) {
        this.investmentList.clear();
        if (list != null) {
            this.investmentList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_investment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Investment item = investmentList.get(position);

        holder.tvName.setText(item.getAssetName());
        holder.tvType.setText(item.getAssetType());

        holder.tvInvested.setText(FormatUtils.formatCurrency(item.getAmountInvested(), currency));
        holder.tvCurrent.setText(FormatUtils.formatCurrency(item.getCurrentValue(), currency));

        double pl = item.getProfitLoss();
        double plPct = item.getProfitLossPercentage();
        DecimalFormat pctFmt = new DecimalFormat("#,##0.0");

        if (pl >= 0) {
            holder.tvPl.setText("+" + FormatUtils.formatCurrency(pl, currency) + " (+" + pctFmt.format(plPct) + "%)");
            holder.tvPl.setTextColor(Color.parseColor("#10B981")); // Emerald
        } else {
            holder.tvPl.setText(FormatUtils.formatCurrency(pl, currency) + " (" + pctFmt.format(plPct) + "%)");
            holder.tvPl.setTextColor(Color.parseColor("#F43F5E")); // Crimson Rose
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditValue(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return investmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvType;
        TextView tvInvested;
        TextView tvCurrent;
        TextView tvPl;
        ImageView btnEdit;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_asset_name);
            tvType = itemView.findViewById(R.id.tv_asset_type_badge);
            tvInvested = itemView.findViewById(R.id.tv_invested_amount);
            tvCurrent = itemView.findViewById(R.id.tv_current_value);
            tvPl = itemView.findViewById(R.id.tv_profit_loss);
            btnEdit = itemView.findViewById(R.id.btn_edit_value);
            btnDelete = itemView.findViewById(R.id.btn_delete_investment);
        }
    }
}
