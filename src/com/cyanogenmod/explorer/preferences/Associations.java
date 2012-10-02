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

package com.cyanogenmod.explorer.preferences;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.cyanogenmod.explorer.model.Association;
import com.cyanogenmod.explorer.model.Association.ASSOCIATION_TYPE;

/**
 * A class for deal with user-defined associations
 */
public class Associations {

    private final static int INVALID_ASSOCIATIONS_ID = -1;

    /**
     * Method that add a new association
     *
     * @param context The current context
     * @param association The association to add or update
     * @return Association The association added
     */
    public static Association addAssociation(Context context, Association association) {
        // Check that has a valid information
        if (association.mType == null || association.mRef == null || association.mIntent == null) {
            return null;
        }

        // Retrieve the content resolver
        ContentResolver contentResolver = context.getContentResolver();

        // Check that the associations not exists
        Association b = getAssociationByTypeAndRef(
                            contentResolver, association.mType, association.mRef);
        if (b != null) return b;

        // Create the content values
        ContentValues values = createContentValues(association);
        Uri uri = context.getContentResolver().insert(Association.Columns.CONTENT_URI, values);
        association.mId = (int) ContentUris.parseId(uri);
        if (association.mId == INVALID_ASSOCIATIONS_ID) {
            return null;
        }
        return association;
    }

    /**
     * Method that removes a association
     *
     * @param context The current context
     * @param association The association to delete
     * @return boolean If the associations was remove
     */
    public static boolean removeAssociation(Context context, Association association) {
        // Check that has a valid information
        if (association.mId == INVALID_ASSOCIATIONS_ID) return false;

        // Retrieve the content resolver
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Association.Columns.CONTENT_URI, association.mId);
        return contentResolver.delete(uri, "", null) == 1; //$NON-NLS-1$
    }

    /**
     * Method that return the association from his identifier
     *
     * @param contentResolver The content resolver
     * @param associationId The association identifier
     * @return Association The association. null if no association exists.
     */
    public static Association getAssociation(ContentResolver contentResolver, int associationId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Association.Columns.CONTENT_URI, associationId),
                Association.Columns.ASSOCIATION_QUERY_COLUMNS,
                null, null, null);
        Association association = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                association = new Association(cursor);
            }
            cursor.close();
        }
        return association;
    }

    /**
     * Method that return all the associations in the database
     *
     * @param contentResolver The content resolver
     * @return Cursor The association cursor
     */
    public static Cursor getAllAssociations(ContentResolver contentResolver) {
        return contentResolver.query(
                Association.Columns.CONTENT_URI,
                Association.Columns.ASSOCIATION_QUERY_COLUMNS,
                null, null, null);
    }

    /**
     * Method that return all the associations in the database for a type of association
     *
     * @param contentResolver The content resolver
     * @param type The association type
     * @return Cursor The association cursor
     */
    public static Cursor getAllAssociationByType(
            ContentResolver contentResolver, ASSOCIATION_TYPE type) {
        final String where = Association.Columns.TYPE + " = ?"; //$NON-NLS-1$
        return contentResolver.query(
                Association.Columns.CONTENT_URI,
                Association.Columns.ASSOCIATION_QUERY_COLUMNS,
                where, new String[]{type.toString()}, null);
    }

    /**
     * Method that return the association from his ref and type
     *
     * @param contentResolver The content resolver
     * @param type The association type
     * @param ref The association identifier
     * @return Association The association. null if no association exists.
     */
    public static Association getAssociationByTypeAndRef(
            ContentResolver contentResolver, ASSOCIATION_TYPE type, String ref) {
        final String where = Association.Columns.TYPE + " = ? and " + //$NON-NLS-1$
                             Association.Columns.REF + " = ?"; //$NON-NLS-1$
        Cursor cursor = contentResolver.query(
                Association.Columns.CONTENT_URI,
                Association.Columns.ASSOCIATION_QUERY_COLUMNS,
                where, new String[]{type.toString(), ref}, null);
        Association association = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                association = new Association(cursor);
            }
            cursor.close();
        }
        return association;
    }

    /**
     * Method that create the {@link ContentValues} from the association
     *
     * @param association The association
     * @return ContentValues The content
     */
    private static ContentValues createContentValues(Association association) {
        ContentValues values = new ContentValues(1);
        values.put(Association.Columns.TYPE, association.mType.toString());
        values.put(Association.Columns.REF, association.mRef);
        values.put(Association.Columns.INTENT, association.mIntent);
        return values;
    }
}
