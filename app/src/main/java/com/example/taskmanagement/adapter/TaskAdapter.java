package com.example.taskmanagement.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagement.R;
import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.TaskStatus;
import com.example.taskmanagement.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern RecyclerView Adapter for displaying task cards with visual indicators for
 * Priority, Status, Due Date, and Overdue state.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskStatusClickListener {
        void onStatusClick(Task task, View anchorView);
    }

    public interface OnTaskEditClickListener {
        void onEditClick(Task task);
    }

    public interface OnTaskDeleteClickListener {
        void onDeleteClick(Task task);
    }

    private final Context context;
    private final List<Task> taskList = new ArrayList<>();
    private OnTaskClickListener clickListener;
    private OnTaskStatusClickListener statusClickListener;
    private OnTaskEditClickListener editClickListener;
    private OnTaskDeleteClickListener deleteClickListener;

    public TaskAdapter(Context context) {
        this.context = context;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTaskStatusClickListener(OnTaskStatusClickListener listener) {
        this.statusClickListener = listener;
    }

    public void setOnTaskEditClickListener(OnTaskEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void setOnTaskDeleteClickListener(OnTaskDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.taskList.clear();
        if (tasks != null) {
            this.taskList.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        if (position >= 0 && position < taskList.size()) {
            return taskList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View viewPriorityIndicator;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvPriority;
        private final TextView tvStatus;
        private final TextView tvStartDate;
        private final TextView tvDueDate;
        private final TextView tvOverdueBadge;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPriorityIndicator = itemView.findViewById(R.id.viewPriorityIndicator);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvStartDate = itemView.findViewById(R.id.tvTaskStartDate);
            tvDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            tvOverdueBadge = itemView.findViewById(R.id.tvOverdueBadge);
            btnEdit = itemView.findViewById(R.id.btnTaskEdit);
            btnDelete = itemView.findViewById(R.id.btnTaskDelete);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onTaskClick(taskList.get(pos));
                }
            });

            tvStatus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && statusClickListener != null) {
                    statusClickListener.onStatusClick(taskList.get(pos), v);
                }
            });

            btnEdit.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && editClickListener != null) {
                    editClickListener.onEditClick(taskList.get(pos));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(taskList.get(pos));
                }
            });
        }

        public void bind(Task task) {
            tvTitle.setText(task.getTitle());

            // Description preview (hide if empty)
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(task.getDescription().trim());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Dates
            tvStartDate.setText(DateUtils.formatDisplayDate(task.getStartDate()));
            tvDueDate.setText(DateUtils.formatDisplayDate(task.getDueDate()));

            // Strikethrough if completed
            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            }

            // Priority Badge & Indicator Stripe
            Priority priority = task.getPriority();
            tvPriority.setText(priority.getDisplayName());
            switch (priority) {
                case HIGH:
                    viewPriorityIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                    tvPriority.setBackgroundResource(R.drawable.badge_priority_high);
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high_text));
                    break;
                case MEDIUM:
                    viewPriorityIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                    tvPriority.setBackgroundResource(R.drawable.badge_priority_medium);
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium_text));
                    break;
                case LOW:
                default:
                    viewPriorityIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                    tvPriority.setBackgroundResource(R.drawable.badge_priority_low);
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low_text));
                    break;
            }

            // Status Badge
            TaskStatus status = task.getStatus();
            tvStatus.setText(status.getDisplayName());
            switch (status) {
                case PENDING:
                    tvStatus.setBackgroundResource(R.drawable.badge_pending);
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                    break;
                case IN_PROGRESS:
                    tvStatus.setBackgroundResource(R.drawable.badge_in_progress);
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_in_progress_text));
                    break;
                case COMPLETED:
                    tvStatus.setBackgroundResource(R.drawable.badge_completed);
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text));
                    break;
            }

            // Overdue warning badge
            if (task.isOverdue()) {
                tvOverdueBadge.setVisibility(View.VISIBLE);
                long days = Math.abs(DateUtils.getDaysRemaining(task.getDueDate()));
                tvOverdueBadge.setText(days > 0 ? days + "d Overdue" : "Overdue");
            } else {
                tvOverdueBadge.setVisibility(View.GONE);
            }
        }
    }
}
