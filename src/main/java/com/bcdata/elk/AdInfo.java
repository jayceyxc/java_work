package com.bcdata.elk;

import java.util.Objects;

public class AdInfo {

    private int campaignID;
    private int policyID;

    public AdInfo() {

    }

    public AdInfo (int campaignId, int groupId) {
        this.campaignID = campaignId;
        this.policyID = groupId;
    }

    public int getCampaignID () {
        return campaignID;
    }

    public void setCampaignID (int campaignID) {
        this.campaignID = campaignID;
    }

    public int getPolicyID () {
        return policyID;
    }

    public void setPolicyID (int policyID) {
        this.policyID = policyID;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;
        AdInfo adInfo = (AdInfo) o;
        return campaignID == adInfo.campaignID &&
                policyID == adInfo.policyID;
    }

    @Override
    public int hashCode () {

        return Objects.hash (campaignID, policyID);
    }

    @Override
    public String toString () {
        return "AdInfo{" +
                "campaignID=" + campaignID +
                ", policyID=" + policyID +
                '}';
    }
}
