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
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.R;

import java.io.File;
import java.lang.reflect.Method;


/**
 * A helper class with useful methods for deal with storages.
 */
public final class StorageHelper {

    /**
     * Method that returns the storage volumes defined in the system.  This method uses
     * reflection to retrieve the method because CM10 has a {@link Context}
     * as first parameter, that AOSP hasn't.
     * 
     * @param ctx The current context
     * @return StorageVolume[] The storage volumes defined in the system
     */
    public static StorageVolume[] getStorageVolumes(Context ctx) {
        //IMP!! Android SDK doesn't have a "getVolumeList" but is supported by CM10.
        //Use reflect to get this value (if possible)
        try {
            StorageManager sm = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
            Method method = sm.getClass().getMethod("getVolumeList"); //$NON-NLS-1$
            return (StorageVolume[])method.invoke(sm);
        } catch (Exception ex) {
            //Ignore. Android SDK StorageManager class doesn't have this method
            //Use default android information from environment
            try {
                File externalStorage = Environment.getExternalStorageDirectory();
                String path = externalStorage.getCanonicalPath();
                String description = null;
                if (path.toLowerCase().indexOf("usb") != -1) { //$NON-NLS-1$
                    description = ctx.getString(R.string.usb_storage);
                } else {
                    description = ctx.getString(R.string.external_storage);
                }
                StorageVolume sv =new StorageVolume(path, description, false, false, 0, false, 0);
                return new StorageVolume[]{sv};
            } catch (Exception ex2) {
                /**NON BLOCK**/
            }
        }
        return new StorageVolume[]{};
    }

    /**
     * Method that returns the storage volume description. This method uses
     * reflection to retrieve the description because CM10 has a {@link Context}
     * as first parameter, that AOSP hasn't.
     *
     * @param ctx The current context
     * @param volume The storage volume
     * @return String The description of the storage volume
     */
    public static String getStorageVolumeDescription(Context ctx, StorageVolume volume) {
        try {
            Method method = volume.getClass().getMethod(
                                            "getDescription", //$NON-NLS-1$
                                            new Class[]{Context.class});
            if (method == null) {
                // AOSP
                method = volume.getClass().getMethod("getDescription"); //$NON-NLS-1$
                return (String)method.invoke(volume);
            }

            // CM10
            return (String)method.invoke(volume, ctx);

        } catch (Throwable _throw) {
            // Returns the volume storage path
            return volume.getPath();
        }
    }

    /**
     * Method that returns if the path is in a volume storage
     * 
     * @param path The path
     * @return boolean If the path is in a volume storage
     */
    public static boolean isPathInStorageVolume(String path) {
        StorageVolume[] volumes =
                getStorageVolumes(ExplorerApplication.getInstance().getApplicationContext());
        for (int i=0; i < volumes.length; i++) {
            StorageVolume vol = volumes[i];
            if (path.startsWith(vol.getPath())) {
                return true;
            }
        }
        return false;
    }

}
