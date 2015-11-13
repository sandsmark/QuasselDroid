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

import android.support.annotation.NonNull;
import android.util.Log;

import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.io.QuasselDbHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Executor;

/**
 * Class holds all the data for a Quassel buffer, this includes all the messages in the buffer, as well as the different states for the buffer.
 */
public class Buffer extends Observable implements Comparable<Buffer> {
    private static final String TAG = Buffer.class.getSimpleName();
    /**
     * Information object about this buffer, contains name, type of buffer etc
     */
    private BufferInfo info;
    /**
     * List that holds all the Ircmessages we have gotten on this buffer.
     */
    private final ArrayList<IrcMessage> backlog;
    /**
     * Filtered version of the backlog, without hidden messages
     */
    private ArrayList<IrcMessage> filteredBacklog;
    /**
     * The message id of the message that was on top of the screen when this buffer was last displayed
     * used to remember position when going back to a buffer
     */
    private int topMessageShown = -1;
    // This is a very bad idea, storing scroll state in here directly :/
    // I just couldn’t find any other solution yet.
    // TODO: Do this properly
    private int scrollState = 0;
    /**
     * Quassel variable, represents the last message seen on a buffer
     */
    private int lastSeenMessage = -1;
    /**
     * Quassel variable, the id of the message where the marker line is placed
     */
    private int markerLineMessage;
    /**
     * The id of the message we where last highlighted in
     */
    private int lastHighlightMessageId;
    /**
     * The id of the last message that was of type plain. Not join/part/mode/quits etc
     */
    private int lastPlainMessageId;
    /**
     * List of the myNick of ppl on this buffer TODO: say something about what this is used for
     */
    private UserCollection users;
    /**
     * The topic for this buffer
     */
    private String topic = "";
    /**
     * Is this buffer joined or parted.
     */
    private boolean active = false;
    /**
     * Number of backlog entries that we have asked for but not yet received, used to determine when we have received all the backlog we requested
     * so we don't request the same backlog more then once
     */
    private boolean backlogPending = false;


    private boolean temporarilyHidden = false;
    private boolean permanentlyHidden = false;
    /**
     * List with all the message types that this buffer should filter
     */
    private ArrayList<IrcMessage.Type> filterTypes;

    private int order = Integer.MAX_VALUE;

    private QuasselDbHelper dbHelper;

    private boolean isMarkerLineFiltered = false;

    private boolean isDisplayed = false;

    public Buffer(BufferInfo info, QuasselDbHelper dbHelper) {
        this.info = info;
        backlog = new ArrayList<>();
        filteredBacklog = new ArrayList<>();
        filterTypes = new ArrayList<>();
        users = new UserCollection();
        this.dbHelper = dbHelper;

        loadFilters();
    }

    /**
     * Add a new message to the buffer, for new backlog addBacklogMessage
     *
     * @param message the message to add to the buffer
     */
    public synchronized void addMessage(IrcMessage message) {
        newBufferEntry(message);
        notifyObservers(R.id.BUFFERUPDATE_NEWMESSAGE);
    }

    /**
     * Private method that adds a new entry to the correct position in the backlog list based on message id
     * Also updates the buffer if the message contains highlights etc
     *
     * @param message message to place in the buffer list
     */
    private synchronized void newBufferEntry(IrcMessage message) {
        if (message.isHighlighted() && message.messageId > lastHighlightMessageId) {
            lastHighlightMessageId = message.messageId;
            this.setChanged();
        }
        if ((message.type == IrcMessage.Type.Plain || message.type == IrcMessage.Type.Action) && message.messageId > lastPlainMessageId) {
            lastPlainMessageId = message.messageId;
            this.setChanged();
        }
        insertMessageInBufferList(backlog, message);
        if (!isMessageFiltered(message)) {
            if (isMarkerLineFiltered && getMarkerLineMessage() == message.messageId)
                isMarkerLineFiltered = false;
            insertMessageInBufferList(filteredBacklog, message);
        } else {
            if (getMarkerLineMessage() == message.messageId) isMarkerLineFiltered = true;
        }
    }

