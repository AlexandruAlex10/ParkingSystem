package com.sw_user;

import java.util.ArrayList;
import java.util.List;

public class PlateNumber {
    private String plateNumber = "";
    private Boolean isPermanent = false;
    private List<String> reservedDates = new ArrayList<>();

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
    public List<String> getReservedDates() {
        return reservedDates;
    }
    public void setReservedDates(List<String> reservedDates) {
        this.reservedDates = reservedDates;
    }
    public void addReservedDate(String date) {
        reservedDates.add(date);
    }
}
