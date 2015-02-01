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

public class IgnoreListManager extends SyncableObject {
    private static final String TAG = IgnoreListManager.class.getSimpleName();

    public List<IgnoreListItem> getIgnoreList() {
        return ignoreList;
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
    }

    @Syncable(type=QVariantType.List)
    private List<IgnoreListItem> ignoreList = new ArrayList<>(0);

    public class IgnoreListItem {
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
    }

    public QVariant<Map<String,QVariant>> initIgnoreList() {
        Map<String,QVariant>     ignoreListMap  = new HashMap<>();
        List<QVariant<IgnoreType>>     ignoreTypeList = new ArrayList<>(ignoreList.size());
        List<QVariant<String>>         ignoreRuleList = new ArrayList<>(ignoreList.size());
        List<QVariant<String>>         scopeRuleList  = new ArrayList<>(ignoreList.size());
        List<QVariant<Boolean>>        isRegExList    = new ArrayList<>(ignoreList.size());
        List<QVariant<ScopeType>>      scopeList      = new ArrayList<>(ignoreList.size());
        List<QVariant<StrictnessType>> strictnessList = new ArrayList<>(ignoreList.size());
        List<QVariant<Boolean>>        isActiveList   = new ArrayList<>(ignoreList.size());

        for (int i = 0; i < ignoreList.size(); i++) {
            ignoreTypeList.add(new QVariant<>(ignoreList.get(i).type,       "IgnoreType"));
            ignoreRuleList.add(new QVariant<>(ignoreList.get(i).ignoreRule, QVariantType.String));
            scopeRuleList .add(new QVariant<>(ignoreList.get(i).scopeRule,  QVariantType.String));
            isRegExList   .add(new QVariant<>(ignoreList.get(i).isRegEx,    QVariantType.Bool));
            scopeList     .add(new QVariant<>(ignoreList.get(i).scope,      "ScopeType"));
            strictnessList.add(new QVariant<>(ignoreList.get(i).strictness, "StrictnessType"));
            isActiveList  .add(new QVariant<>(ignoreList.get(i).isActive,   QVariantType.Bool));
        }

        ignoreListMap.put("ignoreType", new QVariant<>(ignoreTypeList, QVariantType.List));
        ignoreListMap.put("ignoreRule", new QVariant<>(ignoreRuleList, QVariantType.StringList));
        ignoreListMap.put("scopeRule",  new QVariant<>(scopeRuleList,  QVariantType.List));
        ignoreListMap.put("isRegEx",    new QVariant<>(isRegExList,    QVariantType.List));
        ignoreListMap.put("scope",      new QVariant<>(scopeList,      QVariantType.List));
        ignoreListMap.put("strictness", new QVariant<>(strictnessList, QVariantType.List));
        ignoreListMap.put("isActive",   new QVariant<>(isActiveList,   QVariantType.List));

        return new QVariant<>(ignoreListMap, QVariantType.Map);
    }

    public void update(Map<String,QVariant> datamap) throws EmptyQVariantException {
        Map<String,QVariant>           ignoreList = (Map<String, QVariant>)   datamap.get("IgnoreList").getData();
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

        this.ignoreList.clear();
        for (int i = 0; i < ignoreRule.size(); i++) {
            this.ignoreList.add(
                    new IgnoreListItem(
                            IgnoreType.fromValue(ignoreType.get(i).getData()),
                            ignoreRule.get(i),
                            isRegEx.get(i).getData(),
                            StrictnessType.fromValue(strictness.get(i).getData()),
                            ScopeType.fromValue(scope.get(i).getData()),
                            scopeRule.get(i),
                            isActive.get(i).getData()));
        }
        this.setChanged();
        this.notifyObservers();
    }

    public boolean matches(IrcMessage msg) {
        Network network = Client.getInstance().getNetworks().getNetworkById(msg.bufferInfo.networkId);
        Buffer buffer = network.getBuffers().getBuffer(msg.bufferInfo.id);


        if (msg.type != IrcMessage.Type.Plain && msg.type != IrcMessage.Type.Action && msg.type != IrcMessage.Type.Notice)
            return false;

        for (IgnoreListItem item : ignoreList) {
            if (!item.isActive || item.type == IgnoreType.CTCP_IGNORE)
               continue;
            if (item.scope==ScopeType.GLOBAL_SCOPE
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

        sync(type, ignoreRule, isRegEx, strictness, scope, scopeRule, isActive);
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
