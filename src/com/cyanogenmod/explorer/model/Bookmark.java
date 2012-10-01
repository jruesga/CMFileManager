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

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.cyanogenmod.explorer.providers.BookmarksContentProvider;

import java.io.File;
import java.io.Serializable;

/**
 * A class that represent a bookmark.
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

    private static final long serialVersionUID = -7524744999056506867L;

    /**
     * Columns of the database
     */
    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse(
                String.format(
                    "%s%s/%s", //$NON-NLS-1$
                    "content://", //$NON-NLS-1$
                    BookmarksContentProvider.AUTHORITY,
                     "/bookmarks")); //$NON-NLS-1$

        /**
         * The directory of the bookmark
         * <P>Type: TEXT</P>
         */
        public static final String DIRECTORY = "directory"; //$NON-NLS-1$

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                DIRECTORY + " ASC"; //$NON-NLS-1$

        /**
         * @hide
         */
        public static final String[] BOOKMARK_QUERY_COLUMNS = {_ID, DIRECTORY};

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        /**
         * @hide
         */
        public static final int BOOKMARK_ID_INDEX = 0;
        /**
         * @hide
         */
        public static final int BOOKMARK_DIRECTORY_INDEX = 1;
    }

    /** @hide **/
    public int mId;
    /** @hide **/
    public BOOKMARK_TYPE mType;
    /** @hide **/
    public String mName;
    /** @hide **/
    public String mDirectory;

    /**
     * Constructor of <code>Bookmark</code>.
     *
     * @param id The id of the bookmark
     * @param type The type of the bookmark
     * @param name The name of the bookmark
     * @param directory The directory that the bookmark points to
     * @hide
     */
    Bookmark(int id, BOOKMARK_TYPE type, String name, String directory) {
        super();
        this.mId = id;
        this.mType = type;
        this.mName = name;
        this.mDirectory = directory;
    }

    /**
     * Constructor of <code>Bookmark</code>.
     *
     * @param type The type of the bookmark
     * @param name The name of the bookmark
     * @param directory The directory that the bookmark points to
     */
    public Bookmark(BOOKMARK_TYPE type, String name, String directory) {
        super();
        this.mId = -1;
        this.mType = type;
        this.mName = name;
        this.mDirectory = directory;
    }

    /**
     * Constructor of <code>Bookmark</code>.
     *
     * @param c The cursor with the information of the bookmark
     */
    public Bookmark(Cursor c) {
        super();
        this.mId = c.getInt(Columns.BOOKMARK_ID_INDEX);
        this.mType = BOOKMARK_TYPE.USER_DEFINED;
        this.mDirectory = c.getString(Columns.BOOKMARK_DIRECTORY_INDEX);
        this.mName = new File(this.mDirectory).getName();
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
        dest.writeInt(this.mId);
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
            int id = in.readInt();
            BOOKMARK_TYPE type = BOOKMARK_TYPE.valueOf(in.readString());
            String name = in.readString();
            String directory = in.readString();
            return new Bookmark(id, type, name, directory);
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
        return "Bookmark [id=" + this.mId + ", type=" + //$NON-NLS-1$//$NON-NLS-2$
                this.mType + ", name=" + this.mName +  //$NON-NLS-1$
                ", directory=" + this.mDirectory + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }

}
