package em.twitterido.aw;

import java.util.ArrayList;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import dk.itu.infobus.ws.EventBuilder;
import dk.itu.infobus.ws.EventBus;
import dk.itu.infobus.ws.Generator;
import dk.itu.infobus.ws.GeneratorAdapter;
import dk.itu.infobus.ws.Listener;
import dk.itu.infobus.ws.PatternBuilder;
import dk.itu.infobus.ws.PatternOperator;

public class GHService extends Service {
	private static final String EB = "CommonEM_EventBus";
	private static final String TAG = "CommonEM_GHService";

	// private static final String GH_HOST = "tiger.itu.dk";
	// private static final int GH_PORT = 8004;

	private static final String GH_HOST = "idea.itu.dk"; // elena on virtual
															// server PIT
	private static final int GH_PORT = 8000;

	private EventBus eb;

	String gn = android.os.Build.SERIAL;

	Generator activityGenerator = new GeneratorAdapter("em.activity.generator."
			+ gn, "activity", "actor", "content", "timestamp");

	Generator sparkGenerator = new GeneratorAdapter("em.spark.generator." + gn,
			"activity", "actor", "content", "timestamp");

	EventBuilder ebuilder = new EventBuilder();
	String currentActivity;
	String actor;
	String state;
	String content;
	long timestamp;

	private ArrayList<User> contacts = null; // new ArrayList<User>();
	private User currentUser = null; // new User();

	
	// to handle power management to wakeup the device
	// in case of detection of a new event
	PowerManager.WakeLock wl;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * onStart take control of the power manager and instantiate the listener
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// here start event bus
		Log.d(TAG, "phone identification number is " + gn);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"Community events");

		initializeData(intent);

		startGenieHub();

		// here: I need to start the listener depending on my friends list!
		// therefore the friends list should be a Public object accessible from
		// here
		startListener(); // listener to the Genie hub NOT IMPLEMENTED in this
							// application

		return START_NOT_STICKY;
	}

	private void initializeData(Intent intent) {
		// TODO Auto-generated method stub
		if (contacts == null || currentUser == null) {
			Bundle b = intent.getExtras();
			contacts = b.getParcelableArrayList("contacts");
			currentUser = b.getParcelable("currentuser");
			Log.d(TAG, "currentuser: " + currentUser.getFirstName()
					+ " and contacts: " + contacts.size());
			currentActivity = b.getString("activity");
		}
		
	}

	private void startGenieHub() {
		eb = new EventBus(GH_HOST, GH_PORT);
		Log.d(EB, "EB initialiazed");
		try {
			eb.start();
			Log.d(EB, "EB started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			eb.addGenerator(activityGenerator);
			eb.addGenerator(sparkGenerator);
			Log.d(EB, "EB added generators");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * here I want to listen for all my contacts, but not my updates though!
	 */
	private void startListener() {

		// final String activity = "activity";
		// final String actor = "actor";
		// final String timestamp = "timestamp";
		// String[] contacts = {"12","14"};

		String field1 = "activity";
		String field2 = "actor";

		// create an array with the ID of the contacts that the current user
		// wants to listen to
		String[] contact_ids = new String[contacts.size()];
		for (int i = 0; i < contacts.size(); i++) {
			contact_ids[i] = String.valueOf(contacts.get(i).getFullName());
		}

		// create a listener only for the contacts of the user and for the
		// specific activity in Focus
		Listener l = new Listener(new PatternBuilder().add(field1,
				PatternOperator.EQ, currentActivity).add(field2,
				PatternOperator.EQ, contact_ids).getPattern()) {

			@Override
			public void cleanUp() throws Exception {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessage(Map<String, Object> msg) {
				handleEvent(msg);
			}

			public void onStarted() {
			};
		};

		try {
			eb.addListener(l);
			Log.d(EB, "EB added listener");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void handleEvent(Map<String, Object> msg) {
		wl.acquire(10000); // on for 10 seconds

		Intent reportEvent = new Intent(PersonalEMActivity.GHEVENT_INTENT);
		// the Genie Hub returns a MAP of String and Objects
		for (Map.Entry<String, Object> e : msg.entrySet())

			if (!e.getKey().equals("timestamp")) { // TODO: FIX the
													// transcription of
													// timestamp
				Log.d(TAG, "add extra" + e.getKey() + " - "
						+ (String) e.getValue());
				reportEvent.putExtra(e.getKey(), (String) e.getValue());
			}

		sendBroadcast(reportEvent); // broadcast of the GHEVENT_INTENT that is
									// catched from the main activity

		Log.d(EB, "EB Listener detected activity and handled it");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

}
