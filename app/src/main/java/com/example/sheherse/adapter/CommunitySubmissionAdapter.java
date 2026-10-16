package com.example.sheherse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.R;
import com.example.sheherse.model.CommunitySubmission;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CommunitySubmissionAdapter extends RecyclerView.Adapter<CommunitySubmissionAdapter.SubmissionViewHolder> {

    public interface OnUpvoteClickListener {
        void onUpvoteClick(CommunitySubmission submission, int position);
    }

    private final Context context;
    private List<CommunitySubmission> submissions;
    private final OnUpvoteClickListener upvoteClickListener;

    public CommunitySubmissionAdapter(Context context, List<CommunitySubmission> submissions, OnUpvoteClickListener upvoteClickListener) {
        this.context = context;
        this.submissions = submissions != null ? submissions : new ArrayList<>();
        this.upvoteClickListener = upvoteClickListener;
    }

    public void updateSubmissions(List<CommunitySubmission> newSubmissions) {
        this.submissions = newSubmissions != null ? newSubmissions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        CommunitySubmission sub = submissions.get(position);
        holder.bind(sub);
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    class SubmissionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCommType;
        private final TextView tvCommCategory;
        private final TextView tvCommStatus;
        private final TextView tvCommTitle;
        private final TextView tvCommDescription;
        private final TextView tvCommAuthor;
        private final TextView tvCommArea;
        private final MaterialButton btnUpvote;

        public SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommType = itemView.findViewById(R.id.tvCommType);
            tvCommCategory = itemView.findViewById(R.id.tvCommCategory);
            tvCommStatus = itemView.findViewById(R.id.tvCommStatus);
            tvCommTitle = itemView.findViewById(R.id.tvCommTitle);
            tvCommDescription = itemView.findViewById(R.id.tvCommDescription);
            tvCommAuthor = itemView.findViewById(R.id.tvCommAuthor);
            tvCommArea = itemView.findViewById(R.id.tvCommArea);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
        }

        public void bind(CommunitySubmission sub) {
            tvCommType.setText(sub.getType());
            tvCommCategory.setText("• " + sub.getCategory());
            tvCommTitle.setText(sub.getTitle());
            tvCommDescription.setText(sub.getDescription());
            tvCommAuthor.setText("By " + sub.getAuthorName());
            tvCommArea.setText(sub.getArea());
            btnUpvote.setText(sub.getUpvotes() + " ▲");

            String status = sub.getStatus();
            tvCommStatus.setText(status);
            if (CommunitySubmission.STATUS_APPROVED.equalsIgnoreCase(status)) {
                tvCommStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_verified_bg));
                tvCommStatus.setTextColor(ContextCompat.getColor(context, R.color.status_verified_text));
            } else if (CommunitySubmission.STATUS_PENDING.equalsIgnoreCase(status)) {
                tvCommStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending_bg));
                tvCommStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
            } else {
                tvCommStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_rejected_bg));
                tvCommStatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected_text));
            }

            btnUpvote.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && upvoteClickListener != null) {
                    upvoteClickListener.onUpvoteClick(submissions.get(pos), pos);
                }
            });
        }
    }
}