    /**
     * Inserts a message into the correct position in a buffer
     */
    private synchronized void insertMessageInBufferList(final ArrayList<IrcMessage> list, IrcMessage msg) {
        if (list.isEmpty()) {
            list.add(msg);
            this.setChanged();
        } else {
            int i = Collections.binarySearch(list, msg);
            if (i < 0) {
                list.add(i * -1 - 1, msg);
                this.setChanged();
            } else {
                Log.e(TAG, "Getting message buffer already has");
            }
        }
    }

    /**
     * Used to check if a message is filtered in this buffer, aka should not be
     * shown in the list on screen
     *
     * @param msg the ircmessage to check
     * @return true if the message should be filtered, false if it shouldn't
     */
    public synchronized boolean isMessageFiltered(IrcMessage msg) {
        return (filterTypes.contains(msg.type) || msg.isFiltered());
    }

    /**
     * Use when you want to add a backlog message to the buffer, for new messages use the addNewMessage method
     *
     * @param message the backlog message to add
     */
    public synchronized void addBacklogMessage(IrcMessage message) {
        newBufferEntry(message);
        notifyObservers(R.id.BUFFERUPDATE_BACKLOG);
    }

    /**
     * Use when you want to add a list of backlog messages to the buffer, for new messages use the addNewMessage method
     *
     * @param messageList the backlog messages to add
     */
    public synchronized void addBacklogMessages(List<IrcMessage> messageList) {
        for (IrcMessage message : messageList) {
            newBufferEntry(message);
        }
        notifyObservers(R.id.BUFFERUPDATE_BACKLOG);
    }

    /**
     * Set how much backlog has been requested and is pending for this buffer
     *
     * @param backlogPending whether backlog is pending
     */
    public synchronized void setBacklogPending(boolean backlogPending) {
        this.backlogPending = backlogPending;
    }

    /**
     * Check if this buffer is waiting for any backlog
     *
     * @return true if buffer is waiting for backlog, otherwise false
     */
    public synchronized boolean hasPendingBacklog() {
        return backlogPending;
    }

    /**
     * Check if this buffer has any unseen highlights
     *
     * @return true if buffer has unseen highlights, otherwise false
     */
    public synchronized boolean hasUnseenHighlight() {
        return (filteredBacklog.size() != 0 && lastSeenMessage != 0 && lastHighlightMessageId > lastSeenMessage);
    }

    /**
     * Checks if the buffer has any unread messages, not including joins/parts/quits etc
     */
    public synchronized boolean hasUnreadMessage() {
        return (filteredBacklog.size() != 0 && lastSeenMessage != 0 && lastPlainMessageId > lastSeenMessage);
    }

    /**
     * Checks if the buffer has any unread activity, can be anything
     *
     * @return true if buffer has unread activity, false otherwise
     */
    public synchronized boolean hasUnreadActivity() {
        //Last message in the backlog has a bigger messageId than the last seen message
        return ((filteredBacklog.size() != 0 && lastSeenMessage != 0 && lastSeenMessage < filteredBacklog.get(filteredBacklog.size() - 1).messageId) ||
                (lastSeenMessage == -1));
    }

    /**
     * Set the lastseen message of this buffer, called from chat if uses has seen a new message or from service if core sends a sync request
     *
     * @param lastSeenMessage the msgid of the last seen message on te buffer
     */
    public synchronized void setLastSeenMessage(int lastSeenMessage) {
        this.lastSeenMessage = lastSeenMessage;
        this.setChanged();
        notifyObservers();
    }

    /**
     * Set the marker line position for this buffer. Changed from userinteraction of from core sync request
     *
     * @param markerLineMessage the msgid for the marker line, line will be placed under this message
     */
    public synchronized void setMarkerLineMessage(int markerLineMessage) {
        this.markerLineMessage = markerLineMessage;
        for (IrcMessage msg : backlog) {
            if (msg.messageId == markerLineMessage) {
                isMarkerLineFiltered = isMessageFiltered(msg);
            }
        }
        this.setChanged();
        notifyObservers();
    }

    /**
     * Get the bufferinformation object
     *
     * @return the information object for this buffer
     */
    public synchronized BufferInfo getInfo() {
        return info;
    }

