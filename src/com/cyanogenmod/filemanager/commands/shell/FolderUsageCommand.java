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

package com.cyanogenmod.filemanager.commands.shell;

import android.util.Log;

import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.commands.FolderUsageExecutable;
import com.cyanogenmod.filemanager.commands.SIGNAL;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.Directory;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.FolderUsage;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper.MimeTypeCategory;
import com.cyanogenmod.filemanager.util.ParseHelper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for retrieve the disk usage of a folder
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?ls"}
 */
public class FolderUsageCommand extends AsyncResultProgram implements FolderUsageExecutable {

    private static final String TAG = "FolderUsageCommand"; //$NON-NLS-1$

    private static final String ID = "folderusage"; //$NON-NLS-1$

    private final String mDirectory;
    private FolderUsage mFolderUsage;
    private String mPartial;

    /**
     * Constructor of <code>FolderUsageCommand</code>.
     *
     * @param directory The absolute directory to compute
     * @param asyncResultListener The partial result listener
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public FolderUsageCommand(
            String directory, AsyncResultListener asyncResultListener)
            throws InvalidCommandDefinitionException {
        super(ID, asyncResultListener, new String[]{directory});
        this.mFolderUsage = new FolderUsage(directory);
        this.mPartial = ""; //$NON-NLS-1$
        this.mDirectory = directory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartParsePartialResult() {
        this.mFolderUsage = new FolderUsage(this.mDirectory);
        this.mPartial = ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEndParsePartialResult(boolean cancelled) {
        this.mPartial = ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onParsePartialResult(final String partialIn) {

        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            //Read the partial + previous partial and clean partial
            br = new BufferedReader(new StringReader(this.mPartial + partialIn));
            this.mPartial = ""; //$NON-NLS-1$

            //Add all lines to an array
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                lines.add(line);
            }

            //2 lines per file system object translation
            boolean newData = false;
            int c = 0;
            while (lines.size() > 0) {
                try {
                    // Retrieve the info
                    String szLine = lines.get(0).trim();

                    // Parent folder is not necessary here. Only the information relative to
                    // type and size
                    FileSystemObject fso =
                            ParseHelper.toFileSystemObject(
                                    FileHelper.ROOT_DIRECTORY, szLine, true);

                    // Only regular files or directories. No compute Symlinks
                    if (fso instanceof Symlink) {

                    // Directory
                    } else if (fso instanceof Directory) {
                        // Folder
                        this.mFolderUsage.addFolder();
                        newData = true;

                    // Regular File, Block device, ...
                    } else {
                        this.mFolderUsage.addFile();
                        // Compute statistics and size
                        MimeTypeCategory category =
                                MimeTypeHelper.getCategory(null, fso);
                        this.mFolderUsage.addFileToCategory(category);
                        this.mFolderUsage.addSize(fso.getSize());
                        newData = true;
                    }

                    // Partial notification
                    if (c % 5 == 0) {
                        //If a listener is defined, then send the partial result
                        if (getAsyncResultListener() != null && newData) {
                            getAsyncResultListener().onPartialResult(this.mFolderUsage);
                        }
                    }

                } catch (Exception ex) { /**NON BLOCK **/ }

                //Remove the the line
                lines.remove(0);
            }

            //Saves the lines for the next partial read (At this point only one line
            //can exists in the buffer. The rest was processed or discarded)
            if (lines.size() > 0) {
                this.mPartial = lines.get(0).concat(FileHelper.NEWLINE);
            }

            //If a listener is defined, then send the partial result
            if (getAsyncResultListener() != null && newData) {
                getAsyncResultListener().onPartialResult(this.mFolderUsage);
            }

        } catch (Exception ex) {
            Log.w(TAG, "Partial result fails", ex); //$NON-NLS-1$

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onParseErrorPartialResult(String partialErr) {/**NON BLOCK**/}

    /**
     * {@inheritDoc}
     */
    @Override
    public SIGNAL onRequestEnd() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FolderUsage getFolderUsage() {
        return this.mFolderUsage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIgnoreShellStdErrCheck() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {

        //Access a subdirectory without permissions returns 1, but this
        //not must be treated as an error
        //Ignore exit code 143 (cancelled)
        //Ignore exit code 137 (kill -9)
        if (exitCode != 0 && exitCode != 1 && exitCode != 143 && exitCode != 137) {
            throw new ExecutionException(
                        "exitcode != 0 && != 1 && != 143 && != 137"); //$NON-NLS-1$
        }
    }
}
