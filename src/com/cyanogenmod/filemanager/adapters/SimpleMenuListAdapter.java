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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.internal.view.menu.MenuBuilder;
import com.cyanogenmod.filemanager.R;

/**
 * An implementation of {@link BaseAdapter} that is associated with a
 * {@link Menu}. This class only shows the first level of menus.
 */
public class SimpleMenuListAdapter extends BaseAdapter {
    private final Context mContext;
    final LayoutInflater mInflater;
    private final Menu mMenu;

    /**
     * Constructor of <code>SimpleMenuListAdapter</code>.
     *
     * @param context The current context
     * @param menuResourceId The resource identifier
     */
    public SimpleMenuListAdapter(Context context, int menuResourceId) {
        super();
        this.mContext = context;
        this.mMenu = new MenuBuilder(context);
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflateMenu(menuResourceId);
    }

    /**
     * Constructor of <code>SimpleMenuListAdapter</code><br/>
     * <br/>.
     * This constructors uses only the menus of the group passed
     *
     * @param context The current context
     * @param menuResourceId The resource identifier
     * @param menuGroupResourceId The menu group resource identifier
     */
    public SimpleMenuListAdapter(Context context, int menuResourceId, int menuGroupResourceId) {
        this(context, menuResourceId);

        //Remove all item menus that no belongs to the group
        int cc = this.mMenu.size();
        for (int i = cc - 1; i >= 0; i--) {
            MenuItem menuItem = this.mMenu.getItem(i);
            if (menuItem.getGroupId() != menuGroupResourceId) {
                this.mMenu.removeItem(menuItem.getItemId());
            }
        }
    }

    /**
     * Method that returns the menu.
     *
     * @return Menu The menu
     */
    public Menu getMenu() {
        return this.mMenu;
    }


    /**
     * Method that inflates the menu.
     *
     * @param menuResourceId The resource identifier of the menu to be inflated
     */
    private void inflateMenu(int menuResourceId) {
        MenuInflater inflater = new MenuInflater(this.mContext);
        inflater.inflate(menuResourceId, this.mMenu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return this.mMenu.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuItem getItem(int position) {
        return this.mMenu.getItem(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return this.mMenu.getItem(position).getItemId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Recovers the menu item
        MenuItem menuItem = getItem(position);

        //Is a separator?
        View v = null;
        if (menuItem.getTitle() == null || menuItem.getTitle().length() == 0) {
            v = this.mInflater.inflate(R.layout.menu_item_separator, parent, false);
        } else {
            v = this.mInflater.inflate(R.layout.menu_item, parent, false);
        }

        //Set the text if has title
        if (menuItem.getTitle() != null && menuItem.getTitle().length() > 0) {
            TextView tvText = (TextView)v.findViewById(R.id.menu_item_text);
            tvText.setText(menuItem.getTitle());
        }
        v.setEnabled(menuItem.isEnabled());
        v.setVisibility(menuItem.isVisible() ? View.VISIBLE : View.GONE);

        //Return the view
        return v;
    }


}
