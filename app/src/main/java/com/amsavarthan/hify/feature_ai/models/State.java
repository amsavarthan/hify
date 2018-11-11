package com.amsavarthan.hify.feature_ai.models;

public class State {

    String solution_title,name,input;
    String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getSolution_title() {
        return solution_title;
    }

    public void setSolution_title(String solution_title) {
        this.solution_title = solution_title;
    }
}
