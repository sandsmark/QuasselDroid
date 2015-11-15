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

import com.google.common.base.Predicate;
import com.iskrembilen.quasseldroid.protocol.state.Buffer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BufferCollectionHelper {
    public static final Predicate<Buffer> FILTER_ACTIVE = new ActiveFilter();
    public static final Predicate<Buffer> FILTER_VISIBLE = new VisibilityFilter();
    public static final Predicate<Buffer> FILTER_NEW_ACTIVITY = new ActivityFilter(ActivityType.ACTIVITY);
    public static final Predicate<Buffer> FILTER_NEW_MESSAGE = new ActivityFilter(ActivityType.MESSAGE);
    public static final Predicate<Buffer> FILTER_NEW_HIGHLIGHT = new ActivityFilter(ActivityType.HIGHLIGHT);

    public static final Comparator<Buffer> COMPARATOR_ALPHABETICAL = new AlphabeticalComparator();
    public static final Comparator<Buffer> COMPARATOR_ORDER = new OrderComparator();

    public static final Set<Predicate<Buffer>> FILTER_SET_ALL = new HashSet<>();
    public static final Set<Predicate<Buffer>> FILTER_SET_ACTIVE = new HashSet<>();
    public static final Set<Predicate<Buffer>> FILTER_SET_VISIBLE = new HashSet<>();
    public static final Set<Predicate<Buffer>> FILTER_SET_VISIBLE_NEW_ACTIVITY = new HashSet<>();
    public static final Set<Predicate<Buffer>> FILTER_SET_VISIBLE_NEW_MESSAGE = new HashSet<>();
    public static final Set<Predicate<Buffer>> FILTER_SET_VISIBLE_NEW_HIGHLIGHT = new HashSet<>();

    public static final List<Set<Predicate<Buffer>>> LIST_FILTERS = new ArrayList<>();
    public static final String[] FILTER_NAMES;

    static {
        FILTER_SET_VISIBLE.add(FILTER_VISIBLE);

        FILTER_SET_ACTIVE.add(FILTER_ACTIVE);

        FILTER_SET_VISIBLE_NEW_ACTIVITY.add(FILTER_VISIBLE);
        FILTER_SET_VISIBLE_NEW_ACTIVITY.add(FILTER_NEW_ACTIVITY);

        FILTER_SET_VISIBLE_NEW_MESSAGE.add(FILTER_VISIBLE);
        FILTER_SET_VISIBLE_NEW_MESSAGE.add(FILTER_NEW_MESSAGE);

        FILTER_SET_VISIBLE_NEW_HIGHLIGHT.add(FILTER_VISIBLE);
        FILTER_SET_VISIBLE_NEW_HIGHLIGHT.add(FILTER_NEW_HIGHLIGHT);

        LIST_FILTERS.add(FILTER_SET_ALL);
        LIST_FILTERS.add(FILTER_SET_VISIBLE);
        LIST_FILTERS.add(FILTER_SET_ACTIVE);
        LIST_FILTERS.add(FILTER_SET_VISIBLE_NEW_ACTIVITY);
        LIST_FILTERS.add(FILTER_SET_VISIBLE_NEW_MESSAGE);
        LIST_FILTERS.add(FILTER_SET_VISIBLE_NEW_HIGHLIGHT);
        FILTER_NAMES = new String[] {
                "Show all",
                "Show visible",
                "Show active",
                "Show visible with activity",
                "Show visible with messages",
                "Show visible with highlights"
        };
    }

    public enum ActivityType {
        NONE,
        ACTIVITY,
        MESSAGE,
        HIGHLIGHT
    }

    // Comparators

    public static class AlphabeticalComparator implements Comparator<Buffer> {
        public int compare(Buffer lhs, Buffer rhs)
        {
            return lhs.getInfo().name.compareToIgnoreCase(rhs.getInfo().name);
        }
    }

    public static class OrderComparator implements Comparator<Buffer> {
        @Override
        public int compare(Buffer lhs, Buffer rhs) {
            return lhs.getOrder() - rhs.getOrder();
        }
    }

    // Filters

    public static class VisibilityFilter implements Predicate<Buffer> {
        @Override
        public boolean apply(Buffer input) {
            return !input.isPermanentlyHidden() && !input.isTemporarilyHidden();
        }
    }

    public static class ActiveFilter implements Predicate<Buffer> {
        @Override
        public boolean apply(Buffer input) {
            return input.isActive();
        }
    }

    public static class ActivityFilter implements Predicate<Buffer> {
        ActivityType type = ActivityType.NONE;

        public ActivityFilter(ActivityType type) {
            this.type = type;
        }

        @Override
        public boolean apply(Buffer input) {
            switch (type) {
                case ACTIVITY:
                    return input.hasUnreadActivity();
                case MESSAGE:
                    return input.hasUnreadMessage();
                case HIGHLIGHT:
                    return input.hasUnseenHighlight();
                default:
                    return true;
            }
        }
    }
}
