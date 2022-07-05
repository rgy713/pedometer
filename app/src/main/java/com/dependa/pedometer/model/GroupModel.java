package com.dependa.pedometer.model;

public class GroupModel {
    Integer id;
    String name;
    Boolean active;
    public GroupModel(Integer id, String name, Boolean active){
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public Integer getId(){
        return id;
    }
    public String getName(){
        return name;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active){
        this.active = active;
    }
}
