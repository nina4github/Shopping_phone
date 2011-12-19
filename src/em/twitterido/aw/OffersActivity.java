package em.twitterido.aw;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class OffersActivity extends Activity {

	public static String TAG = "PersonalEM_Shopping / OffersActivity";
	public static final int CAMERA_PIC_REQUEST = 2;

	private Uri imageUri;
	private String user, activity;

	private static final String uri = "http://idea.itu.dk:8080/";
	private static final String diaspora = "@idea.itu.dk:3000";
	private SharedPreferences preferences;
	private HttpClient client = new DefaultHttpClient();

	public HttpClient getClient() {
		return client;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offers);

		preferences = getSharedPreferences(PersonalEMActivity.CONFIG_USER, 0);
		user = preferences.getString("user", "");
		activity = getString(R.string.activity);

		YappsCircleGallery yappsGallery = (YappsCircleGallery) findViewById(R.id.galleryOffers);

		yappsGallery.secondaryListener = new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

				TextView t = (TextView) findViewById(R.id.captionOffersText);
				// String name = contact_names[(arg2 % contact_names.length)];
				// String name = contact_names.get((arg2 %
				// contact_names.size()));
				// String started = notificationCounter.get(name) == 0 ? "ikke "
				// : "";
				// t.setText(contact_names.get((arg2 % contact_names.size())) +
				// " er "
				// + started + activity);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		};

		/**
		 * setupButton listeners
		 */
		ImageButton offerButton = (ImageButton) findViewById(R.id.offerButton);
		offerButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
				Date date = new Date();
				String now = dateFormat.format(date);
				Log.d(TAG, now);
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

				String filename = user + "offer" + now;

				File dir = new File(Environment.getExternalStorageDirectory(),
						"EM/shopping");
				dir.mkdirs();

				File file = new File(dir, "offer" + now + ".jpg");
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

		ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
		ImageButton offerpageButton = (ImageButton) findViewById(R.id.offerpageButton);

		homeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent home = new Intent(OffersActivity.this,
						PersonalEMActivity.class);
				startActivity(home);
			}
		});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_PIC_REQUEST) {
			if (resultCode == RESULT_OK) {

				Uri selectedImage = imageUri;

				Log.d(TAG, "RESULT_OK");

				getContentResolver().notifyChange(selectedImage, null);
				ImageView imageView = (ImageView) findViewById(R.id.PhotoCaptured);
				ContentResolver cr = getContentResolver();
				Bitmap bitmap;
				try {
					bitmap = android.provider.MediaStore.Images.Media
							.getBitmap(cr, selectedImage);

					imageView.setImageBitmap(bitmap);

					Toast.makeText(this,
							"Uploaded " + selectedImage.toString(),
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast
							.makeText(this, "Failed to upload",
									Toast.LENGTH_SHORT).show();
					Log.e("Camera", e.toString());
				}

				try {
					//httpPostImage(imageUri);
					postImage(imageUri);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// data.getExtras()

				// ImageView image =(ImageView)
				// findViewById(R.id.PhotoCaptured);
				// image.setImageBitmap(thumbnail);
			} else {
				Toast.makeText(OffersActivity.this, "Picture NOt taken",
						Toast.LENGTH_LONG);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void postImage(Uri img_uri) throws IOException {
		// TODO Auto-generated method stub
		File f = new File(img_uri.getPath());
		HttpClient postClient = getClient();
		HttpPost postImage = new HttpPost(
				uri+"activities/"+this.activity+"/upload.json?user="+this.user+diaspora+
				"&original_filename="+f.getName());
		FileEntity fEntity = new FileEntity(f,"image/*");
		
		postImage.setEntity(fEntity); 
		
		HttpResponse response = postClient.execute(postImage);
		
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					Log.i("response", (resEntity != null ? " not null " : " null "));
					Log.i("response", (resEntity.getContent().toString()));
				}

	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	/**
	 * // public class ImageAdapter extends BaseAdapter { // int
	 * mGalleryItemBackground; // private Context mContext; // // private
	 * Integer[] mImageIds = { // R.drawable.ove, // R.drawable.tove, //
	 * R.drawable.aase, // R.drawable.birthe, // R.drawable.shopping, // }; //
	 * // public ImageAdapter(Context c) { // mContext = c; // TypedArray a =
	 * obtainStyledAttributes(R.styleable.AppTheme); // mGalleryItemBackground =
	 * a.getResourceId( // R.styleable.AppTheme_listDragShadowBackground, 0); //
	 * a.recycle(); // } // // public int getCount() { // return
	 * mImageIds.length; // } // // public Object getItem(int position) { //
	 * return position; // } // // public long getItemId(int position) { //
	 * return position; // } // // public View getView(int position, View
	 * convertView, ViewGroup parent) { // ImageView i = new
	 * ImageView(mContext); // // i.setImageResource(mImageIds[position]); //
	 * i.setLayoutParams(new Gallery.LayoutParams(250, 200)); //
	 * i.setScaleType(ImageView.ScaleType.FIT_XY); //
	 * i.setBackgroundResource(mGalleryItemBackground); // // return i; // } //
	 * }
	 * @throws Exception 
	 */
	
	private byte[] prepareBody(String boundary, File file, String activity) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream((int)file.length());
		PrintWriter writer = new PrintWriter(baos);
		String CRLF = "\r\n";
		String charset = "utf8";

		// Send normal param.
//		writer.append("--" + boundary).append(CRLF);
//		writer.append("Content-Disposition: form-data; name=\"activity\"")
//				.append(CRLF);
//		writer.append("Content-Type: text/plain; charset=" + charset)
//				.append(CRLF);
//		writer.append(CRLF);
//		writer.append(activity).append(CRLF).flush();

		// Send binary file.
		writer.append("--" + boundary).append(CRLF);
		writer.append(
				"Content-Disposition: form-data; name=\"myfile\"; filename=\""
						+ file.getName() + "\"").append(CRLF);
		writer.append(
				"Content-Type: "
						+ URLConnection.guessContentTypeFromName(file
								.getName())).append(CRLF);
//		writer.append("Content-Transfer-Encoding: binary").append(CRLF);
		writer.append(CRLF).flush();
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			for (int length = 0; (length = input.read(buffer)) > 0;) {
				byte[] encoded = Base64Coder.encodeBytes(buffer,0,length);
				baos.write(encoded, 0, encoded.length);
				//baos.write(buffer, 0, length);
				
			}
//			output.flush(); // Important! Output cannot be closed. Close of
							// writer will close output as well.
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException logOrIgnore) {
				}
		}
		writer.append(CRLF).flush(); // CRLF is important! It indicates end
										// of binary boundary.

		// End of multipart/form-data.
		writer.append("--" + boundary + "--").append(CRLF);
		writer.close();
		return baos.toByteArray();
	}
	private void send(String url, File file, String activity) throws Exception {
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just
																		// generate
																		// some
																		// unique
																		// random
																		// value.
//		String CRLF = "\r\n"; // Line separator required by multipart/form-data.

		byte[] body = prepareBody(boundary, file, activity);
		long bodylength = body.length;
		Log.i(TAG,"Sending body-length: "+bodylength);
		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
//		connection.setDoInput(true);
		connection.setRequestProperty("Content-Length",bodylength+"");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		connection
				.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
		OutputStream os = connection.getOutputStream();
		try {
			os.write(body);
			Log.i(TAG,"File sent");
			int status = ((HttpURLConnection)connection).getResponseCode();
			Log.i(TAG, "upload image response code: "+status);
		}
		finally {
			os.close();
//			connection.getInputStream().close();
		}
		

//		PrintWriter writer = null;
//		try {
//			OutputStream output = connection.getOutputStream();
//			String charset = "utf8";
//			writer = new PrintWriter(new OutputStreamWriter(output, charset),
//					true); // true = autoFlush, important!
//
//			// Send normal param.
//			writer.append("--" + boundary).append(CRLF);
//			writer.append("Content-Disposition: form-data; name=\"activity\"")
//					.append(CRLF);
//			writer.append("Content-Type: text/plain; charset=" + charset)
//					.append(CRLF);
//			writer.append(CRLF);
//			writer.append(activity).append(CRLF).flush();
//
//			// Send binary file.
//			writer.append("--" + boundary).append(CRLF);
//			writer.append(
//					"Content-Disposition: form-data; name=\"myfile\"; filename=\""
//							+ file.getName() + "\"").append(CRLF);
//			writer.append(
//					"Content-Type: "
//							+ URLConnection.guessContentTypeFromName(file
//									.getName())).append(CRLF);
//			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//			writer.append(CRLF).flush();
//			InputStream input = null;
//			try {
//				input = new FileInputStream(file);
//				byte[] buffer = new byte[1024];
//				for (int length = 0; (length = input.read(buffer)) > 0;) {
//					output.write(buffer, 0, length);
//				}
//				output.flush(); // Important! Output cannot be closed. Close of
//								// writer will close output as well.
//			} finally {
//				if (input != null)
//					try {
//						input.close();
//					} catch (IOException logOrIgnore) {
//					}
//			}
//			writer.append(CRLF).flush(); // CRLF is important! It indicates end
//											// of binary boundary.
//
//			// End of multipart/form-data.
//			writer.append("--" + boundary + "--").append(CRLF);
//			Log.i(TAG,"File sent");
//			
//			int status = ((HttpURLConnection)connection).getResponseCode();
//			Log.i(TAG, "upload image response code: "+status);
//		} finally {
//			if (writer != null)
//				writer.close();
//			connection.getInputStream().close();
//		}
	}

	private void httpPostImage(Uri image_uri) throws Exception {
		String uri = "http://idea.itu.dk:8080/upload"+"?user="+this.user+"@idea.itu.dk:3000";
		this.send(uri, new File(image_uri.getPath()), this.activity);
		
//		HttpClient uploadClient = getClient();
//		String actor = this.user;
//
//		// HttpPost post = new
//		// HttpPost("http://idea.itu.dk:8080/activities/"+activity+"/upload"+"?user="+actor+"@idea.itu.dk:3000");
//		// String params = "?user=" + this.user + diaspora;
//		HttpPost post = new HttpPost(this.uri + "upload");// +params);
//
//		// InputStream in = this.getContentResolver().openInputStream(uri);
//		// InputStreamBody bin = new InputStreamBody(in, uri.getPath());
//		File f = new File(image_uri.getPath());
//		FileBody fb = new FileBody(f);
//
//		Log.d(TAG, image_uri.getPath());
//		Log.d(TAG, fb.getTransferEncoding());
//
//		MultipartEntity reqEntity = new MultipartEntity();
//
//		// reqEntity.addPart("myfile", fb);
//
//		reqEntity.addPart("activity", new StringBody("shopping"));
//		reqEntity.addPart("user", new StringBody(this.user + diaspora));
//
//		Log.d(TAG, "http client " + post.getURI().toASCIIString());
//		post.setEntity(reqEntity);
//
//		HttpResponse response = uploadClient.execute(post);
//
//		HttpEntity resEntity = response.getEntity();
//		if (resEntity != null) {
//			Log.i("response", (resEntity != null ? " not null " : " null "));
//			Log.i("response", (resEntity.getContent().toString()));
//		}
	}
}
