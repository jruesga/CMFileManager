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

package com.cyanogenmod.filemanager.preferences;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.cyanogenmod.filemanager.model.Bookmark;

/**
 * A class for deal with user-defined bookmarks
 */
public class Bookmarks {

    private final static int INVALID_BOOKMARKS_ID = -1;

    /**
     * Method that add a new bookmark
     *
     * @param context The current context
     * @param bookmark The bookmark to add or update
     * @return Bookmark The bookmark added
     */
    public static Bookmark addBookmark(Context context, Bookmark bookmark) {
        // Check that has a valid information
        if (bookmark.mPath == null) return null;

        // Retrieve the content resolver
        ContentResolver contentResolver = context.getContentResolver();

        // Check that the bookmarks not exists
        Bookmark b = getBookmark(contentResolver, bookmark.mPath);
        if (b != null) return b;

        // Create the content values
        ContentValues values = createContentValues(bookmark);
        Uri uri = context.getContentResolver().insert(Bookmark.Columns.CONTENT_URI, values);
        bookmark.mId = (int) ContentUris.parseId(uri);
        if (bookmark.mId == INVALID_BOOKMARKS_ID) {
            return null;
        }
        return bookmark;
    }

    /**
     * Method that removes a bookmark
     *
     * @param context The current context
     * @param bookmark The bookmark to delete
     * @return boolean If the bookmarks was remove
     */
    public static boolean removeBookmark(Context context, Bookmark bookmark) {
        // Check that has a valid information
        if (bookmark.mId == INVALID_BOOKMARKS_ID) return false;

        // Retrieve the content resolver
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Bookmark.Columns.CONTENT_URI, bookmark.mId);
        return contentResolver.delete(uri, "", null) == 1; //$NON-NLS-1$
    }

    /**
     * Method that return the bookmark from his identifier
     *
     * @param contentResolver The content resolver
     * @param bookmarkId The bookmark identifier
     * @return Bookmark The bookmark. null if no bookmark exists.
     */
    public static Bookmark getBookmark(ContentResolver contentResolver, int bookmarkId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Bookmark.Columns.CONTENT_URI, bookmarkId),
                Bookmark.Columns.BOOKMARK_QUERY_COLUMNS,
                null, null, null);
        Bookmark bookmark = null;
        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    bookmark = new Bookmark(cursor);
                }
            }
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {/**NON BLOCK**/}
        }

        return bookmark;
    }

    /**
     * Method that return all the bookmarks in the database
     *
     * @param contentResolver The content resolver
     * @return Cursor The bookmarks cursor
     */
    public static Cursor getAllBookmarks(ContentResolver contentResolver) {
        return contentResolver.query(
                Bookmark.Columns.CONTENT_URI,
                Bookmark.Columns.BOOKMARK_QUERY_COLUMNS,
                null, null, null);
    }

    /**
     * Method that return the bookmark from his path
     *
     * @param contentResolver The content resolver
     * @param path The bookmark path
     * @return Bookmark The bookmark. null if no bookmark exists.
     */
    public static Bookmark getBookmark(ContentResolver contentResolver, String path) {
        final String where = Bookmark.Columns.PATH + " = ?"; //$NON-NLS-1$
        Cursor cursor = contentResolver.query(
                Bookmark.Columns.CONTENT_URI,
                Bookmark.Columns.BOOKMARK_QUERY_COLUMNS,
                where, new String[]{path}, null);
        Bookmark bookmark = null;
        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    bookmark = new Bookmark(cursor);
                }
                cursor.close();
            }
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {/**NON BLOCK**/}
        }
        return bookmark;
    }

    /**
     * Method that create the {@link ContentValues} from the bookmark
     *
     * @param bookmark The bookmark
     * @return ContentValues The content
     */
    private static ContentValues createContentValues(Bookmark bookmark) {
        ContentValues values = new ContentValues(1);
        values.put(Bookmark.Columns.PATH, bookmark.mPath);
        return values;
    }
}
