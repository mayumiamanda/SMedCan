package com.example.smedcan.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smedcan.R;
import com.example.smedcan.data.model.AppointmentModel;

import java.util.ArrayList;

public class cus_history_adapter extends RecyclerView.Adapter<cus_history_adapter.ViewHolder> {

    private ArrayList p_list;
    private Context context;
    private Activity activity;
    private FragmentManager fragmentManager;

    public cus_history_adapter(Context context, FragmentManager fragmentManager,Activity activity){
        p_list = new ArrayList();
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.activity=activity;
    }

    public void loadHistoryList(ArrayList output) {
        this.p_list = output;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cus_history_adapter, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel ap = (AppointmentModel) p_list.get(position);
        holder.name.setText(ap.getUsername());
        holder.email.setText(ap.getEmail());
        holder.price.setText("Price : Rs: "+ap.getPrice());
        holder.date.setText(ap.getDate());
        holder.time.setText(ap.getTime());

    }

    @Override
    public int getItemCount() {
        return p_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name,email,price,date,time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.Cus_name);
            email=itemView.findViewById(R.id.email);
            price=itemView.findViewById(R.id.Payed);
            date=itemView.findViewById(R.id.call_time);
            time=itemView.findViewById(R.id.call_date);

        }
    }
}