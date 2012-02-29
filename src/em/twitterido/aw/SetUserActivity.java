package em.twitterido.aw;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SetUserActivity extends Activity {
	private SharedPreferences preferences;
	public static final String TAG = "SetUserActivity (Personal EM)";
	String username;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adduser);
		
		Log.d(TAG,"we set up the add user view");
		
		preferences = getSharedPreferences(PersonalEMActivity.CONFIG_USER, 0);
		
		Button save = (Button) findViewById(R.id.addUserButton);
		save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText value = (EditText) findViewById(R.id.addUserText);
				
				Editor e = preferences.edit();
				e.putString("user", value.getText().toString());
				e.apply(); 
				Log.d(TAG,"new user value = "+value.getText().toString());
				Intent i = new Intent();
				i.putExtra("name", value.getText().toString());
				setResult(PersonalEMActivity.SETUSER, i);
				finish();
			}
		});
		
	}
	
	
}
