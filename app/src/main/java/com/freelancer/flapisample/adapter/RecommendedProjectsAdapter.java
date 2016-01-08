package com.freelancer.flapisample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freelancer.flapisample.R;
import com.freelancer.flapisample.model.GafProject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by neil on 9/22/15.
 *
 * Simple array-backed adapter for displaying {@link GafProject} items in a {@link RecyclerView}
 * with support for showing an indeterminate {@link ProgressBar} for indicating a loading state.
 */
public class RecommendedProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROJECT = 0x00;
    private static final int TYPE_LOADING = 0x01;

    private ArrayList<GafProject> projects;
    private boolean isLoading;

    public RecommendedProjectsAdapter () {
        super();
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_PROJECT:
                View view = inflater.inflate(R.layout.li_recommended_project, parent, false);
                return new ProjectViewHolder(view);

            case TYPE_LOADING:
                View progress = inflater.inflate(R.layout.li_progress_bar, parent, false);
                return new ProgressBarViewHolder(progress);

            default:
                return null;
        }
    }

    public void remove(int position) {
        projects.remove(position);
        this.notifyItemRemoved(position);
    }

    public void add(GafProject project) {
        projects.add(0,project);
        this.notifyItemInserted(projects.size()-1);
    }

    @Override
    public int getItemViewType(int position) {
        return !isLoading ? TYPE_PROJECT
                : position == projects.size() ? TYPE_LOADING
                    : TYPE_PROJECT;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case TYPE_PROJECT:
                GafProject project = projects.get(position);
                ProjectViewHolder myViewHolder = (ProjectViewHolder) holder;
                myViewHolder.projectTitle.setText(project.getTitle());
                myViewHolder.projectDescription.setText(project.getPreviewDescription());
                break;

        }
    }

    @Override
    public int getItemCount() {
        return projects.size() + (isLoading ? 1 : 0);
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.project_title)
        TextView projectTitle;

        @Bind(R.id.project_preview_description)
        TextView projectDescription;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class ProgressBarViewHolder extends RecyclerView.ViewHolder{

        public ProgressBarViewHolder(View itemView) {
            super(itemView);
        }
    }
}
