package com.bcdata.elk;

import java.util.Objects;

public class CityInfo {

    private String cityName;
    private String provinceName;

    public CityInfo () {
    }

    public CityInfo (String cityName, String provinceName) {
        this.cityName = cityName;
        this.provinceName = provinceName;
    }

    public String getCityName () {
        return cityName;
    }

    public void setCityName (String cityName) {
        this.cityName = cityName;
    }

    public String getProvinceName () {
        return provinceName;
    }

    public void setProvinceName (String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;
        CityInfo that = (CityInfo) o;
        return Objects.equals (cityName, that.cityName) &&
                Objects.equals (provinceName, that.provinceName);
    }

    @Override
    public int hashCode () {

        return Objects.hash (cityName, provinceName);
    }

    @Override
    public String toString () {
        return "CityInfo{" +
                "cityName='" + cityName + '\'' +
                ", provinceName='" + provinceName + '\'' +
                '}';
    }
}
