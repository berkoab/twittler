package com.berko.tweetsaroundyou;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.berko.tweetsaroundyou.managers.BoundingBox;
import com.berko.tweetsaroundyou.managers.GPSTracker;
import com.berko.tweetsaroundyou.managers.GeoPoint;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ArrayList<Tweet> mTweets = new ArrayList<Tweet>();
	private TweetAdapter mAdapter;
	private boolean mKeepRunning = false;	
	private AsyncTask<Integer, Integer, Integer> streamTask;
	private BoundingBox boundingBox=null;
	private SharedPreferences sp = null;
	double latitude;
	double longitude;
	String location = "";
	String miles;

//	private AdView adView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		TapForTap.initialize(this, getString(R.string.tapfortap_id));
//		adView = new AdView(this, AdSize.BANNER, "0bbb36eb5c5346f5");
//        LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
//        layout.addView(adView);
//        AdRequest request = new AdRequest();
//        adView.loadAd(request);
		
		streamTask = new StreamTask();
		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		miles = sp.getString("miles", "5");
		
		GPSTracker gps = new GPSTracker(this);
		DecimalFormat df = new DecimalFormat("###.##");
		if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            location = "lat: " + df.format(latitude) + ", long: " + df.format(longitude);
            gps.stopUsingGPS();
//            Log.i("gps", "Location: is - \nLat: " + latitude + "\nLong: " + longitude);
                        
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if(null!=listAddresses&&listAddresses.size()>0){
                    Address address = listAddresses.get(0);
                    Log.i("gps", "locality" +address.getLocality()
                    		+",sublocality"+address.getSubLocality()
                    		+",adminarea:"+address.getAdminArea()
                    		+",subadminarea:"+address.getSubAdminArea());
                    String city = "";
                    if(address.getSubLocality()!=null) {
                    	city = address.getSubLocality();
                    } else {
                    	city = address.getLocality();
                    }
                    location = city + ", " + listAddresses.get(0).getAdminArea();
                } 
//                Log.i("gps", "location: " + location);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            GeoPoint geoPoint = new GeoPoint(latitude, longitude);
            boundingBox = new BoundingBox(geoPoint, Integer.valueOf(miles));
//            Log.i("gps", "Bounding box:" + boundingBox.toString());
   
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
            Log.i("gps", "couldn't get location");
            Toast.makeText(getApplicationContext(), "Couldn't get your location", Toast.LENGTH_LONG).show();
    	
            
        }
	    
		ListView tweets = (ListView)findViewById(R.id.Tweets);
		
		tweets.setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Tweet tweet = (Tweet) parent.getItemAtPosition(position);
						Intent intent = new Intent(MainActivity.this, WebActivity.class);
						new Bundle();
						intent.putExtra("ID", tweet.getId());
						intent.putExtra("SCREEN_NAME", tweet.getFrom());
						startActivity(intent); 
					}
					
				}
		            
		);
		
		mAdapter = new TweetAdapter(this, R.layout.tweet_list, mTweets);
		tweets.setAdapter(mAdapter);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	//lifecycle
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	        	startActivity(new Intent(this, UserSettingActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelTask();
	}
	
	private void cancelTask() {
		if(!streamTask.isCancelled()) {
			streamTask.cancel(true);
			mKeepRunning = false;
			((Button)findViewById(R.id.StartStopButton)).setText(R.string.start_button);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancelTask();		
	}
	
	public void startStop( View v ) {
		if(((Button)v).getText().equals(getString(R.string.start_button))) {
			Toast.makeText(getApplicationContext(), "Looking for tweets ...", Toast.LENGTH_LONG).show();
			streamTask = new StreamTask();
			streamTask.execute();
    		mKeepRunning = true;
    		((Button)v).setText(R.string.stop);
		}
		else {
			cancelTask();			
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		miles = sp.getString("miles", "5");
		Toast.makeText(getApplicationContext(), getString(R.string.message_start) + 
				" " + miles + " " + getString(R.string.message_end) + location, Toast.LENGTH_LONG).show();
	}
	
	private class StreamTask extends AsyncTask<Integer, Integer, Integer> {
		
		TwitterStream twitterStream = null;
		
		@Override
		protected Integer doInBackground(Integer... params) {
			try {
				ConfigurationBuilder cb = new ConfigurationBuilder();
		    	cb.setDebugEnabled(true)
		    	  .setOAuthConsumerKey(getString(R.string.OAuthConsumerKey))
		    	  .setOAuthConsumerSecret(getString(R.string.OAuthConsumerSecret))
		    	  .setOAuthAccessToken(getString(R.string.OAuthAccessToken))
		    	  .setOAuthAccessTokenSecret(getString(R.string.OAuthAccessTokenSecret));

		    	TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
		        twitterStream = tf.getInstance();
		        FilterQuery filterQuery = new FilterQuery();
		        double[][] location = {{boundingBox.getMinLng(),boundingBox.getMinLat()},
		        		{boundingBox.getMaxLng(),boundingBox.getMaxLat()}};
		        filterQuery.locations(location);
		        
		        StatusListener listener = new StatusListener() {

		            @Override
		            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

		            @Override
		            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

		            @Override
		            public void onScrubGeo(long userId, long upToStatusId) {}

		            @Override
		            public void onStallWarning(StallWarning warning) {}

		            @Override
		            public void onException(Exception ex) {
		                ex.printStackTrace();
		            }
		            
		            Long time = System.currentTimeMillis();
					@Override
					public void onStatus(twitter4j.Status status) {
						if(!mKeepRunning) {
							twitterStream.cleanUp(); // shutdown internal stream consuming thread
							twitterStream.shutdown(); 
						}
						if(System.currentTimeMillis()>time+1000) {
							time = System.currentTimeMillis();
							parseTweet(status);
						}
					}
		        };

		        twitterStream.addListener(listener);
		        twitterStream.filter(filterQuery); 
		        
			} catch (Exception e) {
				Log.e("Twitter", "doInBackground_" + e.toString());
			}
			return Integer.valueOf(1);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			if(twitterStream!=null) {
				twitterStream.cleanUp(); 
				twitterStream.shutdown();
			}
		}
		
		
		Tweet tweet;
		private void parseTweet(twitter4j.Status status) {
			try {
				GeoLocation geoLocation = status.getGeoLocation();

				if(geoLocation!=null) {
					float[] distances = new float[2];
					Location.distanceBetween(latitude, longitude, geoLocation.getLatitude(), 
							geoLocation.getLongitude(), distances);
//					Log.i("gps", distances[0]);
					double distance = metersToMiles(distances[0]);
					if(distance<=Integer.valueOf(miles)) {
						Log.i("gps", String.valueOf(metersToMiles(distances[0])));
						Log.d("Twitter", "Keep Running: " + mKeepRunning
								+ " Line: " + status.toString());
						tweet = new Tweet(status.getId(), status.getText(), status.getUser().getScreenName());
						tweet.setName(status.getUser().getName());
						tweet.setDistance(distance);
						publishProgress(1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private double metersToMiles(float meters) {
			return meters * 0.00062137119;
			
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			mTweets.add(0, tweet);
			if (mTweets.size() > 20) {
				mTweets.remove(mTweets.size() - 1);
			}
			mAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Integer i) {

		}
	}
}
