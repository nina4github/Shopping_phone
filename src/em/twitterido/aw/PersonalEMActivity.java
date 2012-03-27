package em.twitterido.aw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.util.IO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout.LayoutParams;
import dk.itu.yili.nfc.parser.NFCIntentParser;
import em.twitterido.aw.PersonalEMApplication.StatusListener;

/**
 * 
 * 
 * @author elenanazzi
 * 
 *         this is the first activity displayed by the application SCOPE it is
 *         devoted to show the information about the other members of the
 *         community defined by the activity of shopping (shopping is the first
 *         test we do but it should be easy to extend to other activities)
 * 
 *         SERVICES this activity also takes care to initializing the background
 *         services of the whole application the ADK service for monitoring the
 *         information from the hardware when connected with the phone the GH
 *         service for activating the listeners to the events created by the
 *         other members (these events are notified in realtime by the GenieHub
 *         also called EventBus also called Information bus)
 * 
 *         SET UP this activity also retrieve the information about the user and
 *         the list of its friends from the twitterIDo servcer
 * 
 */
public class PersonalEMActivity extends BaseActivity {

	public static final String ACTIVITY_INTENT = "em.twitterido.aw.PersonalEMActivity.ACTIVITY";

	public static final String GHEVENT_INTENT = "em.twitterido.aw.PersonalEMActivity.GH_EVENT";

	public static final String NFC_DISCOVERED_INTENT = "android.nfc.action.NDEF_DISCOVERED";

	/* DEBUG testing elements */
	public TextView activityNotification;

	/* Gallery Handling */
	public View lastSelectedView = null;

	/* preferences and configuration */
	private SharedPreferences preferences;
	public static final String CONFIG_USER = "config_user"; // where the
	// preferences are

	public static final int SETUSER = 0123; // return code of
	// SetUserActivity.java

	public String content; // the accessory mode is sending the messages
	// defining the content + content is defined when an
	// offer is posted = url of the image just posted

	private boolean ghserviceStarted;
	private Drawable[] profile_images;

	protected StatusListener statusListener;

	/** Called when the activity is first created. */
	@Override
	public void doOnCreate() {
		setContentView(R.layout.main);

		initializePrimaryServices();
		Log.d(TAG, "Starting primary services");

		
	}

	private void initializeGallery() {

		StrictMode.setVmPolicy(new VmPolicy.Builder().detectAll().penaltyLog()
				.build());
		StrictMode.setThreadPolicy(new ThreadPolicy.Builder().permitAll()
				.build());

		String[] mImageUrls = getProfilesImageUrls();

		// asynch task to download the images from mImageUrls
		if (profile_images == null) {
			profile_images = new Drawable[mImageUrls.length];

			DownloaderTask task = new DownloaderTask();
			task.execute(mImageUrls);

		} else {
			this.onProfileImages();
		}

	}

	public void onProfileImages() {
		YappsCircleGallery contacts_gallery = (YappsCircleGallery) findViewById(R.id.gallery);

		ImageAdapterCircleGallery galleryAdapter = new ImageAdapterCircleGallery(
				this);

		galleryAdapter.setmImageDrawables(profile_images);
		contacts_gallery.setAdapter(galleryAdapter);

		contacts_gallery.secondaryListener = new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				TextView t = (TextView) findViewById(R.id.captionText);
				String name = "";

				// int color =
				// R.color.myblue;//R.color.actionbar_background_dark;

				int index = arg2 % profile_images.length;
				if (index == 0) { // this is because the first image belongs to
					// the current user
					name = getCurrentUser().getFirstName();
					// color = R.color.myorange;
				} else {
					Log.d(TAG, "position value: " + index);
					name = getContacts().get(index - 1).getFirstName();
					// if (getContacts().get(index - 1).getStatus() == 1) {
					// // color = R.color.mygreen;
					// }
				}
				t.setText(name);

				// arg1.setBackgroundColor(color);
				Log.d(TAG, "name " + name);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		};

