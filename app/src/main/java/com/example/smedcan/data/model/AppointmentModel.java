package com.example.smedcan.data.model;

public class AppointmentModel {
    private Integer idappointments;
    private String date;
    private String time;
    private String username;
    private String email;
    private String price;
    private String dtype;
    private String docname;

    public AppointmentModel() {
    }

    public AppointmentModel(Doctor doctor, Patients patients, String date, String time) {
        this.date = date;
        this.time = time;
    }

    public Integer getIdappointments() {
        return this.idappointments;
    }

    public void setIdappointments(Integer idappointments) {
        this.idappointments = idappointments;
    }
    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getDocname() {
        return docname;
    }

    public void setDocname(String docname) {
        this.docname = docname;
    }
}
