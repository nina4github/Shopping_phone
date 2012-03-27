package em.twitterido.aw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class Utilities {

	public static String TAG = "Utilities";
	public static final int IMG_MAXSIZE = 400;
	public static final long[] EVENTVIBRATEPATTERN = { 500, 400, 300, 0, 400,
			300, 200, 0, 300, 200, 100, 0 }; // GRADUALLY FADING ?
	public static final long[] UPDATEVIBRATEPATTERN = { 300, 0, 200, 0, 100, 0 };;

	public static void vibrate(Context activity, long[] pattern) {

		// vibrate to signal that new events are available
		Vibrator v = (Vibrator) activity
				.getSystemService(Context.VIBRATOR_SERVICE);

		// // 1. Vibrate for 1000 milliseconds
		// long milliseconds = 1000;
		// v.vibrate(milliseconds);

		// 2. Vibrate in a Pattern with 500ms on, 500ms off for 5 times
		v.vibrate(pattern, -1);
	}

	// UTILS
	public static StringBuffer readStringFromFile(File dir, String name) {
		File file = new File(dir, name);
		Log.d("Utilities", "beginning reading the json string in the " + name
				+ " file");
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				fileData.append(buf, 0, numRead);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		
		return fileData;
	}
	
	static void saveResponseToFile(String response, String filename, File dir) {
		// TODO Auto-generated method stub

		File file = new File(dir, filename);
		try {
			file.createNewFile();
			Log.d(TAG, "set response to file, create " + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeStringToFile(file, response);
		Log.d(TAG, "set response to file, response written to file");
	}

	static void writeStringToFile(File file, String string) {
		// save the response in the file
		Writer out;
		try {
			out = new OutputStreamWriter(new FileOutputStream(file));
			out.write(string);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int calculateActiveThings(
			HashMap<String, Integer> notificationCounter) {

		int activeThings = 0;

		for (String key : notificationCounter.keySet()) {
			activeThings = activeThings + notificationCounter.get(key);
		}

		Log
				.d(TAG, "active things value for initialize message "
						+ activeThings);

		return activeThings;
	}

	public static ArrayList<User> updateActiveThings(File dir, String filename,
			ArrayList<User> contacts) {

		StringBuffer fileData = Utilities.readStringFromFile(dir, filename);
		JSONObject jObj = null;
		try {
			jObj = new JSONObject(fileData.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			JSONArray jsonArray = jObj.getJSONArray("stream");

			//for (int i = 0; i < jsonArray.length(); i++) {
			for (int i = jsonArray.length()-1; i >= 0; i--) { // reverse order!!!
				// get the object
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				// take only if the verb is statusmessage (we want to check
				// for start and stop
				if (jsonObject.getString("verb").equals("StatusMessage")) {

					Log.d(TAG, "scanning post n. " + jsonObject.getInt("id"));
					int id = jsonObject.getJSONObject("actor").getInt("id");

					// get in contacts the user with id = id
					// take only the actors who are things
					for (int j = 0; j < contacts.size(); j++) {
						User actor = (User) contacts.get(j);
						// it will never be the user. but in case you can always
						// add
						// currentUser.getUserId() ==id ||
						// to the following if statement
						if ((actor.getUserId() == id)
								&& actor.getEntityType().equalsIgnoreCase(
										"thing")) {
							String content = jsonObject.getJSONObject("object")
									.getString("content");
							int status = content.contains("start") ? 1 : 0;
							// update the status of the actor who posted this
							// message
							actor.setStatus(status);
							// and update the counter :)

							Log.d(TAG, "updated status of actor "
									+ actor.getUserId() + " named "
									+ actor.getFullName() + " to " + status);
							break;
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return contacts;

	}

	public static HashMap<String, Integer> updateNotificationCounter(
			ArrayList<User> c) {
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		for (User user : c) {
			if (user.getEntityType() == "thing") {
				counter.put(user.getFullName(), user.getStatus());
			}

		}
		return counter;
	}

	public Bitmap getDecentSizeImage(String filepath) {
		int maxsize = 1000;
		File f = new File(filepath);
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			InputStream is = new FileInputStream(f);
			BitmapFactory.decodeStream(is, null, o);
			is.close();

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= maxsize
					&& o.outHeight / scale / 2 >= maxsize) {
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			is = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, o2);
			is.close();
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap createThumbnail(Uri image) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			InputStream is = new FileInputStream(new File(image.getPath()));
			BitmapFactory.decodeStream(is, null, o);
			is.close();

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= IMG_MAXSIZE
					&& o.outHeight / scale / 2 >= IMG_MAXSIZE) {
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			is = new FileInputStream(new File(image.getPath()));
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, o2);
			is.close();
			int fullW = bitmap.getWidth();
			int fullH = bitmap.getHeight();
			int w, h;

			w = (fullW > fullH) ? 300 : (fullW * 300) / fullH;
			h = (fullH > fullW) ? 300 : (fullH * 300) / fullW;

			Bitmap out = Bitmap.createScaledBitmap(bitmap, w, h, false);
			Log.d(TAG, "CreateThumbnail scale:" + scale + ",w/h:" + fullW + "/"
					+ fullH + ",thw:" + w + ", oW/oH:" + o.outWidth + "/"
					+ o.outHeight);

			System.gc();
			return out;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void createAlertDialogue(Context context, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(true).setPositiveButton(
				R.string.okButton, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static User getContactById(int actor, ArrayList<User> c) {

		for (User user : c) {
			if (user.getUserId() == (actor)) {
				return user;
			}
		}
		return null;
	}

	public static void updateActiveEntities(File dir, String filename,
			ArrayList<User> entities) {

		for(User u : entities){
			u.setStatus(0);
		}
		
		StringBuffer fileData = Utilities.readStringFromFile(dir, filename);
		JSONObject jObj = null;
		try {
			jObj = new JSONObject(fileData.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			JSONArray jsonArray = jObj.getJSONArray("stream");

//			for (int i = 0; i < jsonArray.length(); i++) {
			for (int i = jsonArray.length()-1; i >=0 ; i--) {

				// get the object
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				// take only if the verb is statusmessage (we want to check
				// for start and stop

				Log.d(TAG, "scanning post n. " + jsonObject.getInt("id"));
				int id = jsonObject.getJSONObject("actor").getInt("id");

				// get the user with id = id
				// take only the actors who are things
				for (int j = 0; j < entities.size(); j++) {
					User actor = (User) entities.get(j);
					// it will never be the user. but in case you can always
					// add
					// currentUser.getUserId() ==id ||
					// to the following if statement
					if ((actor.getUserId() == id)) {
						if (jsonObject.getString("verb")
								.equals("StatusMessage")) {
							// handle THINGS
							if (actor.getEntityType().equalsIgnoreCase("thing")) {

								String content = jsonObject.getJSONObject(
										"object").getString("content");
								int status = content.contains("start") ? 1 : 0;
								// update the status of the actor who posted
								// this
								// message
								actor.setStatus(status);
								// and update the counter :)
								Log.d(TAG, "updated status of actor "
										+ actor.getUserId() + " named "
										+ actor.getFullName() + " to "
										+ actor.getStatus() + " for post n. "
										+ jsonObject.getString("id"));

							} else if (actor.getEntityType().equalsIgnoreCase(
									"place")) {
								String content = jsonObject.getJSONObject(
										"object").getString("content");

								if (content.contains("enter"))
									actor.setStatus(actor.getStatus() + 1);

								if (content.contains("leave"))
									actor.setStatus(actor.getStatus() == 0 ? 0
											: actor.getStatus() - 1);
								// update the status of the actor who posted
								// this
								// message
								Log.d(TAG, "updated status of actor "
										+ actor.getUserId() + " named "
										+ actor.getFullName() + " to "
										+ actor.getStatus() + " for post n. "
										+ jsonObject.getString("id"));
							}// end type check

						} else if (jsonObject.getString("verb").equals("Photo")) {
							if (actor.getEntityType()
									.equalsIgnoreCase("person"))
								actor.setStatus(actor.getStatus() + 1);

							Log.d(TAG, "updated status of actor "
									+ actor.getUserId() + " named "
									+ actor.getFullName() + " to "
									+ actor.getStatus() + " for post n. "
									+ jsonObject.getString("id"));
						}// end check verbs

						break; // exit when you have found the user of this post
						// and continue to the next post
					}// end user found

				}// end for to iterate on users

			}// end for to iterate on posts

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
