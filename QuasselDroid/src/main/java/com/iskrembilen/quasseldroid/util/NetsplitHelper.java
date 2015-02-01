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

package com.iskrembilen.quasseldroid.util;

import java.util.ArrayList;
import java.util.List;

public class NetsplitHelper {
    private static final int maxNetsplitNicks = 15;

    private List<String> nicks = new ArrayList<String>();
    private String sideOne;
    private String sideTwo;

    public NetsplitHelper(String netsplitString) {
        String[] splitString = netsplitString.split("#:#");
        for (int i = 0; i < splitString.length; i++) {
            if (i < splitString.length - 1) {
                nicks.add(splitString[i].split("!")[0]);
            } else if (i == splitString.length - 1) {
                String[] sides = splitString[i].split(" ");
                if (sides.length > 1) {
                    sideOne = sides[0];
                    sideTwo = sides[1];
                }
            }
        }
    }

    public List<String> getNicks() {
        return nicks;
    }

    public String getSideOne() {
        return sideOne;
    }

    public String getSideTwo() {
        return sideTwo;
    }

    public String formatJoinMessage() {
        return "Netsplit between " + sideOne + " and " + sideTwo + " ended. Users joined: " + formatNickList();
    }

    public String formatQuitMessage() {
        return "Netsplit between " + sideOne + " and " + sideTwo + ". Users quit: " + formatNickList();
    }

    private String formatNickList() {
        String nickList = "";

        int i = 0;
        for (; i < nicks.size() && i < maxNetsplitNicks; i++) {
            nickList += nicks.get(i);
            if ((i < nicks.size() - 1) && (i < maxNetsplitNicks - 1)) {
                nickList += ", ";
            }
        }

        if (i == maxNetsplitNicks && nicks.size() > maxNetsplitNicks) {
            nickList += " (" + (nicks.size() - maxNetsplitNicks) + " more)";
        }

        return nickList;
    }
}
