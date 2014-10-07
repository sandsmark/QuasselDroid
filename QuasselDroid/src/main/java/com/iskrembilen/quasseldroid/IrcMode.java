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
    OWNER("Owner", "q", "s", R.color.nick_owner_color, "●"),
    ADMIN("Admin", "a", "s", R.color.nick_admin_color, "●"),
    OPERATOR("Operator", "o", "s", R.color.nick_operator_color, "●"),
    HALF_OPERATOR("Half-Op", "h", "s", R.color.nick_halfop_color, "●"),
    VOICE("Voiced", "v", "", R.color.nick_voice_color, "●"),
    USER("User", "", "s", R.color.nick_user_color, ""); //This will work as a catch-all for unknown modes

    public final String modeName;
    public final String shortModeName;
    public final String pluralization;
    public final int colorResource;
    public final String icon;

    IrcMode(String modeName, String shortModeName, String pluralization, int colorResource, String icon) {
        this.modeName = modeName;
        this.shortModeName = shortModeName;
        this.pluralization = pluralization;
        this.colorResource = colorResource;
        this.icon = icon;
    }

}
