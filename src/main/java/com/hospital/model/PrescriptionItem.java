package com.hospital.model;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionItem {
    private String medicineName;
    private String strength;
    private String dosage;
    private int durationDays;
    private String foodTiming;
    private List<String> times = new ArrayList<>();
    private int quantity;
    private String instructions;

    public PrescriptionItem() {}

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public String getFoodTiming() { return foodTiming; }
    public void setFoodTiming(String foodTiming) { this.foodTiming = foodTiming; }
    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times == null ? new ArrayList<>() : times; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
