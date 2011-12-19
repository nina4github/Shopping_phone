package em.twitterido.aw;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class ADKService extends Service implements Runnable {
	
	private static final String ACTION_USB_PERMISSION = "dk.itu.thesis.network.action.USB_PERMISSION";
	private static final String USB_ACCESSORY_ATTACHED = "android.hardware.usb.action.USB_ACCESSORY_ATTACHED";
	private static final String TAG = "TestACtivity module";
	public static final String SENSOR_EVENT = "SENSOR_EVENT";
	public static final String CONF = "CONFIGURATION_EVENT";
	
	UsbAccessory mAccessory;
	private volatile boolean detached;
	private UsbManager manager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			Log.d("BROADCAST RECEIVER", "INTENT RECEIVED: "+action);
			if(action.equals("android.hardware.usb.action.USB_ACCESSORY_ATTACHED") || action.equals("USB_ACCESSORY_ATTACHED")) {
				synchronized (this) {
					mAccessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED , false)) {
						openAccessory();
						
					} else {
						synchronized (mUsbReceiver) {
							if (!mPermissionRequestPending) {
								manager.requestPermission(mAccessory,
										mPermissionIntent);
								mPermissionRequestPending = true;
							}
						}
						Log.d(TAG, "permission denied for accessory "+mAccessory);
					}
					mPermissionRequestPending = false;	
				}
			}
			
			if(ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					mAccessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED , false)) {
						openAccessory();
						
					} else {
						Log.d(TAG, "permission denied for accessory "+mAccessory);
					}
					mPermissionRequestPending = false;	
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				detached = true;	
				UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (accessory != null && accessory.equals(mAccessory)) {
						// call method to clean up and close comm with accessory
						closeAccessory();
					}
			}
			
		}
	};
	
	@Override
	public void onCreate() {
		Log.d(TAG, "ONCREATE");
		// Register broadcastreceiver for filtering accessory events
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(USB_ACCESSORY_ATTACHED);
        
        registerReceiver(mUsbReceiver,filter);
        Log.d(TAG, "ADK service oncreate ENDED");
		super.onCreate();
		
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "ONSTARTCOMMAND METHOD ACCESSED");
		manager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		
		synchronized (this) {
				mAccessory = UsbManager.getAccessory(intent);
				
				if (mAccessory != null) {
					if (manager.hasPermission(mAccessory)) {
						Log.e(TAG, "HAS PREMISSION HERE");
						openAccessory();
					} else {
						synchronized (mUsbReceiver) {
							if (!mPermissionRequestPending) {
								manager.requestPermission(mAccessory,
										mPermissionIntent);
								mPermissionRequestPending = true;
							}
						}
					}
					
					mPermissionRequestPending = false;
				}
			}
		
		if (mInputStream != null && mOutputStream != null) {
			return START_NOT_STICKY;
		}
		
	/*	UsbAccessory[] accessories = manager.getAccessoryList();
		mAccessory = (accessories == null ? null : accessories[0]);
		
		if (mAccessory != null) {
			if (manager.hasPermission(mAccessory)) {
				openAccessory();
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						manager.requestPermission(mAccessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		} */
		
		return START_STICKY;
	}

	
		
	/**
	 * Open the accessory
	 */
	private void openAccessory() {
    	Log.d(TAG, "openAccessory: "+mAccessory);
    	//Toast.makeText(getApplicationContext(), "OPEN ACCESSORY", Toast.LENGTH_SHORT);
    	mFileDescriptor = manager.openAccessory(mAccessory);
    	if (mFileDescriptor != null) {
    		FileDescriptor fd = mFileDescriptor.getFileDescriptor();
    		mInputStream = new FileInputStream(fd);
    		mOutputStream = new FileOutputStream(fd);
    		Thread thread = new Thread(null,this,"AccessoryThread");    	
    		thread.start();
    	}
    }
	
	/**
	 * Close the accessory
	 */
	private void closeAccessory() {
	
	    	if (mFileDescriptor != null) {
	    		try {
					mFileDescriptor.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					mFileDescriptor = null;
					mAccessory = null;
				}
	    	}
	}	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(mUsbReceiver);
		closeAccessory();
		super.onDestroy();
		
	}

	
	public void run() {
		//Toast.makeText(getApplicationContext(), "RUN METHOD RUNNING FINE", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "RUN METHOD RUNNING FINE");
		byte[] buffer = new byte[16384];
		while (true) {
		
			if (detached) break;
			try {
				int read = mInputStream.read(buffer);
				if (read == -1) {
					continue;
				}
				byte remoteAddress = buffer[0]; // determine sender
				int messageType = (int) buffer[1]; // determine message type
				int length = (int) buffer[2]; // determine number of parameters
				byte parameters[] = new byte[length];
				for (int i=0;i<length;i++) {
					// store parameters
					parameters[i] = buffer[i+3];
				}
				
				// Handle configuration message
				if (messageType == 0) {
					Intent newIntent = new Intent(CONF);
					newIntent.putExtra("my", remoteAddress);
					newIntent.putExtra("type", parameters[0]);
					sendBroadcast(newIntent);
					// check if device exists in database // data structure
					// if it does, send back an OK message in the other class
					
					// if not, create a dialogue for starting the ADKNetworksetupActivity
					
				} else { // Send intent with incoming data
					Intent newIntent = new Intent(SENSOR_EVENT);
					newIntent.putExtra("my", remoteAddress);
					newIntent.putExtra("type", messageType);
					newIntent.putExtra("data", parameters);
					sendBroadcast(newIntent);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		} 
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		manager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver,filter);
		return null;
	}

}
