/*
 * Copyright (C) 2010 Daniel Nilsson
 * Copyright (C) 2012 THe CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.eatheat.omnisnitch.colorpicker;

import java.util.Locale;

import tk.eatheat.omnisnitch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ColorPickerDialog extends AlertDialog implements
        ColorPickerView.OnColorChangedListener, TextWatcher,
        OnFocusChangeListener {

    private ColorPickerView mColorPicker;
    private EditText mHexColorInput;
    private ColorPanelView mNewColor;
    private LayoutInflater mInflater;

    /**
     * @param context
     * @param initialColor
     */
    public ColorPickerDialog(Context context, int initialColor) {
        super(context);

        init(initialColor);
    }

    private void init(int color) {
        // To fight color banding.
        getWindow().setFormat(PixelFormat.RGBA_8888);
        setUp(color);
    }

    /**
     * This function sets up the dialog with the proper values.
     * 
     * @param color
     *            - the color to set
     */
    private void setUp(int color) {
        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.dialog_color_picker, null);

        mColorPicker = (ColorPickerView) layout
                .findViewById(R.id.color_picker_view);
        mHexColorInput = (EditText) layout.findViewById(R.id.hex_color_input);
        mNewColor = (ColorPanelView) layout.findViewById(R.id.color_panel);

        mColorPicker.setOnColorChangedListener(this);
        mColorPicker.setColor(color, true);

        mHexColorInput.setOnFocusChangeListener(this);

        setView(layout);
        setTitle(R.string.drag_handle_color_dialog_title);
    }

    @Override
    public void onColorChanged(int color) {
        final boolean hasAlpha = mColorPicker.isAlphaSliderVisible();
        final String format = hasAlpha ? "%08x" : "%06x";
        final int mask = hasAlpha ? 0xFFFFFFFF : 0x00FFFFFF;

        mNewColor.setColor(color);
        mHexColorInput.setText(String.format(Locale.US, format, color & mask));
    }

    public void setAlphaSliderVisible(boolean visible) {
        mHexColorInput
                .setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                        visible ? 8 : 6) });
        mColorPicker.setAlphaSliderVisible(visible);
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String hexColor = mHexColorInput.getText().toString();
        if (!hexColor.isEmpty()) {
            try {
                int color = Color.parseColor('#' + hexColor);
                if (!mColorPicker.isAlphaSliderVisible()) {
                    color |= 0xFF000000; // set opaque
                }
                mColorPicker.setColor(color);
                mNewColor.setColor(color);
            } catch (IllegalArgumentException ex) {
                // Number format is incorrect, ignore
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            mHexColorInput.removeTextChangedListener(this);
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } else {
            mHexColorInput.addTextChangedListener(this);
        }
    }
}
