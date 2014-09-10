package com.cremamobile.filemanager;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import java.util.Stack;

import com.cremamobile.filemanager.file.FileListEntry;
import com.cremamobile.filemanager.file.FileLister;
import com.cremamobile.filemanager.file.FileSorter;
import com.cremamobile.filemanager.file.FindHistory;
import com.cremamobile.filemanager.gridview.FileListAdapter;
import com.cremamobile.filemanager.history.HistoryNode;
import com.cremamobile.filemanager.service.*;
import com.cremamobile.filemanager.settingview.SlideAnimationLayout;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.cremamobile.filemanager.IRemoteService;
import com.cremamobile.filemanager.IRemoteServiceCallback;
import com.cremamobile.filemanager.R;

public class CremaActivity extends ActionBarActivity implements
	NavigationDrawerFragment.NavigationDrawerCallbacks {

	public static final int BUTTON_ID_COPY = 1011;
	public static final int BUTTON_ID_MOVE = 1012;
	public static final int BUTTON_ID_RENAME = 1013;
	public static final int BUTTON_ID_NEW_FOLDER = 1014;
	public static final int BUTTON_ID_SORT = 1015;
	public static final int BUTTON_ID_SHARE = 1016;
	public static final int BUTTON_ID_REFRESH = 1017;
	public static final int BUTTON_ID_SETTING = 1018;
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	
	private static Stack<HistoryNode>  mHistoryStack;
	
	
	private static String mCurrentPath;
	private static int mCurrentSelectedItem;
	private static int mWorkMode;
	private static int mViewMode;
	
	private static GridView	mMainGrid;
	private static ListView mMainList;
	
	private static FileListAdapter mFileAdapter;
	
	/**
	 * Option Menu ...
	 */
	private static View settingView;
	private OnClickListener settingMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			settingView.setVisibility(View.GONE);
			
			int id = v.getId();
			switch(id) {
			case R.id.setting_copy:
				break;
			case R.id.setting_move:
				break;
			case R.id.setting_rename:
				break;
			case R.id.setting_newfolder:
				break;
			case R.id.setting_sort:
				break;
			case R.id.setting_share:
				break;
			case R.id.setting_refresh:
				setting_refresh();
				break;
			case R.id.setting_setting:
				setting_setting();
				break;
			}
		}
		
	};
	
	private void setting_refresh() {
		mCurrentSelectedItem = 0;
		setCurrentViewPerPath(mCurrentPath, mCurrentSelectedItem, false);
	}
	
	private void setting_setting() {
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);
	}
	
	/**
	 * mount sdcard ..etc..
	 */
	ExternalMountBroadcastReceiver mReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		getActionBar().setIcon(R.drawable.folder_01);
		
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(1)).commit();
		
		View v = getLayoutInflater().inflate(R.layout.actionbar_idle, null);
		
		ActionBar ab = getSupportActionBar();
		ab.setCustomView(v);
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
		restoreActionBar();
		
		// Start Service
		Intent intent = new Intent("com.cremamobile.filemanager.IRemoteService");
//		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
		
//		Intent intent = new Intent("kr.letsnow.crema.service.IRemoteService");
		startService(intent);
		
		RegisterUpdateReceiver();
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	  
	@Override
	protected void onDestroy() {
		// Stop Service
//		if (mBound)
//			unbindService(mServiceConnection);
		
		Intent intent = new Intent("com.cremamobile.filemanager.IRemoteService");
		stopService(intent);
		
		//
		super.onDestroy();
	}
	
	@Override
	public void onNavigationDrawerItemSelected(long position, String path) {
		// update the main content by replacing fragments
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		fragmentManager
//				.beginTransaction()
//				.replace(R.id.container,
//						PlaceholderFragment.newInstance(position + 1)).commit();
		if (path == null)
			return;
		
		setCurrentViewPerPath(path, 0, true);
	}

	public void onSectionAttached(int number) {
		mTitle = getString(R.string.app_name);
//		switch (number) {
//		case 1:
//			mTitle = getString(R.string.title_section1);
//			break;
//		case 2:
//			mTitle = getString(R.string.title_section2);
//			break;
//		case 3:
//			mTitle = getString(R.string.title_section3);
//			break;
//		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		if (!mNavigationDrawerFragment.isDrawerOpen()) {
//			// Only show items in the action bar relevant to this screen
//			// if the drawer is not showing. Otherwise, let the drawer
//			// decide what to show in the action bar.
//			getMenuInflater().inflate(R.menu.crema, menu);
//			restoreActionBar();
//			return true;
//		}
//		return super.onCreateOptionsMenu(menu);
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			settingView.setVisibility(View.GONE);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			settingView = rootView.findViewById(R.id.layout_settings);
			SlideAnimationLayout slideView = (SlideAnimationLayout) settingView.findViewById(R.id.layout_settingButtons);
			slideView.createAnimation();
			settingMenuInitialize(slideView);

			
			mMainGrid = (GridView) rootView.findViewById(R.id.layout_grid);
			List<FileListEntry> files = FileLister.getFileLists(mCurrentPath, false);
			
			if (files != null) {
				Collections.sort(files, new FileSorter(getActivity().getApplicationContext(), true, FileSorter.SORT_BY_NAME_ASC));
			}
			
			mFileAdapter = new FileListAdapter(getActivity().getApplicationContext(), files);
			mMainGrid.setAdapter(mFileAdapter);
			mMainGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
				}
            });
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((CremaActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}
	

	/*************************************************************************************************/
	// 공통 Handler
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }
        }

    };
    
	/*************************************************************************************************/
	// Service 
