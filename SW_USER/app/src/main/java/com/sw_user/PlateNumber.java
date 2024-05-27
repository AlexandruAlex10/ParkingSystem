package com.sw_user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlateNumber {
    private String key;
    private String plateNumber;
    private Boolean isPermanent;
    private List<Date> allowedDates = new ArrayList<>();

    public String getKey() {
        return key;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public Boolean getIsPermanent() {
        return isPermanent;
    }

    public List<Date> getAllowedDates() {
        return allowedDates;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setIsPermanent(Boolean isPermanent) {
        this.isPermanent = isPermanent;
    }

    public void setAllowedDates(List<Date> allowedDates) {
        this.allowedDates = allowedDates;
    }
}
