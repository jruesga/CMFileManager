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

import com.cyanogenmod.explorer.providers.AssociationsContentProvider;

import java.io.Serializable;

/**
 * A class that represent a association.
 */
public class Association implements Serializable, Comparable<Association>, Parcelable {

    /**
     * Enumeration for types of associations.
     */
    public enum ASSOCIATION_TYPE {
        /**
         * Associations for open with command.<br/>
         * The ref field contains a file extension
         */
        OPENWITH
    }

    private static final long serialVersionUID = 4742766995763317692L;

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
                    AssociationsContentProvider.AUTHORITY,
                     "/associations")); //$NON-NLS-1$

        /**
         * The type of the association.
         * <P>Type: TEXT</P>
         * @see ASSOCIATION_TYPE
         */
        public static final String TYPE = "type"; //$NON-NLS-1$

        /**
         * The reference of the association (a file extensions, an intent, ...)
         * <P>Type: TEXT</P>
         */
        public static final String REF = "ref"; //$NON-NLS-1$

        /**
         * The action of an intent of the association
         * <P>Type: TEXT</P>
         */
        public static final String INTENT = "intent"; //$NON-NLS-1$

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                TYPE + " ASC, " + REF + " ASC"; //$NON-NLS-1$ //$NON-NLS-2$

        /**
         * @hide
         */
        public static final String[] ASSOCIATION_QUERY_COLUMNS = {_ID, TYPE, REF, INTENT};

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        /**
         * @hide
         */
        public static final int ASSOCIATION_ID_INDEX = 0;
        /**
         * @hide
         */
        public static final int ASSOCIATION_TYPE_INDEX = 1;
        /**
         * @hide
         */
        public static final int ASSOCIATION_REF_INDEX = 2;
        /**
         * @hide
         */
        public static final int ASSOCIATION_INTENT_INDEX = 3;
    }

    /** @hide **/
    public int mId;
    /** @hide **/
    public ASSOCIATION_TYPE mType;
    /** @hide **/
    public String mRef;
    /** @hide **/
    public String mIntent;

    /**
     * Constructor of <code>Association</code>.
     *
     * @param id The id of the association
     * @param type The type of the association
     * @param ref The reference of the association. See {@link ASSOCIATION_TYPE} for a valid
     * reference definition
     * @param intent The action of an intent of the association
     * @hide
     */
    Association(ASSOCIATION_TYPE type, String ref, String intent) {
        super();
        this.mType = type;
        this.mRef = ref;
        this.mIntent = intent;
    }

    /**
     * Constructor of <code>Association</code>.
     *
     * @param c The cursor with the information of the association
     */
    public Association(Cursor c) {
        super();
        this.mId = c.getInt(Columns.ASSOCIATION_ID_INDEX);
        this.mType = ASSOCIATION_TYPE.valueOf(c.getString(Columns.ASSOCIATION_TYPE_INDEX));
        this.mRef = c.getString(Columns.ASSOCIATION_REF_INDEX);
        this.mIntent = c.getString(Columns.ASSOCIATION_INTENT_INDEX);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.mId;
        result = prime * result + ((this.mIntent == null) ? 0 : this.mIntent.hashCode());
        result = prime * result + ((this.mRef == null) ? 0 : this.mRef.hashCode());
        result = prime * result + ((this.mType == null) ? 0 : this.mType.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Association other = (Association) obj;
        if (this.mId != other.mId)
            return false;
        if (this.mIntent == null) {
            if (other.mIntent != null)
                return false;
        } else if (!this.mIntent.equals(other.mIntent))
            return false;
        if (this.mRef == null) {
            if (other.mRef != null)
                return false;
        } else if (!this.mRef.equals(other.mRef))
            return false;
        if (this.mType != other.mType)
            return false;
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
        dest.writeString(this.mRef);
        dest.writeString(this.mIntent);
    }

    /**
     * Method that lets create a deserializer of <code>Association</code>.
     */
    public static final Parcelable.Creator<Association> CREATOR = new Parcelable.Creator<Association>() {
        @Override
        public Association createFromParcel(Parcel in) {
            int id = in.readInt();
            ASSOCIATION_TYPE type = ASSOCIATION_TYPE.valueOf(in.readString());
            String ref = in.readString();
            String intent = in.readString();
            Association a = new Association(type, ref, intent);
            a.mId = id;
            return a;
        }

        @Override
        public Association[] newArray(int size) {
            return new Association[size];
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Association another) {
        int c = this.mType.compareTo(another.mType);
        if (c != 0) {
            return c;
        }
        return this.mRef.compareTo(another.mRef);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Association [id=" + this.mId + ", type=" + //$NON-NLS-1$ //$NON-NLS-2$
                this.mType + ", ref=" + this.mRef + ", intent=" + //$NON-NLS-1$ //$NON-NLS-2$
                this.mIntent + "]"; //$NON-NLS-1$
    }

}
