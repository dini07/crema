package com.cremamobile.filemanager;

import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cremamobile.filemanager.device.Device;
import com.cremamobile.filemanager.device.DeviceUtils;
import com.cremamobile.filemanager.device.Size;
import com.cremamobile.filemanager.device.StorageUtil;
import com.cremamobile.filemanager.device.StorageUtil.StorageInfo;
import com.cremamobile.filemanager.file.DeviceFileEntry;
import com.cremamobile.filemanager.file.DirFileEntry;
import com.cremamobile.filemanager.file.FileListEntry;
import com.cremamobile.filemanager.file.FileLister;
import com.cremamobile.filemanager.file.FileUtils;
import com.cremamobile.filemanager.treeview.InMemoryTreeStateManager;
import com.cremamobile.filemanager.treeview.TreeBuilder;
import com.cremamobile.filemanager.treeview.TreeNodeInfo;
import com.cremamobile.filemanager.treeview.TreeStateManager;
import com.cremamobile.filemanager.treeview.TreeViewAdapter;
import com.cremamobile.filemanager.treeview.TreeViewAdapterParent;
import com.cremamobile.filemanager.treeview.TreeViewList;
import com.cremamobile.filemanager.utils.CLog;

import com.cremamobile.filemanager.R;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/**
	 * Per the design guidelines, you should show the drawer on launch until the
	 * user manually expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private NavigationDrawerCallbacks mCallbacks;

	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerLayout mDrawerLayout;
	//private ListView mDrawerListView;
	private View mFragmentContainerView;

	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;

    private final Set<Long> selected = new HashSet<Long>();

    private View		containerView;

    // Root 개수
    private List<FileListEntry> mDevices;
    
    /********************************************************************************************/
    // Storage Select 영역
    private View mButtonParentView;
    private ImageView	mInternalStorageButton;
    private ImageView	mExternalStorageButton;
    private ImageView	mFavoriteButton;
    
    private long mCurentSelectedFavoriteID;
    
    /********************************************************************************************/
    // TreeListView 영역 
    private static NavigationDrawerDirectoryTree mTreeInternal = new NavigationDrawerDirectoryTree();
    private static NavigationDrawerDirectoryTree mTreeExternal = new NavigationDrawerDirectoryTree();
    private static NavigationDrawerFavoriteTree mTreeFavorite = new NavigationDrawerFavoriteTree();
    private static NavigationDrawerTreeHelper mCurrentTree;

    private static DeviceFileEntry mInternalRoot;
    private static List<DeviceFileEntry> mExternalRoot;
    private static List<File> mFavoriteList;

    /********************************************************************************************/
    // Size View 영역 
    private View	mLayoutSizeInfo;
    private TextView mAvailableSizeTextView;
    private TextView mTotalSizeTextView;
    private View progressFrameLayout;
    private ImageView mSizeProgressImageView;

    TreeViewAdapterParent<Long> treeListParent = new TreeViewAdapterParent<Long>() {
		@Override
		public void updateChildList(Long parent, String path, boolean searchChildDir) {
			// TODO Auto-generated method stub
			// 선택한 ID를 표시한다.
			mCurrentTree.currentSelectedID = parent;
			
			if (mCurrentTree.tree_type == NavigationDrawerTreeHelper.TREE_TYPE_FAVORITE) {
				// 즐겨찾기를 눌렀을 경우에는 tree를 닫고 해당 Directory 내용을 표시하도록 메인뷰에게 알린다.
				if (mDrawerLayout != null) {
					mDrawerLayout.closeDrawer(mFragmentContainerView);
				}
				mCallbacks.onNavigationDrawerItemSelected(parent, path);
			} else {
				// 디렉토리를 눌렀을 경우에는 해당 디렉토리 하위 디렉토리가 있으면 expand하고 해당 Directory내용을 표시하도록 메인뷰에 알린다.
				// tree를 닫지 않는다. 사용자가 계속 tree 구조를 navigiation할 수 있도록
				NavigationDrawerDirectoryTree dirTree = (NavigationDrawerDirectoryTree)mCurrentTree;
				if (mCallbacks != null) {
					// TODO. select한  path를 내보
					mCallbacks.onNavigationDrawerItemSelected(parent, path);
				}
	
				if (searchChildDir) {
					TreeBuilder<Long> treeBuilder = dirTree.manager.getTreeBuilder();
					updateTreeBuilder(treeBuilder, parent, path);
				}
			}
		}    	
    };
    
    public void updateTreeBuilder(TreeBuilder<Long> treeBuilder, Long parent, String path) {
    	List<FileListEntry> files = FileLister.getDirectoryLists(new File(path), false);
		if (files != null && files.size() > 0) {
			long i = treeBuilder.getLastAddedId();
			for (FileListEntry file : files) {
				CLog.d(this, "count:" + i + ", file["+file.toString()+"]");
				if (file.getHidden())
					continue;
				DirFileEntry dir = (DirFileEntry) file;
				treeBuilder.addRelation(parent, ++i, dir.getAbsolutePath(), dir.getName(), false, dir.isNeedSearchChild());
			}
		}
    }

    OnClickListener	storageButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == mFavoriteButton) {
				selectTree(mTreeFavorite);
			} else if (v == mInternalStorageButton) {
				selectTree(mTreeInternal);
			} else if (v == mExternalStorageButton) {
				mCurrentTree = mTreeExternal;
				selectTree(mTreeExternal);
			}
		}
    	
    };
    

    
	public NavigationDrawerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated
		// awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		// 최초에 디바이스 정보를 가져온다.
		getDevices();
		
		// Select either the default item (0) or the last selected item.
