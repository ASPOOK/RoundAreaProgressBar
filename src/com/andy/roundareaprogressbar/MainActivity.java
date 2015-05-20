package com.andy.roundareaprogressbar;

import com.andy.widgets.RoundAreaProgressBar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	private RoundAreaProgressBar mProgressBar;
	private int mProgress = 0;
	private Handler mHandler;
	private static final int MSG_PROGRESS_CHANGED = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mProgressBar = (RoundAreaProgressBar) findViewById(R.id.round_area_progress_bar);

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_PROGRESS_CHANGED:
					mProgress = mProgressBar.getProgress();
					mProgressBar.setProgress(++mProgress);
					if (mProgress >= 100) {
						mHandler.removeMessages(MSG_PROGRESS_CHANGED);
						return;
					}
					mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_CHANGED, 150);

					break;

				default:
					break;
				}
			}

		};

		mHandler.sendEmptyMessage(MSG_PROGRESS_CHANGED);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
		case R.id.action_restart:
			mProgress = 0;
			mProgressBar.setProgress(mProgress);
			mHandler.sendEmptyMessage(MSG_PROGRESS_CHANGED);
			
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

}
