package em.twitterido.aw;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import em.twitterido.aw.PersonalEMApplication.StatusListener;

public class HomeActivity extends BaseActivity {

	protected static final int SPARK = 001;

	private static final int LOCATION = 010;

	private static final int ACTIVITY = 000;

	private SharedPreferences preferences;

	private ArrayList<String> shoppingstream = new ArrayList<String>();

	private StatusListener statusListener;

	@Override
	protected void doOnCreate() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);
	}

	private void initializeMessage() {

		TextView message = (TextView) findViewById(R.id.friendsCounter);
		notificationCounter = Utilities
				.updateNotificationCounter(getContacts());
		int activeThings = Utilities.calculateActiveThings(notificationCounter);
		Log.d(TAG, "number of active things = " + activeThings);
		message.setText(" " + activeThings); // get the current
		// number of friends
		// out shopping =>
		// calculated on how
		// many things are
		// out

	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = getIntent();
		if (i.getExtras() != null && i.getExtras().containsKey("actor")) {
			Bundle extras = i.getExtras();
			GHEvent gh = new GHEvent();
			gh.actor = extras.getParcelable("actor");
			gh.activity = extras.getString("activity");
			gh.content = extras.getString("content");
			fireMessage(gh);
		}
		else initializeMessage();
		Log.d(TAG, "message has been initialized");

		initializeListeners();
		Log.d(TAG, "listeners have been initialized");
	}

	private void initializeListeners() {

		LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayoutHome);
		ll.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

		statusListener = new StatusListener() {

			public void onStatusChanged(GHEvent e) {
				Log.d(TAG, "onstatuschanged triggered");
				fireMessage(e);
			}

		};
		((PersonalEMApplication) getApplication())
				.addStatusListener(statusListener);
	}

	private void fireMessage(GHEvent e) {
		// if message = start or stop from things
		if (e.getActor().getEntityType().equals("thing")) {
			initializeMessage(HomeActivity.ACTIVITY, e);
		}

		if (e.getActor().getEntityType().equals("place")) {
			initializeMessage(HomeActivity.LOCATION, e);
		}

		if (e.getActor().getEntityType().equals("people")) {
			initializeMessage(HomeActivity.SPARK, e);
		}

	}

	private void initializeMessage(int i, GHEvent e) {

		
		initializeMessage();
		TextView eventMessage = (TextView) findViewById(R.id.newEventMessage);

		switch (i) {
		case HomeActivity.SPARK:
			eventMessage.setText(e.getActor().getFirstName() + " "
					+ getString(R.string.newSparkMessage));
			break;

		case HomeActivity.LOCATION:
			String username = e.getContent().split(" ")[0];
			String locationMessage = e.getContent().contains("enter") ? getString(R.string.newLocationEnterMessage)
					: getString(R.string.newLocationExitMessage);
			eventMessage.setText(username + " " + locationMessage + " "
					+ e.getActor().getFirstName());
			break;

		case HomeActivity.ACTIVITY:
			String activityMessage;
			if (e.getContent().contains("data")) {
				String[] content = e.getContent().split(":");

				activityMessage = getString(R.string.newActivityDataMessage)
						+ " " + content[1] + content[2].split(" ")[0];
			} else
				activityMessage = e.getContent().contains("start") ? getString(R.string.newActivityStartMessage)
						: getString(R.string.newActivityStopMessage);
			eventMessage.setText(e.getActor().getFirstName() + " "
					+ activityMessage);
			break;

		default:
			break;
		}

		// get the current
		// number of friends
		// out shopping =>
		// calculated on how
		// many things are
		// out

	}

}
