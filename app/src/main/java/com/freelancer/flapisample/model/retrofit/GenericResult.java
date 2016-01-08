package com.freelancer.flapisample.model.retrofit;

import com.freelancer.flapisample.model.GafProject;

import java.util.List;

/**
 * Created by neil on 9/22/15.
 *
 * This class represents the result part of a {@link RetrofitResponse}, which is usually a list of
 * Gaf Objects.
 */
public class GenericResult {
    private List<GafProject> projects;

    public List<GafProject> getProjects() {
        return projects;
    }

    public void setProjects(List<GafProject> projects) {
        this.projects = projects;
    }
}
