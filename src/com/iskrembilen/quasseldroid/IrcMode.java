package com.iskrembilen.quasseldroid;

/**
 * User: Stian
 * Date: 05.07.12
 * Time: 20:14
 */
public enum IrcMode {
    OWNER ("q", 1),
    ADMIN ("a", 2),
    OPERATOR ("o", 3),
    HALF_OPERATOR ("h", 4),
    VOICE ("v", 5),
    USER ("u", 6);


    public final String shortName;
    public final int rank;

    IrcMode(String shortName, int rank) {
        this.shortName = shortName;
        this.rank = rank;
    }
}
