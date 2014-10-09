package com.iskrembilen.quasseldroid;

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
    OWNER("Owner", "q", "s", "●"),
    ADMIN("Admin", "a", "s",  "●"),
    OPERATOR("Operator", "o", "s",  "●"),
    HALF_OPERATOR("Half-Op", "h", "s",  "●"),
    VOICE("Voiced", "v", "",  "●"),
    USER("User", "", "s",  ""); //This will work as a catch-all for unknown modes

    public final String modeName;
    public final String shortModeName;
    public final String pluralization;
    public final String icon;

    IrcMode(String modeName, String shortModeName, String pluralization, String icon) {
        this.modeName = modeName;
        this.shortModeName = shortModeName;
        this.pluralization = pluralization;
        this.icon = icon;
    }

}
