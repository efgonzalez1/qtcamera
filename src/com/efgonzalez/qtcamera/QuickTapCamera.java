package com.efgonzalez.qtcamera;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QuickTapCamera extends Activity {
	private SurfaceView viewFinder;
	private SurfaceHolder viewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private int altShutter;
	private TextView shutterMode;
	private TextView flashMode;
	private TextView sceneMode;
	private TextView colorMode;
	private RelativeLayout hud;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		hud = (RelativeLayout) findViewById(R.id.relativeLayout1);
		shutterMode = (TextView) findViewById(R.id.textView2);
		flashMode = (TextView) findViewById(R.id.textView4);
		sceneMode = (TextView) findViewById(R.id.textView6);
		colorMode = (TextView) findViewById(R.id.textView8);

		viewFinder = (SurfaceView) findViewById(R.id.viewFinder);
		viewFinder.setOnClickListener(takePic);
		viewHolder = viewFinder.getHolder();
		viewHolder.addCallback(surfaceCallback);
		viewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		viewHolder.setSizeFromLayout();
	}

	
	private OnClickListener takePic = new OnClickListener() {
		@Override
		public void onClick(View v) {
			camera.takePicture(null, null, photoCallback);
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (altShutter != -1 && keyCode == altShutter) {
			camera.takePicture(null, null, photoCallback);
			return (true);
		}
		return (super.onKeyDown(keyCode, event));
	}

	private void CreateMenu(Menu menu) {
		menu.setQwertyMode(true);
		MenuItem mnu1 = menu.add(0, 1, 1, "Gallery");
		{
			mnu1.setAlphabeticShortcut('g');
			mnu1.setIcon(R.drawable.ic_menu_smalltiles);
		}
		MenuItem mnu2 = menu.add(0, 2, 2, "Preferences");
		{
			mnu2.setAlphabeticShortcut('p');
			mnu2.setIcon(R.drawable.ic_menu_settings);
		}
		MenuItem mnu3 = menu.add(0, 3, 3, "App News");
		{
			mnu3.setAlphabeticShortcut('n');
			mnu3.setIcon(R.drawable.ic_menu_globe);
		}
		MenuItem mnu4 = menu.add(0, 0, 0, "Toggle HUD");
		{
			mnu4.setAlphabeticShortcut('h');
			mnu4.setIcon(R.drawable.ic_menu_monitor);
		}
	}

	private boolean MenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Toast.makeText(QuickTapCamera.this, "Sorry this option is disabled in the DEMO.", Toast.LENGTH_LONG).show();
			return true;
		case 2:
			startActivity(new Intent(QuickTapCamera.this, Preferences.class));
			return true;
		case 3:
			startActivity(new Intent(QuickTapCamera.this, News.class));
			return true;
		case 0:
			if (hud.getVisibility() == TextView.VISIBLE)
				hud.setVisibility(TextView.INVISIBLE);
			else
				hud.setVisibility(TextView.VISIBLE);
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuChoice(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		camera = Camera.open();
		updateFromPreferences();
	}

	@Override
	public void onPause() {

		if (inPreview) {
			camera.stopPreview();
		}

		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();
	}

	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera.setPreviewDisplay(viewHolder);
			} catch (Throwable t) {
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(width, height);
			// List<Size> supportedPreviewSizes = parameters
			// .getSupportedPreviewSizes();
			// Size optimalPreviewSize = getOptimalPreviewSize(
			// supportedPreviewSizes, 3, 5);
			// parameters.setPreviewSize(optimalPreviewSize.width,
			// optimalPreviewSize.height);

			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);

			camera.setParameters(parameters);

			try {
				camera.setPreviewDisplay(viewHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			camera.startPreview();
			inPreview = true;

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		}

	};

	private Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SaveImage().execute(data);
		}

	};;

	class SaveImage extends AsyncTask<byte[], Void, Void> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			camera.startPreview();
			inPreview = true;
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(QuickTapCamera.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Saving Image...");
			progressDialog.setCancelable(true);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(byte[]... data) {

			Uri uriTarget = getContentResolver().insert(
					Media.EXTERNAL_CONTENT_URI, new ContentValues());

			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uriTarget);
				imageFileOS.write(data[0]);
				imageFileOS.flush();
				imageFileOS.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private void setAltShutter(String s) {
		if (s.equals("0")) {
			altShutter = -1;
		} else if (s.equals("1")) {
			altShutter = KeyEvent.KEYCODE_VOLUME_UP;
		} else if (s.equals("2")) {
			altShutter = KeyEvent.KEYCODE_VOLUME_DOWN;
		}
	}

	private void setFlash(String s) {
		Camera.Parameters parameters = camera.getParameters();
		if (s.equals("0")) {
			parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
		} else if (s.equals("1")) {
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		}
		camera.setParameters(parameters);
	}

	private void updateFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
//		StringBuilder features = new StringBuilder();

		String s = prefs.getString("alt_shutter", "0");
		int q = Integer.parseInt(s);
		String[] qwe = getResources().getStringArray(R.array.shortcuts);
		shutterMode.setText(qwe[q] + " | ");
		setAltShutter(s);

		s = prefs.getString("flash_mode", "0");
		q = Integer.parseInt(s);
		qwe = getResources().getStringArray(R.array.flash);
		flashMode.setText(qwe[q] + " | ");
		setFlash(s);

		s = prefs.getString("scene_mode", "0");
		q = Integer.parseInt(s);
		qwe = getResources().getStringArray(R.array.scene);
		sceneMode.setText(qwe[q] + " | ");
		setScene(s);

		s = prefs.getString("color_effect", "0");
		q = Integer.parseInt(s);
		qwe = getResources().getStringArray(R.array.effects);
		colorMode.setText(qwe[q]);
		setEffect(s);
	}

	private void setEffect(String s) {
		Camera.Parameters parameters = camera.getParameters();
		if (s.equals("0")) {
			parameters.setColorEffect(Parameters.EFFECT_NONE);
		} else if (s.equals("1")) {
			parameters.setColorEffect(Parameters.EFFECT_AQUA);
		} else if (s.equals("2")) {
			parameters.setColorEffect(Parameters.EFFECT_BLACKBOARD);
		} else if (s.equals("3")) {
			parameters.setColorEffect(Parameters.EFFECT_MONO);
		} else if (s.equals("4")) {
			parameters.setColorEffect(Parameters.EFFECT_NEGATIVE);
		} else if (s.equals("5")) {
			parameters.setColorEffect(Parameters.EFFECT_POSTERIZE);
		} else if (s.equals("6")) {
			parameters.setColorEffect(Parameters.EFFECT_SEPIA);
		} else if (s.equals("7")) {
			parameters.setColorEffect(Parameters.EFFECT_SOLARIZE);
		} else if (s.equals("8")) {
			parameters.setColorEffect(Parameters.EFFECT_WHITEBOARD);
		}
		camera.setParameters(parameters);
	}

	private void setScene(String s) {
		Camera.Parameters parameters = camera.getParameters();
		if (s.equals("0")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
		} else if (s.equals("1")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_ACTION);
		} else if (s.equals("2")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_BEACH);
		} else if (s.equals("3")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_CANDLELIGHT);
		} else if (s.equals("4")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_FIREWORKS);
		} else if (s.equals("5")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_LANDSCAPE);
		} else if (s.equals("6")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_NIGHT);
		} else if (s.equals("7")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_NIGHT_PORTRAIT);
		} else if (s.equals("8")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_PARTY);
		} else if (s.equals("9")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_PORTRAIT);
		} else if (s.equals("10")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_SNOW);
		} else if (s.equals("11")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_SPORTS);
		} else if (s.equals("12")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_STEADYPHOTO);
		} else if (s.equals("13")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_SUNSET);
		} else if (s.equals("14")) {
			parameters.setSceneMode(Parameters.SCENE_MODE_THEATRE);
		}
		camera.setParameters(parameters);
	}
	
//	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
//		final double ASPECT_TOLERANCE = 0.05;
//		double targetRatio = (double) w / h;
//		if (sizes == null)
//			return null;
//
//		Size optimalSize = null;
//		double minDiff = Double.MAX_VALUE;
//
//		int targetHeight = h;
//
//		// Try to find an size match aspect ratio and size
//		for (Size size : sizes) {
//			double ratio = (double) size.width / size.height;
//			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
//				continue;
//			if (Math.abs(size.height - targetHeight) < minDiff) {
//				optimalSize = size;
//				minDiff = Math.abs(size.height - targetHeight);
//			}
//		}
//
//		// Cannot find the one match the aspect ratio, ignore the requirement
//		if (optimalSize == null) {
//			minDiff = Double.MAX_VALUE;
//			for (Size size : sizes) {
//				if (Math.abs(size.height - targetHeight) < minDiff) {
//					optimalSize = size;
//					minDiff = Math.abs(size.height - targetHeight);
//				}
//			}
//		}
//		return optimalSize;
//	}
	
}