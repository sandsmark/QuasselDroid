package com.iskrembilen.quasseldroid.events;

public class ManageChannelEvent {
	public enum ChannelAction {
		TEMP_HIDE, PERM_HIDE, DELETE;
	}
	
	public final int bufferId;
	public final ChannelAction action;
	
	public ManageChannelEvent(int bufferId, ChannelAction action) {
		this.bufferId = bufferId;
		this.action = action;
	}

}