//	CremaFileService mService;
//	boolean			mBound;
//	
//	private ServiceConnection	mServiceConnection = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			// TODO Auto-generated method stub
//			CremaFileBinder binder = (CremaFileBinder) service;
//			mService = binder.getService();
//			mBound = true;
//			
//			//test!!!!!!!!!
//			mService.showToast("bind success!!!");
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			// TODO Auto-generated method stub
//			mBound = false;
//			
//		}
//		
//	};

	// Remote Service
	private IRemoteService	mService = null;
	boolean	mBound = false;
	private IRemoteServiceCallback mServiceCallback = new IRemoteServiceCallback.Stub() {
		public void MessageCallback(int msg) {
			mHandler.sendEmptyMessage(msg);
		}
	};
	
	private ServiceConnection	mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mService = IRemoteService.Stub.asInterface(service);
			mBound = true;
			try {
				mService.registerCallback(mServiceCallback);
			} catch(RemoteException e) {
				// TODO... 
				
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mService = null;
			mBound = false;
		}
		
	};
	
	/*************************************************************************************************/
	
	public static void updateLanguage(Context ctx, String lang)
	{
		Configuration cfg = new Configuration();
		if (!TextUtils.isEmpty(lang))
			cfg.locale = new Locale(lang);
		else
			cfg.locale = Locale.getDefault();
		
		ctx.getResources().updateConfiguration(cfg, null);
	}
	
	private void RegisterUpdateReceiver()
	{
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
	    intentFilter.addDataScheme("file");
	    mReceiver = new ExternalMountBroadcastReceiver();
	    this.registerReceiver(mReceiver, intentFilter);
	    
	    //<data android:scheme="file" />
	}
	
	class ExternalMountBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
	        if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
	            // react to event
	        	//TODO!!!
	        }
		}
		
	};
	
	/*****************************************************************************************************/
	private boolean setCurrentViewPerPath(String path, int currentPosition, boolean saveHistory) {
		//TODO.
//		int pos = gridview.getFirstVisiblePosition(); 
//		gridview.smoothScrollToPosition(currentPosition);
		
		if (path == null)
			return false;

		if (saveHistory) {
			// 현재 화면을 History에 넣는다.
			pushHistory(mCurrentPath, mCurrentSelectedItem);
		}
		
		mCurrentPath = path;
		List<FileListEntry> files = FileLister.getFileLists(path, false);
		if (files == null) {
			// 파일 존재하지 않음.. TODO
			return false;
		}
		
		Collections.sort(files, new FileSorter(getApplicationContext(), true, FileSorter.SORT_BY_NAME_ASC));
		mFileAdapter.setList(files);
		
		return true;
	}
	
	
	private void backHistory() {
		String className = "";
		String currentName = "";
		
		if (mHistoryStack == null || mHistoryStack.isEmpty()){
			return;
		}
		
		HistoryNode history = mHistoryStack.peek();
		if (history.getPath() != null && !history.getPath().equals(mCurrentPath)) {
			
			boolean result = setCurrentViewPerPath(history.getPath(), history.getCurrentPosition(), false);
			if (result) mHistoryStack.pop();
		}
	}
	
	private void pushHistory(String path, int selected) {
		
		if (mHistoryStack == null) {
			mHistoryStack = new Stack<HistoryNode>();
		}
		
		// 마지막 History가 현재 화면과 같으면 history에 넣지 않는다.
		HistoryNode peek = mHistoryStack.peek();
		if (peek == null || ! peek.getPath().equals(path)) { 
			HistoryNode currentHistory = new HistoryNode(path, selected);
			mHistoryStack.push(currentHistory);
		}
	}
	
	private void settingMenuInitialize(View container) {
		Button settingButton = (Button) container.findViewById(R.id.setting_copy);
		settingButton.setOnClickListener(settingMenuClickListener);
		
		settingButton = (Button) container.findViewById(R.id.setting_move);
		settingButton.setOnClickListener(settingMenuClickListener);
		
		settingButton = (Button) container.findViewById(R.id.setting_rename);
		settingButton.setOnClickListener(settingMenuClickListener);
		
		settingButton = (Button) container.findViewById(R.id.setting_newfolder);
		settingButton.setOnClickListener(settingMenuClickListener);

		settingButton = (Button) container.findViewById(R.id.setting_sort);
		settingButton.setOnClickListener(settingMenuClickListener);

		settingButton = (Button) container.findViewById(R.id.setting_share);
		settingButton.setOnClickListener(settingMenuClickListener);
		
		settingButton = (Button) container.findViewById(R.id.setting_refresh);
		settingButton.setOnClickListener(settingMenuClickListener);

		settingButton = (Button) container.findViewById(R.id.setting_setting);
		settingButton.setOnClickListener(settingMenuClickListener);
	}
	
	private void showOptionSetting() {
		
//		Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
//		iew.startAnimation(slide);
	}
	
	private void hideOptionSetting() {
		
		
	}
}
