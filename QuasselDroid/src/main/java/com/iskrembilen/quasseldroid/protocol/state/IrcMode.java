/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.iskrembilen.quasseldroid.protocol.state;

import com.iskrembilen.quasseldroid.R;

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
