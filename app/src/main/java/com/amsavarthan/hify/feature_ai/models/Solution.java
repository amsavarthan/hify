package com.amsavarthan.hify.feature_ai.models;

import java.util.ArrayList;
import java.util.List;

public class Solution {

    String title;
    String description;
    boolean isDefaultCard,isStateResult,isStepbyStepSolution;
    String src;
    public List<String> state_input;
    public List<String> state_name;
    int state_count;
    int height,width;
    String input;


    public boolean isStepbyStepSolution() {
        return isStepbyStepSolution;
    }

    public void setStepbyStepSolution(boolean stepbyStepSolution) {
        isStepbyStepSolution = stepbyStepSolution;
    }

    public boolean isStateResult() {
        return isStateResult;
    }

    public void setStateResult(boolean stateResult) {
        isStateResult = stateResult;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getState_count() {
        return state_count;
    }

    public void setState_count(int state_count) {
        this.state_count = state_count;
    }

    public List<String> getState_input() {
        return state_input;
    }

    public List<String> getState_name() {
        return state_name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultCard() {
        return isDefaultCard;
    }

    public void setDefaultCard(boolean defaultCard) {
        isDefaultCard = defaultCard;
    }

}
