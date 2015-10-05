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

import com.cyanogenmod.filemanager.commands.ListExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.shell.ShellConsole;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.ParentDirectory;
import com.cyanogenmod.filemanager.util.FileHelper;
import com.cyanogenmod.filemanager.util.ParseHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for list information about files and directories.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?stat"}
 */
public class ListCommand extends SyncResultProgram implements ListExecutable {

    private static final String TAG = "ListCommand"; //$NON-NLS-1$

    private static final String ID_LS = "ls";  //$NON-NLS-1$
    private static final String ID_FILEINFO = "fileinfo";  //$NON-NLS-1$

    private final LIST_MODE mMode;
    private final List<FileSystemObject> mFiles;
    private String mParentDir;

    /**
     * Constructor of <code>ListCommand</code>. List mode.
     *
     * @param src The file system object to be listed
     * @param console The console in which retrieve the parent directory information.
     * <code>null</code> to attach to the default console
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public ListCommand(String src, ShellConsole console)
            throws InvalidCommandDefinitionException {
        // Always add backslash for list the files of the directory, instead of
        // the directory.
        super(ID_LS, new String[]{ FileHelper.addTrailingSlash(src) });

        //Initialize files to something distinct of null
        this.mFiles = new ArrayList<FileSystemObject>();
        this.mMode = LIST_MODE.DIRECTORY;

        //Retrieve parent directory information
        if (src.compareTo(FileHelper.ROOT_DIRECTORY) == 0) {
            this.mParentDir = null;
        } else {
            this.mParentDir = new File(src).getAbsolutePath();
        }
    }

    /**
     * Constructor of <code>ListCommand</code>. FileInfo mode
     *
     * @param src The file system object to be listed
     * @param followSymlinks If follow the symlink
     * @param console The console in which retrieve the parent directory information.
     * <code>null</code> to attach to the default console
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory couldn't be checked
     */
    public ListCommand(String src, boolean followSymlinks, ShellConsole console)
            throws InvalidCommandDefinitionException, FileNotFoundException, IOException {
        // Always remove backslash for avoid listing the files of the directory, instead of
        // the directory.
        super(ID_FILEINFO,
                new String[]{
                    FileHelper.removeTrailingSlash(
                            followSymlinks ?
                                    new File(src).getCanonicalPath() :
                                    new File(src).getAbsolutePath())});

        //Initialize files to something distinct of null
        this.mFiles = new ArrayList<FileSystemObject>();
        this.mMode = LIST_MODE.FILEINFO;

        //Get the absolute path
        if (followSymlinks) {
            this.mParentDir =
                    FileHelper.removeTrailingSlash(
                            new File(src).getCanonicalFile().getParent());
        } else {
            this.mParentDir =
                    FileHelper.removeTrailingSlash(
                            new File(src).getAbsoluteFile().getParent());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the array
        this.mFiles.clear();

        // Read every line and parse it
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(in));
            String line = null;
            while ((line = br.readLine()) != null) {
                //Checks that there is some text in the line. Otherwise ignore it
                if (line.trim().length() == 0) {
                    break;
                }

                // Parse and add to result files
                try {
                    this.mFiles.add(ParseHelper.parseStatOutput(line));
                } catch (Exception e) {
                    // Log the parsing error
                    if (isTrace()) {
                        Log.w(TAG,
                            String.format(
                                    "Failed to parse output: %s", //$NON-NLS-1$
                                    String.valueOf(line)));
                    }
                }
            }

            // Add the parent directory
            if (this.mParentDir != null &&
                    this.mParentDir.compareTo(FileHelper.ROOT_DIRECTORY) != 0 &&
                    this.mMode.compareTo(LIST_MODE.DIRECTORY) == 0) {
                this.mFiles.add(0, new ParentDirectory(new File(this.mParentDir).getParent()));
            }

        } catch (IOException ioEx) {
            throw new ParseException(ioEx.getMessage(), 0);

        } catch (Exception ex) {
            throw new ParseException(ex.getMessage(), 0);

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
    public List<FileSystemObject> getResult() {
        return this.mFiles;
    }

    /**
     * Method that returns a single result of the program invocation.
     * Only must be called within a <code>FILEINFO</code> mode listing.
     *
     * @return FileSystemObject The file system object reference
     */
    public FileSystemObject getSingleResult() {
        return this.mFiles.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {
        // 123: stat failed ... Function not implemented (for broken symlinks)
        if (exitCode != 0 && exitCode != 1 && exitCode != 123) {
            throw new ExecutionException("exitcode != 0 && != 1 && != 123"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWaitOnNewDataReceipt() {
        return true;
    }
}
