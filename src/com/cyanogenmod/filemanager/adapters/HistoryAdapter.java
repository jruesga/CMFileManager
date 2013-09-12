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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.model.History;
import com.cyanogenmod.filemanager.parcelables.NavigationViewInfoParcelable;
import com.cyanogenmod.filemanager.parcelables.SearchInfoParcelable;
import com.cyanogenmod.filemanager.ui.IconHolder;
import com.cyanogenmod.filemanager.ui.ThemeManager;
import com.cyanogenmod.filemanager.ui.ThemeManager.Theme;

import java.util.List;

/**
 * An implementation of {@link ArrayAdapter} for display history.
 */
public class HistoryAdapter extends ArrayAdapter<History> {

    /**
     * A class that conforms with the ViewHolder pattern to performance
     * the list view rendering.
     */
    private static class ViewHolder {
        /**
         * @hide
         */
        public ViewHolder() {
            super();
        }
        ImageView mIvIcon;
        TextView mTvName;
        TextView mTvDirectory;
        TextView mTvPosition;
    }

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
        Drawable mDwIcon;
        String mName;
        String mDirectory;
        String mPosition;
    }



    private DataHolder[] mData;
    private IconHolder mIconHolder;

    //The resource item layout
    private static final int RESOURCE_LAYOUT = R.layout.history_item;

    //The resource of the item icon
    private static final int RESOURCE_ITEM_ICON = R.id.history_item_icon;
    //The resource of the item name
    private static final int RESOURCE_ITEM_NAME = R.id.history_item_name;
    //The resource of the item directory
    private static final int RESOURCE_ITEM_DIRECTORY = R.id.history_item_directory;
    //The resource of the item position
    private static final int RESOURCE_ITEM_POSITION = R.id.history_item_position;

    /**
     * Constructor of <code>HistoryAdapter</code>.
     *
     * @param context The current context
     * @param history The history reference
     */
    public HistoryAdapter(Context context, List<History> history) {
        super(context, RESOURCE_ITEM_NAME, history);
        notifyThemeChanged(); // Reload icons

        //Do cache of the data for better performance
        processData(history);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        processData(null);
        super.notifyDataSetChanged();
    }

    /**
     * Method that dispose the elements of the adapter.
     */
    public void dispose() {
        clear();
        this.mData = null;
        if (mIconHolder != null) {
            mIconHolder.cleanup();
            mIconHolder = null;
        }
    }

    /**
     * Method that process the data before use {@link #getView} method.
     *
     * @param historyData The list of histories (to better performance) or null.
     */
    private void processData(List<History> historyData) {
        this.mData = new DataHolder[getCount()];
        int cc = (historyData == null) ? getCount() : historyData.size();
        for (int i = 0; i < cc; i++) {
            //History info
            History history = (historyData == null) ? getItem(i) : historyData.get(i);

            //Build the data holder
            this.mData[i] = new HistoryAdapter.DataHolder();
            if (history.getItem() instanceof NavigationViewInfoParcelable) {
                this.mData[i].mDwIcon =
                        this.mIconHolder.getDrawable("ic_fso_folder_drawable"); //$NON-NLS-1$
            } else if (history.getItem() instanceof SearchInfoParcelable) {
                this.mData[i].mDwIcon =
                        this.mIconHolder.getDrawable("ic_history_search_drawable"); //$NON-NLS-1$
            }
            this.mData[i].mName = history.getItem().getTitle();
            if (this.mData[i].mName == null || this.mData[i].mName.trim().length() == 0) {
                // Root directory
                this.mData[i].mName = getContext().getString(R.string.root_directory_name);
            }
            this.mData[i].mDirectory = history.getItem().getDescription();
            this.mData[i].mPosition = String.format("#%d", Integer.valueOf(i + 1)); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Check to reuse view
        View v = convertView;
        if (v == null) {
            //Create the view holder
            LayoutInflater li =
                    (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(RESOURCE_LAYOUT, parent, false);
            ViewHolder viewHolder = new HistoryAdapter.ViewHolder();
            viewHolder.mIvIcon = (ImageView)v.findViewById(RESOURCE_ITEM_ICON);
            viewHolder.mTvName = (TextView)v.findViewById(RESOURCE_ITEM_NAME);
            viewHolder.mTvDirectory = (TextView)v.findViewById(RESOURCE_ITEM_DIRECTORY);
            viewHolder.mTvPosition = (TextView)v.findViewById(RESOURCE_ITEM_POSITION);
            v.setTag(viewHolder);

            // Apply the current theme
            Theme theme = ThemeManager.getCurrentTheme(getContext());
            theme.setBackgroundDrawable(
                    getContext(), v, "selectors_deselected_drawable"); //$NON-NLS-1$
            theme.setTextColor(
                    getContext(), viewHolder.mTvName, "text_color"); //$NON-NLS-1$
            theme.setTextColor(
                    getContext(), viewHolder.mTvDirectory, "text_color"); //$NON-NLS-1$
            theme.setTextColor(
                    getContext(), viewHolder.mTvPosition, "text_color"); //$NON-NLS-1$
        }

        //Retrieve data holder
        final DataHolder dataHolder = this.mData[position];

        //Retrieve the view holder
        ViewHolder viewHolder = (ViewHolder)v.getTag();

        //Set the data
        viewHolder.mIvIcon.setImageDrawable(dataHolder.mDwIcon);
        viewHolder.mTvName.setText(dataHolder.mName);
        viewHolder.mTvDirectory.setText(dataHolder.mDirectory);
        viewHolder.mTvPosition.setText(dataHolder.mPosition);

        //Return the view
        return v;
    }

    /**
     * Method that should be invoked when the theme of the app was changed
     */
    public void notifyThemeChanged() {
        if (mIconHolder != null) {
            mIconHolder.cleanup();
        }
        // Empty icon holder (only have folders and search icons)
        this.mIconHolder = new IconHolder(getContext(), false);
    }

}
