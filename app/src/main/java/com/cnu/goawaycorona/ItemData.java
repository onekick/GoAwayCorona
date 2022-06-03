package com.cnu.goawaycorona;

import android.util.Log;

public class ItemData {
    int case_num = 0;
    int sequence_num = 0;
    boolean status = true;
    String time	= null;
    String ans = null;
    double ans_x = 0;
    double ans_y = 0;
    double app_x = 0;
    double app_y = 0;
    double basic_error = 0;
    private String address = null;

    public ItemData(int case_num, boolean status, String time, String ans, double ans_x, double ans_y, double app_x, double app_y, double basic_error) {
        this.case_num = case_num;
        this.status = status;
        this.time = time;
        this.ans = ans;
        this.ans_x = ans_x;
        this.ans_y = ans_y;
        this.app_x = app_x;
        this.app_y = app_y;
        this.basic_error = basic_error;
    }

    public ItemData(String[] record) {
        try {

            this.case_num = Integer.valueOf(record[0]);
            this.status = record[1].equals("1");
            this.time = record[2];
            this.ans = record[3];
            this.ans_x = Double.valueOf(record[4]);
            this.ans_y = Double.valueOf(record[5]);
            this.app_x = Double.valueOf(record[6]);
            this.app_y = Double.valueOf(record[7]);
            this.basic_error = Double.valueOf(record[8]);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("ItemData","record size="+record.length);
            for(String str : record) Log.e("ItemData","str="+str);
        }
    }

    public int getCase_num() {
        return case_num;
    }

    public void setCase_num(int case_num) {
        this.case_num = case_num;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAns() {
        return ans;
    }

    public void setAns(String ans) {
        this.ans = ans;
    }

    public double getAns_x() {
        return ans_x;
    }

    public void setAns_x(double ans_x) {
        this.ans_x = ans_x;
    }

    public double getAns_y() {
        return ans_y;
    }

    public void setAns_y(double ans_y) {
        this.ans_y = ans_y;
    }

    public double getApp_x() {
        return app_x;
    }

    public void setApp_x(double app_x) {
        this.app_x = app_x;
    }

    public double getApp_y() {
        return app_y;
    }

    public void setApp_y(double app_y) {
        this.app_y = app_y;
    }

    public double getBasic_error() {
        return basic_error;
    }

    public void setBasic_error(double basic_error) {
        this.basic_error = basic_error;
    }

    public void setAddress(String addr) {
        address = addr;
    }

    public String getAddress() {
        return address;
    }
}
