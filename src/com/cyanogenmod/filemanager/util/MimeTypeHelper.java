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

package com.cyanogenmod.filemanager.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.model.BlockDevice;
import com.cyanogenmod.filemanager.model.CharacterDevice;
import com.cyanogenmod.filemanager.model.Directory;
import com.cyanogenmod.filemanager.model.DomainSocket;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.NamedPipe;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.model.SystemFile;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A helper class with useful methods for deal with mime types.
 */
public final class MimeTypeHelper {

    /**
     * Enumeration of mime/type' categories
     */
    public enum MimeTypeCategory {
        /**
         * No category
         */
        NONE,
        /**
         * System file
         */
        SYSTEM,
        /**
         * Application, Installer, ...
         */
        APP,
        /**
         * Binary file
         */
        BINARY,
        /**
         * Text file
         */
        TEXT,
        /**
         * Document file (text, spreedsheet, presentation, pdf, ...)
         */
        DOCUMENT,
        /**
         * e-Book file
         */
        EBOOK,
        /**
         * Mail file (email, message, contact, calendar, ...)
         */
        MAIL,
        /**
         * Compressed file
         */
        COMPRESS,
        /**
         * Executable file
         */
        EXEC,
        /**
         * Database file
         */
        DATABASE,
        /**
         * Font file
         */
        FONT,
        /**
         * Image file
         */
        IMAGE,
        /**
         * Audio file
         */
        AUDIO,
        /**
         * Video file
         */
        VIDEO,
        /**
         * Security file (certificate, keys, ...)
         */
        SECURITY
    }

    /**
     * An internal class for holding the mime/type database structure
     */
    private static class MimeTypeInfo {
        MimeTypeInfo() {/**NON BLOCK**/}
        public MimeTypeCategory mCategory;
        public String mMimeType;
        public String mDrawable;
    }

    private static final String TAG = "MimeTypeHelper"; //$NON-NLS-1$

    /**
     * A constant that defines a string of all mime-types
     */
    public static final String ALL_MIME_TYPES = "*/*"; //$NON-NLS-1$

    private static Map<String, Integer> sCachedIndentifiers;
    private static Map<String, MimeTypeInfo> sMimeTypes;

    /**
     * Constructor of <code>MimeTypeHelper</code>.
     */
    private MimeTypeHelper() {
        super();
    }

    /**
     * Method that returns the associated mime/type icon resource identifier of
     * the {@link FileSystemObject}.
     *
     * @param context The current context
     * @param fso The file system object
     * @return int The associated mime/type icon resource identifier
     */
    public static final int getIcon(Context context, FileSystemObject fso) {
        //Ensure that mime types are loaded
        if (sMimeTypes == null) {
            loadMimeTypes(context);
        }

        //Check if the argument is a folder
        if (fso instanceof Directory || FileHelper.isSymlinkRefDirectory(fso)) {
            return R.drawable.ic_fso_folder;
        }

        //Get the extension and delivery
        String ext = FileHelper.getExtension(fso);
        if (ext != null) {
            MimeTypeInfo mimeTypeInfo = sMimeTypes.get(ext);
            if (mimeTypeInfo != null) {
                //Search the identifier in the cache
                int drawableId = 0;
                if (sCachedIndentifiers.containsKey(ext)) {
                    drawableId = sCachedIndentifiers.get(ext).intValue();
                } else {
                    drawableId = ResourcesHelper.getIdentifier(
                          context.getResources(), "drawable", //$NON-NLS-1$
                          mimeTypeInfo.mDrawable);
                    sCachedIndentifiers.put(ext, Integer.valueOf(drawableId));
                }
                return drawableId;
            }
        }

        // Check  system file
        if (FileHelper.isSystemFile(fso)) {
            return R.drawable.fso_type_system;
        }
        // Check if the fso is executable (but not a symlink)
        if (!(fso instanceof Symlink)) {
            if (fso.getPermissions().getUser().isExecute() ||
                fso.getPermissions().getGroup().isExecute() ||
                fso.getPermissions().getOthers().isExecute()) {
                return R.drawable.fso_type_executable;
            }
        }
        return R.drawable.ic_fso_default;
    }

