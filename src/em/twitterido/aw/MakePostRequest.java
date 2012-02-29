package em.twitterido.aw;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class MakePostRequest extends AsyncTask<String, Integer, String> {
	private static HttpClient client = new DefaultHttpClient();
	private String TAG = "make get request - async task";
	private Context context;
	ProgressDialog dialog = null;
	
	public MakePostRequest(Context c) {
		// TODO Auto-generated constructor stub
		this.context = c;
	}
	
	
	@Override
	protected void onPreExecute() {
		
		dialog = ProgressDialog.show(context,"", "Loading. Please wait...",
				true);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	
		
	}
	public String makePostRequest(String request, final String text) {
		// TODO Auto-generated method stub
		HttpPost post = new HttpPost(request);

//		HttpParams params = new BasicHttpParams();
//		params.setParameter("text", text);
//		post.setParams(params);

		List<NameValuePair> vals = new LinkedList<NameValuePair>() {{
			add(new BasicNameValuePair("text", text));
		}};

		Log.d(TAG + " makepostrequest ", "request line: "
//				+ post.getParams() + " and uri: "
				+ post.getURI().toASCIIString());

		StringBuilder builder = new StringBuilder();

		try {
			post.setEntity(new UrlEncodedFormEntity(vals,HTTP.UTF_8));
			HttpResponse response = client.execute(post);
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
				response.getEntity().consumeContent();
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
		
		return makePostRequest(params[0],params[1]);
	}
	

    protected void onPostExecute(String result) {
    	Log.d(TAG, "executed request with result "+ result);
    	dialog.dismiss();
    }
}
