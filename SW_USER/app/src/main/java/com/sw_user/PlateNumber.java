package com.sw_user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlateNumber {
    private String plateNumber;
    private Boolean isPermanent;
    private List<String> allowedDates = new ArrayList<>();

    public String getPlateNumber() {
        return plateNumber;
    }
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    public Boolean getIsPermanent() {
        return isPermanent;
    }
    public void setIsPermanent(Boolean isPermanent) {
        this.isPermanent = isPermanent;
    }
    public List<String> getAllowedDates() {
        return allowedDates;
    }
    public void setAllowedDates(List<String> allowedDates) {
        this.allowedDates = allowedDates;
    }
    public void addAllowedDate(String allowedDate) {
        allowedDates.add(allowedDate);
    }
    public void emptyAllowedDates() {
        allowedDates.clear();
    }
}
