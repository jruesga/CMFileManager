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

import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.listeners.OnRequestRefreshListener;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.util.CommandHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;

/**
 * A class with the convenience methods for resolve navigation related actions
 */
public final class NavigationActionPolicy extends ActionsPolicy {

    /**
     * Method that navigate to parent folder
     *
     * @param ctx The current context
     * @param fso The file system object which to navigate to parent
     * @param onRequestRefreshListener The request listener
     */
    public static void openParentFolder(
            final Context ctx, final FileSystemObject fso,
            OnRequestRefreshListener onRequestRefreshListener) {
        try {
            // Retrieve the parent
            FileSystemObject parent =
                    CommandHelper.getFileInfo(ctx, fso.getParent(), null);
            if (parent == null) {
                throw new NoSuchFileOrDirectory(fso.getParent());
            }

            // Navigate to parent folder
            onRequestRefreshListener.onNavigateTo(parent);

        } catch (Exception e) {
            ExceptionUtil.translateException(ctx, e);
        }
    }

}