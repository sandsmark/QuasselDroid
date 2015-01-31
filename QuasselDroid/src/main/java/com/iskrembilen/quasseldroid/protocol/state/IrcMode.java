package com.iskrembilen.quasseldroid.protocol.state;

import com.iskrembilen.quasseldroid.R;

/**
 * User: Stian
 * Date: 05.07.12
 * Time: 20:14
 */
public enum IrcMode implements Comparable<IrcMode> {
    /*
     * Declare in order of rank, this way values() will naturally
     * return the different modes based on rank
     */
    OWNER         (R.plurals.mode_owner,         "q", "•", "~"),
    ADMIN         (R.plurals.mode_admin,         "a", "•", "&"),
    OPERATOR      (R.plurals.mode_operator,      "o", "•", "@"),
    HALF_OPERATOR (R.plurals.mode_half_operator, "h", "•", "%"),
    VOICE         (R.plurals.mode_voice,         "v", "•", "+"),
    USER          (R.plurals.mode_user,           "",  "",  ""); //This will work as a catch-all for unknown modes

    public final int modeName;
    public final String shortModeName;
    public final String fancyIcon;
    public final String icon;

    IrcMode(int modeName, String shortModeName, String fancyIcon, String icon) {
        this.modeName = modeName;
        this.shortModeName = shortModeName;
        this.fancyIcon = fancyIcon;
        this.icon = icon;
    }
}
