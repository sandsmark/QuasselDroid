package com.iskrembilen.quasseldroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Stian
 * Date: 05.07.12
 * Time: 20:14
 */
public enum IrcMode {
    /*
     * Declare in order of rank, this way values() will naturally
     * return the different modes based on rank
     */
    //TODO: Move static strings to strings.xml
    //TODO: Add icon paths to the objects
    OWNER ("owner","q"),
    ADMIN ("admin","a"),
    OPERATOR ("operator","o"),
    HALF_OPERATOR ("half operator","h"),
    VOICE ("voice","v"),
    USER ("user",""); //This will work as a catch-all for unknown modes

    public final String modeName;
    public final String shortModeName;

    IrcMode(String modeName, String shortModeName) {
        this.modeName = modeName;
        this.shortModeName = shortModeName;
    }

}
