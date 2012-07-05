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
    OWNER ("owner","q", 1),
    ADMIN ("admin","a", 2),
    OPERATOR ("operator","o", 3),
    HALF_OPERATOR ("half operator","h", 4),
    VOICE ("voice","v", 5),
    USER ("user","u", 6);

    public final String name;
    public final String shortName;
    public final int rank;

    IrcMode(String name, String shortName, int rank) {
        this.name = name;
        this.shortName = shortName;
        this.rank = rank;
    }
}
