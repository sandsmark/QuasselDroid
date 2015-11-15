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

import android.util.Log;

import com.iskrembilen.quasseldroid.events.RequestCreateIdentityEvent;
import com.iskrembilen.quasseldroid.events.RequestRemoteSyncEvent;
import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.protocol.state.serializers.Syncable;
import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;

import java.util.List;
import java.util.Map;

public class Identity extends SyncableObject {
    @Syncable(type=QVariantType.String)
    private String       identityName;
    @Syncable(type=QVariantType.StringList)
    private List<String> nicks;
    @Syncable(type=QVariantType.String)
    private String       ident;
    @Syncable(type=QVariantType.String)
    private String       realName;
    @Syncable(userType="IdentityId")
    private int          identityId;

    @Syncable(type=QVariantType.Bool)
    private boolean      autoAwayEnabled;
    @Syncable(type=QVariantType.Bool)
    private boolean      autoAwayReasonEnabled;
    @Syncable(type=QVariantType.Int)
    private int          autoAwayTime;
    @Syncable(type=QVariantType.Bool)
    private boolean      awayNickEnabled;
    @Syncable(type=QVariantType.Bool)
    private boolean      awayReasonEnabled;
    @Syncable(type=QVariantType.Bool)
    private boolean      detachAwayEnabled;
    @Syncable(type=QVariantType.Bool)
    private boolean      detachAwayReasonEnabled;

    @Syncable(type=QVariantType.String)
    private String       awayReason;
    @Syncable(type=QVariantType.String)
    private String       autoAwayReason;
    @Syncable(type=QVariantType.String)
    private String       detachAwayReason;

    @Syncable(type=QVariantType.String)
    private String       partReason;
    @Syncable(type=QVariantType.String)
    private String       quitReason;
    @Syncable(type=QVariantType.String)
    private String       awayNick;

    @Syncable(type=QVariantType.String)
    private String       kickReason;

    public void create() {
        BusProvider.getInstance().post(new RequestCreateIdentityEvent(this.identityId, this.toVariantMap()));
    }

    @Override
    public String toString() {
        return identityName;
    }

    public String getIdentityName () {
        return this.identityName;
    }
    public String getRealName () {
        return this.realName;
    }
    public String getAwayNick () {
        return this.awayNick;
    }
    public String getAwayReason () {
        return this.awayReason;
    }
    public String getAutoAwayReason () {
        return this.autoAwayReason;
    }
    public String getDetachAwayReason () {
        return this.detachAwayReason;
    }
    public String getIdent () {
        return this.ident;
    }
    public String getKickReason () {
        return this.kickReason;
    }
    public String getPartReason () {
        return this.partReason;
    }
    public String getQuitReason () {
        return this.quitReason;
    }
    public int getIdentityId () {
        return this.identityId;
    }
    public int getAutoAwayTime () {
        return this.autoAwayTime;
    }
    public boolean getAwayNickEnabled () {
        return this.awayNickEnabled;
    }
    public boolean getAwayReasonEnabled () {
        return this.awayReasonEnabled;
    }
    public boolean getAutoAwayEnabled () {
        return this.autoAwayEnabled;
    }
    public boolean getAutoAwayReasonEnabled () {
        return this.autoAwayReasonEnabled;
    }
    public boolean getDetachAwayEnabled () {
        return this.detachAwayEnabled;
    }
    public boolean getDetachAwayReasonEnabled () {
        return this.detachAwayReasonEnabled;
    }
    public List<String> getNicks () {
        return this.nicks;
    }

