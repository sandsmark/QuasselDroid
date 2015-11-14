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

import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.protocol.state.serializers.Syncable;
import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;
import de.kuschku.util.BetterSparseArray;
import com.iskrembilen.quasseldroid.util.RegExp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class IgnoreListManager extends SyncableObject implements Observer {
    private static final String TAG = IgnoreListManager.class.getSimpleName();

    public List<IgnoreListItem> getIgnoreList() {
        return ignoreList;
    }

    /**
     * This method is called if the specified {@code Observable} object's
     * {@code notifyObservers} method is called (because the {@code Observable}
     * object has been updated.
     *
     * @param observable the {@link java.util.Observable} object.
     * @param data       the data passed to {@link java.util.Observable#notifyObservers(Object)}.
     */
    @Override
    public void update(Observable observable, Object data) {
        sync("requestUpdate", toVariantMap());
    }

    public enum IgnoreType {
        SENDER_IGNORE(0),
        MESSAGE_IGNORE(1),
        CTCP_IGNORE(2);

        private int val;
        private static BetterSparseArray<IgnoreType> vals = new BetterSparseArray<>(values().length);
        static {
            for (IgnoreType val : values()) {
                vals.put(val.val,val);
            }
        }
        IgnoreType(int val) {
            this.val = val;
        }
        public static IgnoreType fromValue(int val) {
            return vals.get(val);
        }

        public int value() {
            return val;
        }
    }

    public enum StrictnessType {
        UNMATCHED_STRICTNESS(0),
        SOFT_STRICTNESS(1),
        HARD_STRICTNESS(2);

        private int val;
        private static BetterSparseArray<StrictnessType> vals = new BetterSparseArray<>(values().length);
        static {
            for (StrictnessType val : values()) {
                vals.put(val.val, val);
            }
        }
        StrictnessType(int val) {
            this.val = val;
        }
        public static StrictnessType fromValue(int val) {
            return vals.get(val);
        }

        public int value() {
            return val;
        }
    }

    public enum ScopeType {
        GLOBAL_SCOPE(0),
        NETWORK_SCOPE(1),
        CHANNEL_SCOPE(2);

        private int val;
        private static BetterSparseArray<ScopeType> vals = new BetterSparseArray<>(values().length);
        static {
            for (ScopeType val : values()) {
                vals.put(val.val,val);
            }
        }
        ScopeType(int val) {
            this.val = val;
        }
        public static ScopeType fromValue(int val) {
            return vals.get(val);
        }

        public int value() {
            return val;
        }
    }

    @Syncable(type=QVariantType.List)
    private final List<IgnoreListItem> ignoreList = new ArrayList<>(0);

    public static class IgnoreListItem extends Observable {
        public IgnoreType getType() {
            return type;
        }

        public void setType(IgnoreType type) {
            this.type = type;
            this.setChanged();
            this.notifyObservers();
        }

        public String getIgnoreRule() {
            return ignoreRule;
        }

        public void setIgnoreRule(String ignoreRule) {
            this.ignoreRule = ignoreRule;
            this.regEx.setPattern(ignoreRule);
            this.setChanged();
            this.notifyObservers();
        }

        public boolean isRegEx() {
            return isRegEx;
        }

        public void setRegEx(boolean isRegEx) {
            this.isRegEx = isRegEx;
            this.setChanged();
            this.notifyObservers();
        }

        public StrictnessType getStrictness() {
            return strictness;
        }

        public void setStrictness(StrictnessType strictness) {
            this.strictness = strictness;
            this.setChanged();
            this.notifyObservers();
        }

        public ScopeType getScope() {
            return scope;
        }

        public void setScope(ScopeType scope) {
            this.scope = scope;
            this.setChanged();
            this.notifyObservers();
        }

        public String getScopeRule() {
            return scopeRule;
        }

        public void setScopeRule(String scopeRule) {
            this.scopeRule = scopeRule;
            this.setChanged();
            this.notifyObservers();
        }

        public RegExp getRegEx() {
            return regEx;
        }

        public void setRegEx(RegExp regEx) {
            this.regEx = regEx;
            this.setChanged();
            this.notifyObservers();
        }

        IgnoreType type;
        String ignoreRule;
        boolean isRegEx;
        StrictnessType strictness;
        ScopeType scope;
        String scopeRule;
        boolean isActive;
        RegExp regEx = new RegExp();

        public IgnoreListItem() {}
        public IgnoreListItem(IgnoreType type, String ignoreRule, boolean isRegEx, StrictnessType strictness, ScopeType scope, String scopeRule, boolean isActive) {
            this.type = type;
            this.ignoreRule = ignoreRule;
            this.regEx.setPattern(ignoreRule);
            this.isRegEx = isRegEx;
            this.strictness = strictness;
            this.scope = scope;
            this.scopeRule = scopeRule;
            this.isActive = isActive;

            this.regEx.setCaseSensitivity(RegExp.CaseSensitivity.CASE_INSENSITIVE);
            if (!isRegEx) {
                this.regEx.setPatternSyntax(RegExp.PatternSyntax.WILDCARD);
            }
        }

        public boolean matchScope(CharSequence compare) {
            return regEx.compilePattern(scopeRule).matcher(compare).matches();
        }

        public boolean matchIgnore(CharSequence compare) {
            return regEx.compilePattern(ignoreRule).matcher(compare).matches();
        }

        public String getRule() {
            return ignoreRule;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
            this.setChanged();
            this.notifyObservers();
        }

        public void setAttributes(IgnoreType type, String ignoreRule, boolean isRegEx, StrictnessType strictness, ScopeType scope, String scopeRule, boolean isActive) {
            this.type = type;
            this.ignoreRule = ignoreRule;
            this.regEx.setPattern(ignoreRule);
            this.isRegEx = isRegEx;
            this.strictness = strictness;
            this.scope = scope;
            this.scopeRule = scopeRule;
            this.isActive = isActive;
            this.setChanged();
            this.notifyObservers();
        }
    }

    @Override
    public void fromVariantMap(Map<String,QVariant<?>> map) throws EmptyQVariantException {
        Map<String,QVariant>           ignoreList = (Map<String, QVariant>)   map.get("IgnoreList").getData();
        List<QVariant<Integer>>        ignoreType = (List<QVariant<Integer>>) ignoreList.get("ignoreType").getData();
        List<String>                   ignoreRule = (List<String>)            ignoreList.get("ignoreRule").getData();
        List<String>                   scopeRule  = (List<String>)            ignoreList.get("scopeRule").getData();
        List<QVariant<Boolean>>        isRegEx    = (List<QVariant<Boolean>>) ignoreList.get("isRegEx").getData();
        List<QVariant<Integer>>        scope      = (List<QVariant<Integer>>) ignoreList.get("scope").getData();
        List<QVariant<Integer>>        strictness = (List<QVariant<Integer>>) ignoreList.get("strictness").getData();
        List<QVariant<Boolean>>        isActive   = (List<QVariant<Boolean>>) ignoreList.get("isActive").getData();

        int count = ignoreRule.size();
        if (count != scopeRule.size() || count != isRegEx.size() ||
            count != scope.size() || count != strictness.size() || count != ignoreType.size() || count != isActive.size()) {
            Log.w(TAG, "Corrupted IgnoreList settings! (Count missmatch)");
        }

        synchronized (this.ignoreList) {
            this.ignoreList.clear();

            IgnoreListItem item;
            for (int i = 0; i < ignoreRule.size(); i++) {
                item = new IgnoreListItem(
                        IgnoreType.fromValue(ignoreType.get(i).getData()),
                        ignoreRule.get(i),
                        isRegEx.get(i).getData(),
                        StrictnessType.fromValue(strictness.get(i).getData()),
                        ScopeType.fromValue(scope.get(i).getData()),
                        scopeRule.get(i),
                        isActive.get(i).getData());
                item.addObserver(this);
                this.ignoreList.add(item);
            }
        }
        this.setChanged();
        this.notifyObservers();
    }


    public void update(Map<String,QVariant<?>> datamap) throws EmptyQVariantException {
        fromVariantMap(datamap);
    }

    @Override
    public QVariant<Map<String,QVariant<?>>> toVariantMap() {
        Map<String,QVariant<?>> ignoreList = new HashMap<>();

        List<QVariant<Integer>>        ignoreType = new ArrayList<>(this.ignoreList.size());
        List<String>                   ignoreRule = new ArrayList<>(this.ignoreList.size());
        List<String>                   scopeRule  = new ArrayList<>(this.ignoreList.size());
        List<QVariant<Boolean>>        isRegEx    = new ArrayList<>(this.ignoreList.size());
        List<QVariant<Integer>>        scope      = new ArrayList<>(this.ignoreList.size());
        List<QVariant<Integer>>        strictness = new ArrayList<>(this.ignoreList.size());
        List<QVariant<Boolean>>        isActive   = new ArrayList<>(this.ignoreList.size());

        for (IgnoreListItem item : this.ignoreList) {
            ignoreType.add(new QVariant<>(item.type.val, QVariantType.Int));
            ignoreRule.add(item.ignoreRule);
            scopeRule.add(item.scopeRule);
            isRegEx.add(new QVariant<>(item.isRegEx, QVariantType.Bool));
            scope.add(new QVariant<>(item.scope.val, QVariantType.Int));
            strictness.add(new QVariant<>(item.strictness.val, QVariantType.Int));
            isActive.add(new QVariant<>(item.isActive, QVariantType.Bool));
        }

        ignoreList.put("ignoreType", new QVariant<>(ignoreType, QVariantType.List));
        ignoreList.put("ignoreRule", new QVariant<>(ignoreRule, QVariantType.StringList));
        ignoreList.put("scopeRule",  new QVariant<>(scopeRule,  QVariantType.StringList));
        ignoreList.put("isRegEx",    new QVariant<>(isRegEx,    QVariantType.List));
        ignoreList.put("scope",      new QVariant<>(scope,      QVariantType.List));
        ignoreList.put("strictness", new QVariant<>(strictness, QVariantType.List));
        ignoreList.put("isActive",   new QVariant<>(isActive,   QVariantType.List));

        Map<String,QVariant<?>> datamap = new HashMap<>();
        datamap.put("IgnoreList", new QVariant<Map>(ignoreList, QVariantType.Map));

        return new QVariant<>(datamap,QVariantType.Map);
    }

    public boolean matches(IrcMessage msg) {
        /*
        synchronized (ignoreList) {
            Network network = Client.getInstance().getNetworks().getNetworkById(msg.bufferInfo.networkId);
            Buffer buffer;
            if (msg.bufferInfo.type == BufferInfo.Type.StatusBuffer) {
                buffer = network.getStatusBuffer();
            } else {
                buffer = network.getBuffers().getBuffer(msg.bufferInfo.id);
            }

            if (msg.type != IrcMessage.Type.Plain && msg.type != IrcMessage.Type.Action && msg.type != IrcMessage.Type.Notice)
                return false;

            for (IgnoreListItem item : ignoreList) {
                if (!item.isActive || item.type == IgnoreType.CTCP_IGNORE)
                    continue;
                if (item.scope == ScopeType.GLOBAL_SCOPE
                        || (item.scope == ScopeType.NETWORK_SCOPE && item.matchScope(network.getName()))
                        || (item.scope == ScopeType.CHANNEL_SCOPE && item.matchScope(buffer.getInfo().name))) {
                    String str;
                    if (item.type == IgnoreType.MESSAGE_IGNORE)
                        str = msg.content.toString();
                    else
                        str = msg.getSender();

                    if (item.matchIgnore(str)) {
                        return true;
                    }
                }
            }
        }
        */

        return false;

    }

    int indexOf(String ignore) {
        for (int i = 0; i < ignoreList.size(); i++) {
            if (ignoreList.get(i).ignoreRule.equals(ignore))
                return i;
        }
        return -1;
    }

    public boolean contains(String ignoreRule) {
        return indexOf(ignoreRule) > -1;
    }

    public void toggleIgnoreRule(String ignoreRule) {
        int idx = indexOf(ignoreRule);
        if (idx == -1)
            return;
        ignoreList.get(idx).isActive = !ignoreList.get(idx).isActive;

        sync(ignoreRule);
    }

    public void addIgnoreListItem(int type, String ignoreRule, boolean isRegEx, int strictness, int scope, String scopeRule, boolean isActive)
    {
        if (contains(ignoreRule)) {
            return;
        }

        IgnoreListItem newItem = new IgnoreListItem(IgnoreType.fromValue(type), ignoreRule, isRegEx, StrictnessType.fromValue(strictness), ScopeType.fromValue(scope), scopeRule, isActive);
        ignoreList.add(newItem);

        sync("requestUpdate", toVariantMap());
    }

    public void addIgnoreListItem(IgnoreListItem item)
    {
        if (contains(item.ignoreRule)) {
            return;
        }

        ignoreList.add(item);

        sync("requestUpdate", toVariantMap());
    }

    public void removeIgnoreListItem(String ignoreRule)
    {
        ignoreList.remove(indexOf(ignoreRule));
        sync(ignoreRule);
    }

    @Override
    public String getObjectName() {
        return "";
    }
}
