package em.twitterido.aw;

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

//	private static final String GH_HOST = "tiger.itu.dk";
//	private static final int GH_PORT = 8004;

	private static final String GH_HOST = "idea.itu.dk"; // elena on virtual server PIT
	private static final int GH_PORT = 8000;

	
	private EventBus eb;

	
	String gn =  android.os.Build.SERIAL;
	
	Generator activityGenerator = new GeneratorAdapter("em.activity.generator."+gn,
			"activity", "actor", "content", "timestamp");

	Generator sparkGenerator = new GeneratorAdapter("em.spark.generator."+gn,
			"activity", "actor", "content", "timestamp");

	EventBuilder ebuilder = new EventBuilder();
	String activity;
	String actor;
	String state;
	String content;
	long timestamp;

	// this is to handle the broadcasted intents WITHIN the activity, 
	// i.e. when it is on focus
	// ACTIVITY_INTENT (catching start and stop of activities)
	// SHARE_INTENT (catch the sharing of content related to a specific activity)
	BroadcastReceiver br = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			// ACTIVITY_INTENT (catching start and stop of activities)
			if (intent.getAction().equals(PersonalEMActivity.ACTIVITY_INTENT)) {
				Log.d(TAG, "ACTIVITY_INTENT with start catched");
				Bundle extras = intent.getExtras();
				activity = extras.getString("activity");
				actor = extras.getString("actor");
				content = extras.getString("content");
				timestamp = System.currentTimeMillis();
				
				activityGenerator.publish(ebuilder.put("activity", activity)
						.put("actor", actor).put("content",
								content).put("timestamp", timestamp).getEvent());

			}

			// SHARE_INTENT (catch the sharing of content related to a specific activity)
			if (intent.getAction().equals(PersonalEMActivity.SHARE_INTENT)) {
				Log.d(TAG, "SHARE_INTENT catched");
				Bundle extras = intent.getExtras();
				activity = extras.getString("activity");
				actor = extras.getString("actor");	// I am not sure I will need an object
				
				content = extras.getString("content"); // I guess that this field will be a URL
				timestamp = System.currentTimeMillis();

				sparkGenerator.publish(ebuilder.put("activity", activity).put(
						"actor",actor).put("content",
						content).put("timestamp", timestamp).getEvent());
			}

		}

	};

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
		Log.d(TAG,"phone identification number is "+gn);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"Community events");

		startGenieHub();
		
		// here: I need to start the listener depending on my friends list!
		// therefore the friends list should be a Public object accessible from here
		startListener(); // listener to the Genie hub NOT IMPLEMENTED in this application

		
		// define the intents this service will catch
		IntentFilter flt = new IntentFilter();
		flt.addAction(PersonalEMActivity.ACTIVITY_INTENT);
		flt.addAction(PersonalEMActivity.SHARE_INTENT);

		registerReceiver(br, flt);
		// TODO Auto-generated method stub
		return START_NOT_STICKY;
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
	 * the following is not implemented in this application because not relevant
	 * it is instead interesting for the personal objects 
	 * or for the future
	 */
	private void startListener() {

		final String activity = "activity";
		final String actor = "actor";
		final String timestamp = "timestamp";
		String[] contacts = {"12","14"};

		Listener l = new Listener(new PatternBuilder().add(activity,
				PatternOperator.EQ, "shopping").add(actor,
						PatternOperator.EQ,contacts).getPattern()) {

			@Override
			public void cleanUp() throws Exception {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessage(Map<String, Object> msg) {
				updateInfo(msg);
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

	protected void updateInfo(Map<String, Object> msg) {
		wl.acquire(10000); // on for 10 seconds

		Intent reportEvent = new Intent(PersonalEMActivity.GHEVENT_INTENT);
		// the Genie Hub returns a MAP of String and Objects
		for (Map.Entry<String, Object> e : msg.entrySet())

			if (!e.getKey().equals("timestamp")) { // TODO: FIX the transcription of timestamp
				Log.d(TAG, "add extra" + e.getKey() + " - "
						+ (String) e.getValue());
				reportEvent.putExtra(e.getKey(), (String) e.getValue());
			}

		sendBroadcast(reportEvent); // broadcast of the GHEVENT_INTENT that is catched from the main activity

		Log.d(EB, "EB Listener detected activity");

	}

	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(br);
		super.onDestroy();
		
		
	}

}
