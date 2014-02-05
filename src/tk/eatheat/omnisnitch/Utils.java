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
package tk.eatheat.omnisnitch;

import java.util.Iterator;
import java.util.List;

import tk.eatheat.omnisnitch.ui.BitmapFilter;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Utils {

    public static void parseFavorites(String favoriteListString,
            List<String> favoriteList) {
        if (favoriteListString.length() == 0){
            return;
        }

        if (favoriteListString.indexOf("##") == -1){
            favoriteList.add(favoriteListString);
            return;
        }
        String[] split = favoriteListString.split("##");
        for (int i = 0; i < split.length; i++) {
            favoriteList.add(split[i]);
        }
    }

    public static String flattenFavorites(List<String> favoriteList) {
        Iterator<String> nextFavorite = favoriteList.iterator();
        StringBuffer buffer = new StringBuffer();
        while (nextFavorite.hasNext()) {
            String favorite = nextFavorite.next();
            buffer.append(favorite + "##");
        }
        if (buffer.length() != 0) {
            return buffer.substring(0, buffer.length() - 2).toString();
        }
        return buffer.toString();
    }

    public static String getActivityLabel(PackageManager pm, Intent intent) {
        ActivityInfo ai = intent.resolveActivityInfo(pm,
                PackageManager.GET_ACTIVITIES);
        String label = null;

        if (ai != null) {
            label = ai.loadLabel(pm).toString();
            if (label == null) {
                label = ai.name;
            }
        }
        return label;
    }
    
    public static Drawable rotate(Resources resources, Drawable image, int deg) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bmResult = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);
        tempCanvas.rotate(deg, b.getWidth() / 2, b.getHeight() / 2);
        tempCanvas.drawBitmap(b, 0, 0, null);
        return new BitmapDrawable(resources, bmResult);
    }

    public static Drawable resize(Resources resources, Drawable image, int iconSize, int borderSize, float density) {
        int size = (int) (iconSize * density + 0.5f);
        int border = (int) (borderSize * density + 0.5f);

        Bitmap b = ((BitmapDrawable) image).getBitmap();
        int originalHeight = b.getHeight();
        int originalWidth = b.getWidth();

        int l = originalHeight > originalWidth ? originalHeight : originalWidth;
        float factor = (float) size / (float) l;

        int resizedHeight = (int) (originalHeight * factor);
        int resizedWidth = (int) (originalWidth * factor);

        // create a border around the icon
        Bitmap bmResult = Bitmap.createBitmap(resizedHeight + border, resizedWidth + border,
                Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, resizedWidth,
                resizedHeight, false);
        tempCanvas.drawBitmap(bitmapResized, border/2, border/2, null);

        return new BitmapDrawable(resources, bmResult);
    }
    
    public static boolean[] buttonStringToArry(String buttonString){
        String[] splitParts = buttonString.split(",");
        boolean[] buttons = new boolean[splitParts.length];
        for(int i = 0; i < splitParts.length; i++){
            if (splitParts[i].equals("0")){
                buttons[i]=false;
            } else if (splitParts[i].equals("1")){
                buttons[i]=true;
            }
        }
        return buttons;
    }
    
    public static String buttonArrayToString(boolean[] buttons){
        String buttonString = "";
        for(int i = 0; i < buttons.length; i++){
            boolean value = buttons[i];
            if (value){
                buttonString = buttonString + "1,";
            } else {
                buttonString = buttonString + "0,";
            }
        }
        if(buttonString.length() > 0){
            buttonString = buttonString.substring(0, buttonString.length() - 1);
        }
        return buttonString;
    }
    
    public static Bitmap getGlow(Resources resources, String name, int color, Drawable image) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        return BitmapFilter.getSingleton().getGlow(name, color, b);
    }
    
    public static Drawable getGlowDrawable(Resources resources, String name, int color, Drawable image) {
        return new BitmapDrawable(resources, getGlow(resources, name, color, image));
    }
    
    public static Drawable colorize(Resources resources, int color, Drawable image) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        BitmapDrawable b1 = new BitmapDrawable(resources, b);
        b1.setColorFilter(color, Mode.SRC_ATOP);
        return b1;
    }
}
