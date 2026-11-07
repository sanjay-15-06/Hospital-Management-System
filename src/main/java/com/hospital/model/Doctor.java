package com.hospital.model;

public class Doctor {
    private Long id;
    private String name;
    private String specialization;
    private String phone;

    public Doctor() {}
    public Doctor(Long id, String name, String specialization, String phone) {
        this.id=id; this.name=name; this.specialization=specialization; this.phone=phone;
    }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String specialization){this.specialization=specialization;}
    public String getPhone(){return phone;} public void setPhone(String phone){this.phone=phone;}
}
