/*
 * Copyright (C) 2013 The OmniROM Project
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tk.eatheat.omnisnitch;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public final class TaskDescription {

	final ResolveInfo resolveInfo;

	final private int taskId; // application task id for curating apps

	final private int persistentTaskId; // persistent id

	final private Intent intent; // launch intent for application

	final private String packageName; // used to override animations (see onClick())

	final private CharSequence description;

	private Drawable mIcon; // application package icon

	private CharSequence mLabel; // application package label

	private boolean mLoaded;

	private boolean mKilled;

	public TaskDescription(int _taskId, int _persistentTaskId,
			ResolveInfo _resolveInfo, Intent _intent,
			String _packageName, CharSequence _description) {
		resolveInfo = _resolveInfo;
		intent = _intent;
		taskId = _taskId;
		persistentTaskId = _persistentTaskId;

		description = _description;
		packageName = _packageName;
	}

	public TaskDescription() {
		resolveInfo = null;
		intent = null;
		taskId = -1;
		persistentTaskId = -1;

		description = null;
		packageName = null;
	}

	public void setLoaded(boolean loaded) {
		mLoaded = loaded;
	}

	public boolean isLoaded() {
		return mLoaded;
	}

	public boolean isNull() {
		return resolveInfo == null;
	}

	// mark all these as locked?
	public CharSequence getLabel() {
		return mLabel;
	}

	public void setLabel(CharSequence label) {
		mLabel = label;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public void setIcon(Drawable icon) {
		mIcon = icon;
	}

	public int getTaskId() {
		return taskId;
	}

	public Intent getIntent() {
		return intent;
	}

	public int getPersistentTaskId() {
		return persistentTaskId;
	}

	public String getPackageName() {
		return packageName;
	}

	public boolean isKilled() {
		return mKilled;
	}

	public void setKilled() {
		this.mKilled = true;
	}
}
