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

package com.cyanogenmod.explorer.adapters;

import android.content.Context;
import android.content.res.Resources;

import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Identifiable;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.util.ResourcesHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of {@link CheckableListAdapter} for display settings.<br/>
 * Only 2 type of settings are allowed:
 * <ul>
 * <li>{@link Enum}&lt;{@link Identifiable}&gt;</li>
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
        ExplorerSettings mSetting;
        CheckableListAdapter.CheckableItem mItem;
    }

    private List<DataHolder> mData;

    /**
     * Constructor of <code>MenuSettingsAdapter</code>.
     *
     * @param context The current context
     * @param setting The setting to add to the current list
     */
    public MenuSettingsAdapter(Context context, ExplorerSettings setting) {
        this(context, Arrays.asList(new ExplorerSettings[]{setting}));
        addSetting(context, setting);
    }

    /**
     * Constructor of <code>MenuSettingsAdapter</code>.
     *
     * @param context The current context
     * @param settings An array of setting to add to the current list
     */
    public MenuSettingsAdapter(Context context, List<ExplorerSettings> settings) {
        super(context, new ArrayList<CheckableListAdapter.CheckableItem>());

        //Process the data
        this.mData = new ArrayList<MenuSettingsAdapter.DataHolder>();
        for (int i = 0; i < settings.size(); i++) {
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
     * @return ExplorerSettings The setting
     */
    public ExplorerSettings getSetting(int position) {
        return this.mData.get(position).mSetting;
    }

    /**
     * Method that adds a new setting to the collection of items.<br />
     *
     * @param context The current context
     * @param setting The setting to add to the current list
     */
    private void addSetting(Context context, ExplorerSettings setting) {
        //Only 2 type of settings are allowed
        final Resources res = context.getResources();
        try {
            // Enum<Identifiable>
            if (setting.getDefaultValue() instanceof Enum<?>
                && setting.getDefaultValue() instanceof Identifiable) {
                //Retrieve all the items of the enumeration
                int resid =
                        ResourcesHelper.getIdentifier(res, "array", setting.getId()); //$NON-NLS-1$
                String[] titles = res.getStringArray(resid);
                Method method =
                        setting.getDefaultValue().getClass().getMethod("values"); //$NON-NLS-1$
                Identifiable[] ids = (Identifiable[])method.invoke(null);
                int defaultid = ((Identifiable)setting.getDefaultValue()).getId();
                int selected =
                        Preferences.getSharedPreferences().getInt(setting.getId(), defaultid);
                for (int i = 0; i < ids.length; i++) {
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
            int id, ExplorerSettings setting, String title, boolean selected) {
        DataHolder dataHolder = new DataHolder();
        dataHolder.mId = id;
        dataHolder.mSetting = setting;
        dataHolder.mItem = new CheckableItem(title, true, selected);
        return dataHolder;
    }

}
