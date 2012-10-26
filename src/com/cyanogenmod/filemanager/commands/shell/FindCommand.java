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
import com.cyanogenmod.filemanager.commands.FindExecutable;
import com.cyanogenmod.filemanager.commands.SIGNAL;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.Query;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.ParseHelper;
import com.cyanogenmod.filemanager.util.SearchHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for search files.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?find"}
 */
public class FindCommand extends AsyncResultProgram implements FindExecutable {

    //IMP!! This command must returns in the same command a line with the
    //full path of the file, and a list style line of the find file in
    //the next line
    //xe:
    //
    // /mnt/emmc/test79.txt
    // ----rwxr-x system   sdcard_rw        0 2012-05-15 12:15 test79.txt
    //

    private static final String TAG = "FindCommand"; //$NON-NLS-1$

    private static final String ID = "find";  //$NON-NLS-1$

    private final String mDirectory;
    private final List<FileSystemObject> mFiles;
    private String mPartial;

    /**
     * Constructor of <code>FindCommand</code>.
     *
     * @param directory The absolute directory where start the search
     * @param query The terms to be searched
     * @param asyncResultListener The partial result listener
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public FindCommand(
            String directory, Query query, AsyncResultListener asyncResultListener)
            throws InvalidCommandDefinitionException {
        super(ID, asyncResultListener, createArgs(directory, query));
        this.mFiles = new ArrayList<FileSystemObject>();
        this.mPartial = ""; //$NON-NLS-1$
        this.mDirectory = directory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartParsePartialResult() {
        this.mFiles.clear();
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
        final List<FileSystemObject> partialFiles = new ArrayList<FileSystemObject>();
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
            while (lines.size() >= 2) {
                try {
                    //Data is synchronized?? Have two valid lines?
                    if (!lines.get(0).startsWith(File.separator)) {
                        //Discard line. The data is no synchronized (some wrong in the output)
                        lines.remove(0);
                        continue;
                    }
                    if (lines.get(1).startsWith(File.separator)) {
                        //Discard line. The data is no synchronized (some wrong in the output)
                        lines.remove(1);
                        continue;
                    }

                    //Extract the parent directory
                    String parentDir = new File(lines.get(0)).getParent();
                    if (parentDir == null || parentDir.trim().length() == 0) {
                        parentDir = FileHelper.ROOT_DIRECTORY;
                    }

                    //Retrieve the file system object and calculate relevance
                    FileSystemObject fso = ParseHelper.toFileSystemObject(parentDir, lines.get(1));
                    if (fso.getName() != null && fso.getName().length() > 0) {
                        // Don't return the directory of the search. Only files under this
                        // directory
                        if (this.mDirectory.compareTo(fso.getFullPath()) != 0) {
                            String name = new File(lines.get(0)).getName();
                            // In some situations, xe when the name has a -> the name is
                            // incorrect resolved, but src name should by fine in this case
                            fso.setName(name);
                            // The symlink is not resolved here

                            this.mFiles.add(fso);
                            partialFiles.add(fso);
                        }
                    }

                } catch (Exception ex) {
                    Log.w(TAG, "Partial result fails", ex); //$NON-NLS-1$
                }

                //Remove the pair of lines
                lines.remove(0);
                lines.remove(0);
            }

            //Saves the lines for the next partial read (At this point only one line
            //can exists in the buffer. The rest was processed or discarded)
            if (lines.size() > 0) {
                this.mPartial = lines.get(0).concat(FileHelper.NEWLINE);
            }

            //If a listener is defined, then send the partial result
            if (getAsyncResultListener() != null) {
                getAsyncResultListener().onPartialResult(partialFiles);
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
    public boolean isIgnoreShellStdErrCheck() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {

        //Search in a subdirectory without permissions returns 1, but this
        //not must be treated as an error
        //Ignore exit code 143 (cancelled)
        //Ignore exit code 137 (kill -9)
        if (exitCode != 0 && exitCode != 1 && exitCode != 143 && exitCode != 137) {
            throw new ExecutionException(
                        "exitcode != 0 && != 1 && != 143 && != 137"); //$NON-NLS-1$
        }
    }

    /**
     * Method that create the arguments of this command, using the directory and
     * arguments and creating the regular expressions of the search.
     *
     * @param directory The directory where to search
     * @param query The query make for user
     * @return String[] The arguments of the command
     */
    private static String[] createArgs(String directory, Query query) {
        String[] args = new String[query.getSlotsCount() + 1];
        args[0] = directory;
        int cc = query.getSlotsCount();
        for (int i = 0; i < cc; i++) {
            args[i + 1] = SearchHelper.toIgnoreCaseRegExp(query.getSlot(i), false);
        }
        return args;
    }
}
