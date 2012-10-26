/**
 * 
 */
package com.cyanogenmod.filemanager.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ViewConfiguration;

/**
 * A helper class with useful methods for deal with android.
 */
public final class AndroidHelper {

    /**
     * Method that returns if the device is a tablet
     * 
     * @param ctx The current context
     * @return boolean If device is a table
     */
    public static boolean isTablet(Context ctx) {
        Configuration configuration = ctx.getResources().getConfiguration();
        return (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    
    /**
    * Method that returns if an option menu has to be displayed
    *
    * @param ctx The current context
    * @return boolean If an option menu has to be displayed
    */
    public static boolean showOptionsMenu(Context ctx) {
        // Show overflow button?
        return !ViewConfiguration.get(ctx).hasPermanentMenuKey();
    }

}
