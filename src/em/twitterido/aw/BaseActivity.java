package em.twitterido.aw;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {

	public final String TAG = this.getClass().getSimpleName();

	protected Uri imageUri;
	public static final int CAMERA_PIC_REQUEST = 2;

	protected static final String uri = "http://idea.itu.dk:8080/";
	protected static final String service = "@idea.itu.dk:3000";

	public static final File activity_dir = new File(Environment
			.getExternalStorageDirectory(), "EM/shopping/");
	public static final File configuration_dir = new File(Environment
			.getExternalStorageDirectory(), "EM/shopping/config/");
	public static final File stream_dir = new File(Environment
			.getExternalStorageDirectory(), "EM/shopping/stream/");
	public static final File offers_dir = new File(Environment
			.getExternalStorageDirectory(), "EM/shopping/offers/");
	public static final File profiles_dir = new File(Environment
			.getExternalStorageDirectory(), "EM/shopping/profiles/");

	protected HashMap<String, Integer> notificationCounter = new HashMap<String, Integer>();

	protected ArrayList<User> getContacts() {
		return ((PersonalEMApplication) getApplication()).contacts;

	}

	protected User getCurrentUser() {
		return ((PersonalEMApplication) getApplication()).currentUser;
	}

	protected String getActivity() {
		return ((PersonalEMApplication) getApplication()).activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// set these directories if they are not yet configured
		activity_dir.mkdirs();
		configuration_dir.mkdirs();
		offers_dir.mkdirs();
		stream_dir.mkdirs();
		profiles_dir.mkdirs();

		doOnCreate();
	}

	protected abstract void doOnCreate();

	protected void initializeButtons(int home, int friends, int offers,
			int newoffer) {

		ImageButton homepageButton = (ImageButton) findViewById(home);
		ImageButton friendspageButton = (ImageButton) findViewById(friends);
		ImageButton offerpageButton = (ImageButton) findViewById(offers);
		ImageButton newofferButton = (ImageButton) findViewById(newoffer);

		homepageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Log.d(TAG, "clicked on homepageButton");
				Intent home = new Intent(BaseActivity.this, HomeActivity.class);
				startActivity(home);
			}
		});

		friendspageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Log.d(TAG, "clicked on friendspageButton");
				Intent friends = new Intent(BaseActivity.this,
						PersonalEMActivity.class);
				startActivity(friends);
			}
		});

		offerpageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Log.d(TAG, "clicked on offerpageButton");
				Intent offers = new Intent(BaseActivity.this,
						OffersActivity.class);

				startActivity(offers);
			}
		});

		
		// NewOffer starts an intent to activate the Camera
		// with the button click we start creating the file for the picture that we are going to take
		newofferButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				Log.d(TAG, "clicked on newofferButton");
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMdd_HHmmss");
				String now = dateFormat.format(new Date());
				Log.d(TAG, now);
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

				String filename = getCurrentUser().getFullName() + "_offer_"
						+ now;

				File dir = new File(Environment.getExternalStorageDirectory(),
						"EM/shopping/myoffers");
				dir.mkdirs();

				File file = new File(dir, filename + ".jpg");
				Log.d(TAG, file.getName());
				try {
					file.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				imageUri = Uri.fromFile(file);
				Log.d(TAG, imageUri.toString());
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);

			}
		});

	}

	// TODO: when upload picture then save it somewhere in the gallery
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_PIC_REQUEST) {
			if (resultCode == RESULT_OK) {

				Uri selectedImage = imageUri;

				Log.d(TAG, "RESULT_OK");

				getContentResolver().notifyChange(selectedImage, null);

				try {
					MakePostImageTask postImageTask = new MakePostImageTask(
							this);
					postImageTask.execute(imageUri.toString(), getActivity(),
							getCurrentUser().getFullName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Toast.makeText(BaseActivity.this, "Picture NOt taken",
						Toast.LENGTH_LONG);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public class MakePostImageTask extends AsyncTask<String, Integer, Boolean> {
		// TODO create async task
		// implement in the diaspora client the GH new message

		Context context = null;

		ProgressDialog dialog = null;

		public MakePostImageTask(Context c) {
			context = c;
		}

		@Override
		protected void onPreExecute() {

			dialog = ProgressDialog.show(BaseActivity.this, "",
					"Sending image. Please wait...", true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		private void postImage(Uri img_uri, String activity, String user) {
			// TODO Auto-generated method stub

			File f = new File(img_uri.getPath());

			HttpClient postClient = new DefaultHttpClient();
			HttpPost postImage = new HttpPost(uri + "activities/" + activity
					+ "/upload.json?user=" + user + service
					+ "&original_filename=" + f.getName());
			FileEntity fEntity = new FileEntity(f, "image/*");

			postImage.setEntity(fEntity);

			HttpResponse response;
			try {
				response = postClient.execute(postImage);
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					Log.d("response", (resEntity != null ? " not null "
							: " null "));
					Log.d("response", (resEntity.getContent().toString()));
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		protected Boolean doInBackground(String... params) {

			Uri uri = Uri.parse(params[0]);
			String activity = params[1];
			String user = params[2];

			postImage(uri, activity, user);

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			offerUploaded();
		}

	}

	protected void offerUploaded() {
		// do nothing, subclass extend if needed 
		// it is called after returning from the camera intent and saving the file
		// it is used in OffersActivity.java to update the gallery (and the list of offers)
	}

	
	protected void updateCurrentUser(int status) {

	}
}
