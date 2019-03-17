package com.attendence.attendenceapp;

/**
 * Created by User on 2/27/2019.
 */

public class Employee {
    //private final int name;
    private final String name;
    private String imageName;
    private final int id;
    private final int department;
    private int status;
    private boolean isFavorite;

    public Employee(String name, String imageName, int department,int id, Boolean isFavorite, int status) {
        this.name = name;
        this.id = id;
        this.department = department;
        this.imageName = imageName;
        this.status = status;
        this.isFavorite = isFavorite;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return Integer.toString(this.id);
// return this.imageName;
    }

    public int getDepartment(){
        return department;
    }

    public int getId() {
        return id;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int newStatus) {
        status = newStatus;
    }

    public boolean getIsFavorite() {
        return isFavorite;
    }
    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void toggleFavorite() {
        isFavorite = !isFavorite;
    }
}
