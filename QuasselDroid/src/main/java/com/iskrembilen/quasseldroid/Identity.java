package com.iskrembilen.quasseldroid;

import com.iskrembilen.quasseldroid.events.RequestCreateIdentityEvent;
import com.iskrembilen.quasseldroid.events.RequestUpdateIdentityEvent;
import com.iskrembilen.quasseldroid.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.util.BusProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Identity {
    String       identityName;
    List<String> nicks;
    String       ident;
    String       realName;
    int          identityId;

    boolean      autoAwayEnabled;
    boolean      autoAwayReasonEnabled;
    int          autoAwayTime;
    boolean      awayNickEnabled;
    boolean      awayReasonEnabled;
    boolean      detachAwayEnabled;
    boolean      detachAwayReasonEnabled;

    String       awayReason;
    String       autoAwayReason;
    String       detachAwayReason;

    String       partReason;
    String       quitReason;
    String       awayNick;

    String       kickReason;

    public static Identity fromQVariant(QVariant<?> variant) throws EmptyQVariantException{
        Map<String,QVariant<?>> rawData = (HashMap<String,QVariant<?>>) variant.getData();
        Identity result = new Identity();

        result.identityName = (String) rawData.get("identityName").getData();
        result.nicks = (List<String>) rawData.get("nicks").getData();
        result.ident = (String) rawData.get("ident").getData();
        result.realName = (String) rawData.get("realName").getData();
        result.identityId = (Integer) rawData.get("identityId").getData();

        result.autoAwayEnabled = (Boolean) rawData.get("autoAwayEnabled").getData();
        result.autoAwayReasonEnabled = (Boolean) rawData.get("autoAwayReasonEnabled").getData();
        result.autoAwayTime = (Integer) rawData.get("autoAwayTime").getData();
        result.awayNickEnabled = (Boolean) rawData.get("awayNickEnabled").getData();
        result.awayReasonEnabled = (Boolean) rawData.get("awayReasonEnabled").getData();
        result.detachAwayEnabled = (Boolean) rawData.get("detachAwayEnabled").getData();
        result.detachAwayReasonEnabled = (Boolean) rawData.get("detachAwayReasonEnabled").getData();

        result.awayReason = (String) rawData.get("awayReason").getData();
        result.autoAwayReason = (String) rawData.get("autoAwayReason").getData();
        result.detachAwayReason = (String) rawData.get("detachAwayReason").getData();

        result.partReason = (String) rawData.get("partReason").getData();
        result.quitReason = (String) rawData.get("quitReason").getData();
        result.awayNick = (String) rawData.get("awayNick").getData();

        result.kickReason = (String) rawData.get("kickReason").getData();

        return result;
    }

    public void fromOther(Identity other) {
        this.identityId              = other.identityId;
        this.identityName            = other.identityName;

        this.ident                   = other.ident;
        this.realName                = other.realName;
        this.nicks                   = other.nicks;

        this.awayReasonEnabled       = other.awayReasonEnabled;
        this.awayReason              = other.awayReason;

        this.awayNickEnabled         = other.awayNickEnabled;
        this.awayNick                = other.awayNick;

        this.autoAwayEnabled         = other.autoAwayEnabled;
        this.autoAwayTime            = other.autoAwayTime;

        this.autoAwayReasonEnabled   = other.autoAwayReasonEnabled;
        this.autoAwayReason          = other.autoAwayReason;

        this.detachAwayEnabled       = other.detachAwayEnabled;
        this.detachAwayReasonEnabled = other.detachAwayReasonEnabled;
        this.detachAwayReason        = other.detachAwayReason;

        this.partReason              = other.partReason;
        this.quitReason              = other.quitReason;
        this.kickReason              = other.kickReason;
    }

    public void create() {
        BusProvider.getInstance().post(new RequestCreateIdentityEvent(this.identityId, this.toQVariant()));
    }

    @Override
    public String toString() {
        return identityName;
    }

    public QVariant<Map<String,QVariant<?>>> toQVariant() {
        Map<String,QVariant<?>> map = new HashMap<>();

        map.put("identityName", new QVariant<>(this.identityName, QVariantType.String));
        map.put("nicks", new QVariant<>(this.nicks, QVariantType.StringList));
        map.put("ident", new QVariant<>(this.ident, QVariantType.String));
        map.put("realName", new QVariant<>(this.realName, QVariantType.String));

        // Here we use the usertype "IdentityId", which is also used by the official implementation
        map.put("identityId", new QVariant<>(this.identityId, "IdentityId"));

        map.put("autoAwayEnabled", new QVariant<>(this.autoAwayEnabled, QVariantType.Bool));
        map.put("autoAwayReasonEnabled", new QVariant<>(this.autoAwayReasonEnabled, QVariantType.Bool));
        map.put("autoAwayTime", new QVariant<>(this.autoAwayTime, QVariantType.Int));
        map.put("awayNickEnabled", new QVariant<>(this.awayNickEnabled, QVariantType.Bool));
        map.put("awayReasonEnabled", new QVariant<>(this.awayReasonEnabled, QVariantType.Bool));
        map.put("detachAwayEnabled", new QVariant<>(this.detachAwayEnabled, QVariantType.Bool));
        map.put("detachAwayReasonEnabled", new QVariant<>(this.detachAwayReasonEnabled, QVariantType.Bool));

        map.put("awayReason", new QVariant<>(this.awayReason, QVariantType.String));
        map.put("autoAwayReason", new QVariant<>(this.autoAwayReason, QVariantType.String));
        map.put("detachAwayReason", new QVariant<>(this.detachAwayReason, QVariantType.String));

        map.put("partReason", new QVariant<>(this.partReason, QVariantType.String));
        map.put("quitReason", new QVariant<>(this.quitReason, QVariantType.String));
        map.put("awayNick", new QVariant<>(this.awayNick, QVariantType.String));

        map.put("kickReason", new QVariant<>(this.kickReason, QVariantType.String));

        return new QVariant<>(map,QVariantType.Map);
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
        if (this.identityName==identityName)
            return;

        this.identityName = identityName;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setRealName (String realName) {
        if (this.realName==realName)
            return;

        this.realName = realName;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAwayNick (String awayNick) {
        if (this.awayNick==awayNick)
            return;

        this.awayNick = awayNick;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAwayReason (String awayReason) {
        if (this.awayReason==awayReason)
            return;

        this.awayReason = awayReason;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAutoAwayReason (String autoAwayReason) {
        if (this.autoAwayReason==autoAwayReason)
            return;

        this.autoAwayReason = autoAwayReason;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setDetachAwayReason (String detachAwayReason) {
        if (this.detachAwayReason==detachAwayReason)
            return;

        this.detachAwayReason = detachAwayReason;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setIdent (String ident) {
        if (this.ident==ident)
            return;

        this.ident = ident;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setKickReason (String kickReason) {
        if (this.kickReason==kickReason)
            return;

        this.kickReason = kickReason;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setPartReason (String partReason) {
        if (this.partReason==partReason)
            return;

        this.partReason = partReason;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setQuitReason (String quitReason) {
        if (this.quitReason==quitReason)
            return;

        this.quitReason = quitReason;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setIdentityId (int identityId) {
        if (this.identityId==identityId)
            return;

        this.identityId = identityId;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAutoAwayTime (int autoAwayTime) {
        if (this.autoAwayTime==autoAwayTime)
            return;

        this.autoAwayTime = autoAwayTime;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAwayNickEnabled (boolean awayNickEnabled) {
        if (this.awayNickEnabled==awayNickEnabled)
            return;

        this.awayNickEnabled = awayNickEnabled;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAwayReasonEnabled (boolean awayReasonEnabled) {
        if (this.awayReasonEnabled==awayReasonEnabled)
            return;

        this.awayReasonEnabled = awayReasonEnabled;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAutoAwayEnabled (boolean autoAwayEnabled) {
        if (this.autoAwayEnabled==autoAwayEnabled)
            return;

        this.autoAwayEnabled = autoAwayEnabled;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setAutoAwayReasonEnabled (boolean autoAwayReasonEnabled) {
        if (this.autoAwayReasonEnabled==autoAwayReasonEnabled)
            return;

        this.autoAwayReasonEnabled = autoAwayReasonEnabled;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setDetachAwayEnabled (boolean detachAwayEnabled) {
        if (this.detachAwayEnabled==detachAwayEnabled)
            return;

        this.detachAwayEnabled = detachAwayEnabled;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setDetachAwayReasonEnabled (boolean detachAwayReasonEnabled) {
        if (this.detachAwayReasonEnabled==detachAwayReasonEnabled)
            return;

        this.detachAwayReasonEnabled = detachAwayReasonEnabled;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
    public void setNicks (List<String> nicks) {
        if (this.nicks==nicks)
            return;

        this.nicks = nicks;
        BusProvider.getInstance().post(new RequestUpdateIdentityEvent(this.identityId, "requestUpdate", this.toQVariant()));
    }
}
