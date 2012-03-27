package em.twitterido.aw;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class FullscreenImage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fullscreenimg);
		ImageView fullscreenview = (ImageView) this.findViewById(R.id.fullscreenimg);
		fullscreenview.setImageDrawable(R.id.imageSelected); //<-- what should 'imageSelected' be?
		
	}
	
	

}
