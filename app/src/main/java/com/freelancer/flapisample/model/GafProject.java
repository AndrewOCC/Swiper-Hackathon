package com.freelancer.flapisample.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by neil on 9/21/15.
 *
 * A model class that describes the information that a project posted at freelancer
 * would have.
 */
public class GafProject {
    public enum FrontendProjectStatus {
        /**
         * Projects that are active for bidding or are frozen because the employer has
         * awarded the project to a freelancer
         */
        OPEN,

        /**
         * Projects that have been awarded to a freelancer and have been accepted by the
         * winning freelancer
         */
        WORK_IN_PROGRESS,

        /**
         * Projects that have been completed
         */
        COMPLETE,

        /**
         * Projects that are still waiting to be approved for posting
         */
        PENDING;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum ProjectState {
        /**
         * The project has not been awarded by the owner
         */
        ACTIVE,

        /**
         * The project is waiting on some other user interaction.
         * Should check sub_state for more information
         */
        FROZEN,

        /**
         * Project can no longer be awarded
         */
        CLOSED,

        /**
         * Project is in the draft state. This *shouldnt* really happen
         */
        DRAFT,


        /**
         * Project is deleted
         */
        DELETED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum SubState {

        /**
         * The project has been awarded, but has not yet been accepted by the user
         */
        FROZEN_AWARDED,

        /**
         * The project has not been awarded to any freelancer
         */
        FROZEN_TIMEOUT,

        /**
         * Project has been awarded AND accepted by the bidding user
         */
        CLOSED_AWARDED,

        /**
         * Project timed out without award (after FROZEN_TIMEOUT).
         */
        CLOSED_EXPIRED,

        /**
         * The buyer has cancelled this project
         */
        CANCEL_BUYER,

        /**
         * An admin has cancelled the project
         */
        CANCEL_ADMIN;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static enum ProjectType {
        @SerializedName("fixed")
        FIXED,
        @SerializedName("hourly")
        HOURLY;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

    }


    private long id;

    @SerializedName("owner_id")
    private long ownerId;

    private String title;

    @SerializedName("preview_description")
    private String previewDescription;

    @SerializedName("submitdate")
    private long submitDate;

    @SerializedName("bidperiod")
    private int bidPeriod;

    private GafCurrency currency;

    @SerializedName("status")
    private ProjectState state;

    @SerializedName("sub_status")
    private SubState substate;

    @SerializedName("type")
    private ProjectType projectType;

    @SerializedName("frontend_project_status")
    private FrontendProjectStatus frontendProjectStatus;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreviewDescription() {
        return previewDescription;
    }

    public void setPreviewDescription(String previewDescription) {
        this.previewDescription = previewDescription;
    }

    public long getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(long submitDate) {
        this.submitDate = submitDate;
    }

    public int getBidPeriod() {
        return bidPeriod;
    }

    public void setBidPeriod(int bidPeriod) {
        this.bidPeriod = bidPeriod;
    }

    public GafCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(GafCurrency currency) {
        this.currency = currency;
    }

    public ProjectState getState() {
        return state;
    }

    public void setState(ProjectState state) {
        this.state = state;
    }

    public SubState getSubstate() {
        return substate;
    }

    public void setSubstate(SubState substate) {
        this.substate = substate;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public FrontendProjectStatus getFrontendProjectStatus() {
        return frontendProjectStatus;
    }

    public void setFrontendProjectStatus(FrontendProjectStatus frontendProjectStatus) {
        this.frontendProjectStatus = frontendProjectStatus;
    }
}
