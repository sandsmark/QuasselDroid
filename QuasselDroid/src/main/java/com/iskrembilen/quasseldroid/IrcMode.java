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
    OWNER("Owner", "q", "s", R.drawable.irc_operator),
    ADMIN("Admin", "a", "s", R.drawable.irc_operator),
    OPERATOR("Operator", "o", "s", R.drawable.irc_operator),
    HALF_OPERATOR("Half-Op", "h", "s", R.drawable.irc_voice),
    VOICE("Voiced", "v", "", R.drawable.irc_voice),
    USER("User", "", "s", R.drawable.im_user); //This will work as a catch-all for unknown modes

    public final String modeName;
    public final String shortModeName;
    public final String pluralization;
    public final int iconResource;

    IrcMode(String modeName, String shortModeName, String pluralization, int iconResource) {
        this.modeName = modeName;
        this.shortModeName = shortModeName;
        this.pluralization = pluralization;
        this.iconResource = iconResource;
    }

}