    /**
     * Get a massage from this buffer
     *
     * @param pos the position of the message in this buffer
     * @return the Ircmessage at pos
     */
    public synchronized IrcMessage getBacklogEntry(int pos) {
        return filteredBacklog.get(pos);
    }

    /**
     * Returns the ircmessage at pos from the hole backlog. Used when you want a message
     * that is not based on the filtered backlog postitions
     *
     * @param pos the position
     * @return the IrcMessage at pos in the original backlog
     */
    public synchronized IrcMessage getUnfilteredBacklogEntry(int pos) {
        return backlog.get(pos);
    }

    public List<IrcMessage> getBacklog() {
        return filteredBacklog;
    }

    /**
     * Get the id for the last seen message
     *
     * @return msgid of last seen message
     */
    public synchronized int getLastSeenMessage() {
        return lastSeenMessage;
    }

    /**
     * Get the id for the marker line, id is the message above the marker line
     *
     * @return msgid of ircmessage above marker line
     */
    public synchronized int getMarkerLineMessage() {
        return markerLineMessage;
    }

    /**
     * Check if buffer has a given message already
     *
     * @param message the ircmessage to check
     * @return true if buffer has message, false otherwise
     */
    public synchronized boolean hasMessage(IrcMessage message) {
        return Collections.binarySearch(backlog, message) >= 0;
    }

    /**
     * Get the size of the backlog list, number of messages in the buffer, or if buffer is filtered return the size
     * of the filtered list
     *
     * @return number of messages in buffer
     */
    public synchronized int getSize() {
        return filteredBacklog.size();
    }

    /**
     * Get the size of the whole backlog unfiltered. Used in request more backlog for instance to know the size of the buffer we have
     *
     * @return int
     */
    public synchronized int getUnfilteredSize() {
        return backlog.size();
    }

    /**
     * Set this buffer as read TODO: we don't really know what this means atm
     */
    public synchronized void setRead() {
        if (backlog.isEmpty())
            return;

        lastSeenMessage = backlog.get(backlog.size() - 1).messageId;
    }

//	/**
//	 * set the myNick list for this buffer. Nick of ppl that is on this buffer
//	 * @param nicks list of nicks
//	 */
//	public void setNicks(List<String> nicks) {
//		this.nicks = nicks;
//	}

    public synchronized int getScrollState() {
        return scrollState;
    }

    public synchronized void setScrollState(int scrollState) {
        this.scrollState = scrollState;
    }

    /**
     * Get the list of nicks for this buffer
     *
     * @return myNick list
     */
    public synchronized UserCollection getUsers() {
        return users;
    }

    /**
     * Set the topic for this buffer
     *
     * @param topic the topic to set
     */
    public synchronized void setTopic(String topic) {
        this.topic = topic;
        this.setChanged();
        notifyObservers(R.id.BUFFERUPDATE_TOPICCHANGED);
    }

    /**
     * Get the buffers topic
     *
     * @return a string with the topic
     */
    public synchronized String getTopic() {
        return topic;
    }

    /**
     * Set the name of the buffer, displayed in the bufferlist of the phone
     *
     * @param name the buffer name
     */
    public synchronized void setName(String name) {
        info.name = name;
        this.setChanged();
        notifyObservers();
    }

    /**
     * Get the msgid for the message that was on top of the screen the last time this buffer was displayed
     * Used to restore the users position if we exits and enters a buffer
     *
     * @return the msgid of the message that was on top
     */
    public synchronized int getTopMessageShown() {
        return topMessageShown;
    }

    /**
     * Set the id for the message that is on the top of the screen when a user exits the chatActivity
     * So we can restore the position when we enters it again
     *
     * @param topMessageShown the msgid for the message at the top
     */
    public synchronized void setTopMessageShown(int topMessageShown) {
        this.topMessageShown = topMessageShown;
    }

    @Override
    public synchronized int compareTo(@NonNull Buffer another) {
        return BufferUtils.compareBuffers(this, another);
    }

    public synchronized void setTemporarilyHidden(boolean temporarilyHidden) {
        this.temporarilyHidden = temporarilyHidden;
        this.setChanged();
        notifyObservers(R.id.BUFFER_HIDDEN_CHANGED);
    }

