package em.twitterido.aw;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class MakeGetRequest extends AsyncTask<String, Integer, String> {
	private static HttpClient client = new DefaultHttpClient();
	private String TAG = "make get request - async task";
	private Context context;
	ProgressDialog dialog = null;
	
	public MakeGetRequest(Context c) {
		// TODO Auto-generated constructor stub
		this.context = c;
	}
	
	
	@Override
	protected void onPreExecute() {
		
		dialog = ProgressDialog.show(context,"", "Loading. Please wait...",
				true);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
	public String makeGetRequest(String request) {
		// TODO Auto-generated method stub
		HttpGet get = new HttpGet(request);

		Log.d(TAG + " makegetrequest ", "request line: "
				+ get.getParams() + " and uri: "
				+ get.getURI().toASCIIString());

		StringBuilder builder = new StringBuilder();

		try {
			HttpResponse response = client.execute(get);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				entity.consumeContent();
				content.close();
			} else {
				Log.e(TAG, "Failed to download JSON statuscode: " + statusCode);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, builder.toString());
		return builder.toString();
	}


	@Override
	protected String doInBackground(String... params) {
		
		return makeGetRequest(params[0]);
	}
	

    protected void onPostExecute(String result) {
    	Log.d(TAG, "executed request with result "+ result);
    	dialog.dismiss();
    }
}
