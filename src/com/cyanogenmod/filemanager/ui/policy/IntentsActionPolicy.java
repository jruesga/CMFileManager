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

package com.cyanogenmod.filemanager.ui.policy;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.activities.ShortcutActivity;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.RegularFile;
import com.cyanogenmod.filemanager.ui.dialogs.AssociationsDialog;
import com.cyanogenmod.filemanager.util.DialogHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper.MimeTypeCategory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class with the convenience methods for resolve intents related actions
 */
public final class IntentsActionPolicy extends ActionsPolicy {

    private static final String TAG = "IntentsActionPolicy"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    /**
     * Extra field for the internal action
     */
    public static final String EXTRA_INTERNAL_ACTION =
            "com.cyanogenmod.filemanager.extra.INTERNAL_ACTION"; //$NON-NLS-1$

    /**
     * Category for all the internal app viewers
     */
    public static final String CATEGORY_INTERNAL_VIEWER =
            "com.cyanogenmod.filemanager.category.INTERNAL_VIEWER"; //$NON-NLS-1$

    /**
     * Category for all the app editor
     */
    public static final String CATEGORY_EDITOR =
            "com.cyanogenmod.filemanager.category.EDITOR"; //$NON-NLS-1$

    /**
     * Method that opens a {@link FileSystemObject} with the default registered application
     * by the system, or ask the user for select a registered application.
     *
     * @param ctx The current context
     * @param fso The file system object
     * @param choose If allow the user to select the application to open with
     * @param onCancelListener The cancel listener
     * @param onDismissListener The dismiss listener
     */
    public static void openFileSystemObject(
            final Context ctx, final FileSystemObject fso, final boolean choose,
            OnCancelListener onCancelListener, OnDismissListener onDismissListener) {
        try {
            // Create the intent to
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);

            // Obtain the mime/type and passed it to intent
            String mime = MimeTypeHelper.getMimeType(ctx, fso);
            File file = new File(fso.getFullPath());
            if (mime != null) {
                intent.setDataAndType(Uri.fromFile(file), mime);
            } else {
                intent.setData(Uri.fromFile(file));
            }

            // Resolve the intent
            resolveIntent(
                    ctx,
                    intent,
                    choose,
                    createInternalIntents(ctx,  fso),
                    0,
                    R.string.associations_dialog_openwith_title,
                    R.string.associations_dialog_openwith_action,
                    true, onCancelListener, onDismissListener);

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }

