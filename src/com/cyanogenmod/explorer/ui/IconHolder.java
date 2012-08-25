/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.explorer.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that holds icons for a more efficient access.
 */
public class IconHolder {

    private final Map<Integer, Drawable> mIcons;

    /**
     * Constructor of <code>IconHolder</code>.
     */
    public IconHolder() {
        super();
        this.mIcons = new HashMap<Integer, Drawable>();
    }

    /**
     * Method that loads, cache and returns a drawable reference
     * of a icon.
     *
     * @param context The current context
     * @param resid The resource identifier
     * @return Drawable The drawable icon reference
     */
    public Drawable getDrawable(Context context, final int resid) {
        //Check if the icon exists in the cache
        Integer id = new Integer(resid);
        if (this.mIcons.containsKey(id)) {
            return this.mIcons.get(id);
        }

        //Load the drawable, cache and returns reference
        Resources res = context.getResources();
        Drawable dw = res.getDrawable(resid);
        this.mIcons.put(id, dw);
        return dw;
    }

}