    /**
     * Method that returns the mime/type of the {@link FileSystemObject}.
     *
     * @param context The current context
     * @param fso The file system object
     * @return String The mime/type
     */
    public static final String getMimeType(Context context, FileSystemObject fso) {
        //Ensure that mime types are loaded
        if (sMimeTypes == null) {
            loadMimeTypes(context);
        }

        //Get the extension and delivery
        String ext = FileHelper.getExtension(fso);
        if (ext != null) {
            //Load from the database of mime types
            MimeTypeInfo mimeTypeInfo = sMimeTypes.get(ext);
            if (mimeTypeInfo != null) {
                return mimeTypeInfo.mMimeType;
            }
        }
        return null;
    }

    /**
     * Method that returns the mime/type description of the {@link FileSystemObject}.
     *
     * @param context The current context
     * @param fso The file system object
     * @return String The mime/type description
     */
    public static final String getMimeTypeDescription(Context context, FileSystemObject fso) {
        Resources res = context.getResources();

        //Ensure that mime types are loaded
        if (sMimeTypes == null) {
            loadMimeTypes(context);
        }

        //Check if the argument is a folder
        if (fso instanceof Directory) {
            return res.getString(R.string.mime_folder);
        }
        if (fso instanceof Symlink) {
            return res.getString(R.string.mime_symlink);
        }

        // System files
        if (fso instanceof BlockDevice || FileHelper.isSymlinkRefBlockDevice(fso)) {
            return context.getString(R.string.device_blockdevice);
        }
        if (fso instanceof CharacterDevice || FileHelper.isSymlinkRefCharacterDevice(fso)) {
            return context.getString(R.string.device_characterdevice);
        }
        if (fso instanceof NamedPipe || FileHelper.isSymlinkRefNamedPipe(fso)) {
            return context.getString(R.string.device_namedpipe);
        }
        if (fso instanceof DomainSocket || FileHelper.isSymlinkRefDomainSocket(fso)) {
            return context.getString(R.string.device_domainsocket);
        }

        //Get the extension and delivery
        String ext = FileHelper.getExtension(fso);
        if (ext != null) {
            //Load from the database of mime types
            MimeTypeInfo mimeTypeInfo = sMimeTypes.get(ext);
            if (mimeTypeInfo != null) {
                return mimeTypeInfo.mMimeType;
            }
        }
        return res.getString(R.string.mime_unknown);
    }

    /**
     * Method that returns the mime/type category of the file.
     *
     * @param context The current context
     * @param file The file
     * @return MimeTypeCategory The mime/type category
     */
    public static final MimeTypeCategory getCategory(Context context, File file) {
        // Ensure that have a context
        if (context == null && sMimeTypes == null) {
            // No category
            return MimeTypeCategory.NONE;
        }
        //Ensure that mime types are loaded
        if (sMimeTypes == null) {
            loadMimeTypes(context);
        }

        // Directory and Symlinks no computes as category
        if (file.isDirectory()) {
            return MimeTypeCategory.NONE;
        }

        //Get the extension and delivery
        String ext = FileHelper.getExtension(file.getName());
        if (ext != null) {
            //Load from the database of mime types
            MimeTypeInfo mimeTypeInfo = sMimeTypes.get(ext);
            if (mimeTypeInfo != null) {
                return mimeTypeInfo.mCategory;
            }
        }

        // No category
        return MimeTypeCategory.NONE;
    }

