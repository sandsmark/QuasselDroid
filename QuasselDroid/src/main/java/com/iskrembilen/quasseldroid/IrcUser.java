/*
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Ken Børge Viktil
 	Copyright (C) 2011 Magnus Fjell
 	Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>

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

package com.iskrembilen.quasseldroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class IrcUser extends Observable implements Comparable<IrcUser> {
    public String name;
    public boolean away;
    public String awayMessage;
    public String ircOperator;
    public String nick;
    public List<String> channels = new ArrayList<String>();
    public String server;
    public String realName;

    public String toString() {
        return nick + " away: " + away + " Num chans: " + channels.size();
    }

    public void changeNick(String newNick) {
        nick = newNick;
        this.setChanged();
        notifyObservers(R.id.USER_CHANGEDNICK);
    }

    @Override
    public int compareTo(IrcUser another) {
        return this.nick.compareToIgnoreCase(another.nick);
    }
}
