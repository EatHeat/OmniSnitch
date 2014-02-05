/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tk.eatheat.omnisnitch.ui;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tk.eatheat.omnisnitch.R;
import tk.eatheat.omnisnitch.SettingsActivity;
import tk.eatheat.omnisnitch.Utils;
import tk.eatheat.omnisnitch.dslv.DragSortController;
import tk.eatheat.omnisnitch.dslv.DragSortListView;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FavoriteDialog extends AlertDialog implements
		DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
	private static final String TAG = "FavoriteDialog";

	private LayoutInflater mInflater;
	private List<Drawable> mFavoriteIcons;
	private List<String> mFavoriteNames;
	private List<String> mFavoriteList;
	private SettingsActivity mContext;
	private FavoriteListAdapter mFavoriteAdapter;
	private DragSortListView mFavoriteConfigList;
	private AlertDialog mAddFavoriteDialog;

	ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);

	public class FavoriteListAdapter extends ArrayAdapter<String> {

		public FavoriteListAdapter(Context context, int resource,
				List<String> values) {
			super(context, R.layout.favorite_app_item, resource, values);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = null;
			rowView = mInflater.inflate(R.layout.favorite_app_item, parent,
					false);
			final TextView item = (TextView) rowView
					.findViewById(R.id.app_item);
			item.setText(mFavoriteNames.get(position));
			final ImageView image = (ImageView) rowView
					.findViewById(R.id.app_icon);
			image.setImageDrawable(mFavoriteIcons.get(position));
			return rowView;
		}
	}

	private class FavoriteDragSortController extends DragSortController {

		public FavoriteDragSortController() {
			super(mFavoriteConfigList, R.id.drag_handle,
					DragSortController.ON_DOWN,
					DragSortController.FLING_RIGHT_REMOVE);
			setRemoveEnabled(true);
			setSortEnabled(true);
			setBackgroundColor(0x363636);
		}

		@Override
		public void onDragFloatView(View floatView, Point floatPoint,
				Point touchPoint) {
			floatView.setLayoutParams(params);
			mFavoriteConfigList.setFloatAlpha(0.8f);
		}

		@Override
		public View onCreateFloatView(int position) {
			View v = mFavoriteAdapter.getView(position, null,
					mFavoriteConfigList);
			v.setLayoutParams(params);
			return v;
		}

		@Override
		public void onDestroyFloatView(View floatView) {
		}
	}

	public FavoriteDialog(SettingsActivity context, List<String> favoriteList) {
		super(context);
		mContext = context;
		mFavoriteList = favoriteList;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final Context context = getContext();
		final View view = getLayoutInflater().inflate(R.layout.favorite_dialog,
				null);
		setView(view);
		setTitle(R.string.favorite_apps_config_title);
		setCancelable(true);

		setButton(DialogInterface.BUTTON_POSITIVE,
				context.getString(android.R.string.ok), this);
		setButton(DialogInterface.BUTTON_NEUTRAL,
				context.getString(R.string.favorite_add), this);
		setButton(DialogInterface.BUTTON_NEGATIVE,
				context.getString(android.R.string.cancel), this);

		super.onCreate(savedInstanceState);

		mFavoriteConfigList = (DragSortListView) view
				.findViewById(R.id.favorite_apps);
		mFavoriteAdapter = new FavoriteListAdapter(mContext,
				android.R.layout.simple_list_item_single_choice, mFavoriteList);
		mFavoriteConfigList.setAdapter(mFavoriteAdapter);

		final DragSortController dragSortController = new FavoriteDragSortController();
		mFavoriteConfigList.setFloatViewManager(dragSortController);
		mFavoriteConfigList
				.setDropListener(new DragSortListView.DropListener() {
					@Override
					public void drop(int from, int to) {
						String intent = mFavoriteList.remove(from);
						mFavoriteList.add(to, intent);
						updateFavorites(mFavoriteList);
						mFavoriteAdapter.notifyDataSetChanged();
					}
				});
		mFavoriteConfigList
				.setRemoveListener(new DragSortListView.RemoveListener() {
					@Override
					public void remove(int which) {
						mFavoriteList.remove(which);
						updateFavorites(mFavoriteList);
						mFavoriteAdapter.notifyDataSetChanged();
					}
				});
		mFavoriteConfigList.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return dragSortController.onTouch(view, motionEvent);
			}
		});
		mFavoriteConfigList.setItemsCanFocus(false);
		updateFavorites(mFavoriteList);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Button neutralButton = getButton(DialogInterface.BUTTON_NEUTRAL);
		neutralButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddFavoriteDialog();
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mAddFavoriteDialog != null) {
			mAddFavoriteDialog.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mAddFavoriteDialog != null) {
			mAddFavoriteDialog = null;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			mContext.applyChanges(mFavoriteList);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			cancel();
		}
	}

	private void showAddFavoriteDialog() {
		if (mAddFavoriteDialog != null && mAddFavoriteDialog.isShowing()) {
			return;
		}

		mAddFavoriteDialog = new AddFavoriteDialog(getContext());
		mAddFavoriteDialog.setOnDismissListener(this);
		mAddFavoriteDialog.show();
	}

	private void updateFavorites(List<String> favoriteList) {
		final PackageManager pm = mContext.getPackageManager();
		mFavoriteIcons = new ArrayList<Drawable>();
		mFavoriteNames = new ArrayList<String>();
		Iterator<String> nextFavorite = favoriteList.iterator();
		while (nextFavorite.hasNext()) {
			String favorite = nextFavorite.next();
			Intent intent = null;
			try {
				intent = Intent.parseUri(favorite, 0);
				mFavoriteIcons.add(pm.getActivityIcon(intent));
			} catch (NameNotFoundException e) {
				Log.e(TAG, "NameNotFoundException: [" + favorite + "]");
				continue;
			} catch (URISyntaxException e) {
				Log.e(TAG, "URISyntaxException: [" + favorite + "]");
				continue;
			}
			String label = Utils.getActivityLabel(pm, intent);
			if (label == null) {
				label = favorite;
			}
			mFavoriteNames.add(label);
		}
	}

	public void applyChanges(List<String> favoriteList) {
		mFavoriteList.clear();
		mFavoriteList.addAll(favoriteList);
		updateFavorites(mFavoriteList);
		mFavoriteAdapter.notifyDataSetChanged();
	}

	private class AddFavoriteDialog extends AlertDialog implements
			DialogInterface.OnClickListener {

		private PackageAdapter mPackageAdapter;
		private List<String> mChangedFavoriteList;
		private List<PackageItem> mInstalledPackages;
		private ListView mListView;

		private class PackageItem implements Comparable<PackageItem> {
			CharSequence title;
			String packageName;
			Drawable icon;
			String intent;

			@Override
			public int compareTo(PackageItem another) {
				int result = title.toString().compareToIgnoreCase(
						another.title.toString());
				return result != 0 ? result : packageName
						.compareTo(another.packageName);
			}
		}
		
	    private Drawable getDefaultActivityIcon() {
	        return mContext.getResources().getDrawable(R.drawable.ic_default);
	    }

		private class PackageAdapter extends BaseAdapter {

			private void reloadList() {
				final PackageManager pm = mContext.getPackageManager();

				mInstalledPackages.clear();

				final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> installedAppsInfo = pm.queryIntentActivities(
						mainIntent, 0);

				for (ResolveInfo info : installedAppsInfo) {
					ApplicationInfo appInfo = info.activityInfo.applicationInfo;

					final PackageItem item = new PackageItem();
					item.packageName = appInfo.packageName;

					ActivityInfo activity = info.activityInfo;
					ComponentName name = new ComponentName(
							activity.applicationInfo.packageName, activity.name);
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					intent.setComponent(name);
					item.intent = intent.toUri(0);
					try {
						item.icon = pm.getActivityIcon(intent);
					} catch (NameNotFoundException e) {
						continue;
					}
					item.title = Utils.getActivityLabel(pm, intent);
					if (item.title == null) {
						item.title = appInfo.loadLabel(pm);
					}
					if (item.icon == null) {
					    item.icon = getDefaultActivityIcon();
					}
					mInstalledPackages.add(item);
				}
				Collections.sort(mInstalledPackages);
			}

			public PackageAdapter() {
				reloadList();
			}

			@Override
			public int getCount() {
				return mInstalledPackages.size();
			}

			@Override
			public PackageItem getItem(int position) {
				return mInstalledPackages.get(position);
			}

			@Override
			public long getItemId(int position) {
				// intent is guaranteed to be unique in mInstalledPackages
				return mInstalledPackages.get(position).intent.hashCode();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				if (convertView != null) {
					holder = (ViewHolder) convertView.getTag();
				} else {
					convertView = getLayoutInflater().inflate(
							R.layout.installed_app_item, parent, false);
					holder = new ViewHolder();
					convertView.setTag(holder);

					holder.item = (TextView) convertView
							.findViewById(R.id.app_item);
					holder.check = (CheckBox) convertView
							.findViewById(R.id.app_check);
					holder.image = (ImageView) convertView
							.findViewById(R.id.app_icon);
				}
				PackageItem applicationInfo = getItem(position);
				holder.item.setText(applicationInfo.title);
				holder.image.setImageDrawable(applicationInfo.icon);
				holder.check.setChecked(mChangedFavoriteList
						.contains(applicationInfo.intent));

				Log.d(TAG, "add " + applicationInfo.title);
				return convertView;
			}
		}

		private class ViewHolder {
			TextView item;
			CheckBox check;
			ImageView image;
		}

		protected AddFavoriteDialog(Context context) {
			super(context);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				applyChanges(mChangedFavoriteList);
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				cancel();
			}
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			final Context context = getContext();
			final View view = getLayoutInflater().inflate(
					R.layout.installed_apps_dialog, null);
			setView(view);
			setTitle(R.string.favorite_apps_add_dialog_title);
			setCancelable(true);

			setButton(DialogInterface.BUTTON_POSITIVE,
					context.getString(android.R.string.ok), this);
			setButton(DialogInterface.BUTTON_NEGATIVE,
					context.getString(android.R.string.cancel), this);

			super.onCreate(savedInstanceState);
			mChangedFavoriteList = new ArrayList<String>();
			mChangedFavoriteList.addAll(mFavoriteList);

			mListView = (ListView) view.findViewById(R.id.installed_apps);
			mInstalledPackages = new LinkedList<PackageItem>();
			mPackageAdapter = new PackageAdapter();
			mListView.setAdapter(mPackageAdapter);
			mListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					PackageItem info = (PackageItem) parent
							.getItemAtPosition(position);
					ViewHolder viewHolder = (ViewHolder) view.getTag();
					viewHolder.check.setChecked(!viewHolder.check.isChecked());
					if (viewHolder.check.isChecked()) {
						if (!mChangedFavoriteList.contains(info.intent)) {
							mChangedFavoriteList.add(info.intent);
						}
					} else {
						mChangedFavoriteList.remove(info.intent);
					}
				}
			});
		}
	}
}
