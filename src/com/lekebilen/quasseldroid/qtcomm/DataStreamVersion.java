/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm;

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
    DataStreamVersion(int value){
    	this.value = value;
    }
    public int getValue(){
    	return value;
    }
};