    public void setIdentityName (String identityName) {
        if (!identityName.equals(this.identityName)) {
            this.identityName = identityName;
            update();
        }
    }
    public void setRealName (String realName) {
        if (!realName.equals(this.realName)) {
            this.realName = realName;
            update();
        }
    }
    public void setAwayNick (String awayNick) {
        if (!awayNick.equals(this.awayNick)) {
            this.awayNick = awayNick;
            update();
        }
    }
    public void setAwayReason (String awayReason) {
        if (!awayReason.equals(this.awayReason)) {
            this.awayReason = awayReason;
            update();
        }
    }
    public void setAutoAwayReason (String autoAwayReason) {
        if (!autoAwayReason.equals(this.autoAwayReason)) {
            this.autoAwayReason = autoAwayReason;
            update();
        }
    }
    public void setDetachAwayReason (String detachAwayReason) {
        if (!detachAwayReason.equals(this.detachAwayReason)) {
            this.detachAwayReason = detachAwayReason;
            update();
        }
    }
    public void setIdent (String ident) {
        if (!ident.equals(this.ident)) {
            this.ident = ident;
            update();
        }
    }
    public void setKickReason (String kickReason) {
        if (!kickReason.equals(this.kickReason)) {
            this.kickReason = kickReason;
            update();
        }
    }
    public void setPartReason (String partReason) {
        if (!partReason.equals(this.partReason)) {
            this.partReason = partReason;
            update();
        }
    }
    public void setQuitReason (String quitReason) {
        if (!quitReason.equals(this.quitReason)) {
            this.quitReason = quitReason;
            update();
        }
    }
    public void setIdentityId (int identityId) {
        if (identityId != this.identityId) {
            this.identityId = identityId;
            update();
        }
    }
    public void setAutoAwayTime (int autoAwayTime) {
        if (autoAwayTime != this.autoAwayTime) {
            this.autoAwayTime = autoAwayTime;
            update();
        }
    }
    public void setAwayNickEnabled (boolean awayNickEnabled) {
        if (awayNickEnabled != this.awayNickEnabled) {
            this.awayNickEnabled = awayNickEnabled;
            update();
        }
    }
    public void setAwayReasonEnabled (boolean awayReasonEnabled) {
        if (awayReasonEnabled != this.awayReasonEnabled) {
            this.awayReasonEnabled = awayReasonEnabled;
            update();
        }
    }
    public void setAutoAwayEnabled (boolean autoAwayEnabled) {
        if (autoAwayEnabled != this.autoAwayEnabled) {
            this.autoAwayEnabled = autoAwayEnabled;
            update();
        }
    }
    public void setAutoAwayReasonEnabled (boolean autoAwayReasonEnabled) {
        if (autoAwayReasonEnabled != this.autoAwayReasonEnabled) {
            this.autoAwayReasonEnabled = autoAwayReasonEnabled;
            update();
        }
    }
    public void setDetachAwayEnabled (boolean detachAwayEnabled) {
        if (detachAwayEnabled != this.detachAwayEnabled) {
            this.detachAwayEnabled = detachAwayEnabled;
            update();
        }
    }
    public void setDetachAwayReasonEnabled (boolean detachAwayReasonEnabled) {
        if (detachAwayReasonEnabled != this.detachAwayReasonEnabled) {
            this.detachAwayReasonEnabled = detachAwayReasonEnabled;
            update();
        }
    }
    public void setNicks (List<String> nicks) {
        if (nicks != this.nicks) {
            this.nicks = nicks;
            update();
        }
    }

    @Override
    public String getObjectName() {
        return String.valueOf(identityId);
    }

    public void update(Map<String,QVariant<?>> data) throws EmptyQVariantException {
        // If we called the method via a sync call, don’t sync back again
        if (Thread.currentThread().getStackTrace().length>=7) {
            StackTraceElement parentMethod = Thread.currentThread().getStackTrace()[5];
            if (parentMethod.getClassName().equals(SyncableObject.class.getCanonicalName()) && parentMethod.getMethodName().equals("execute"))
                return;
        }
        this.fromVariantMap(data);
        sync(new RequestRemoteSyncEvent(getClassName(), getObjectName(), "requestUpdate", this.toVariantMap()));
    }

    public void update() {
        sync(new RequestRemoteSyncEvent(getClassName(), getObjectName(), "requestUpdate", this.toVariantMap()));
    }
}
