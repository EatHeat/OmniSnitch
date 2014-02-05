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

import java.util.Arrays;
import java.util.List;

import tk.eatheat.omnisnitch.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CheckboxListDialog extends AlertDialog implements
        DialogInterface.OnClickListener {

    private String[] mListItems;
    private Drawable[] mListImages;
    private boolean[] mCheckedItems;
    private ListView mListView;
    private LayoutInflater mInflater;
    private ArrayAdapter<String> mListAdapter;
    private ApplyRunnable mApplyRunnable;
    private String mTitle;

    public interface ApplyRunnable {
        public void apply(boolean[] buttons);
    };
    private class CheckboxListAdapter extends ArrayAdapter<String> {

        public CheckboxListAdapter(Context context, int resource, List<String> values) {
            super(context, R.layout.checkbox_item, resource, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.checkbox_item, parent, false);

            final TextView item = (TextView)rowView.findViewById(R.id.item_text);
            item.setText(mListItems[position]);

            final CheckBox check = (CheckBox)rowView.findViewById(R.id.item_check);
            check.setChecked(mCheckedItems[position]);

            final ImageView image = (ImageView)rowView.findViewById(R.id.item_image);
            image.setImageDrawable(mListImages[position]);

            return rowView;
        }   
    }

    public CheckboxListDialog(Context context, String[] items, Drawable[] images, boolean[] checked, ApplyRunnable applyRunnable, String title) {
        super(context);
        mTitle = title;
        mApplyRunnable = applyRunnable;
        mListItems = items;
        mListImages = images;
        mCheckedItems = new boolean[checked.length];
        System.arraycopy(checked, 0, mCheckedItems, 0, checked.length);
        
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void applyChanges() {
        mApplyRunnable.apply(mCheckedItems);
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            applyChanges();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = getContext();
        final View view = getLayoutInflater().inflate(
                R.layout.checkbox_list, null);
        setView(view);
        setTitle(mTitle);
        setCancelable(true);

        setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(android.R.string.ok), this);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel), this);

        super.onCreate(savedInstanceState);

        mListView = (ListView) view.findViewById(R.id.item_list);

        mListAdapter = new CheckboxListAdapter(getContext(),
                android.R.layout.simple_list_item_multiple_choice, Arrays.asList(mListItems));
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mCheckedItems[position] = !mCheckedItems[position];
                mListAdapter.notifyDataSetChanged();
            }
        });
    }
}
