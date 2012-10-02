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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for opening the associations database from multiple providers. Also provides
 * some common functionality.
 */
public class AssociationsDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "AssociationsDatabaseHelper"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    private static final String DATABASE_NAME = "associations.db"; //$NON-NLS-1$
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructor of <code>AssociationsDatabaseHelper</code>
     *
     * @param context The current context
     */
    public AssociationsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE associations (" + //$NON-NLS-1$
                   "_id INTEGER PRIMARY KEY, " + //$NON-NLS-1$
                   "type TEXT, " + //$NON-NLS-1$
                   "ref TEXT, " + //$NON-NLS-1$
                   "intent TEXT);"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        if (DEBUG) {
            Log.v(TAG, "Upgrading associations database from version " + //$NON-NLS-1$
                oldVersion + " to " + currentVersion + //$NON-NLS-1$
                ", which will destroy all old data"); //$NON-NLS-1$
        }
        db.execSQL("DROP TABLE IF EXISTS associations"); //$NON-NLS-1$
        onCreate(db);
    }
}