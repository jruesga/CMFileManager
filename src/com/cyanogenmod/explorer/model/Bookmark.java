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

package com.cyanogenmod.explorer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * A class that represent a folder bookmark.
 */
public class Bookmark implements Serializable, Comparable<Bookmark>, Parcelable {

    /**
     * Enumeration for types of bookmarks.
     */
    public enum BOOKMARK_TYPE {
        /**
         * The home preference directory.
         */
        HOME,
        /**
         * The root directory.
         */
        FILESYSTEM,
        /**
         * An Secure Digital Card mount point.
         */
        SDCARD,
        /**
         * An USB mount point.
         */
        USB,
        /**
         * A bookmark added by the user.
         */
        USER_DEFINED
    }

    private static final long serialVersionUID = -2326688160035972659L;


    private final BOOKMARK_TYPE mType;
    private final String mName;
    private final String mDirectory;


    /**
     * Constructor of <code>Bookmark</code>.
     *
     * @param type The type of the bookmark
     * @param name The name of the bookmark
     * @param directory The directory that the bookmark points to
     */
    public Bookmark(BOOKMARK_TYPE type, String name, String directory) {
        super();
        this.mType = type;
        this.mName = name;
        this.mDirectory = directory;
    }

    /**
     * Method that returns the type of the bookmark.
     *
     * @return BOOKMARK_TYPE The type of the bookmark
     */
    public BOOKMARK_TYPE getType() {
        return this.mType;
    }

    /**
     * Method that returns the name of the bookmark.
     *
     * @return String The name of the bookmark
     */
    public String getName() {
        return this.mName;
    }

    /**
     * Method that returns the directory that the bookmark points to.
     *
     * @return String The directory that the bookmark points to
     */
    public String getDirectory() {
        return this.mDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.mDirectory == null) ? 0 : this.mDirectory.hashCode());
        result = prime * result + ((this.mName == null) ? 0 : this.mName.hashCode());
        result = prime * result + ((this.mType == null) ? 0 : this.mType.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Bookmark other = (Bookmark) obj;
        if (this.mDirectory == null) {
            if (other.mDirectory != null) {
                return false;
            }
        } else if (!this.mDirectory.equals(other.mDirectory)) {
            return false;
        }
        if (this.mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!this.mName.equals(other.mName)) {
            return false;
        }
        if (this.mType != other.mType) {
            return false;
        }
        return true;
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
        dest.writeString(this.mType.toString());
        dest.writeString(this.mName);
        dest.writeString(this.mDirectory);
    }

    /**
     * Method that lets create a deserializer of <code>Bookmark</code>.
     */
    public static final Parcelable.Creator<Bookmark> CREATOR = new Parcelable.Creator<Bookmark>() {
        @Override
        public Bookmark createFromParcel(Parcel in) {
            BOOKMARK_TYPE type = BOOKMARK_TYPE.valueOf(in.readString());
            String name = in.readString();
            String directory = in.readString();
            return new Bookmark(type, name, directory);
        }

        @Override
        public Bookmark[] newArray(int size) {
            return new Bookmark[size];
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Bookmark another) {
        int c = this.mType.compareTo(another.mType);
        if (c != 0) {
            return c;
        }
        return this.mDirectory.compareTo(another.mDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Bookmark [type=" + this.mType + ", name=" + this.mName +  //$NON-NLS-1$//$NON-NLS-2$
                ", directory=" + this.mDirectory + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }

}