		// routines for initiating communication with friends
		contacts_gallery
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {

						int index = arg2 % profile_images.length;
						final String num;
						final String name;
						final String skypename;
						boolean isPerson = false;
						if (index == 0) { // this is because the first image
							// belongs to
							// the current user

							num = getCurrentUser().getBio();
							name = getCurrentUser().getFirstName();
							isPerson = getCurrentUser().getEntityType().equals(
									"person");
							skypename = "sip_aw_"
									+ getCurrentUser().getFullName();
							return false; // although I should not be able to
							// call myself so from here we exit
							// :)

						} else {
							Log.d(TAG, "position value: " + index);

							// check if they are a person
							// if(contacts.get(index - 1).getTags().)

							num = getContacts().get(index - 1).getBio();
							name = getContacts().get(index - 1).getFirstName();
							isPerson = getContacts().get(index - 1)
									.getEntityType().equals("person");
							skypename = "sip_aw_"
									+ getContacts().get(index - 1)
											.getFullName();
						}

						if (isPerson) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									PersonalEMActivity.this);
							builder
									.setMessage(
											"Do you want to call " + name + "?")
									.setCancelable(false)
									.setPositiveButton(
											"Yes",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													// Intent intent = new
													// Intent(
													// Intent.ACTION_CALL);
													// intent
													// .setData(Uri
													// .parse("tel:"
													// + num));
													// startActivity(intent);

													Intent sky = new Intent(
															"android.intent.action.VIEW");
													String test = "echo123";

													sky
															.setData(Uri
																	.parse("skype:"
																			+ skypename));
													startActivity(sky);

												}
											})
									.setNegativeButton(
											"No",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													dialog.cancel();
												}
											});
							AlertDialog alert = builder.create();
							alert.show();
							TextView textView = (TextView) alert
									.findViewById(android.R.id.message);
							textView.setTextSize(30);

						}
						return false;
					}

				});

	}

	private String[] getProfilesImageUrls() {
		// TODO Auto-generated method stub
		String[] images = new String[getContacts().size() + 1];
		images[0] = getCurrentUser().getImageUrl();
		// Log.d(TAG, "name 0: " + images[0]);
		for (int i = 0; i < getContacts().size(); i++) {
			if (getContacts().get(i).getImageUrl().equals(
					"/images/user/default.png")) {
				images[i + 1] = "http://idea.itu.dk:3000"
						+ "/images/user/default.png";
			} else
				images[i + 1] = getContacts().get(i).getImageUrl();

			// Log.d(TAG, "name " + (i + 1) + ": " + images[i + 1]);
		}
		return images;
	}

	private void initializeData() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences(CONFIG_USER, 0);

		if (getCurrentUser() != null) {
			onCurrentUser();
		} else {
			if (preferences.contains("user")) {
				getUserFromDiaspora(preferences.getString("user", "user01"));
			} else if (getCurrentUser() == null) {
				startActivityForResult(new Intent(this, SetUserActivity.class),
						SETUSER);
			}
		}

	}

	private void onCurrentUser() {

		Log.d(TAG, "the user is set: " + getCurrentUser().getFullName());

		if (getContacts() == null || getContacts().isEmpty()) {
			setFriends();
		} else {
			onFriends();
		}

	}

	private void onFriends() {
		
		initializeUsersStatus();
		
		Log.d(TAG, "contacts number " + getContacts().size());
		notificationCounter = Utilities
				.updateNotificationCounter(getContacts());

		initializeGallery();
		Log.d(TAG, "Gallery is set");

		initializeServices();
		Log.d(TAG, "Services are launched ");
	}

	private void initializeUsersStatus() {
		if (isFriendsStatusUpdated()) {

			ArrayList<User> entities = new ArrayList<User>();
			entities.addAll(getContacts());
			entities.add(getCurrentUser());
			Utilities.updateActiveEntities(stream_dir, "stream.txt", entities);

		} else {
			updateFriendsStatus();
		}

	}

	private void updateFriendsStatus() {
		String response = getTodayStreamFromDiaspora();
		Utilities.saveResponseToFile(response, "stream.txt", stream_dir);

		ArrayList<User> entities = new ArrayList<User>();
		entities.add(getCurrentUser());
		entities.addAll(getContacts());
		Utilities.updateActiveEntities(stream_dir, "stream.txt", entities);
		// // Utilities.updateActiveThings(stream_dir, "stream.txt",
		// getContacts()); // this will update contacts :)

		// Utilities.updateActiveUsers(stream_dir, "stream.txt", entities);
		// Utilities.updateActivePlaces(stream_dir, "stream.txt",
		// getContacts());

	}

	private boolean isFriendsStatusUpdated() {

		Log.d(TAG, "check if stream.txt exists and is updated");

		if (!(new File(stream_dir, "stream.txt").exists())) {
			Log.d(TAG, "stream.txt does not exist, therefore is not updated");
			return false; // exit
		}

		String laststream = getTodayStreamFromDiaspora();

		StringBuffer fileData = Utilities.readStringFromFile(stream_dir,
				"stream.txt");

		if (!fileData.toString().equalsIgnoreCase(laststream)) {

			Log.d(TAG, "new updates are available");
			return false;
		}
		Log.d(TAG, "the situation is already updated");
		return true;
	}

	private void onFriendsStatus() {

	}

	private void getUserFromDiaspora(String username) {

		// TODO Auto-generated method stub
		String params = "?user=" + username + service;
		String requestProfile = uri + "me.json" + params;

		// get the response
		// String response = makeGetRequest(requestProfile);
		MakeGetRequest request = new MakeGetRequest(this);
		request.execute(requestProfile);

		String response = null;
		try {
			response = request.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSONObject jObj = null;
		try {
			if (response == null) {
				Log.d(TAG, "empty response");
			}
			jObj = new JSONObject(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JSONObject actor = null;
		try {
			actor = jObj.getJSONObject("actor");
			((PersonalEMApplication) getApplication()).currentUser = setNewContact(actor);
			onCurrentUser();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private String getTodayStreamFromDiaspora() {

		Log.d(TAG, "calling get stream from diaspora");
		String params = "?user=" + getCurrentUser().getFullName() + service;
		String requestStream = uri + "activities/" + getActivity()
				+ "/today.json" + params;

		// get the response
		MakeGetRequest request = new MakeGetRequest(this);
		request.execute(requestStream);
		String response = null;
		try {
			response = request.get();
			Log.d(TAG, "stream response: " + response);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;

	}

	private void initializeServices() {
		// TODO Auto-generated method stub

		/**
		 * TODO: fix this service to serve the GenieHub when included in the
		 * Client Application. Starting the Service that controls the GenieHub
		 * generators and listeners.
		 * 
		 */
		if (!ghserviceStarted) {
			Intent ghIntent = new Intent(this, GHService.class);
			ghIntent.putParcelableArrayListExtra("contacts", getContacts());
			ghIntent.putExtra("currentuser", getCurrentUser());
			ghIntent.putExtra("activity", getActivity());
			startService(ghIntent);
			ghserviceStarted = true;
		}
		Log.d(TAG, "Started Service GHService");

		// start ADKservice listener that will do the updates to diaspora :)
		Intent adklistenerIntent = new Intent(this, ADKListenerService.class);
		// adklistenerIntent.putExtra(name, value);
		startService(adklistenerIntent);

	}

	private void setFriends() {
		// TODO Auto-generated method stub
		try {
			// setFriendsFromFile();

			String response = getFriendsFromDiaspora();
			Utilities.saveResponseToFile(response, "contacts.txt",
					configuration_dir);

			File file = new File(configuration_dir, "contacts.txt");
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				fileData.append(buf, 0, numRead);
			}
			reader.close();
			JSONObject jObj = null;
			try {
				jObj = new JSONObject(fileData.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/**
			 * Convert JSONArray to our user type.
			 */
			try {
				((PersonalEMApplication) getApplication()).contacts = new ArrayList<User>();
				JSONObject con = jObj.getJSONObject("contacts");
				JSONArray jsonArray = con.getJSONArray("actor");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					JSONArray tags = jsonObject.getJSONArray("tags");
					// Log.i("Contact", "JSon is: " + jsonObject + "\n");
					getContacts().add(setNewContact(jsonObject));
				}
				onFriends();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	enum EntityType {
		person, thing, place;
		static EntityType fromString(String str) {
			for (EntityType t : EntityType.values()) {
				if (t.name().equals(str)) {
					return t;
				}
			}
			return null;
		}
	}

	private User setNewContact(JSONObject jsonObject) throws JSONException {
		// TODO Auto-generated method stub

		User newContact = new User();
		int id = jsonObject.getInt("id");
		String image_url = jsonObject.getString("picture");
		String gender = jsonObject.getString("gender");
		String first_name = jsonObject.getString("name");
		String last_name = jsonObject.getString("nichname");
		String full_name = jsonObject.getString("preferredUsername");
		String bio = jsonObject.getString("note");

		JSONArray tags = jsonObject.getJSONArray("tags");

		ArrayList<String> tags_list = new ArrayList<String>(tags.length());

		String entityType = null;
		for (int i = 0; i < tags.length(); i++) {
			tags_list.add(tags.getString(i));
			EntityType t = EntityType.fromString(tags.getString(i));
			if (null != t) {
				entityType = t.name();
			}
		}

		newContact.setImageUrl(image_url);
		if (gender.equalsIgnoreCase("male"))
			newContact.setGender(Gender.Male);
		else
			newContact.setGender(Gender.Female);
		newContact.setFirstName(first_name);
		newContact.setLastName(last_name);
		newContact.setFullName(full_name);
		newContact.setBirthDay(new Date()); // Birthday
		newContact.setBio(bio);
		newContact.setEntityType(entityType);
		newContact.setStatus(0); // by default status is 0 = OFF
		newContact.setUserId(id);

		// just a check for not having duplications
		return newContact;
	}

	private String getFriendsFromDiaspora() {
		// TODO Auto-generated method stub
		String params = "?user=" + getCurrentUser().getFullName() + service;
		String requestContacts = uri + "activities/" + getActivity()
				+ "/contacts.json" + params;

		// get the response
		MakeGetRequest request = new MakeGetRequest(this);
		request.execute(requestContacts);
		String response = null;
		try {
			response = request.get();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	@Override
	protected void onNewIntent(Intent intent) {

		Log.i(TAG, "received a new intent, do something " + intent.getAction());
		if (intent.getAction() != null) {
			if (intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED")) {
				Log.d(TAG, "TAG_DISCOVERED Receiver catched it");

				String s = NFCIntentParser.parse(intent);
				Log.d(TAG, "NFC string parsed ONNEWINTENT " + s);

			}

			if (intent.getAction().equals(
					"android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
				Log.d(TAG, "Starting? Service ADKService");
				Intent i = new Intent(this, ADKService.class);// generate new

				i.putExtras(intent);
				startService(i);
				Log.d(TAG, "Started Service ADKService");
				// activityNotification.setText("ADK connected");
			}
		}

		// if (intent.getAction().equals(ADKService.SENSOR_EVENT)) {
		// Log.d(TAG, "Senso rEvent catched on new intent");
		// activityNotification.setText("Here we have a sensor intent!!");
		// handleSensorEvent(intent);
		// }
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.main);
		initializeButtons(R.id.homepageButton_f, R.id.friendspageButton_f,
				R.id.offerpageButton_f, R.id.newofferButton_f);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		initializeButtons(R.id.homepageButton_f, R.id.friendspageButton_f,
				R.id.offerpageButton_f, R.id.newofferButton_f);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.userMenu:
			// call new activity set user name
			startActivityForResult(new Intent(this, SetUserActivity.class),
					SETUSER);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SETUSER) {
			Bundle extras = data.getExtras();
			if (extras == null) {
				return;
			}
			Log.d(TAG, "we have extras from SetUser");
			String user = extras.getString("name");
			getUserFromDiaspora(user);
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume is executed");
		super.onResume();

		
		initializeButtons(R.id.homepageButton_f, R.id.friendspageButton_f,
				R.id.offerpageButton_f, R.id.newofferButton_f);

		initializeData();
		Log.d(TAG, "Data has been initialized");
		
		activityNotification = (TextView) findViewById(R.id.activityNew);

		initializeListeners();

	}

	private void initializePrimaryServices() {

		Intent intent = getIntent();

		if (intent.getAction() == null) {
		} else {
			// Parse NFC
			if (intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED")) {
				Log.d(TAG, "NFC_DISCOVERED Receiver catched it oncreate");

				String s = NFCIntentParser.parse(intent);
				Log.d(TAG, "NFC string parsed" + s);
				activityNotification.setText("NFC string: " + s);

			}

			/**
			 * start the Accessory Service we need to send all the intent that
			 * activates this activity to the service in order for it to get the
			 * actual information that the accessory is transmitting
			 **/

			if (intent.getAction().equals(
					"android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
				Log.d(TAG, "FOUND USB in primary services, are you going?");
				Intent i = new Intent(PersonalEMActivity.this, ADKService.class);// generate
				// new

				i.putExtras(intent);
				startService(i);
				Log.d(TAG, "Started Service ADKService");
				// activityNotification.setText("ADK connected");
			}

		}
	}

	private void initializeListeners() {

		statusListener = new StatusListener() {

			public void onStatusChanged(GHEvent e) {
				Log.d(TAG, "onstatuschanged triggered");

				// here I already have updated the user corresponding to actor

				onProfileImages(); // update the gallery with the icons
				// representing the status of each contact

				Intent home = new Intent(PersonalEMActivity.this,
						HomeActivity.class);
				notificationCounter = Utilities
						.updateNotificationCounter(getContacts());

				home.putExtra("actor", e.actor);
				home.putExtra("content", e.content);
				home.putExtra("activity", e.activity);
				startActivity(home);

				// CREATE DIALOG DISPLAYING NEW EVENT
				// String message = activeThings + " "+
				// getString(R.string.activeFriendsText);
				// Utilities.createAlertDialogue(PersonalEMActivity.this,
				// message);

			}

		};
		((PersonalEMApplication) getApplication())
				.addStatusListener(statusListener);
	}

	public class ImageAdapterCircleGallery extends BaseAdapter {

		private Context mContext;

		private Integer[] mImageIds;
		private String[] mImageUrls;

		public Drawable[] getmImageDrawables() {
			return mImageDrawables;
		}

		public void setmImageDrawables(Drawable[] mImageDrawables) {
			this.mImageDrawables = mImageDrawables;
		}

		private Drawable[] mImageDrawables;

		public String[] getmImageUrls() {
			return mImageUrls;
		}

		public void setmImageUrls(String[] mImageUrls) {
			this.mImageUrls = mImageUrls;
		}

		public Integer[] getmImageIds() {
			return mImageIds;
		}

		public void setmImageIds(Integer[] mImageIds) {
			this.mImageIds = mImageIds;
		}

		public ImageAdapterCircleGallery(Context c) {
			mContext = c;
		}

		// public ImageAdapterCircleGallery(Context c, Integer[] imgIds) {
		// mContext = c;
		// mImageIds=imgIds;
		//
		// }

		public int getCount() {
			return Integer.MAX_VALUE;
		}

		public Object getItem(int position) {
			return getPosition(position);
		}

		public long getItemId(int position) {
			return getPosition(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView i = new ImageView(mContext);
			position = getPosition(position);

			i.setImageDrawable(mImageDrawables[position]);
			i.setLayoutParams(new Gallery.LayoutParams(180, 180));

			i.setScaleType(ImageView.ScaleType.FIT_XY);
			i.setPadding(10, 10, 10, 10);

			RelativeLayout statusLayout = new RelativeLayout(mContext);
			Gallery.LayoutParams statusLayoutParams = new Gallery.LayoutParams(
					180, 180);
			statusLayout.setLayoutParams(statusLayoutParams);
			ImageView statusIcon = new ImageView(mContext);

			if (position == 0) {
				Log.d(TAG, "current user status is: "
						+ getCurrentUser().getStatus());
				if (getCurrentUser().getStatus() >= 1) {
					statusIcon.setImageResource(R.drawable.star);
				}
			} else {
				User actor = getContacts().get(position - 1);
				Log.d(TAG, "user status is: " + actor.getStatus());
				if (actor.getStatus() >= 1) {
					if (actor.getEntityType().equals("thing"))
						statusIcon.setImageResource(R.drawable.greenbag);
					// statusIcon.setClickable(false) possible solution / check also focusable.
					if (actor.getEntityType().equals("person"))
						statusIcon.setImageResource(R.drawable.star);
					if (actor.getEntityType().equals("place"))
						statusIcon.setImageResource(R.drawable.group);
				}

				// else {
				// statusIcon
				// .setBackgroundColor(R.color.actionbar_background_light);
				// }
			}

			RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(
					200, 200);
			iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			statusIcon.setLayoutParams(iconLayoutParams);

			RelativeLayout.LayoutParams contactLayoutParams = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			i.setLayoutParams(contactLayoutParams);
			statusLayout.addView(i);
			statusLayout.addView(statusIcon);
			// borderImg.setPadding(10, 10, 10, 10);
			// borderImg.setBackgroundColor(0xff00ff00);
			// borderImg.addView(i);

			// return i;
			return statusLayout;
		}

		public int checkPosition(int position) {
			return getPosition(position);
		}

		int getPosition(int position) {
			if (position >= mImageDrawables.length) {
				position = position % mImageDrawables.length;
			}
			return position;
		}
	}

	public class DownloaderTask extends
			AsyncTask<String[], Integer, Drawable[]> {

		ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {

			dialog = ProgressDialog.show(PersonalEMActivity.this, "",
					"Loading. Please wait...", true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		@Override
		protected Drawable[] doInBackground(String[]... params) {

			Drawable[] files = new Drawable[params[0].length];
			Log
					.d(TAG + " downloaderTask", "params lenght: "
							+ params[0].length);

			for (int i = 0; i < params[0].length; i++) {
				User user = null;
				if (i == 0) {
					user = getCurrentUser();
				} else {
					user = getContacts().get(i - 1);
				}
				files[i] = ImageOperations(params[0][i], user.getFirstName()
						+ ".jpg");

			}

			return files;
		}

		private Drawable ImageOperations(String url, String saveFilename) {

			// file where the image of us and our contacts are stored
			String filename = profiles_dir.getPath() + "/" + saveFilename;
			File f = new File(filename);

			InputStream is = null;
			try {
				if (!f.exists()) {
					Log.d(TAG + " downloaderTask",
							"NO such file, url to search: " + url);
					is = (InputStream) this.fetch(url);

					// save image to filename
					FileOutputStream out = new FileOutputStream(filename);
					IO.copy(is, out); // copy copy image to file ;)
					out.close();

				} else {
					Log.d(TAG + " downloaderTask", "Profile image found for: "
							+ url);
					is = new FileInputStream(new File(filename));
				}
				Drawable d = Drawable.createFromStream(is, profiles_dir
						.getPath()
						+ "/" + saveFilename);
				is.close();
				System.gc(); // call garbage collector

				return d;
				// try {
				// // String url_debug
				// //
				// ="idea.itu.dk:3000/uploads/images/thumb_large_dd368fb316ff15338281.jpg";
				// InputStream is = (InputStream) this.fetch(url);
				//
				// Drawable d = Drawable.createFromStream(is, activity_dir
				// + saveFilename);
				// is.close();
				// return d;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		public Object fetch(String address) throws MalformedURLException,
				IOException {

			URL url = new URL(address);
			Log.d(TAG + " downloaderTask", "fetching image from url: "
					+ url.toString());
			Object content = url.getContent();

			//			
			// HttpClient client = new DefaultHttpClient();
			// HttpGet get = new
			// HttpGet("https://lh3.googleusercontent.com/-0ENWOuHq_mY/Tw8sKhiaQII/AAAAAAAADGI/ZUa-5J7l1nc/s400/IMG_20120112_105938.jpg");
			// Log.d(TAG,"connected test start");
			// try {
			// HttpResponse r = client.execute(get);
			// r.getEntity().consumeContent();
			// Log.d(TAG,"connected test succeded");
			// } catch (ClientProtocolException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			//			
			return content;// url.openConnection().getInputStream();//client.execute(new
			// HttpGet(address)).getEntity().getContent();
		}

		@Override
		protected void onPostExecute(Drawable[] result) {
			profile_images = result;
			onProfileImages();
			dialog.dismiss();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

		}

	}

	@Override
	protected void offerUploaded() {
		getCurrentUser().setStatus(getCurrentUser().getStatus()+1);
		
		Log.d(TAG, "offerUploaded");
		onResume();
	}

}