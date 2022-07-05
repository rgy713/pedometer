package com.dependa.pedometer.model;

public class MealInfoModel {
    Integer id;
    String foodName;
    Integer foodDataId;
    Boolean breakfast;
    Boolean lunch;
    Boolean dinner;

    public MealInfoModel(Integer id, String foodName, Integer foodDataId, Boolean breakfast, Boolean lunch, Boolean dinner) {
        this.id = id;
        this.foodName = foodName;
        this.foodDataId = foodDataId;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    public Integer getId() {
        return id;
    }

    public String getFoodName() {
        return foodName;
    }

    public Integer getFoodDataId() {
        return foodDataId;
    }

    public Boolean getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(Boolean check) {
        breakfast = check;
    }

    public Boolean getLunch() {
        return lunch;
    }

    public void setLunch(Boolean check) {
        lunch = check;
    }

    public Boolean getDinner() {
        return dinner;
    }

    public void setDinner(Boolean check) {
        dinner = check;
    }
}
