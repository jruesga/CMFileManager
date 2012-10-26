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

package com.cyanogenmod.filemanager.adapters;

import android.content.Context;
import android.content.res.Resources;

import com.cyanogenmod.filemanager.preferences.FileManagerSettings;
import com.cyanogenmod.filemanager.preferences.ObjectIdentifier;
import com.cyanogenmod.filemanager.preferences.ObjectStringIdentifier;
import com.cyanogenmod.filemanager.preferences.Preferences;
import com.cyanogenmod.filemanager.util.ResourcesHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of {@link CheckableListAdapter} for display settings.<br/>
 * Only 2 type of settings are allowed:
 * <ul>
 * <li>{@link Enum}&lt;{@link ObjectIdentifier}&gt;</li>
 * <li>{@link Boolean}</li>
 * </ul>
 *
 * @see CheckableListAdapter
 * @see CheckableListAdapter.CheckableItem
 */
public class MenuSettingsAdapter extends CheckableListAdapter {

    /**
     * A class that holds the full data information.
     */
    private static class DataHolder {
        /**
         * @hide
         */
        public DataHolder() {
            super();
        }
        int mId;
        FileManagerSettings mSetting;
        CheckableListAdapter.CheckableItem mItem;
    }

    private List<DataHolder> mData;

    /**
     * Constructor of <code>MenuSettingsAdapter</code>.
     *
     * @param context The current context
     * @param setting The setting to add to the current list
     */
    public MenuSettingsAdapter(Context context, FileManagerSettings setting) {
        this(context, Arrays.asList(new FileManagerSettings[]{setting}));
        addSetting(context, setting);
    }

    /**
     * Constructor of <code>MenuSettingsAdapter</code>.
     *
     * @param context The current context
     * @param settings An array of setting to add to the current list
     */
    public MenuSettingsAdapter(Context context, List<FileManagerSettings> settings) {
        super(context, new ArrayList<CheckableListAdapter.CheckableItem>());

        //Process the data
        this.mData = new ArrayList<MenuSettingsAdapter.DataHolder>();
        int cc = settings.size();
        for (int i = 0; i < cc; i++) {
            addSetting(context, settings.get(i));
        }
    }

    /**
     * Method that dispose the elements of the adapter.
     */
    @Override
    public void dispose() {
        this.mData = null;
        super.dispose();
    }

    /**
     * Method that returns the identifier of the setting.
     *
     * @param position The position of the item
     * @return int The identifier of the setting
     */
    @Override
    public int getId(int position) {
        return this.mData.get(position).mId;
    }

    /**
     * Method that returns the setting.
     *
     * @param position The position of the item
     * @return FileManagerSettings The setting
     */
    public FileManagerSettings getSetting(int position) {
        return this.mData.get(position).mSetting;
    }

    /**
     * Method that adds a new setting to the collection of items.<br />
     *
     * @param context The current context
     * @param setting The setting to add to the current list
     */
    private void addSetting(Context context, FileManagerSettings setting) {
        //Only 2 type of settings are allowed
        final Resources res = context.getResources();
        try {
            // Enum<ObjectIdentifier>
            if (setting.getDefaultValue() instanceof Enum<?>
                && setting.getDefaultValue() instanceof ObjectIdentifier) {
                //Retrieve all the items of the enumeration
                int resid =
                        ResourcesHelper.getIdentifier(res, "array", setting.getId()); //$NON-NLS-1$
                String[] titles = res.getStringArray(resid);
                Method method =
                        setting.getDefaultValue().getClass().getMethod("values"); //$NON-NLS-1$
                ObjectIdentifier[] ids = (ObjectIdentifier[])method.invoke(null);
                int defaultid = ((ObjectIdentifier)setting.getDefaultValue()).getId();
                int selected =
                        Preferences.getSharedPreferences().getInt(setting.getId(), defaultid);
                int cc = ids.length;
                for (int i = 0; i < cc; i++) {
                    //Create the data holder
                    DataHolder dataHolder =
                            createDataHolder(
                                    ids[i].getId(),
                                    setting,
                                    titles[i],
                                    ids[i].getId() == selected);
                    this.mData.add(dataHolder);

                    //Add to the list
                    add(dataHolder.mItem);
                }
                return;
            }

            // Enum<ObjectStringIdentifier>
            if (setting.getDefaultValue() instanceof Enum<?>
                && setting.getDefaultValue() instanceof ObjectStringIdentifier) {
                //Retrieve all the items of the enumeration
                int resid =
                        ResourcesHelper.getIdentifier(res, "array", setting.getId()); //$NON-NLS-1$
                String[] titles = res.getStringArray(resid);
                Method method =
                        setting.getDefaultValue().getClass().getMethod("values"); //$NON-NLS-1$
                ObjectStringIdentifier[] ids = (ObjectStringIdentifier[])method.invoke(null);
                String defaultid = ((ObjectStringIdentifier)setting.getDefaultValue()).getId();
                String selected =
                        Preferences.getSharedPreferences().getString(setting.getId(), defaultid);
                int cc = ids.length;
                for (int i = 0; i < cc; i++) {
                    //Create the data holder
                    DataHolder dataHolder =
                            createDataHolder(
                                    i,
                                    setting,
                                    titles[i],
                                    ids[i].getId() == selected);
                    this.mData.add(dataHolder);

                    //Add to the list
                    add(dataHolder.mItem);
                }
                return;
            }

            // Boolean
            if (setting.getDefaultValue() instanceof Boolean) {
                int resid =
                        ResourcesHelper.getIdentifier(
                                res, "string", setting.getId()); //$NON-NLS-1$
                String title = res.getString(resid);
                boolean selected =
                        Preferences.getSharedPreferences().
                            getBoolean(
                                setting.getId(),
                                ((Boolean)setting.getDefaultValue()).booleanValue());

                //Create the data holder
                DataHolder dataHolder = createDataHolder(-1, setting, title, selected);
                this.mData.add(dataHolder);

                //Add to the list
                add(dataHolder.mItem);
                return;
            }
        } catch (Exception e) {
            /**NON BLOCK**/
        }
        //Not allowed
        throw new IllegalArgumentException();
    }

    /**
     * Method that creates a data holder.
     *
     * @param id The identifier of the setting
     * @param title The title
     * @param setting The setting
     * @param selected If the setting is selected
     * @return DataHolder The holder with the data
     */
    @SuppressWarnings("static-method")
    private DataHolder createDataHolder(
            int id, FileManagerSettings setting, String title, boolean selected) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.mId = id;
        dataHolder.mSetting = setting;
        dataHolder.mItem = new CheckableItem(title, true, selected);
        return dataHolder;
    }

}
