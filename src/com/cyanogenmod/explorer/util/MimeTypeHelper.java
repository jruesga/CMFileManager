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

package com.cyanogenmod.explorer.util;

import android.content.Context;
import android.util.Log;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.model.Directory;
import com.cyanogenmod.explorer.model.FileSystemObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A helper class with useful methods for deal with mime types.
 */
public final class MimeTypeHelper {

    private static final String TAG = "MimeTypeHelper"; //$NON-NLS-1$

    private static Properties sMimeTypes;
    private static Map<String, Integer> sCachedIndentifiers =
            new HashMap<String, Integer>();

    /**
     * Constructor of <code>MimeTypeHelper</code>.
     */
    private MimeTypeHelper() {
        super();
    }

    /**
     * Method that returns the associated icon.
     *
     * @param context The current context
     * @param fso The file system object
     * @return The associated icon
     */
    public static int getIcon(Context context, FileSystemObject fso) {
        //Ensure that mime types are loaded
        if (sMimeTypes == null) {
            loadMimeTypes(context);
        }

        //Check if the argument is a folder
        if (fso instanceof Directory || FileHelper.isSymlinkRefDirectory(fso)) {
            return R.drawable.ic_fso_folder;
        }

        //Get the extension and delivery
        String ext = FileHelper.getExtension(fso);
        if (ext != null) {
            //Search the identifier in the cache
            if (sCachedIndentifiers.containsKey(ext)) {
                return sCachedIndentifiers.get(ext).intValue();
            }

            //Load from the raw mime type file
            String mimeTypeInfo = sMimeTypes.getProperty(ext);
            if (mimeTypeInfo != null) {
                String[] mime = mimeTypeInfo.split("\\|");  //$NON-NLS-1$
                int resId = ResourcesHelper.getIdentifier(
                        context.getResources(), "drawable", mime[1].trim()); //$NON-NLS-1$
                if (resId != 0) {
                    sCachedIndentifiers.put(ext, new Integer(resId));
                    return resId;
                }
            }
        }
        return R.drawable.ic_fso_default;
    }

    /**
     * Method that loads the mime type information.
     *
     * @param context The current context
     */
    //IMP! This must be invoked from the main activity creation
    public static synchronized void loadMimeTypes(Context context) {
        if (sMimeTypes == null) {
            try {
                sMimeTypes = new Properties();
                sMimeTypes.load(context.getResources().openRawResource(R.raw.mime_types));
            } catch (Exception e) {
                Log.e(TAG, "Fail to load mime types raw file.", e); //$NON-NLS-1$
            }
        }
    }

}
