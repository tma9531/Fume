package com.fumedatabase.fumedatabase_api.model;

/**
 * Represents a platform on which video games can exist on and users can own.
 */
public class Platform {

    private String name;
    private int pnr;

    /**
     * Constructor to initialize a Platform object with the provided name
     * @param name name of the platform ("PSP, "PS4", "PC", etc.)
     */
    public Platform(String name) {
        this.name = name;
    }

    /**
     * Constructor to initialize a Platform object with the provided PNR and name
     * @param pnr platform number(unique identifier)
     * @param name name of the platform ("PSP, "PS4", "PC", etc.)
     */
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
