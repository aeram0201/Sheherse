package com.example.sheherse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.R;
import com.example.sheherse.model.Enquiry;

import java.util.ArrayList;
import java.util.List;

public class EnquiryAdapter extends RecyclerView.Adapter<EnquiryAdapter.EnquiryViewHolder> {

    private final Context context;
    private List<Enquiry> enquiries;

    public EnquiryAdapter(Context context, List<Enquiry> enquiries) {
        this.context = context;
        this.enquiries = enquiries != null ? enquiries : new ArrayList<>();
    }

    public void updateEnquiries(List<Enquiry> newEnquiries) {
        this.enquiries = newEnquiries != null ? newEnquiries : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EnquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enquiry, parent, false);
        return new EnquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnquiryViewHolder holder, int position) {
        Enquiry enquiry = enquiries.get(position);
        holder.bind(enquiry);
    }

    @Override
    public int getItemCount() {
        return enquiries.size();
    }

    static class EnquiryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEnquiryTarget;
        private final TextView tvEnquiryDate;
        private final TextView tvEnquirySender;
        private final TextView tvEnquiryContact;
        private final TextView tvEnquiryPreferred;
        private final TextView tvEnquiryMessage;

        public EnquiryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnquiryTarget = itemView.findViewById(R.id.tvEnquiryTarget);
            tvEnquiryDate = itemView.findViewById(R.id.tvEnquiryDate);
            tvEnquirySender = itemView.findViewById(R.id.tvEnquirySender);
            tvEnquiryContact = itemView.findViewById(R.id.tvEnquiryContact);
            tvEnquiryPreferred = itemView.findViewById(R.id.tvEnquiryPreferred);
            tvEnquiryMessage = itemView.findViewById(R.id.tvEnquiryMessage);
        }

        public void bind(Enquiry enquiry) {
            tvEnquiryTarget.setText("Booking / Enquiry: " + enquiry.getTargetTitle());
            tvEnquiryDate.setText(enquiry.getCreatedAt());
            tvEnquirySender.setText("From: " + enquiry.getSenderName() + " (" + enquiry.getPartySize() + " people)");
            tvEnquiryContact.setText("Contact: " + enquiry.getSenderContact());
            tvEnquiryPreferred.setText("Preferred Date: " + enquiry.getPreferredDate());
            tvEnquiryMessage.setText("“" + enquiry.getMessage() + "”");
        }
    }
}
