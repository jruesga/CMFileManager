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

package com.cyanogenmod.filemanager.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import com.cyanogenmod.filemanager.model.History;

import java.util.ArrayList;
import java.util.List;

/**
 * A serializer/deserializer class for {@link "NavigationActivity"}.
 */
public class NavigationInfoParcelable implements Parcelable {

    private NavigationViewInfoParcelable[] mNavigationViews;
    private List<History> mHistoryInfo;

    /**
     * Constructor of <code>NavigationInfoParcelable</code>.
     */
    public NavigationInfoParcelable() {
        super();
    }

    /**
     * Constructor of <code>NavigationInfoParcelable</code>.
     *
     * @param in The parcel information
     */
    public NavigationInfoParcelable(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Method that returns the serialized navigation views info.
     *
     * @return NavigationViewInfoParcelable[] the serialized navigation views info
     */
    public NavigationViewInfoParcelable[] getNavigationViews() {
        return this.mNavigationViews;
    }

    /**
     * Method that sets the serialized navigation views info.
     *
     * @param navigationViews The serialized navigation views info
     */
    public void setNavigationViews(NavigationViewInfoParcelable[] navigationViews) {
        this.mNavigationViews = navigationViews;
    }

    /**
     * Method that returns the history information.
     *
     * @return SearchInfoParcelable The history information
     */
    public List<History> getHistoryInfo() {
        return this.mHistoryInfo;
    }

    /**
     * Method that sets the history information.
     *
     * @param historyInfo The history information
     */
    public void setHistoryInfo(List<History> historyInfo) {
        this.mHistoryInfo = historyInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //- 0
        dest.writeInt(this.mNavigationViews.length);
        int cc = this.mNavigationViews.length;
        for (int i = 0; i < cc; i++) {
            dest.writeParcelable(this.mNavigationViews[i], 0);
        }
        //- 1
        dest.writeInt(this.mHistoryInfo == null ? 0 : this.mHistoryInfo.size());
        if (this.mHistoryInfo != null) {
            dest.writeList(this.mHistoryInfo);
        }
    }

    /**
     * Fill the object from the parcel information.
     *
     * @param in The parcel information to recreate the object
     */
    private void readFromParcel(Parcel in) {
        //- 0
        int navigationViewsCount = in.readInt();
        this.mNavigationViews = new NavigationViewInfoParcelable[navigationViewsCount];
        for (int i = 0; i < navigationViewsCount; i++) {
            this.mNavigationViews[i] =
                    in.readParcelable(NavigationInfoParcelable.class.getClassLoader());
        }
        //- 1
        int hasHistoryInfo = in.readInt();
        if (hasHistoryInfo != 0) {
            List<History> history = new ArrayList<History>(hasHistoryInfo);
            in.readList(history, NavigationInfoParcelable.class.getClassLoader());
            this.mHistoryInfo = new ArrayList<History>(history);
        }
    }

    /**
     * The {@link android.os.Parcelable.Creator}.
     *
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays.
     */
    public static final Parcelable.Creator<NavigationInfoParcelable> CREATOR =
            new Parcelable.Creator<NavigationInfoParcelable>() {
        /**
         * {@inheritDoc}
         */
        @Override
        public NavigationInfoParcelable createFromParcel(Parcel in) {
            return new NavigationInfoParcelable(in);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigationInfoParcelable[] newArray(int size) {
            return new NavigationInfoParcelable[size];
        }
    };

}