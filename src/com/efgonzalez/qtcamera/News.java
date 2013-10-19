package com.efgonzalez.qtcamera;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class News extends Activity {

	private TextView title;
	private TextView txt;
	private Button backBtn;

	private OnClickListener back = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);
		title = (TextView) findViewById(R.id.txtTitle);
		txt = (TextView) findViewById(R.id.msgBody);
		backBtn = (Button) findViewById(R.id.aboutBtnBack);
		backBtn.setOnClickListener(back);
		new getAbout().execute();
	}

	class getAbout extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;
		private String str;
		private Document home;

		@Override
		protected void onPreExecute() {
			title.setVisibility(View.INVISIBLE);
			txt.setVisibility(View.INVISIBLE);
			backBtn.setVisibility(View.INVISIBLE);
			progressDialog = new ProgressDialog(News.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading news...");
			progressDialog.setCancelable(true);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String url = "https://sites.google.com/a/pipeline.sbcc.edu/qtcamera/";
				home = Jsoup.connect(url).get();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try{
			str = home.getElementById("sites-canvas-main-content").html();
			}
			catch (NullPointerException e){
				e.printStackTrace();
				str = "Failed to load news. Check your internet connection.";
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			txt.setText(Html.fromHtml(str));
			title.setVisibility(View.VISIBLE);
			txt.setVisibility(View.VISIBLE);
			backBtn.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}
}