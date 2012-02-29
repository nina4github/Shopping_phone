package em.twitterido.aw;

import java.util.ArrayList;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;


public class PersonalEMApplication extends Application {

	public final String TAG = this.getClass().getSimpleName();

	protected User currentUser;
	protected ArrayList<User> contacts;
	protected String activity;

	BroadcastReceiver br = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG, intent.getAction());

			if (intent.getAction().equals(PersonalEMActivity.GHEVENT_INTENT)) {
				Log.d(TAG, "GHEVENT_INTENT catched by application");

				handleGHEvent(intent);
			}

			// TODO to implement NFC discovered

			// if
			// (intent.getAction().equals("android.nfc.action.TAG_DISCOVERED")){
			// Log.d(TAG, "NFC_DISCOVERED Receiver catched it");
			//				
			// String s = NFCIntentParser.parse(intent);
			// Log.d(TAG, "NFC string parsed"+s);
			//		
			// }
			// if
			// (intent.getAction().equals(PersonalEmActivity.NFC_SHARE_INTENT))
			// {
			// Log.d(TAG, "NFC_SHARE catched");
			// if (intent.getExtras() != null) {
			// shareSpark(intent.getExtras());
			// }
			//
			// }

		}

	};

	/**
	 * 
	 * method called when a new GHEVENT_INTENT has been filtered
	 */
	protected void handleGHEvent(Intent intent) {
		
		// retrieve event data
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return;
		}
		// TODO this is an example of vibration for NEW EVENTs
		Utilities.vibrate(this,Utilities.EVENTVIBRATEPATTERN); 
		
		User actor = null;
		Log.d(TAG, "GH Event, intent extras");
		for (User user : contacts) {
			if (user.getFullName().equals(extras.getString("actor"))) {
				actor = user;
				break;
			}
		}
		int status = 0;
		String data = "";
		if (extras.getString("content") != null) {

			if (extras.getString("content").contains("start")) {
				status = 1;
				// notificationCounter.put(extras.getString("actor"), status);

			} else if (extras.getString("content").contains("stop")) {
				status = 0;
				// notificationCounter.put(extras.getString("actor"), status);

			} else if (extras.getString("content").contains("data")) {
				status = 0; // assumed the message is fired when I have stopped.
				data = extras.getString("content").split(":")[1]; // assumed the
				// format of
				// the
				// content
				// is
				// data:500meters
				// message = extras.getString("actor")
				// + getString(R.string.shareText)
				// + extras.getString("content");
			}
			if (actor != null) {
				actor.setStatus(status); // this is updating our user status
				// here handle the event in the specific activity

				GHEvent gh = new GHEvent();
				gh.setActivity(extras.getString("activity"));
				gh.setActor(actor);
				gh.setContent(extras.getString("content"));
				gh.setData(data);

				fireStatusEvent(gh);
				
			}
		}

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		IntentFilter flt = new IntentFilter();
		flt.addAction(PersonalEMActivity.GHEVENT_INTENT);
		registerReceiver(br, flt);

		activity = getString(R.string.activity);
		Log.d(TAG, "activity set");

	}

	public interface StatusListener {
		void onStatusChanged(GHEvent e);
	}

	// TODO
	ArrayList<StatusListener> statusListeners = new ArrayList<StatusListener>();

	protected void addStatusListener(StatusListener l) {
		statusListeners.add(l);
	}

	protected void removeStatusListener(StatusListener l) {
		statusListeners.remove(l);
	}

	protected void fireStatusEvent(GHEvent e) {
		int numListeners = statusListeners.size();
		for (int i = 0; i < numListeners; i++) {
			((StatusListener) statusListeners.get(i)).onStatusChanged(e);
		}
	}
}