    /**
     * Method that sends a {@link FileSystemObject} with the default registered application
     * by the system, or ask the user for select a registered application.
     *
     * @param ctx The current context
     * @param fso The file system object
     * @param onCancelListener The cancel listener
     * @param onDismissListener The dismiss listener
     */
    public static void sendFileSystemObject(
            final Context ctx, final FileSystemObject fso,
            OnCancelListener onCancelListener, OnDismissListener onDismissListener) {
        try {
            // Create the intent to
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(MimeTypeHelper.getMimeType(ctx, fso));
            Uri uri = Uri.fromFile(new File(fso.getFullPath()));
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            // Resolve the intent
            resolveIntent(
                    ctx,
                    intent,
                    false,
                    null,
                    0,
                    R.string.associations_dialog_sendwith_title,
                    R.string.associations_dialog_sendwith_action,
                    false, onCancelListener, onDismissListener);

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }

    /**
     * Method that resolve
     *
     * @param ctx The current context
     * @param intent The intent to resolve
     * @param choose If allow the user to select the application to select the registered
     * application. If no preferred app or more than one exists the dialog is shown.
     * @param internals The list of internals intents that can handle the action
     * @param icon The icon of the dialog
     * @param title The title of the dialog
     * @param action The button title of the dialog
     * @param allowPreferred If allow the user to mark the selected app as preferred
     * @param onCancelListener The cancel listener
     * @param onDismissListener The dismiss listener
     */
    private static void resolveIntent(
            Context ctx, Intent intent, boolean choose, List<Intent> internals,
            int icon, int title, int action, boolean allowPreferred,
            OnCancelListener onCancelListener, OnDismissListener onDismissListener) {
        //Retrieve the activities that can handle the file
        final PackageManager packageManager = ctx.getPackageManager();
        if (DEBUG) {
            intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        }
        List<ResolveInfo> info =
                packageManager.
                    queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // Add the internal editors
        int count = 0;
        if (internals != null) {
            int cc = internals.size();
            for (int i = 0; i < cc; i++) {
                Intent ii = internals.get(i);
                List<ResolveInfo> ris =
                        packageManager.
                            queryIntentActivities(ii, 0);
                if (ris.size() > 0) {
                    ResolveInfo ri = ris.get(0);
                    // Mark as internal
                    if (ri.activityInfo.metaData == null) {
                        ri.activityInfo.metaData = new Bundle();
                        ri.activityInfo.metaData.putString(EXTRA_INTERNAL_ACTION, ii.getAction());
                        ri.activityInfo.metaData.putBoolean(CATEGORY_INTERNAL_VIEWER, true);
                    }

                    // Only one result must be matched
                    info.add(count, ri);
                    count++;
                }
            }
        }

        // Retrieve the preferred activity that can handle the file
        final ResolveInfo mPreferredInfo = packageManager.resolveActivity(intent, 0);

        // No registered application
        if (info.size() == 0) {
            DialogHelper.showToast(ctx, R.string.msgs_not_registered_app, Toast.LENGTH_SHORT);
            return;
        }

        // Is a simple open and we have an application that can handle the file?
        if (!choose &&
                ((mPreferredInfo  != null && mPreferredInfo.match != 0) || info.size() == 1)) {
            // But not if the only match is the an internal editor
            ResolveInfo ri = info.get(0);
            if (ri.activityInfo.metaData == null ||
                    !ri.activityInfo.metaData.getBoolean(CATEGORY_INTERNAL_VIEWER, false)) {
                ctx.startActivity(intent);
                return;
            }
        }

        // Otherwise, we have to show the open with dialog
        AssociationsDialog dialog =
                new AssociationsDialog(
                        ctx,
                        icon,
                        ctx.getString(title),
                        ctx.getString(action),
                        intent,
                        info,
                        mPreferredInfo,
                        allowPreferred,
                        onCancelListener,
                        onDismissListener);
        dialog.show();
    }

    /**
     * Method that creates a shortcut in the desktop of the device of {@link FileSystemObject}.
     *
     * @param ctx The current context
     * @param fso The file system object
     */
    public static void createShortcut(Context ctx, FileSystemObject fso) {
        try {
            // Create the intent that will handle the shortcut
            Intent shortcutIntent = new Intent(ctx, ShortcutActivity.class);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (FileHelper.isDirectory(fso)) {
                shortcutIntent.putExtra(
                        ShortcutActivity.EXTRA_TYPE,ShortcutActivity.SHORTCUT_TYPE_NAVIGATE);
            } else {
                shortcutIntent.putExtra(
                        ShortcutActivity.EXTRA_TYPE, ShortcutActivity.SHORTCUT_TYPE_OPEN);
            }
            shortcutIntent.putExtra(ShortcutActivity.EXTRA_FSO, fso.getFullPath());

            // The intent to send to broadcast for register the shortcut intent
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, fso.getName());
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(
                            ctx, MimeTypeHelper.getIcon(ctx, fso)));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT"); //$NON-NLS-1$
            ctx.sendBroadcast(intent);

            // Show the confirmation
            DialogHelper.showToast(
                    ctx, R.string.shortcut_creation_success_msg, Toast.LENGTH_SHORT);

        } catch (Exception e) {
            Log.e(TAG, "Failed to create the shortcut", e); //$NON-NLS-1$
            DialogHelper.showToast(
                    ctx, R.string.shortcut_creation_failed_msg, Toast.LENGTH_SHORT);
        }
    }

    /**
     * This method creates a list of internal activities that could handle the fso.
     *
     * @param ctx The current context
     * @param fso The file system object to open
     */
    private static List<Intent> createInternalIntents(Context ctx, FileSystemObject fso) {
        List<Intent> intents = new ArrayList<Intent>();
        intents.addAll(createEditorIntent(ctx, fso));
        return intents;
    }

    /**
     * This method creates a list of internal activities for editing files
     *
     * @param ctx The current context
     * @param fso FileSystemObject
     */
    private static List<Intent> createEditorIntent(Context ctx, FileSystemObject fso) {
        List<Intent> intents = new ArrayList<Intent>();
        MimeTypeCategory category = MimeTypeHelper.getCategory(ctx, fso);

        //- Internal Editor. This editor can handle TEXT and NONE mime categories but
        //  not system files, directories, ..., only regular files (no symlinks)
        if (fso instanceof RegularFile &&
            (category.compareTo(MimeTypeCategory.NONE) == 0 ||
             category.compareTo(MimeTypeCategory.EXEC) == 0 ||
             category.compareTo(MimeTypeCategory.TEXT) == 0)) {
            Intent editorIntent = new Intent();
            editorIntent.setAction(Intent.ACTION_EDIT);
            editorIntent.addCategory(CATEGORY_INTERNAL_VIEWER);
            editorIntent.addCategory(CATEGORY_EDITOR);
            intents.add(editorIntent);
        }

        return intents;
    }
}