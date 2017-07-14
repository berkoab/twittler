package com.berko.tweetsaroundyou;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TweetAdapter extends ArrayAdapter<Tweet> {
	private ArrayList<Tweet> tweets;
	
	public TweetAdapter(Context context, int textViewResourceId, ArrayList<Tweet> objects) {
		super(context, textViewResourceId, objects);
		this.tweets = objects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.tweet_list, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Tweet i = tweets.get(position);

		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView text1 = (TextView) v.findViewWithTag("text1");
			TextView text2 = (TextView) v.findViewWithTag("text2");
			TextView text3 = (TextView) v.findViewById(R.id.text3);
			TextView location = (TextView) v.findViewById(R.id.distance);
			TextView id = (TextView) v.findViewById(R.id.hidden);

			// check to see if each individual textview is null.
			// if not, assign some text!
			if (text1 != null){
				text1.setText(i.getText());
			}
			if (text2 != null){
				text2.setText(i.getName());
			}
			if (text3 != null){
				text3.setText(i.getFrom());
			}
			if (location != null){
				location.setText(i.getDistance());
			}
			if (id != null){
				id.setText(i.getId());
			}
		}

		// the view must be returned to our activity
		return v;

	}

}
