package com.bcdata.elk;

import java.util.Objects;

public class AdDetail {

    private String uid;
    private int aid;
    private String ip;
    private int campaign;
    private int policy;
    private int city_id;
    private String city;
    private String province;
    private String sp;
    private String network;
    private String time;
    private long timeVal;
    private String domain;
    private String host;
    private String url;
    private String agent;
    private String bidder;
    private String pushid;
    private String lacci;
    private int show;
    private int click;
    private double price;

    public AdDetail () {
        this.show = 0;
        this.click = 0;
    }

    public String getUid () {
        return uid;
    }

    public void setUid (String uid) {
        this.uid = uid;
    }

    public int getAid () {
        return aid;
    }

    public void setAid (int aid) {
        this.aid = aid;
    }

    public String getIp () {
        return ip;
    }

    public void setIp (String ip) {
        this.ip = ip;
    }

    public int getCampaign () {
        return campaign;
    }

    public void setCampaign (int campaign) {
        this.campaign = campaign;
    }

    public int getPolicy () {
        return policy;
    }

    public void setPolicy (int policy) {
        this.policy = policy;
    }

    public int getCity_id () {
        return city_id;
    }

    public void setCity_id (int city_id) {
        this.city_id = city_id;
    }

    public String getCity () {
        return city;
    }

    public void setCity (String city) {
        this.city = city;
    }

    public String getProvince () {
        return province;
    }

    public void setProvince (String province) {
        this.province = province;
    }

    public String getSp () {
        return sp;
    }

    public void setSp (String sp) {
        this.sp = sp;
    }

    public String getNetwork () {
        return network;
    }

    public void setNetwork (String network) {
        this.network = network;
    }

    public String getTime () {
        return time;
    }

    public void setTime (String time) {
        this.time = time;
    }

    public long getTimeVal () {
        return timeVal;
    }

    public void setTimeVal (long timeVal) {
        this.timeVal = timeVal;
    }

    public String getDomain () {
        return domain;
    }

    public void setDomain (String domain) {
        this.domain = domain;
    }

    public String getHost () {
        return host;
    }

    public void setHost (String host) {
        this.host = host;
    }

    public String getUrl () {
        return url;
    }

    public void setUrl (String url) {
        this.url = url;
    }

    public String getAgent () {
        return agent;
    }

    public void setAgent (String agent) {
        this.agent = agent;
    }

    public String getBidder () {
        return bidder;
    }

    public void setBidder (String bidder) {
        this.bidder = bidder;
    }

    public String getPushid () {
        return pushid;
    }

    public void setPushid (String pushid) {
        this.pushid = pushid;
    }

    public String getLacci () {
        return lacci;
    }

    public void setLacci (String lacci) {
        this.lacci = lacci;
    }

    public int getShow () {
        return show;
    }

    public void setShow (int show) {
        this.show = show;
    }

    public int getClick () {
        return click;
    }

    public void setClick (int click) {
        this.click = click;
    }

    public double getPrice () {
        return price;
    }

    public void setPrice (double price) {
        this.price = price;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;
        AdDetail adDetail = (AdDetail) o;
        return aid == adDetail.aid &&
                timeVal == adDetail.timeVal &&
                campaign == adDetail.campaign &&
                policy == adDetail.policy &&
                city_id == adDetail.city_id &&
                show == adDetail.show &&
                click == adDetail.click &&
                Double.compare (adDetail.price, price) == 0 &&
                Objects.equals (uid, adDetail.uid) &&
                Objects.equals (ip, adDetail.ip) &&
                Objects.equals (city, adDetail.city) &&
                Objects.equals (province, adDetail.province) &&
                Objects.equals (sp, adDetail.sp) &&
                Objects.equals (network, adDetail.network) &&
                Objects.equals (time, adDetail.time) &&
                Objects.equals (domain, adDetail.domain) &&
                Objects.equals (host, adDetail.host) &&
                Objects.equals (url, adDetail.url) &&
                Objects.equals (agent, adDetail.agent) &&
                Objects.equals (bidder, adDetail.bidder) &&
                Objects.equals (pushid, adDetail.pushid) &&
                Objects.equals (lacci, adDetail.lacci);
    }

    @Override
    public int hashCode () {

        return Objects.hash (uid, aid, ip, campaign, policy, city_id, city, province, sp, network, time, timeVal, domain, host, url, agent, bidder, pushid, lacci, show, click, price);
    }

    @Override
    public String toString () {
        return "AdDetail{" +
                "uid='" + uid + '\'' +
                ", aid=" + aid +
                ", ip='" + ip + '\'' +
                ", campaign=" + campaign +
                ", policy=" + policy +
                ", city_id=" + city_id +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", sp='" + sp + '\'' +
                ", network='" + network + '\'' +
                ", time='" + time + '\'' +
                ", timeVal='" + timeVal + '\'' +
                ", domain='" + domain + '\'' +
                ", host='" + host + '\'' +
                ", url='" + url + '\'' +
                ", agent='" + agent + '\'' +
                ", bidder='" + bidder + '\'' +
                ", pushid='" + pushid + '\'' +
                ", lacci='" + lacci + '\'' +
                ", show=" + show +
                ", click=" + click +
                ", price=" + price +
                '}';
    }
}
