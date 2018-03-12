package com.dreamhack.example.jasper.dto;

public class Employee {
    private String FIRST_NAME;
    private String LAST_NAME;
    private Integer SALARY;

    public Employee(String FIRST_NAME, String LAST_NAME, Integer SALARY) {
        this.FIRST_NAME = FIRST_NAME;
        this.LAST_NAME = LAST_NAME;
        this.SALARY = SALARY;
    }

    public String getFIRST_NAME() {
        return FIRST_NAME;
    }

    public void setFIRST_NAME(String FIRST_NAME) {
        this.FIRST_NAME = FIRST_NAME;
    }

    public String getLAST_NAME() {
        return LAST_NAME;
    }

    public void setLAST_NAME(String LAST_NAME) {
        this.LAST_NAME = LAST_NAME;
    }

    public Integer getSALARY() {
        return SALARY;
    }

    public void setSALARY(Integer SALARY) {
        this.SALARY = SALARY;
    }
}