//		selectItem(mCurrentSelectedPosition);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of
		// actions in the action bar.
		setHasOptionsMenu(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		mDrawerListView = (ListView) inflater.inflate(
//				R.layout.fragment_navigation_drawer, container, false);
//		mDrawerListView
//				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//					@Override
//					public void onItemClick(AdapterView<?> parent, View view,
//							int position, long id) {
//						selectItem(position);
//					}
//				});
//		mDrawerListView.setAdapter(new ArrayAdapter<String>(getActionBar()
//				.getThemedContext(), android.R.layout.simple_list_item_1,
//				android.R.id.text1, new String[] {
//						getString(R.string.title_section1),
//						getString(R.string.title_section2),
//						getString(R.string.title_section3), }));
//		mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
//		return mDrawerListView;

		// StorageButton create
		View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mInternalStorageButton = (ImageView) view.findViewById(R.id.storage_device);
        mInternalStorageButton.setOnClickListener(storageButtonClickListener);
        mExternalStorageButton = (ImageView) view.findViewById(R.id.storage_extend);
        mExternalStorageButton.setOnClickListener(storageButtonClickListener);
        mFavoriteButton = (ImageView) view.findViewById(R.id.favorite);
        mFavoriteButton.setOnClickListener(storageButtonClickListener);
		
        setStorageButtonView(view);
		
        // tree view create
        mTreeInternal.listView = (TreeViewList) view.findViewById(R.id.directory_internal_treeview);
        mTreeExternal.listView = (TreeViewList) view.findViewById(R.id.directory_external_treeview);
        mTreeFavorite.listView = (ListView) view.findViewById(R.id.directory_favorite_listview);
        
		// TODO...
		boolean newCollapsible;
		if (savedInstanceState != null) {
//			mCurrentSelectedPosition = savedInstanceState
//					.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;

            mTreeInternal.manager = (TreeStateManager<Long>) savedInstanceState.getSerializable("internal_treeManager");
            mTreeExternal.manager = (TreeStateManager<Long>) savedInstanceState.getSerializable("external_treeManager");
            mTreeInternal.treeLevel = savedInstanceState.getInt("internal_treeLevel");
            mTreeExternal.treeLevel = savedInstanceState.getInt("external_treeLevel");
		}
		
        if (mTreeInternal.manager == null || mTreeExternal.manager == null) {
			mTreeInternal.createTreeManager(getActivity(), treeListParent, NavigationDrawerTreeHelper.TREE_TYPE_INTERNAL);
			mTreeExternal.createTreeManager(getActivity(), treeListParent, NavigationDrawerTreeHelper.TREE_TYPE_EXTERNAL);
        }

        if (mTreeFavorite.listAdapter == null) {
        	mTreeFavorite.createTreeManager(getActivity(), treeListParent, NavigationDrawerTreeHelper.TREE_TYPE_FAVORITE);
        }
        
        // 디바이스 정보를 가져와서 Root에 넣는다.
		setRootDeviceInfoToTree();
        
        if (mCurrentTree == null) {
        	mCurrentTree = mTreeInternal;
        }
        
		
		// 사이즈 정보를
        mLayoutSizeInfo = view.findViewById(R.id.layout_sizeinfo);
		mTotalSizeTextView = (TextView) mLayoutSizeInfo.findViewById(R.id.total_size);
		mAvailableSizeTextView = (TextView) mLayoutSizeInfo.findViewById(R.id.use_size);
		mSizeProgressImageView = (ImageView) mLayoutSizeInfo.findViewById(R.id.size_progress);
		progressFrameLayout = mLayoutSizeInfo.findViewById(R.id.progress_framlayout);
		
//        treeViewAdapter = new TreeViewAdapter(getActivity(), treeListParent, manager, treeLevel);
//        mDirectoryTreeView.setAdapter(treeViewAdapter);
//        registerForContextMenu(mDirectoryTreeView);

        // Device를 선택해서 인자로 넣는다.
		selectTree(mCurrentTree);
//        setStorageSize(mCurrentTree.root);
		setSizeView();
		return view;
	}
	
	/**
	 * 내부 저장소/외부 저장소 선택에 따라 Treeview/storage size info view를 교체한다.
	 * @param treeHelper
	 */
	private void selectTree(NavigationDrawerTreeHelper treeHelper) {

		mCurrentTree = treeHelper;

		if (mCurrentTree.tree_type == NavigationDrawerTreeHelper.TREE_TYPE_FAVORITE) {
			mTreeInternal.listView.setVisibility(View.GONE);
			mTreeExternal.listView.setVisibility(View.GONE);
			mTreeFavorite.listView.setVisibility(View.VISIBLE);
			
			mLayoutSizeInfo.setVisibility(View.GONE);
		} else {
			NavigationDrawerDirectoryTree dirTree = (NavigationDrawerDirectoryTree) mCurrentTree;
			
			mTreeFavorite.listView.setVisibility(View.GONE);
			// 디바이스 사이즈 정보를 표시한다.
			mLayoutSizeInfo.setVisibility(View.VISIBLE);
			setStorageSize(dirTree.root);
			
			if (dirTree.tree_type == NavigationDrawerTreeHelper.TREE_TYPE_INTERNAL) {
				// 내부 저장소일 경우, 내부 저장소 directory tree를 보인다. 
				mTreeInternal.listView.setVisibility(View.VISIBLE);
				mTreeExternal.listView.setVisibility(View.GONE);
			} else {
				// 외부 저장소일 경우, 내부 저장소 directory tree를 보인다. 
				mTreeInternal.listView.setVisibility(View.GONE);
				mTreeExternal.listView.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void setStorageButtonView(View frame) {
		// 버튼 정보들..
		if (mButtonParentView == null)
			mButtonParentView = frame.findViewById(R.id.layout_storage_select);
		
		// device 정보에 따라  button 을 생성한다.
		if (mInternalRoot  == null) {
			getDevices();
		}
		
		View extView = frame.findViewById(R.id.storage_extend);
		if (mExternalRoot == null || mExternalRoot.size() <= 0) {
			//외장 메모리가 하나 이상 있다는 의미..
			extView.setVisibility(View.GONE);
		} else {
			extView.setVisibility(View.VISIBLE);
		}
	}
	
	private void getDevices() {
		mInternalRoot = null;
		mExternalRoot = null;
	
		// 1개의 Internal, 여러개의 Extenral을 리턴한다.
		List<FileListEntry> devices = FileLister.getDevices(getActivity().getApplicationContext());
		
		if (devices == null)
			return;
		
		if (devices.size() > 1)
			mExternalRoot = new ArrayList<DeviceFileEntry>();
		
		for (FileListEntry item : devices) {
			DeviceFileEntry device = (DeviceFileEntry) item;
			if (! device.isExternalDevice() ) {
				mInternalRoot = device;
			} else {
				mExternalRoot.add(device);
			}
		}

	}
	
	private void setRootDeviceInfoToTree() {
		TreeBuilder<Long> treeBuilder;
		
		if (mInternalRoot == null) {
			// 내부 저장소가 없다면 getDevice() 를 한번 더 호출해 확인한다. 
			getDevices();
		}
		
		if (mInternalRoot == null)
			return;
		
		// Tree  ID ->  must be Unique!!
		long id = 0;
		
		//========================================================
		// 내부 저장소 Tree 화
		treeBuilder = mTreeInternal.manager.getTreeBuilder();
		
		// 내부 저장소일 경우는 1 depth 디렉토리 리스트를 최초로 넣는다.
    	List<FileListEntry> childDir = FileLister.getDirectoryLists(mInternalRoot.getPath(), false);
		if (childDir != null && childDir.size() > 0) {
			for (FileListEntry dir : childDir) {
				CLog.d(this, "count:" + id + ", file["+dir.toString()+"]");
				if (dir.getHidden())
					continue;

				DirFileEntry dirInfo = (DirFileEntry) dir;
				treeBuilder.sequentiallyAddNextNode(id++, dirInfo.getAbsolutePath(), dirInfo.getName(), false, dirInfo.isNeedSearchChild(), 0);
			}
		}
		mTreeInternal.root = mInternalRoot.getPath();
		mTreeInternal.treeLevel = 1;

		//========================================================
		// 외부 저장소 Tree 화
		// 외부 저장소가 1 개일 경우에는 내부 저장소와 같은 처리
		// 2개 이상일 경우는 Root node부터 넣는다.

		if (mExternalRoot == null)
			return;
		
		treeBuilder = mTreeExternal.manager.getTreeBuilder();
			
		long parentId = 0;
		
		for (DeviceFileEntry extFile : mExternalRoot) {
			if (parentId == 0)
				parentId = id;
			//외부 저자서일 경우는 Device 정보를 root node로 넣는다.
			treeBuilder.sequentiallyAddNextNode(id++, extFile.getAbsolutePath(), extFile.getName(), true, true, 0);
		}

		// 현재는 첫번째 외부 저장소를 임의로 선택.. 
		mTreeExternal.root = mExternalRoot.get(0).getPath();
		// TODO. 사용자가 이전에 선택한 외부저장소를 펼쳐야함.
		
    	List<FileListEntry> childDirEx = FileLister.getDirectoryLists(mExternalRoot.get(0).getPath(), false);
		if (childDirEx != null && childDirEx.size() > 0) {
			for (FileListEntry dir : childDirEx) {
				CLog.d(this, "count:" + id + ", file["+dir.toString()+"]");
				if (dir.getHidden())
					continue;

				DirFileEntry dirInfo = (DirFileEntry) dir;
				treeBuilder.addRelation(parentId, id++, dirInfo.getAbsolutePath(), dirInfo.getName(), false, dirInfo.isNeedSearchChild());
			}
		}
		
		mTreeExternal.treeLevel = 2;
	}
	
	private void setStorageSize(File path) {
		
		Size deviceSize = Size.getSpace(path);
		
//		long totalSize = FileUtils.getTotalExternalMemorySize();
//		long availSize = FileUtils.getAvaiableExternalMemorySize();
		
		mTotalSizeTextView.setText(getString(R.string.total_size) + " " +FileUtils.formatSize(deviceSize.getTotalSize()));
		mAvailableSizeTextView.setText(getString(R.string.use_size) +  " " + FileUtils.formatSize(deviceSize.getSpaceSize()));

		int usePercent = deviceSize.getPercentage();
		int totalWidth = progressFrameLayout.getWidth();
		FrameLayout.LayoutParams lp= (FrameLayout.LayoutParams)mSizeProgressImageView.getLayoutParams();
		int percentWidth= Math.max(1, (int)(totalWidth*usePercent/100));
		lp.width=percentWidth;
		mSizeProgressImageView.setLayoutParams(lp);
	}
	
	private void setSizeView() {
		
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation
	 * drawer interactions.
	 * 
	 * @param fragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.navigation_drawer_open, /*
										 * "open drawer" description for
										 * accessibility
										 */
		R.string.navigation_drawer_close /*
										 * "close drawer" description for
										 * accessibility
										 */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().supportInvalidateOptionsMenu(); // calls
																// onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to
					// prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true)
							.apply();
				}

				getActivity().supportInvalidateOptionsMenu(); // calls
																// onPrepareOptionsMenu()
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce
		// them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void selectItem(int position) {
//		mCurrentSelectedPosition = position;
		//TODO.
//		if (mDrawerListView != null) {
//			mDrawerListView.setItemChecked(position, true);
//		}
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if (mCallbacks != null) {
			// TODO. select한  path를 내보
			mCallbacks.onNavigationDrawerItemSelected(position, null);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
//		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
		
        outState.putSerializable("treeManager_internal", mTreeInternal.manager);
        outState.putSerializable("treeManager_external", mTreeExternal.manager);
        
        outState.putInt("treeLevel_internal", mTreeInternal.treeLevel);
        outState.putInt("treeLevel_external", mTreeExternal.treeLevel);
        
        outState.putInt("selected_tree", mTreeExternal.treeLevel);
        
        super.onSaveInstanceState(outState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar.
		// See also
		// showGlobalContextActionBar, which controls the top-left area of the
		// action bar.
		if (mDrawerLayout != null && isDrawerOpen()) {
			inflater.inflate(R.menu.global, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

//		if (item.getItemId() == R.id.action_example) {
//			Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT)
//					.show();
//			return true;
//		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to
	 * show the global app 'context', rather than just what's in the current
	 * screen.
	 */
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.app_name);
	}

	private ActionBar getActionBar() {
		return ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

	/**
	 * Callbacks interface that all activities using this fragment must
	 * implement.
	 */
	public static interface NavigationDrawerCallbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(long id, String path);
	}
}
