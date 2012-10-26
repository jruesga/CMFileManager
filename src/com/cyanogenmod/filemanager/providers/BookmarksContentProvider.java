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

package com.cyanogenmod.filemanager.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.cyanogenmod.filemanager.model.Bookmark;
import com.cyanogenmod.filemanager.preferences.BookmarksDatabaseHelper;

/**
 * A content provider for manage the user-defined bookmarks
 */
public class BookmarksContentProvider extends ContentProvider  {

    private static final boolean DEBUG = false;

    private static final String TAG = "BookmarksContentProvider"; //$NON-NLS-1$

    private BookmarksDatabaseHelper mOpenHelper;

    private static final int BOOKMARKS = 1;
    private static final int BOOKMARKS_ID = 2;

    /**
     * The authority string name.
     */
    public static final String AUTHORITY =
            "com.cyanogenmod.filemanager.providers.bookmarks"; //$NON-NLS-1$

    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI(
                AUTHORITY,
                "bookmarks", BOOKMARKS); //$NON-NLS-1$
        sURLMatcher.addURI(
                AUTHORITY,
                "bookmarks/#", BOOKMARKS_ID); //$NON-NLS-1$
    }

    /**
     * Constructor of <code>BookmarksContentProvider</code>.
     */
    public BookmarksContentProvider() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        this.mOpenHelper = new BookmarksDatabaseHelper(getContext());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
            case BOOKMARKS:
                qb.setTables("bookmarks"); //$NON-NLS-1$
                break;
            case BOOKMARKS_ID:
                qb.setTables("bookmarks"); //$NON-NLS-1$
                qb.appendWhere("_id="); //$NON-NLS-1$
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url); //$NON-NLS-1$
        }

        // Open the database
        SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, sort);
        if (cursor == null) {
            if (DEBUG) {
                Log.v(TAG, "Bookmarks.query: failed"); //$NON-NLS-1$
            }
        } else {
            cursor.setNotificationUri(getContext().getContentResolver(), url);
        }

        return cursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case BOOKMARKS:
                return "vnd.android.cursor.dir/bookmarks"; //$NON-NLS-1$
            case BOOKMARKS_ID:
                return "vnd.android.cursor.item/bookmarks"; //$NON-NLS-1$
            default:
                throw new IllegalArgumentException("Unknown URL"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        switch (match) {
            case BOOKMARKS_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update(
                        "bookmarks", values, "_id=" + rowId, null); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + url); //$NON-NLS-1$
            }
        }
        if (DEBUG) {
            Log.v(TAG,
                  "*** notifyChange() rowId: " + //$NON-NLS-1$
                  rowId + " url " + url); //$NON-NLS-1$
        }
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) != BOOKMARKS) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url); //$NON-NLS-1$
        }

        // Add the bookmark
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        long rowId = db.insert("bookmarks", null, initialValues); //$NON-NLS-1$
        if (rowId < 0) {
            throw new SQLException("Failed to insert row"); //$NON-NLS-1$
        }
        if (DEBUG) {
            Log.v(TAG, "Added bookmark rowId = " + rowId); //$NON-NLS-1$
        }
        Uri newUrl = ContentUris.withAppendedId(Bookmark.Columns.CONTENT_URI, rowId);

        // Notify changes
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        int count;
        String whereQuery = where;
        switch (sURLMatcher.match(url)) {
            case BOOKMARKS:
                count = db.delete("bookmarks", whereQuery, whereArgs); //$NON-NLS-1$
                break;
            case BOOKMARKS_ID:
                String segment = url.getPathSegments().get(1);
                if (TextUtils.isEmpty(whereQuery)) {
                    whereQuery = "_id=" + segment; //$NON-NLS-1$
                } else {
                    whereQuery = "_id=" + segment + //$NON-NLS-1$
                                 " AND (" + whereQuery + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                count = db.delete("bookmarks", whereQuery, whereArgs); //$NON-NLS-1$
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url); //$NON-NLS-1$
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
}