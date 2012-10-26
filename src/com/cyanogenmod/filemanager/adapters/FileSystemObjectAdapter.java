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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.ParentDirectory;
import com.cyanogenmod.filemanager.ui.IconHolder;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link ArrayAdapter} for display file system objects.
 */
public class FileSystemObjectAdapter
    extends ArrayAdapter<FileSystemObject> implements OnClickListener {

    /**
     * An interface to communicate selection changes events.
     */
    public interface OnSelectionChangedListener {
        /**
         * Method invoked when the selection changed.
         *
         * @param selectedItems The new selected items
         */
        void onSelectionChanged(List<FileSystemObject> selectedItems);
    }

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
        ImageButton mBtCheck;
        ImageView mIvIcon;
        TextView mTvName;
        TextView mTvSummary;
        TextView mTvSize;
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
        boolean mSelected;
        Drawable mDwCheck;
        Drawable mDwIcon;
        String mName;
        String mSummary;
        String mSize;
    }


    private DataHolder[] mData;
    private IconHolder mIconHolder;
    private final int mItemViewResourceId;
    private List<FileSystemObject> mSelectedItems;
    private final boolean mPickable;

    private OnSelectionChangedListener mOnSelectionChangedListener;

    //The resource of the item check
    private static final int RESOURCE_ITEM_CHECK = R.id.navigation_view_item_check;
    //The resource of the item icon
    private static final int RESOURCE_ITEM_ICON = R.id.navigation_view_item_icon;
    //The resource of the item name
    private static final int RESOURCE_ITEM_NAME = R.id.navigation_view_item_name;
    //The resource of the item summary information
    private static final int RESOURCE_ITEM_SUMMARY = R.id.navigation_view_item_summary;
    //The resource of the item size information
    private static final int RESOURCE_ITEM_SIZE = R.id.navigation_view_item_size;

    /**
     * Constructor of <code>FileSystemObjectAdapter</code>.
     *
     * @param context The current context
     * @param files The list of file system objects
     * @param itemViewResourceId The identifier of the layout that represents an item
     * of the list adapter
     * @param pickable If the adapter should act as a pickable browser.
     */
    public FileSystemObjectAdapter(
            Context context, List<FileSystemObject> files,
            int itemViewResourceId, boolean pickable) {
        super(context, RESOURCE_ITEM_NAME, files);
        this.mIconHolder = new IconHolder();
        this.mItemViewResourceId = itemViewResourceId;
        this.mSelectedItems = new ArrayList<FileSystemObject>();
        this.mPickable = pickable;

        //Do cache of the data for better performance
        loadDefaultIcons();
        processData();
    }

    /**
     * Method that sets the listener which communicates selection changes.
     *
     * @param onSelectionChangedListener The listener reference
     */
    public void setOnSelectionChangedListener(
            OnSelectionChangedListener onSelectionChangedListener) {
        this.mOnSelectionChangedListener = onSelectionChangedListener;
    }

    /**
     * Method that loads the default icons (known icons and more common icons).
     */
    private void loadDefaultIcons() {
        this.mIconHolder.getDrawable(getContext(), R.drawable.btn_holo_light_check_on_normal);
        this.mIconHolder.getDrawable(getContext(), R.drawable.btn_holo_light_check_off_normal);
        this.mIconHolder.getDrawable(getContext(), R.drawable.ic_fso_default);
        this.mIconHolder.getDrawable(getContext(), R.drawable.ic_fso_folder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        processData();
        super.notifyDataSetChanged();
    }

    /**
     * Method that dispose the elements of the adapter.
     */
    public void dispose() {
        clear();
        this.mData = null;
        this.mIconHolder = null;
        this.mSelectedItems.clear();
    }

    /**
     * Method that returns the {@link FileSystemObject} reference from his path.
     *
     * @param path The path of the file system object
     * @return FileSystemObject The file system object reference
     */
    public FileSystemObject getItem(String path) {
        int cc = getCount();
        for (int i = 0; i < cc; i++) {
          //File system object info
            FileSystemObject fso = getItem(i);
            if (fso.getFullPath().compareTo(path) == 0) {
                return fso;
            }
        }
        return null;
    }

    /**
     * Method that process the data before use {@link #getView} method.
     */
    private void processData() {
        Resources res = getContext().getResources();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        this.mData = new DataHolder[getCount()];
        int cc = getCount();
        for (int i = 0; i < cc; i++) {
            //File system object info
            FileSystemObject fso = getItem(i);

            //Parse the last modification time and permissions
            StringBuilder sbSummary = new StringBuilder();
            if (fso instanceof ParentDirectory) {
                sbSummary.append(res.getString(R.string.parent_dir));
            } else {
                sbSummary.append(df.format(fso.getLastModifiedTime()));
                sbSummary.append("   "); //$NON-NLS-1$
                sbSummary.append(fso.toRawString());
            }

            //Build the data holder
            this.mData[i] = new FileSystemObjectAdapter.DataHolder();
            this.mData[i].mSelected = this.mSelectedItems.contains(fso);
            this.mData[i].mDwCheck = (this.mData[i].mSelected)
                    ? this.mIconHolder.getDrawable(
                            getContext(), R.drawable.btn_holo_light_check_on_normal)
                    : this.mIconHolder.getDrawable(
                            getContext(), R.drawable.btn_holo_light_check_off_normal);
            this.mData[i].mDwIcon = this.mIconHolder.getDrawable(
                    getContext(),
                    MimeTypeHelper.getIcon(getContext(), fso));
            this.mData[i].mName = fso.getName();
            this.mData[i].mSummary = sbSummary.toString();
            this.mData[i].mSize = FileHelper.getHumanReadableSize(fso);

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
            v = li.inflate(this.mItemViewResourceId, parent, false);
            ViewHolder viewHolder = new FileSystemObjectAdapter.ViewHolder();
            viewHolder.mIvIcon = (ImageView)v.findViewById(RESOURCE_ITEM_ICON);
            viewHolder.mTvName = (TextView)v.findViewById(RESOURCE_ITEM_NAME);
            viewHolder.mTvSummary = (TextView)v.findViewById(RESOURCE_ITEM_SUMMARY);
            viewHolder.mTvSize = (TextView)v.findViewById(RESOURCE_ITEM_SIZE);
            if (!this.mPickable) {
                viewHolder.mBtCheck = (ImageButton)v.findViewById(RESOURCE_ITEM_CHECK);
                viewHolder.mBtCheck.setOnClickListener(this);
            } else {
                viewHolder.mBtCheck = (ImageButton)v.findViewById(RESOURCE_ITEM_CHECK);
                viewHolder.mBtCheck.setVisibility(View.GONE);
            }
            v.setTag(viewHolder);
        }

        //Retrieve data holder
        final DataHolder dataHolder = this.mData[position];

        //Retrieve the view holder
        ViewHolder viewHolder = (ViewHolder)v.getTag();

        //Set the data
        viewHolder.mIvIcon.setImageDrawable(dataHolder.mDwIcon);
        viewHolder.mTvName.setText(dataHolder.mName);
        if (viewHolder.mTvSummary != null) {
            viewHolder.mTvSummary.setText(dataHolder.mSummary);
        }
        if (viewHolder.mTvSize != null) {
            viewHolder.mTvSize.setText(dataHolder.mSize);
        }
        if (!this.mPickable) {
            viewHolder.mBtCheck.setVisibility(
                    dataHolder.mName.compareTo(
                            FileHelper.PARENT_DIRECTORY) == 0 ? View.INVISIBLE : View.VISIBLE);
            viewHolder.mBtCheck.setImageDrawable(dataHolder.mDwCheck);
            viewHolder.mBtCheck.setTag(Integer.valueOf(position));
            v.setBackgroundResource(
                    dataHolder.mSelected
                        ? R.drawable.holo_list_selector_selected
                        : R.drawable.holo_list_selector_deseleted);
        }

        //Return the view
        return v;
    }

    /**
     * Method that returns if the item of the passed position is selected.
     *
     * @param position The position of the item
     * @return boolean If the item of the passed position is selected
     */
    public boolean isSelected(int position) {
        return this.mData[position].mSelected;
    }

    /**
     * Method that selects in the {@link ArrayAdapter} the passed item.
     *
     * @param item The view to select
     */
    public void toggleSelection(View item) {
        ImageButton view = (ImageButton)item.findViewById(RESOURCE_ITEM_CHECK);
        onClick(view);
    }

    /**
     * Method that selects in the {@link ArrayAdapter} the passed item.
     *
     * @param fso The file system object to select
     */
    public void toggleSelection(FileSystemObject fso) {
        toggleSelection(null, fso);
    }

    /**
     * Method that selects in the {@link ArrayAdapter} the passed item.
     *
     * @param v The check view object (can be null)
     * @param fso The file system object to select
     */
    private void toggleSelection(View v, FileSystemObject fso) {
        if (this.mData != null) {
            int cc = this.mData.length;
            for (int i = 0; i < cc; i++) {
                DataHolder data = this.mData[i];
                if (data.mName.compareTo(fso.getName()) == 0) {
                    //Select/Deselect the item
                    data.mSelected = !data.mSelected;
                    if (v != null) {
                        ((View)v.getParent()).setSelected(data.mSelected);
                    }
                    data.mDwCheck = data.mSelected
                            ? this.mIconHolder.getDrawable(
                                    getContext(), R.drawable.btn_holo_light_check_on_normal)
                            : this.mIconHolder.getDrawable(
                                    getContext(), R.drawable.btn_holo_light_check_off_normal);
                    if (v != null) {
                        ((ImageView)v).setImageDrawable(data.mDwCheck);
                        ((View)v.getParent()).setBackgroundResource(
                                data.mSelected
                                        ? R.drawable.holo_list_selector_selected
                                        : R.drawable.holo_list_selector_deseleted);
                    }
                    notifyDataSetInvalidated();

                    //Add or remove from the global selected items
                    if (data.mSelected) {
                        FileSystemObjectAdapter.this.mSelectedItems.add(fso);
                    } else {
                        FileSystemObjectAdapter.this.mSelectedItems.remove(fso);
                    }

                    //Communicate event
                    if (this.mOnSelectionChangedListener != null) {
                        List<FileSystemObject> selection =
                                new ArrayList<FileSystemObject>(
                                        FileSystemObjectAdapter.this.mSelectedItems);
                        this.mOnSelectionChangedListener.onSelectionChanged(selection);
                    }

                    //Found
                    return;
                }
            }
        }
    }

    /**
     * Method that deselect all items.
     */
    public void deselectedAll() {
        this.mSelectedItems.clear();
        doSelectDeselectAllVisibleItems(false);
    }

    /**
     * Method that select all visible items.
     */
    public void selectedAllVisibleItems() {
        doSelectDeselectAllVisibleItems(true);
    }

    /**
     * Method that deselect all visible items.
     */
    public void deselectedAllVisibleItems() {
        doSelectDeselectAllVisibleItems(false);
    }

    /**
     * Method that select/deselect all items.
     *
     * @param select Indicates if select (true) or deselect (false) all items.
     */
    private void doSelectDeselectAllVisibleItems(boolean select) {
        if (this.mData != null && this.mData.length > 0) {
            int cc = this.mData.length;
            for (int i = 0; i < cc; i++) {
                DataHolder data = this.mData[i];
                if (data.mName.compareTo(FileHelper.PARENT_DIRECTORY) == 0) {
                    // No select the parent directory
                    continue;
                }
                data.mSelected = select;
                data.mDwCheck = data.mSelected
                        ? this.mIconHolder.getDrawable(
                                getContext(), R.drawable.btn_holo_light_check_on_normal)
                        : this.mIconHolder.getDrawable(
                                getContext(), R.drawable.btn_holo_light_check_off_normal);

                //Add or remove from the global selected items
                FileSystemObject fso = getItem(i);
                if (data.mSelected) {
                    FileSystemObjectAdapter.this.mSelectedItems.add(fso);
                } else {
                    if (FileSystemObjectAdapter.this.mSelectedItems.contains(fso)) {
                        FileSystemObjectAdapter.this.mSelectedItems.remove(fso);
                    }
                }
            }
            //Invalidate data for repainting
            notifyDataSetInvalidated();

            //Communicate event
            if (this.mOnSelectionChangedListener != null) {
                List<FileSystemObject> selection =
                        new ArrayList<FileSystemObject>(
                                FileSystemObjectAdapter.this.mSelectedItems);
                this.mOnSelectionChangedListener.onSelectionChanged(selection);
            }
        }
    }

    /**
     * Method that returns the selected items.
     *
     * @return List<FileSystemObject> The selected items
     */
    public List<FileSystemObject> getSelectedItems() {
        return new ArrayList<FileSystemObject>(this.mSelectedItems);
    }

    /**
     * Method that sets the selected items.
     *
     * @param selectedItems The selected items
     */
    public void setSelectedItems(List<FileSystemObject> selectedItems) {
        this.mSelectedItems = selectedItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {

        //Select or deselect the item
        int pos = ((Integer)v.getTag()).intValue();

        //Retrieve data holder
        final FileSystemObject fso = getItem(pos);

        //What button was pressed?
        switch (v.getId()) {
            case RESOURCE_ITEM_CHECK:
                //Get the row item view
                toggleSelection(v, fso);
                break;
            default:
                break;
        }
    }


}
