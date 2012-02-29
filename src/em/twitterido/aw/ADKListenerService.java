package em.twitterido.aw;

import java.nio.ByteBuffer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This Service class has to handle the sensor events and send a post request to the twitterIDo server to notify the new info.
 * @author elenanazzi
 *
 */
public class ADKListenerService extends Service {

	public static String TAG = ADKListenerService.class.toString();
	
	BroadcastReceiver br = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG, intent.getAction());


			if (intent.getAction().equals(ADKService.SENSOR_EVENT)) {
				Log.d(TAG, "ADKService - Sensor_event catched");
				Toast.makeText(getApplicationContext(), "DEBUG Sensor Event catched", Toast.LENGTH_SHORT);
				handleSensorEvent(intent);
			}
		}

	};
	
	
	protected void handleSensorEvent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return;
		}
		Log.d(TAG, "we have extras from SensorEvent");

		byte my = extras.getByte("my");
		// my = (byte) (my - 48);// test this first :) it might be not needed
		my = (byte) (my);
		Log.d(TAG, "SENDER ADDRESS IS: " + my);

		int type = extras.getInt("type");
		byte data[] = extras.getByteArray("data");

		ByteBuffer bb = ByteBuffer.wrap(data, 0, data.length);
		bb.order(null); // get the proper order for conversion from c to java
		Short value = bb.getShort(1); // the second element tells me if we are
		// active or not :)

		// I skip the first
		// byte
		// because that bb.getShort(1) is supposed to be the state of the button:
		// 1 => active (then converted to start),
		// 0 => inactive (then converted to stop)
		// bb.getShort(0) must be some control data

		
		String msg = "";
		msg += "Actor: " + Byte.toString(my);
		msg += " just sent this message: ";
		// for (int i = 0; i < data.length; i++) {
		// msg += Byte.toString(data[i]) + " | ";
		// }
		msg += Byte.toString(data[0]) + " | ";
		msg += Short.toString(value);
		msg += " of type " + type;

		

		Toast.makeText(getApplicationContext(), "data length: " + data.length
				+ " ByteBuffer length: " + bb.capacity(), Toast.LENGTH_LONG);
		Log.d(TAG, "data length: " + data.length + " ByteBuffer length: "
				+ bb.capacity());

		

		String actor = defineActor(my);
		String content = (value == 1) ? "start" : "stop";
		String activity = ((PersonalEMApplication) getApplication()).activity;
		
		sendSensorEvent(activity, actor, content); // send a post request to the
		// twitterIDo Server

	}
	
	// TODO: this is a very temporary solution for the definition of the thing
	// it will need to be done through NFC!
	private String defineActor(byte my) {
		String actor = "bagrollator0" + String.valueOf(my);
		return actor;
	}

	private void sendSensorEvent(String activity, String actor, String content) {

		if (activity != "unknown") {
			
			String params = "?user=" + actor + BaseActivity.service;
			String postActivity = BaseActivity.uri + ((PersonalEMApplication) getApplication()).activity+".json" + params;

			MakePostRequest request = new MakePostRequest(this);
			String text = content + " #" + activity;
			Toast.makeText(getApplicationContext(), "making post request", Toast.LENGTH_LONG);
			request.execute(postActivity, text);

		} else {
			Log.d(TAG, "Unknown activity with object " + actor);
			
		}

	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter flt = new IntentFilter();
		//flt.addAction(PersonalEMActivity.GHEVENT_INTENT);
		flt.addAction(ADKService.SENSOR_EVENT);
		registerReceiver(br, flt);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(br);
		super.onDestroy();

	}
}
// // int integer = (int)( // NOTE: type cast not necessary for int
		// // (0xff & data[0]) << 24 |
		// // (0xff & data[1]) << 16 |
		// // (0xff & data[2]) << 8 |
		// // (0xff & data[3]) << 0);
		// // Float flt = Float.intBitsToFloat(integer);

		// storing the data from the message received from the ADK
		// MY => object id
		// activity => shopping
		// content => start/stop/distance: