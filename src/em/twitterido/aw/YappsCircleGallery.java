package em.twitterido.aw;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class YappsCircleGallery extends Gallery {
	AdapterView.OnItemSelectedListener secondaryListener;
	//private Integer[] mImageIds = {R.drawable.shopping};

//	public Integer[] getmImageIds() {
//		return mImageIds;
//	}
//
//	public void setmImageUrls(Integer[] mImageIds) {
//		this.mImageIds = mImageIds;
//	}

	// 3 default constructors
	public YappsCircleGallery(Context context) {
		super(context);
		initiateAdapter(context);
	}

	public YappsCircleGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		initiateAdapter(context);
	}

	public YappsCircleGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initiateAdapter(context);
	}

//	public YappsCircleGallery(Context context, Integer[] mImageIds) {
//		super(context);
//		this.mImageIds = mImageIds;
//		initiateAdapter(context);
//	}

	View lastSelectedView = null;

	private void initiateAdapter(Context context) {

		this
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if (lastSelectedView != null)
							lastSelectedView.setLayoutParams(new Gallery.LayoutParams(
											180, 180));
						arg1.setLayoutParams(new Gallery.LayoutParams(350,350));
					
						lastSelectedView = arg1;

						// for this ask francesco :) this is a trick for adding
						// another listener on need
						// the best option would have been to create a generic
						// listener that would have allowed
						// for more onItemSelectedListeners!
						if (secondaryListener != null) {
							secondaryListener.onItemSelected(arg0, arg1, arg2,
									arg3);
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						if (secondaryListener != null) {
							secondaryListener.onNothingSelected(arg0);
						}
					}
				});

		//ImageAdapterCircleGallery adapter = new ImageAdapterCircleGallery(context);
		
		//setAdapter(new ImageAdapterCircleGallery(context));

		// To select the xSelected one -> 0 is the first.
		int xSelected = 0;
		// To make the view go to the middle of our 'endless' array
		setSelection(Integer.MAX_VALUE / 2 + (Integer.MAX_VALUE / 2) % 5 - 1
				+ xSelected);
	}

//	public class ImageAdapterCircleGallery extends BaseAdapter {
//
//		private Context mContext;
//
//		private Integer[] mImageIds = {};
//		private String[] mImageUrls = {};
//
//		public String[] getmImageUrls() {
//			return mImageUrls;
//		}
//
//		public void setmImageUrls(String[] mImageUrls) {
//			this.mImageUrls = mImageUrls;
//		}
//
//		public Integer[] getmImageIds() {
//			return mImageIds;
//		}
//
//		public void setmImageIds(Integer[] mImageIds) {
//			this.mImageIds = mImageIds;
//		}
//		public ImageAdapterCircleGallery(Context c) {
//			mContext = c;
//		}
////		public ImageAdapterCircleGallery(Context c, Integer[] imgIds) {
////			mContext = c;
////			mImageIds=imgIds;
////
////		}
//
//		public int getCount() {
//			return Integer.MAX_VALUE;
//		}
//
//		public Object getItem(int position) {
//			return getPosition(position);
//		}
//
//		public long getItemId(int position) {
//			return getPosition(position);
//		}
//
//		public View getView(int position, View convertView, ViewGroup parent) {
//
//			ImageView i = new ImageView(mContext);
//			position = getPosition(position);
//			i.setImageResource(mImageIds[position]);
//			i.setLayoutParams(new Gallery.LayoutParams(200, 200));
//			i.setScaleType(ImageView.ScaleType.FIT_XY);
//			i.setPadding(10, 10, 10, 10);
//			i.setBackgroundColor(R.color.actionbar_background_dark);
//			// TextView captionNotification= new TextView(mContext);
//			// captionNotification.setText("This is user "+position);
//			// captionNotification.setTextSize(30);
//			// LinearLayout l = new LinearLayout(mContext);
//			// l.setOrientation(LinearLayout.VERTICAL);
//			// l.addView(i);
//			// l.addView(captionNotification);
//			return i;
//		}
//
//		public int checkPosition(int position) {
//			return getPosition(position);
//		}
//
//		int getPosition(int position) {
//			if (position >= mImageIds.length) {
//				position = position % mImageIds.length;
//			}
//			return position;
//		}
//	}
}
