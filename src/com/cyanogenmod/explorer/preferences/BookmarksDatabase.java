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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.model.Bookmark;
import com.cyanogenmod.explorer.model.Bookmark.BOOKMARK_TYPE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class that holds and manage the database of bookmarks.
 */
public class BookmarksDatabase {

    private static final String BOOKMARKS_DB_FILENAME = "bookmarks.db"; //$NON-NLS-1$

    private static BookmarksDatabase sSingleton;
    private static final Object SYNC = new Object();

    /**
     * Method that returns the instance of the database of bookmarks.
     *
     * @return BookmarksDatabase The the database of bookmarks instance
     */
    public static synchronized BookmarksDatabase getInstance() {
        if (sSingleton == null) {
            sSingleton = new BookmarksDatabase();
        }
        return sSingleton;
    }

    /**
     * Method that returns the shared preferences where save the bookmarks.
     *
     * @return SharedPreferences The shared preferences
     */
    @SuppressWarnings("static-method")
    private SharedPreferences getSharedPreferences() {
        return ExplorerApplication.getInstance().getSharedPreferences(
                BOOKMARKS_DB_FILENAME, Context.MODE_PRIVATE);
    }

    /**
     * Method that adds a new bookmark to the database.
     *
     * @param bookmark The bookmark to add
     */
    public void addBookmark(Bookmark bookmark) {
        synchronized (SYNC) {
            //Get the preferences editor
            SharedPreferences sp = getSharedPreferences();
            Editor editor = sp.edit();
            editor.putString(bookmark.getName(), bookmark.getDirectory());
            //Commit settings
            editor.commit();
        }
    }

    /**
     * Method that removes a new bookmark to the database.
     *
     * @param bookmark The bookmark to remove
     * @return boolean If the operation completes successfully
     */
    public boolean removeBookmark(Bookmark bookmark) {
        synchronized (SYNC) {
            //Get the preferences editor
            SharedPreferences sp = getSharedPreferences();
            if (!sp.contains(bookmark.getName())) {
                return false;
            }
            Editor editor = sp.edit();
            editor.remove(bookmark.getName());
            //Commit settings
            editor.commit();
        }
        return true;
    }

    /**
     * Method that returns all bookmarks stored in the database.
     *
     * @return List<Bookmark> The bookmarks stored in the database
     */
    public List<Bookmark> getAllBookmarks() {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        synchronized (SYNC) {
            //List all the properties in the shared preferences file
            SharedPreferences sp = getSharedPreferences();
            Map<String, ?> props = sp.getAll();
            Iterator<String> it = props.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                String directory = (String)props.get(name);
                bookmarks.add(new Bookmark(BOOKMARK_TYPE.USER_DEFINED, name, directory));
            }
        }
        return bookmarks;
    }

    /**
     * Method that returns a bookmark for the name passed.
     *
     * @param name The name of the bookmark
     * @return Bookmark The bookmarks or {@link "null"} if not found
     */
    public Bookmark getBookmark(String name) {
        synchronized (SYNC) {
            //Get the property in the shared preferences file
            SharedPreferences sp = getSharedPreferences();
            String directory = sp.getString(name, null);
            if (directory != null) {
                return new Bookmark(BOOKMARK_TYPE.USER_DEFINED, name, directory);
            }
        }
        //Not found
        return null;
    }
}
