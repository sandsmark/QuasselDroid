package com.lekebilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import java.util.Observable;

import android.app.PendingIntent;
import android.test.IsolatedContext;
import android.util.Log;

import com.lekebilen.quasseldroid.BufferInfo.Type;
import com.lekebilen.quasseldroid.gui.ChatActivity;

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
	private ArrayList<IrcMessage> backlog = null;
	/**
	 * Filtered version of the backlog, without hidden messages
	 */
	private ArrayList<IrcMessage> filteredBacklog;
	/**
	 * The message id of the message that was on top of the screen when this buffer was last displayed
	 * used to remember position when going back to a buffer
	 */
	private int topMessageShown = 0;
	/**
	 * Quassel variable, represents the last message seen on a buffer 
	 */
	private int lastSeenMessage;
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
	 * List of the nick of ppl on this buffer TODO: say something about what this is used for
	 */
	private List<String> nicks;
	/**
	 * The topic for this buffer
	 */
	private String topic;
	/**
	 * Number of backlog entries that we have asked for but not yet recived, used to determine when we have recived all the backlog we requested
	 * so we don't request the same backlog more then once
	 */
	private int  backlogPending = 0;
	/**
	 * Temp storage for backlog entries that will get placed in the buffer list when we have received all we requested. 
	 */
	private List<IrcMessage> backlogStash;


	private boolean temporarilyHidden = false;
	private boolean permanentlyHidden = false;
	/**
	 * List with all the message types that this buffer should filter
	 */
	private ArrayList<IrcMessage.Type> filterTypes;

	private int order = Integer.MAX_VALUE;

	public Buffer(BufferInfo info) {
		this.info = info;
		backlog = new ArrayList<IrcMessage>();
		filteredBacklog = new ArrayList<IrcMessage>();
		backlogStash = new ArrayList<IrcMessage>();
		filterTypes= new ArrayList<IrcMessage.Type>();
	}

	/**
	 * Add a new message to the buffer, for new backlog addBacklogMessage
	 * @param message the message to add to the buffer
	 */
	public void addMessage(IrcMessage message ) {
		newBufferEntry(message);
		notifyObservers(R.id.BUFFERUPDATE_NEWMESSAGE);
	}

	/**
	 * Private method that adds a new entry to the correct position in the backlog list based on message id
	 * Also updates the buffer if the message contains highlights etc
	 * @param message message to place in the buffer list
	 */
	private void newBufferEntry(IrcMessage message) {		
		if (message.isHighlighted() && message.messageId > lastHighlightMessageId){
			lastHighlightMessageId = message.messageId;
			this.setChanged();
		}
		if (message.type==IrcMessage.Type.Plain && message.messageId > lastPlainMessageId) {
			lastPlainMessageId = message.messageId;
			this.setChanged();
		}

		insertMessageInBufferList(backlog, message);
		if(filterTypes.size()!=0 && !isMessageFiltered(message)){
			insertMessageInBufferList(filteredBacklog, message);
		}
	}
	
	/**
	 * Inserts a message into the correct position in a buffer
	 */
	private void insertMessageInBufferList(ArrayList<IrcMessage> list, IrcMessage msg) {
		if (list.isEmpty()) {
			list.add(msg);
			this.setChanged();	
		}else {
			int i = Collections.binarySearch(list, msg);
			if (i<0) {
				list.add(i*-1-1, msg);
				this.setChanged();
			}else {
				Log.e(TAG, "Getting message buffer already has");
			}
		}
	}

	/**
	 * Used to check if a message is filtered in this buffer, aka should not be 
	 * shown in the list on screen
	 * @param msg the ircmessage to check
	 * @return true if the message should be filtered, false if it shouldn't
	 */
	private boolean isMessageFiltered(IrcMessage msg) {
		if (filterTypes.contains(msg.type)){
			return true;
		}else{
			return false;
		}

	}

	/**
	 * Use when you want to add a backlog message to the buffer, for new messages use the addNewMessage method
	 * Message will be put in stash untill all pending backlog entries are recived and then all will be added to the backlog at the same time. 
	 * @param message the backlog message to add
	 */
	public void addBacklogMessage(IrcMessage message) {
		backlogStash.add(message);

		if (backlogPending==0 || backlogPending<=backlogStash.size()) {
			for (IrcMessage item : backlogStash) {
				newBufferEntry(item);
			}
			backlogStash.clear();
			backlogPending=0;
		}
		notifyObservers(R.id.BUFFERUPDATE_BACKLOG);
	}

	/**
	 * Set how much backlog has been requested and is pending for this buffer
	 * @param amount the nr of backlog entries requested
	 */
	public void setBacklogPending(int amount) {
		backlogPending = amount;
	}

	/**
	 * Check if this buffer is waiting for any backlog
	 * @return true if buffer is waiting for backlog, otherwise false
	 */
	public boolean hasPendingBacklog() {
		return backlogPending>0;
	}

	/**
	 * Check if this buffer has any unseen highlights
	 * @return true if buffer has unseen highlights, otherwise false
	 */
	public boolean hasUnseenHighlight(){
		if (backlog.size() != 0 && lastSeenMessage!=0 && lastHighlightMessageId > lastSeenMessage){
			return true;
		}
		return false;
	}

	/**
	 * Checks if the buffer has any unread messages, not including joins/parts/quits etc
	 */
	public boolean hasUnreadMessage(){
		if (backlog.size() != 0 && lastSeenMessage!=0 && lastPlainMessageId>lastSeenMessage){
			return true;
		}
		return false;
	}
	/**
	 * Checks if the buffer has any unread activity, can be anything
	 * @return true if buffer has unread activity, false otherwise
	 */
	public boolean hasUnreadActivity(){
		//Last message in the backlog has a bigger messageId than the last seen message
		if (backlog.size() != 0 && lastSeenMessage!=0 && lastSeenMessage < backlog.get(backlog.size()-1).messageId){
			return true;
		}
		return false;
	}

	/**
	 * Set the lastseen message of this buffer, called from chat if uses has seen a new message or from service if core sends a sync request
	 * @param lastSeenMessage the msgid of the last seen message on te buffer
	 */
	public void setLastSeenMessage(int lastSeenMessage) {
		this.lastSeenMessage = lastSeenMessage;
		this.setChanged();
		notifyObservers();
	}	

	/**
	 * Set the marker line position for this buffer. Changed from userinteraction of from core sync request
	 * @param markerLineMessage the msgid for the marker line, line will be placed under this message
	 */
	public void setMarkerLineMessage(int markerLineMessage) {
		this.markerLineMessage = markerLineMessage;
		this.setChanged();
		notifyObservers();
	}

	/**
	 * Get the bufferinformation object
	 * @return the information object for this buffer
	 */
	public BufferInfo getInfo() {
		return info;
	}

	/**
	 * Get a massage from this buffer
	 * @param pos the position of the message in this buffer
	 * @return the Ircmessage at pos
	 */
	public IrcMessage getBacklogEntry(int pos) {
		if(filterTypes.size()!=0){
			return filteredBacklog.get(pos);			
		}
		return backlog.get(pos);
	}
	
	/**
	 * Returns the ircmessage at pos from the hole backlog. Used when you want a message
	 * that is not based on the filtered backlog postitions
	 * @param pos the position
	 * @return the IrcMessage at pos in the original backlog
	 */
	public IrcMessage getUnfilteredBacklogEntry(int pos) {
		return backlog.get(pos);
	}

	/**
	 * Get the id for the last seen message 
	 * @return msgid of last seen message
	 */
	public int getLastSeenMessage() {
		return lastSeenMessage;
	}

	/**
	 * Get the id for the marker line, id is the message above the marker line
	 * @return msgid of ircmessage above marker line
	 */
	public int getMarkerLineMessage() {
		return markerLineMessage;
	}

	/**
	 * Check if buffer has a given message already 
	 * @param message the ircmessage to check
	 * @return true if buffer has message, false otherwise
	 */
	public boolean hasMessage(IrcMessage message) {
		return Collections.binarySearch(backlog, message)>=0;
	}

	/**
	 * Get the size of the backlog list, number of messages in the buffer, or if buffer is filtered return the size
	 * of the filtered list
	 * @return number of messages in buffer
	 */
	public int getSize() {
		if(filterTypes.size()!=0){
			return filteredBacklog.size();
		}
		return backlog.size();
	}

	/**
	 * Set this buffer as read TODO: we dont really know what this means atm
	 */
	public void setRead() {
		if (backlog.isEmpty())
			return;

		lastSeenMessage = backlog.get(backlog.size()-1).messageId;
	}

	/**
	 * set the nick list for this buffer. Nick of ppl that is on this buffer
	 * @param nicks list of nicks
	 */
	public void setNicks(List<String> nicks) {
		this.nicks = nicks;
	}

	/**
	 * Get the list of nicks for this buffer
	 * @return nick list
	 */
	public List<String> getNicks() {
		return nicks;
	}

	/**
	 * Remove a specific nick from the nick list
	 * @param nick the nick to remove
	 */
	public void removeNick(String nick) {
		nicks.remove(nick);
	}

	/**
	 * Add a specific nick to the nick list
	 * @param nick the nick to add
	 */
	public void addNick(String nick) {
		nicks.add(nick);
	}

	/**
	 * Set the topic for this buffer
	 * @param topic the topic to set
	 */
	public void setTopic(String topic) {
		//TODO: notify observers
		this.topic = topic;
	}

	/**
	 * Get the buffers topic
	 * @return a string with the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Set the name of the buffer, displayed in the bufferlist of the phone
	 * @param name the buffer name
	 */
	public void setName(String name) {
		info.name = name;
		notifyObservers();
	}

	/**
	 * Get the msgid for the message that was on top of the screen the last time this buffer was displayed
	 * Used to restore the users position if we exits and enters a buffer
	 * @return the msgid of the message that was on top
	 */
	public int getTopMessageShown() {
		return topMessageShown;
	}

	/**
	 * Set the id for the message that is on the top of the screen when a user exits the chatActivity
	 * So we can restore the postion when we enters it again
	 * @param topMessageShown the msgid for the message at the top
	 */
	public void setTopMessageShown(int topMessageShown) {
		this.topMessageShown = topMessageShown;
	}

	@Override
	public int compareTo(Buffer another) {
		if (info.networkId != another.info.networkId)
			return info.networkId - another.info.networkId;
		else if (!(this.temporarilyHidden || this.permanentlyHidden) && (another.temporarilyHidden || another.permanentlyHidden))
			return -1;
		else if ((this.temporarilyHidden || this.permanentlyHidden) && !(another.temporarilyHidden || another.permanentlyHidden))
			return -1;
		else if (this.temporarilyHidden && !another.temporarilyHidden)
			return -1;
		else if (!this.temporarilyHidden && another.temporarilyHidden)
			return 1;
		else if (this.permanentlyHidden && !another.permanentlyHidden)
			return -1;
		else if (!this.permanentlyHidden && another.permanentlyHidden)
			return 1;
		else if ((this.permanentlyHidden&& another.permanentlyHidden)||(this.temporarilyHidden&&another.temporarilyHidden)) {
			if (info.type != another.info.type)
				return info.type.value - another.info.type.value;
			else return info.name.compareToIgnoreCase(another.info.name);
		} else if (!BufferCollection.orderAlphabetical)
			return this.order - another.order;
		else {
			if (info.type != another.info.type)
				return info.type.value - another.info.type.value;
			else return info.name.compareToIgnoreCase(another.info.name);
		}
	}

	public void setTemporarilyHidden(boolean temporarilyHidden) {
		this.setChanged();
		notifyObservers(R.id.BUFFER_HIDDEN_CHANGED);
		this.temporarilyHidden = temporarilyHidden;
	}

	public boolean isTemporarilyHidden() {
		return temporarilyHidden;
	}

	public void setPermanentlyHidden(boolean permanentlyHidden) {
		this.permanentlyHidden = permanentlyHidden;
		this.setChanged();
		notifyObservers(R.id.BUFFER_HIDDEN_CHANGED);
	}

	public boolean isPermanentlyHidden() {
		return permanentlyHidden;
	}

	public void setOrder(int order) {
		Log.d(TAG, "Order " +this.getInfo().name + " : " + order);
		this.order = order;
		this.setChanged();
		notifyObservers(R.id.BUFFER_ORDER_CHANGED);
	}

	public int getOrder() {
		return order;
	}
	
	/**
	 * Add a new IrcMesssage type that this buffer should filter(hidde type)
	 * @param type
	 */
	public void addFilterType(IrcMessage.Type type) {
		filterTypes.add(type);
		filterBuffer();
		this.setChanged();
		notifyObservers();
	}

	/**
	 * Remove a ircmessage type that should no longer be filtered
	 * @param type
	 */
	public void removeFilterType(IrcMessage.Type type) {
		filterTypes.remove(type);
		filterBuffer();
		this.setChanged();
		notifyObservers();
	}
	
	public ArrayList<IrcMessage.Type> getFilters() {
		return filterTypes;
	}
	
	/**
	 * Clear all filters from this buffer
	 */
	public void clearFilters() {
		filterTypes.clear();
	}
	
	/**
	 * Filter buffer, creates the filteredBacklog list from scratch. Should be called
	 * if some of the filter types have changed, so we can build the list again
	 */
	public void filterBuffer() {
		filteredBacklog.clear();
		for (IrcMessage msg : backlog) {
			if(!isMessageFiltered(msg)) {
				filteredBacklog.add(msg);
			}
		}
	}
}
