package com.example.mycamera;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = MainActivity.class.getSimpleName();
	private SurfaceView mCameraView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	private boolean safeToTakePicture = false;
	private Button startButton;
	private TextView countdownTextView;
	private Handler timerUpdateHandler;
	private int currentTime = 10;
	private boolean timerRunning =false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mCameraView = (SurfaceView) findViewById(R.id.sv_camera_perview);
		mSurfaceHolder = mCameraView.getHolder();
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.addCallback(this);
		mCameraView.setFocusable(true);
		mCameraView.setFocusableInTouchMode(true);
		mCameraView.setClickable(true);
		
		countdownTextView = (TextView) findViewById(R.id.CountDownTextView);
		
		startButton = (Button) findViewById(R.id.CountDownButton);
		startButton.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				takePicture();
				if (!timerRunning) {
					timerRunning = true;
					timerUpdateHandler.post(timerUpdateTask);
				}
			}
		});

		timerUpdateHandler = new Handler();
		
	}

	private Runnable timerUpdateTask = new Runnable() {
		public void run() {
			if (currentTime > 1) {
				currentTime--;
				timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
			} else {
				 takePicture();
				timerRunning = false;
				currentTime = 10;
			}
			countdownTextView.setText("" + currentTime);
		}
	};
	
	@SuppressWarnings("deprecation")
	private void takePicture() {
		if (safeToTakePicture) {
			mCamera.takePicture(null, null, new Camera.PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					onPictureTacken(data, camera);
				}
			});
			safeToTakePicture = false;
		}
	}

	private void onPictureTacken(byte[] data, @SuppressWarnings("deprecation") Camera camera) {
		Uri imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
		try {
			OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
			imageFileOS.write(data);
			imageFileOS.flush();
			imageFileOS.close();
		} catch (FileNotFoundException e) {
			Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
			t.show();
		} catch (IOException e) {
			Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
			t.show();
		}
		safeToTakePicture = true;
		camera.startPreview();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			Camera.Parameters parameters = mCamera.getParameters();
			int bestWith = 0;
			int bestHight = 0;
			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
			if (previewSizes.size() > 1) {
				Iterator<Camera.Size> ceIterator = previewSizes.iterator();
				while (ceIterator.hasNext()) {
					Camera.Size aSize = ceIterator.next();
					Log.v(TAG, "aSize.With	" + aSize.width + " aSize.Hight " + aSize.height);
					if (aSize.height > bestHight && aSize.width > bestWith) {
						bestHight = aSize.height;
						bestWith = aSize.width;
					}
				}
				if (bestHight != 0 && bestWith != 0) {
					parameters.setPreviewSize(bestWith, bestHight);
					// mCameraView.setLayoutParams(new
					// RelativeLayout.LayoutParams(bestWith,bestHight));
				}
			}
			if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				parameters.set("orientation", "portrait");
				mCamera.setDisplayOrientation(90);
				parameters.setRotation(90);
			}

			List<String> colorEffects = parameters.getSupportedColorEffects();
			Iterator<String> cei = colorEffects.iterator();
			while (cei.hasNext()) {
				String currentEffect = cei.next();
				if (currentEffect.equals(Camera.Parameters.EFFECT_SOLARIZE)) {
					parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
					break;
				}
			}
			// End Effects for Android Version 2.0 and higher

			mCamera.setParameters(parameters);
		} catch (IOException exception) {
			mCamera.release();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		mCamera.startPreview();
		safeToTakePicture = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera.stopPreview();
		mCamera.release();
	}
}
