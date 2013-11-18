/**
 QuasselDroid - Quassel client for Android
 Copyright (C) 2010 Frederik M. J. Vestre

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


package com.iskrembilen.quasseldroid.qtcomm;

public enum DataStreamVersion {
    Qt_1_0(1),
    Qt_2_0(2),
    Qt_2_1(3),
    Qt_3_0(4),
    Qt_3_1(5),
    Qt_3_3(6),
    Qt_4_0(7),
    Qt_4_1(7),
    Qt_4_2(8),
    Qt_4_3(9),
    Qt_4_4(10),
    Qt_4_5(11),
    Qt_4_6(12),
    Qt_4_7(12),
    Qt_4_8(12);
    int value;

    DataStreamVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
};