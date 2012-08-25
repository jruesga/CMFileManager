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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of {@link FragmentPagerAdapter} for display page
 * inside a {@link "ViewPager"}.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragments;

    /**
     * Constructor of <code>PagerAdapter</code>.
     *
     * @param fm The fragment manager
     */
    public PagerAdapter(FragmentManager fm) {
        super(fm);
        this.mFragments = Collections.synchronizedList(new ArrayList<Fragment>());
    }

    /**
     * Method that add a new fragment to the pager.
     *
     * @param fragment The fragment to add
     */
    public void addFragment(Fragment fragment) {
        this.mFragments.add(fragment);
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        return this.mFragments.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return this.mFragments.size();
    }


}
