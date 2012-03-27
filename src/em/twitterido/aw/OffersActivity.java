package em.twitterido.aw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.util.IO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
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
import em.twitterido.aw.PersonalEMApplication.StatusListener;

public class OffersActivity extends BaseActivity {

	private SharedPreferences preferences;

	private Drawable[] offers_images;
	private ArrayList<Offer> shoppingoffers = new ArrayList<Offer>();

	/* Gallery Handling */
	public View lastSelectedView = null;
	// private ImageView image_selected;
	private Gallery offers_gallery;

	private StatusListener statusListener;

	@Override
	protected void doOnCreate() {
		setContentView(R.layout.offers);
	}

	private void initializeData() {

		

		if (isUpdated()) {
			
			if (shoppingoffers.isEmpty())
				createOffersFromFile(); // it will return to initialize gallery
			else
				initializeGallery();
		} else
			updateOffers();
	}

	private void initializeGallery() {
		notificationCounter = Utilities
		.updateNotificationCounter(getContacts());
		
		offers_gallery = (Gallery) findViewById(R.id.galleryOffers);
		// image_selected = (ImageView) findViewById(R.id.imageSelected);

		Log.d(TAG, "offers_gallery created");
		ImageAdapterCircleGallery galleryAdapter = new ImageAdapterCircleGallery(
				this);

		// if (offers_images == null) {
		offers_images = new Drawable[shoppingoffers.size()];
		int i = 0;
		for (Offer offer : shoppingoffers) {
			offers_images[i] = offer.getImageFile();
			//Log.d(TAG, "offers_image " + i + offers_images[i].toString());
			i = i + 1;
		}
		// }

//		Log.d(TAG, "shopping offers: " + shoppingoffers.size());
//		Log.d(TAG, "offers images drawables: " + offers_images.length);

		galleryAdapter.setmImageDrawables(offers_images);

//		Log.d(TAG, "gallery drawables: "
//				+ galleryAdapter.getmImageDrawables().length);
		offers_gallery.setAdapter(galleryAdapter);
		galleryAdapter.notifyDataSetChanged();

		offers_gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View v,
					int position, long id) {
				// Toast.makeText(OffersActivity.this, "" + position,
				// Toast.LENGTH_SHORT).show();

				// image_selected.setImageDrawable(shoppingoffers.get(position).getImageFile());
				TextView message = (TextView) findViewById(R.id.captionOffersText);
				User user = shoppingoffers.get(position).getSharedByUser();
				String username = user.getFullName();
				String text = "Fra " + username + "d. "
						+ shoppingoffers.get(position).getPublished();

				message.setText(text);
				//message.setTextColor(R.color.mygreen);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// image_selected.setImageDrawable(shoppingoffers.get(0).getImageFile());

			}
		});

		offers_gallery
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						Drawable drawable = (Drawable) parent.getAdapter().getItem(position);
					
						startFullscreenImageView(drawable);
						return false;
					}

				});

	}

	private void startFullscreenImageView(Drawable drawable){
		Intent intent = new Intent(this, FullscreenImage.class);
		
		startActivity(intent);
	}
	private boolean isUpdated() {

		Log.d(TAG, "check if offers.txt exists and is updated");

		if (!(new File(offers_dir, "offers.txt").exists())) {
			Log.d(TAG, "offers.txt does not exist, therefore is not updated");
			return false;
		} else {

			String lastoffers = getOffersFromDiaspora();
			// chech if last offers == offers.txt
			// if equal do nothing
			// if updated check latest photo id
			// if latest photo id > max shoppingoffers_id
			// save new offers.txt, download all the new photos and update
			// shopping offer,
			// else do nothing
			StringBuffer fileData = Utilities.readStringFromFile(offers_dir,
					"offers.txt");

			if (!fileData.toString().equalsIgnoreCase(lastoffers)) {

				Log.d(TAG, "new updates are available");

				// checkOfferUpdates(fileData.toString());
				// simply return false because there might be new offers :P
				// TODO check now if the new elements are offers, only in this
				// case
				// it is worth reloading
				return false;
			}

			Log.d(TAG,
					"the data is the same so the situation is already updated");
			return true;
		}
	}

	// private void checkOfferUpdates(String data) {
	// JSONObject jObj = null;
	// try {
	// jObj = new JSONObject(fileData.toString());
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }

	// Integer[] shopping_offers_ids = new
	// Integer[shoppingoffers.size()];
	// ArrayList<Integer> ids = new ArrayList<Integer>();
	// int max_id = 0;
	// for (int i = 0; i < shoppingoffers.size(); i++) {
	// // shopping_offers_ids[i] = shoppingoffers.get(i).getId();
	// // ids.add(shoppingoffers.get(i).getId());
	// int current_id = shoppingoffers.get(i).getId();
	// max_id = (max_id > current_id) ? max_id : current_id;
	// Log.d(TAG, "max id of current offers is :" + max_id);
	// }

	/**
	 * Check in JSONArray for new photos.
	 */

	// JSONArray jsonArray;
	// try {
	// jsonArray = jObj.getJSONArray("stream");
	//
	// for (int i = 0; i < jsonArray.length(); i++) {
	// JSONObject jsonObject = jsonArray.getJSONObject(i);
	//
	// if (jsonObject.getString("verb").equals("Photo")) {
	// int id = jsonObject.getInt("id");
	// if (id > max_id) {
	// Log.d(TAG, "we have a photo that is more recent");
	// // 1. redo everything
	// // 2. check what is already there and update
	// // easier solution => redo everything :P
	// resetOffers(lastoffers);
	// break;
	// }
	// }
	// }
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	private void updateOffers() {
		Log.d(TAG, "updateOffers");
		// Utilities.vibrate(OffersActivity.this,
		// Utilities.UPDATEVIBRATEPATTERN);
		shoppingoffers.clear();

		// retrieve the offers from twitterIDo client
		// save them in offers.txt
		// createOffersFrom that file and store them in shoppingoffers (that is
		// a shared resource)
		String response = getOffersFromDiaspora();
		Utilities.saveResponseToFile(response, "offers.txt", offers_dir);

		createOffersFromFile();
		//initializeGallery();
	}

	/*
	 * function to parse the JSON stream saved in offers.txt and build the array
	 * list of shopping offers the parsing is delegated to an Async task,
	 * MakeOffersTask, because it might involve the download of the picture when
	 * a new one is retrieved
	 */
	private void createOffersFromFile() {

		StringBuffer fileData = Utilities.readStringFromFile(offers_dir,
				"offers.txt");

		JSONObject jObj = null;
		try {
			jObj = new JSONObject(fileData.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			JSONObject jsonObject = jObj.getJSONObject("stream");

			//Log.d(TAG, "this is the string array: " + jsonObject.toString());
			MakeOffersTask makeOffersTask = new MakeOffersTask();
			makeOffersTask.execute(jsonObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getOffersFromDiaspora() {

		Log.d(TAG, "calling get offers from diaspora");
		String params = "?user=" + getCurrentUser().getFullName() + service;
		String requestOffers = uri + "activities/" + getActivity()
				+ "/week.json" + params;

		// get the response
		MakeGetRequest request = new MakeGetRequest(this);
		request.execute(requestOffers);
		String response = null;
		try {
			response = request.get();
			Log.d(TAG, "offers response: " + response);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;

	}

	public class ImageAdapterCircleGallery extends BaseAdapter {

		private Context mContext;

		private Integer[] mImageIds;
		private String[] mImageUrls;
		private Drawable[] mImageDrawables;

		private Bitmap[] mImageBitmaps;

		public Bitmap[] getmImageBitmaps() {
			return mImageBitmaps;
		}

		public void setmImageBitmaps(Bitmap[] mImageBitmaps) {
			this.mImageBitmaps = mImageBitmaps;
		}

		public Drawable[] getmImageDrawables() {
			return mImageDrawables;
		}

		public void setmImageDrawables(Drawable[] mImageDrawables) {
			this.mImageDrawables = mImageDrawables;
		}

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
			return mImageDrawables.length;// Integer.MAX_VALUE;
		}
		
		

		public Object getItem(int position) {
		//	return position;// getPosition(position);
			return this.mImageDrawables[position];
		}

		public long getItemId(int position) {
			return position;// getPosition(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

//			Log.d(TAG, "drawing offer # " + position + " of "
//					+ mImageDrawables.length);
			ImageView i = new ImageView(mContext);
			// position = getPosition(position);

			i.setImageDrawable(mImageDrawables[position]);

			// i.setImageBitmap(mImageBitmaps[position]);
			// i.setImageResource(R.drawable.aase);
			int height = parent.getHeight();
			i.setLayoutParams(new Gallery.LayoutParams(height, height));

			i.setScaleType(ImageView.ScaleType.FIT_XY);
			i.setPadding(20, 20, 20, 20);

			// i.setBackgroundColor(R.color.actionbar_background_light);

			RelativeLayout rl = new RelativeLayout(mContext);
			rl.setPadding(10, 10, 10, 10);

			// TextView captionNotification= new TextView(mContext);
			// captionNotification.setText("This is user "+position);
			// captionNotification.setTextSize(30);
			// LinearLayout l = new LinearLayout(mContext);
			// l.setOrientation(LinearLayout.VERTICAL);
			// l.addView(i);
			// l.addView(captionNotification);
			// return i;
			rl.addView(i);

			return rl;
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

	@Override
	protected void onResume() {

		super.onResume();

		initializeButtons(R.id.homepageButton_o, R.id.friendspageButton_o,
				R.id.offerpageButton_o, R.id.newofferButton_o);
		Log.d(TAG, "user interface is initialized");

		initializeData();
		Log.d(TAG, "data has been initialized");

		if (!shoppingoffers.isEmpty())
			initializeGallery();

		initializeListeners();
	}

	private void initializeListeners() {
		statusListener = new StatusListener() {

			public void onStatusChanged(GHEvent e) {
				Log.d(TAG, "onstatuschanged triggered");

				// CREATE DIALOG DISPLAYING NEW EVENT
				// String message = e.actor.getStatus() == 1 ?
				// getString(R.string.offline) : getString(R.string.online);
				// Utilities.createAlertDialogue(OffersActivity.this,
				// e.actor.getFullName() + message);

				if (e.content.contains("spark")) {
					updateOffers();
				}

				Intent home = new Intent(OffersActivity.this,
						HomeActivity.class);
				home.putExtra("actor", e.actor);
				home.putExtra("content", e.content);
				home.putExtra("activity", e.activity);
				startActivity(home);
			}

		};
		((PersonalEMApplication) getApplication())
				.addStatusListener(statusListener);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();

	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		onStart();

	}

	public class MakeOffersTask extends AsyncTask<JSONObject, Integer, Void> {

		ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {

			dialog = ProgressDialog.show(OffersActivity.this, "",
					"Loading. Please wait...", true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		private Drawable ImageOperations(String url, String saveFilename) {

			String filename = offers_dir.getPath() + "/" + saveFilename;
			String filename_tab = offers_dir.getPath() + "/tab_" + saveFilename;

			File f = new File(filename);

			Drawable bitmap = null;

			// first if the file is not there, download the image from the url
			// then put it into a file (yes, I want to save the files)
			//
			// second create a drawable from the image file saved on the phone
			// here we assume that the name of the offer will be saved with
			// username_offer_offerid in the offers directory
			//
			// if the file already exists

			try {
				if (!f.exists()) {
					InputStream is = (InputStream) this.fetch(url);

					try {
						// save image to filename
						FileOutputStream out = new FileOutputStream(filename);
						IO.copy(is, out); // copy copy image to file ;)
						out.close();

						// create a thumbnail to display in the view
						Bitmap scaled = Utilities.createThumbnail(Uri
								.parse("file://" + filename));

						FileOutputStream outTabnail = new FileOutputStream(
								filename_tab);
						// 100 means compress for high quality
						scaled.compress(Bitmap.CompressFormat.JPEG, 100,
								outTabnail);

						bitmap = new BitmapDrawable(scaled);
						outTabnail.close();

						System.gc(); // call garbage collector
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					FileInputStream is = new FileInputStream(new File(
							filename_tab));
					bitmap = Drawable.createFromStream(is, filename_tab);
					is.close();
				}

				// Drawable d =
				// + "/" + saveFilename);

				return bitmap;
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
			Object content = url.getContent();
			return content;
		}

		private Offer setNewOffer(JSONObject jsonObject) throws JSONException {

			Offer newOffer = new Offer();
			int id = jsonObject.getInt("id");

			JSONObject object = jsonObject.getJSONObject("object");
			String originalImageUrl = object.getString("remotePhotoPath")
					+ object.getString("remotePhotoName");
			String actor = jsonObject.getJSONObject("actor").getString("id");
			String content = object.getString("content");
			JSONArray jTags = object.getJSONArray("tags");
			String[] tags = new String[jTags.length()];
			for (int j = 0; j < jTags.length(); j++) {
				tags[j] = jTags.get(j).toString();
			}
			String published = jsonObject.getString("published");

			newOffer.setContent(content);
			newOffer.setId(id);
			newOffer.setOriginalImageUrl(originalImageUrl);
			newOffer.setPublished(published);
			newOffer.setTags(tags);

			// String filename = getCurrentUser().getFullName() + "_offer_" + id
			// + ".jpg";
			ArrayList<User> contacts = getContacts();
			contacts.add(getCurrentUser());
			String actor_name = Utilities.getContactById(
					Integer.parseInt(actor), contacts).getFullName();
			String filename = actor_name + "_offer_" + id + ".jpg";
			Drawable imageFile = ImageOperations(originalImageUrl, filename);
			newOffer.setImageFile(imageFile);

			// set the actor who created the offer by checking that with the
			// contacts that I already have.
			int actor_id = Integer.parseInt(actor);
			if (getCurrentUser().getUserId() == actor_id) {
				newOffer.setSharedByUser(getCurrentUser());
			} else {

				for (User contact : getContacts()) {
					if (contact.getUserId() == actor_id) {
						newOffer.setSharedByUser(contact);
						break; // found it, go out
					}
				}
			}

			return newOffer;
		}

		@Override
		protected Void doInBackground(JSONObject... params) {

			try {

				Iterator iterator = params[0].keys();
				// browse all the days of the last week with data
				while (iterator.hasNext()) {
					String key = (String) iterator.next();

					JSONArray jsonArray = params[0].getJSONArray(key);

					for (int j = 0; j < jsonArray.length(); j++) {
						JSONObject jsonObject = jsonArray.getJSONObject(j);
						if (jsonObject.getString("verb").equals("Photo")) {
							shoppingoffers.add(setNewOffer(jsonObject));
						}

					}

				}
				Collections.sort(shoppingoffers, new OfferComparator());

				// if (jsonObject.getString("verb").equals("Photo")) {
				// shoppingoffers.add(setNewOffer(jsonObject));
				// }

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
			initializeGallery();
		}
	}

	protected void offerUploaded() {
		Log.d(TAG, "launching updateOffers after picture update");
		updateOffers(); // re load the offers and re draw the gallery
	}

}
