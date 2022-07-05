package com.dependa.pedometer.model;

public class SleepInfoModel {
    Integer id;
    Integer sqId;
    Integer level;
    String sqName;
    String level0;
    String level1;
    String level2;
    String level3;


    public SleepInfoModel(Integer id, Integer sqId, Integer level, String sqName, String level0, String level1, String level2, String level3) {
        this.id = id;
        this.sqId = sqId;
        this.level = level;
        this.sqName = sqName;
        this.level0 = level0;
        this.level1 = level1;
        this.level2 = level2;
        this.level3 = level3;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSqId() {
        return sqId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getSqName() {
        return sqName;
    }

    public String getLevel0() {
        return level0;
    }

    public String getLevel1() {
        return level1;
    }

    public String getLevel2() {
        return level2;
    }

    public String getLevel3() {
        return level3;
    }
}