    public synchronized boolean isTemporarilyHidden() {
        return temporarilyHidden;
    }

    public synchronized void setPermanentlyHidden(boolean permanentlyHidden) {
        this.permanentlyHidden = permanentlyHidden;
        this.setChanged();
        notifyObservers(R.id.BUFFER_HIDDEN_CHANGED);
    }

    public synchronized boolean isPermanentlyHidden() {
        return permanentlyHidden;
    }

    public synchronized void setOrder(int order) {
        this.order = order;
        this.setChanged();
        notifyObservers(R.id.BUFFER_ORDER_CHANGED);
    }

    public synchronized int getOrder() {
        return order;
    }

    /**
     * Add a new IrcMessage type that this buffer should filter(hidden type)
     */
    public synchronized void addFilterType(IrcMessage.Type type) {
        filterTypes.add(type);
        dbHelper.open();
        dbHelper.addHiddenEvent(type, getInfo().id);
        dbHelper.close();
        filterBuffer();
        this.setChanged();
        notifyObservers();
    }

    /**
     * Remove a ircmessage type that should no longer be filtered
     *
     * @param type
     */
    public synchronized void removeFilterType(IrcMessage.Type type) {
        filterTypes.remove(type);
        dbHelper.open();
        dbHelper.deleteHiddenEvent(type, getInfo().id);
        dbHelper.close();
        filterBuffer();
        this.setChanged();
        notifyObservers();
    }

    public synchronized ArrayList<IrcMessage.Type> getFilters() {
        return filterTypes;
    }

    private synchronized void loadFilters() {
        dbHelper.open();
        IrcMessage.Type[] filteredEvents = dbHelper.getHiddenEvents(getInfo().id);
        if (filteredEvents != null) {
            Collections.addAll(filterTypes,filteredEvents);
        }
        dbHelper.close();
        filterBuffer();

    }

    /**
     * Filter buffer, creates the filteredBacklog list from scratch. Should be called
     * if some of the filter types have changed, so we can build the list again
     */
    public synchronized void filterBuffer() {
        filteredBacklog.clear();
        for (IrcMessage msg : backlog) {
            if (!isMessageFiltered(msg)) {
                if (getMarkerLineMessage() == msg.messageId) isMarkerLineFiltered = false;
                filteredBacklog.add(msg);
            } else if (getMarkerLineMessage() == msg.messageId) isMarkerLineFiltered = true;
        }
        notifyObservers();
    }

    public void updateIgnore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<IrcMessage> newBacklog;
                synchronized (backlog) {
                    newBacklog = new ArrayList<>(backlog);
                }

                boolean newisMarkerLineFiltered = false;


                for (int i = 0; i < newBacklog.size(); i++) {
                    IrcMessage msg = newBacklog.get(i);
                    msg.setFiltered(Client.getInstance().getIgnoreListManager().matches(msg));
                    if (!isMessageFiltered(msg)) {
                        if (getMarkerLineMessage() == msg.messageId)
                            newisMarkerLineFiltered = false;
                    } else {
                        if (getMarkerLineMessage() == msg.messageId)
                            newisMarkerLineFiltered = true;
                        newBacklog.remove(msg);
                    }
                }

                final boolean copy_of_newisMarkerLineFiltered = newisMarkerLineFiltered;

                Client.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filteredBacklog = newBacklog;
                        isMarkerLineFiltered = copy_of_newisMarkerLineFiltered;
                        ChatFragment.chatFragment.adapter.backlogData = newBacklog;
                        ChatFragment.chatFragment.adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public synchronized boolean isMarkerLineFiltered() {
        return isMarkerLineFiltered;
    }

    /**
     * Get if buffer is active or parted
     *
     * @return true if active
     */
    public synchronized boolean isActive() {
        return this.active;
    }

    /**
     * Set is buffer is active or parted
     *
     * @param active true if buffer is active, false if parted
     */
    public synchronized void setActive(boolean active) {
        this.active = active;
        this.setChanged();
        notifyObservers();
    }

    public synchronized void setDisplayed(boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public synchronized boolean isDisplayed() {
        return isDisplayed;
    }
}
