package com.iskrembilen.quasseldroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.gui.BufferActivity;
import com.iskrembilen.quasseldroid.gui.LoginActivity;
import com.iskrembilen.quasseldroid.service.CoreConnService;

public class QuasseldroidNotificationManager {

	//TODO: lots of duplicate code in this class, clean up

	private Context context;
	private SharedPreferences preferences;
	private List<Integer> highlightedBuffers;
	NotificationManager notifyManager;

	public QuasseldroidNotificationManager(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		highlightedBuffers = new ArrayList<Integer>();
	}

	public void notifyHighlightsRead(int bufferId) {
		highlightedBuffers.remove((Integer)bufferId);
		if(highlightedBuffers.size() == 0) {
			notifyConnected();
		}else{
			notifyHighlight(null);
		}
	}

	public void notifyConnected() {
		CharSequence text = context.getText(R.string.notification_connected);
		int icon = R.drawable.icon;
		int temp_flags = Notification.FLAG_ONGOING_EVENT;			

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(icon, text, System.currentTimeMillis());
		notification.flags |= temp_flags;
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent;

		Intent launch = new Intent(context, BufferActivity.class);
		launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), text,
				contentIntent);

		// Send the notification.
		notifyManager.notify(R.id.NOTIFICATION, notification);
	}
	
	public void notifyConnecting() {
		CharSequence text = context.getText(R.string.notification_connecting);
		int icon = R.drawable.connecting;
		int temp_flags = Notification.FLAG_ONGOING_EVENT;			

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(icon, text, System.currentTimeMillis());
		notification.flags |= temp_flags;
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent;

		Intent launch = new Intent(context, BufferActivity.class);
		launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), text,
				contentIntent);

		// Send the notification.
		notifyManager.notify(R.id.NOTIFICATION, notification);
	}	

	public void notifyHighlight(Integer bufferId) {
		if(bufferId != null && !highlightedBuffers.contains(bufferId)) {
			highlightedBuffers.add(bufferId);			
		}

		CharSequence text = "You have highlights on " + highlightedBuffers.size() + " buffers";
		int icon = R.drawable.highlight;
		int temp_flags = Notification.FLAG_ONGOING_EVENT;			

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(icon, text, System.currentTimeMillis());
		notification.flags |= temp_flags;
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent;

		Intent launch = new Intent(context, BufferActivity.class);
		launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), text,
				contentIntent);
		if(bufferId != null) {
			if(preferences.getBoolean(context.getString(R.string.preference_notification_sound), false))
				notification.defaults |= Notification.DEFAULT_SOUND;
			if(preferences.getBoolean(context.getString(R.string.preference_notification_light), false))
				notification.defaults |= Notification.DEFAULT_LIGHTS;
			if(preferences.getBoolean(context.getString(R.string.preference_notification_vibrate), false))
				notification.defaults |= Notification.DEFAULT_VIBRATE;	
		}
		// Send the notification.
		notifyManager.notify(R.id.NOTIFICATION, notification);
	}

	public void notifyDisconnected() {
		CharSequence text = context.getText(R.string.notification_disconnected);
		int icon = R.drawable.inactive;
		int temp_flags = Notification.FLAG_ONLY_ALERT_ONCE;

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(icon, text, System.currentTimeMillis());
		notification.flags |= temp_flags;
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent;

		Intent launch = new Intent(context, LoginActivity.class);
		contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), text,
				contentIntent);
		// Send the notification.
		notifyManager.notify(R.id.NOTIFICATION, notification);
	}

	private void showNotification(boolean connected) {
		//TODO: Remove when "leaving" the application
		CharSequence text =  "";
		int temp_flags = 0; 
		int icon;
		if (connected) {
			text = context.getText(R.string.notification_connected);
			icon = R.drawable.icon;
			temp_flags = Notification.FLAG_ONGOING_EVENT;			
		} else {
			text = context.getText(R.string.notification_disconnected);
			icon = R.drawable.inactive;
			temp_flags = Notification.FLAG_ONLY_ALERT_ONCE;
		}
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(icon, text, System.currentTimeMillis());
		notification.flags |= temp_flags;
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent;

		// TODO: Fix so that if a chat is currently on top, launch that one,
		// instead of the BufferActivity
		if (connected) { // Launch the Buffer Activity.
			Intent launch = new Intent(context, BufferActivity.class);
			launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
		} else {
			Intent launch = new Intent(context, LoginActivity.class);
			contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
		}
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), text,
				contentIntent);
		// Send the notification.
		notifyManager.notify(R.id.NOTIFICATION, notification);
	}

}
