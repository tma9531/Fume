/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.fumedatabase.fumedatabase_api.model;

/**
 *
 * @author emmettpeterson
 */
public class Platform {

    private String name;
    private int pnr;

    public Platform(String name) {
        this.name = name;
    }

    public Platform(int pnr, String name) {
        this.pnr = pnr;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getPnr(){
        return pnr;
    }

    public void setName(String name){
        this.name = name;
    }

    

}
