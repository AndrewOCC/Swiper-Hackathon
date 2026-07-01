package com.freelancer.flapisample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freelancer.flapisample.R;
import com.freelancer.flapisample.model.GafProject;

import java.util.ArrayList;
import java.util.List;

public class RecommendedProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROJECT = 0x00;
    private static final int TYPE_LOADING = 0x01;

    private final ArrayList<GafProject> projects;
    private boolean isLoading;

    public RecommendedProjectsAdapter() {
        projects = new ArrayList<>();
    }

    public void addAll(List<GafProject> projects) {
        this.projects.addAll(projects);
        notifyDataSetChanged();
    }

    public void clear() {
        projects.clear();
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_LOADING) {
            View progress = inflater.inflate(R.layout.li_progress_bar, parent, false);
            return new ProgressBarViewHolder(progress);
        }

        View view = inflater.inflate(R.layout.li_recommended_project, parent, false);
        return new ProjectViewHolder(view);
    }

    public void remove(int position) {
        projects.remove(position);
        notifyItemRemoved(position);
    }

    public void add(GafProject project) {
        projects.add(0, project);
        notifyItemInserted(0);
    }

    @Override
    public int getItemViewType(int position) {
        return !isLoading || position != projects.size() ? TYPE_PROJECT : TYPE_LOADING;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) != TYPE_PROJECT) {
            return;
        }

        GafProject project = projects.get(position);
        ProjectViewHolder projectViewHolder = (ProjectViewHolder) holder;
        projectViewHolder.projectTitle.setText(project.getTitle());
        projectViewHolder.projectDescription.setText(project.getPreviewDescription());
    }

    @Override
    public int getItemCount() {
        return projects.size() + (isLoading ? 1 : 0);
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {

        final TextView projectTitle;
        final TextView projectDescription;

        ProjectViewHolder(View itemView) {
            super(itemView);
            projectTitle = itemView.findViewById(R.id.project_title);
            projectDescription = itemView.findViewById(R.id.project_preview_description);
        }
    }

    static class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        ProgressBarViewHolder(View itemView) {
            super(itemView);
        }
    }
}