    /**
     * Method that returns the mime/type category of the file system object.
     *
     * @param context The current context
     * @param fso The file system object
     * @return MimeTypeCategory The mime/type category
     */
    public static final MimeTypeCategory getCategory(Context context, FileSystemObject fso) {
        // Ensure that have a context
        if (context == null && sMimeTypes == null) {
            // No category
            return MimeTypeCategory.NONE;
        }
        //Ensure that mime types are loaded
        if (sMimeTypes == null) {
            loadMimeTypes(context);
        }

        // Directory and Symlinks no computes as category
        if (FileHelper.isDirectory(fso)) {
            return MimeTypeCategory.NONE;
        }
        if (fso instanceof Symlink) {
            return MimeTypeCategory.NONE;
        }

        //Get the extension and delivery
        String ext = FileHelper.getExtension(fso);
        if (ext != null) {
            //Load from the database of mime types
            MimeTypeInfo mimeTypeInfo = sMimeTypes.get(ext);
            if (mimeTypeInfo != null) {
                return mimeTypeInfo.mCategory;
            }
        }
        // Check  system file
        if (fso instanceof SystemFile) {
            return MimeTypeCategory.SYSTEM;
        }

        // No category
        return MimeTypeCategory.NONE;
    }

    /**
     * Method that returns the description of the category
     *
     * @param context The current context
     * @param category The category
     * @return String The description of the category
     */
    public static final String getCategoryDescription(
                    Context context, MimeTypeCategory category) {
        if (category == null || category.compareTo(MimeTypeCategory.NONE) == 0) {
            return "-";  //$NON-NLS-1$
        }
        try {
            String id = "category_" + category.toString().toLowerCase(); //$NON-NLS-1$
            int resid = ResourcesHelper.getIdentifier(
                    context.getResources(), "string", id); //$NON-NLS-1$
            return context.getString(resid);
        } catch (Throwable e) {/**NON BLOCK**/}
        return "-";  //$NON-NLS-1$
    }

    /**
     * Method that returns if a file system object matches with a mime-type expression.
     *
     * @param ctx The current context
     * @param fso The file system object to check
     * @param mimeTypeExpression The mime-type expression (xe: *&#47;*, audio&#47;*)
     * @return boolean If the file system object matches the mime-type expression
     */
    public static final boolean matchesMimeType(
            Context ctx, FileSystemObject fso, String mimeTypeExpression) {
        String mimeType = getMimeType(ctx, fso);
        if (mimeType == null) return false;
        return mimeType.matches(convertToRegExp(mimeTypeExpression));
    }

    /**
     * Method that loads the mime type information.
     *
     * @param context The current context
     */
    //IMP! This must be invoked from the main activity creation
    public static synchronized void loadMimeTypes(Context context) {
        if (sMimeTypes == null) {
            try {
                // Create a new icon holder
                sCachedIndentifiers = new HashMap<String, Integer>();

                Properties mimeTypes = new Properties();
                mimeTypes.load(context.getResources().openRawResource(R.raw.mime_types));

                // Parse the properties to an in-memory structure
                // Format:  <extension> = <category> | <mime type> | <drawable>
                sMimeTypes = new HashMap<String, MimeTypeInfo>(mimeTypes.size());
                Enumeration<Object> e = mimeTypes.keys();
                while (e.hasMoreElements()) {
                    try {
                        String extension = (String)e.nextElement();
                        String data = mimeTypes.getProperty(extension);
                        String[] mimeData = data.split("\\|");  //$NON-NLS-1$

                        // Create a reference of MimeType
                        MimeTypeInfo mimeTypeInfo = new MimeTypeInfo();
                        mimeTypeInfo.mCategory = MimeTypeCategory.valueOf(mimeData[0].trim());
                        mimeTypeInfo.mMimeType = mimeData[1].trim();
                        mimeTypeInfo.mDrawable = mimeData[2].trim();
                        sMimeTypes.put(extension, mimeTypeInfo);

                    } catch (Exception e2) { /**NON BLOCK**/}
                }

            } catch (Exception e) {
                Log.e(TAG, "Fail to load mime types raw file.", e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Method that converts the mime-type expression to a regular expression
     *
     * @param mimeTypeExpression The mime-type expression
     * @return String The regular expression
     */
    private static String convertToRegExp(String mimeTypeExpression) {
        return mimeTypeExpression.replaceAll("\\*", ".\\*"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
