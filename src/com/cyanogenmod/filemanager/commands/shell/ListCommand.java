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

import com.cyanogenmod.filemanager.FileManagerApplication;
import com.cyanogenmod.filemanager.commands.ListExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ConsoleAllocException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.console.OperationTimeoutException;
import com.cyanogenmod.filemanager.console.shell.ShellConsole;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.ParentDirectory;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.util.CommandHelper;
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
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?ls"}
 */
public class ListCommand extends SyncResultProgram implements ListExecutable {

    private static final String ID_LS_DIRECTORY = "ls";  //$NON-NLS-1$
    private static final String ID_LS_INFO = "fileinfo";  //$NON-NLS-1$

    private static final String SYMLINK_REF = ">SIMLINKS>";  //$NON-NLS-1$
    private static final String SYMLINK_DATA_REF = ">SIMLINKS_DATA>";  //$NON-NLS-1$

    private final String mSrc;
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
     * @throws FileNotFoundException If the initial directory not exists
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws IOException If initial directory couldn't be checked
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     */
    public ListCommand(String src, ShellConsole console)
            throws InvalidCommandDefinitionException, FileNotFoundException,
            NoSuchFileOrDirectory, IOException, ConsoleAllocException,
            InsufficientPermissionsException, CommandNotFoundException,
            OperationTimeoutException, ExecutionException {
        //If the mode is listing directory, for avoid problems with symlink,
        //always append a / to the end of the path (if not exists)
        super(ID_LS_DIRECTORY, new String[]{ FileHelper.addTrailingSlash(src) });

        //Initialize files to something distinct of null
        this.mFiles = new ArrayList<FileSystemObject>();
        this.mMode = LIST_MODE.DIRECTORY;
        this.mSrc = src;

        //Retrieve parent directory information
        if (src.compareTo(FileHelper.ROOT_DIRECTORY) == 0) {
            this.mParentDir = null;
        } else {
            this.mParentDir =
                CommandHelper.getAbsolutePath(
                        FileManagerApplication.
                            getInstance().getApplicationContext(), src, console);
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
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws IOException If initial directory couldn't be checked
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     */
    public ListCommand(String src, boolean followSymlinks, ShellConsole console)
            throws InvalidCommandDefinitionException, FileNotFoundException,
            NoSuchFileOrDirectory, IOException, ConsoleAllocException,
            InsufficientPermissionsException, CommandNotFoundException,
            OperationTimeoutException, ExecutionException {
        //If the mode is listing directory, for avoid problems with symlink,
        //always append a / to the end of the path (if not exists)
        super(ID_LS_INFO,
                new String[]{
                    FileHelper.removeTrailingSlash(
                            followSymlinks ?
                                    new File(src).getCanonicalPath() :
                                    new File(src).getAbsolutePath())});

        //Initialize files to something distinct of null
        this.mFiles = new ArrayList<FileSystemObject>();
        this.mMode = LIST_MODE.FILEINFO;
        this.mSrc = src;

        //Get the absolute path
        try {
            if (followSymlinks) {
                this.mParentDir =
                        FileHelper.removeTrailingSlash(
                                new File(src).getCanonicalFile().getParent());
            } else {
                this.mParentDir =
                        FileHelper.removeTrailingSlash(
                                new File(src).getAbsoluteFile().getParent());
            }

        } catch (Exception e) {
            // Try to resolve from a console
            String abspath =
                CommandHelper.getAbsolutePath(
                        FileManagerApplication.getInstance().
                            getApplicationContext(), src, console);
            //Resolve the parent directory
            this.mParentDir =
                CommandHelper.getParentDir(
                        FileManagerApplication.getInstance().getApplicationContext(),
                        abspath, console);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the array
        this.mFiles.clear();

        // Check the in buffer to extract information
        BufferedReader br = null;
        int line = 0;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = null;
            boolean symlinks = false;
            int symlinksCount = 0;
            while ((szLine = br.readLine()) != null) {
                //Checks that there is some text in the line. Otherwise ignore it
                if (szLine.trim().length() == 0) {
                    break;
                }

                //For a fast recovery, command return non symlink first and
                //symlinks files, the resolution and the his info
                //Is now symlinks?
                if (szLine.startsWith(SYMLINK_REF)) {
                    //Ignore the control line
                    szLine = br.readLine();
                    line++;
                    symlinks = true;
                }

                //Parse the line into a FileSystemObject reference
                if (!symlinks) {
                    try {
                        FileSystemObject fso =
                                ParseHelper.toFileSystemObject(this.mParentDir, szLine);
                        if (this.mMode.compareTo(LIST_MODE.FILEINFO) == 0 &&
                            fso instanceof Symlink) {
                            // In some situations, xe when the name has a -> the name is
                            // incorrect resolved, but src name should by fine in this case
                            fso.setName(new File(this.mSrc).getName());
                            // The symlink is not resolved here
                        }
                        this.mFiles.add(fso);
                    } catch (ParseException pEx) {
                        throw new ParseException(pEx.getMessage(), line);
                    }
                } else {
                    //Is ending symlink reference
                    if (szLine.startsWith(SYMLINK_DATA_REF)) {
                        if (symlinksCount == 0) {
                            //No more data
                            break;
                        }
                        //Ignore the control line
                        szLine = br.readLine();
                        line++;

                        //The next information is known:  symlinksCount * 3
                        String[] name = new String[symlinksCount];
                        String[] absPath = new String[symlinksCount];
                        String[] refPath = new String[symlinksCount];
                        for (int i = 0; i < symlinksCount; i++) {
                            if (szLine == null || szLine.trim().length() == 0) {
                                name[i] = null;
                                szLine = br.readLine();
                                line++;
                                continue;
                            }
                            name[i] = szLine;
                            szLine = br.readLine();
                            line++;
                        }
                        for (int i = 0; i < symlinksCount; i++) {
                            if (szLine == null || szLine.trim().length() == 0) {
                                absPath[i] = null;
                                szLine = br.readLine();
                                line++;
                                continue;
                            }
                            absPath[i] = szLine;
                            szLine = br.readLine();
                            line++;
                        }
                        for (int i = 0; i < symlinksCount; i++) {
                            if (szLine == null || szLine.trim().length() == 0) {
                                refPath[i] = null;
                                szLine = br.readLine();
                                line++;
                                continue;
                            }
                            refPath[i] = szLine;
                            szLine = br.readLine();
                            line++;
                        }

                        //Fill the parent if is null
                        for (int i = 0; i < symlinksCount; i++) {
                            Symlink symLink =
                                    ((Symlink)this.mFiles.get(
                                            this.mFiles.size() - symlinksCount + i));
                            if (symLink.getParent() == null) {
                                symLink.setParent(FileHelper.ROOT_DIRECTORY);
                            }
                        }

                        // Symlink can cause incoherences in the name because "->" string
                        // Now, we have the real name of the symlink
                        for (int i = 0; i < symlinksCount; i++) {
                            if (name[i] != null) {
                                Symlink symLink =
                                        ((Symlink)this.mFiles.get(
                                                this.mFiles.size() - symlinksCount + i));
                                symLink.setName(name[i]);
                            }
                        }

                        //Fill the data
                        for (int i = 0; i < symlinksCount; i++) {
                            try {
                                if (absPath[i] != null && absPath[i].length() > 0) {
                                    Symlink symLink =
                                            ((Symlink)this.mFiles.get(
                                                    this.mFiles.size() - symlinksCount + i));
                                    String parentLink = new File(absPath[i]).getParent();
                                    if (parentLink == null) {
                                        parentLink = FileHelper.ROOT_DIRECTORY;
                                    }
                                    String info = refPath[i];
                                    FileSystemObject fsoRef =
                                            ParseHelper.toFileSystemObject(parentLink, info);
                                    symLink.setLinkRef(fsoRef);
                                }
                            } catch (Throwable ex) {
                                //If parsing the file failed, ignore it and threat as a regular
                                //file (the destination file not exists or can't be resolved)
                            }
                        }
                        break;
                    }

                    //Add the symlink
                    try {
                        this.mFiles.add(ParseHelper.toFileSystemObject(this.mParentDir, szLine));
                        symlinksCount++;
                    } catch (ParseException pEx) {
                        throw new ParseException(pEx.getMessage(), line);
                    }
                }

                line++;
            }

            // Add the parent directory
            if (this.mParentDir != null &&
                    this.mParentDir.compareTo(FileHelper.ROOT_DIRECTORY) != 0 &&
                    this.mMode.compareTo(LIST_MODE.DIRECTORY) == 0) {
                this.mFiles.add(0, new ParentDirectory(new File(this.mParentDir).getParent()));
            }

        } catch (IOException ioEx) {
            throw new ParseException(ioEx.getMessage(), line);

        } catch (ParseException pEx) {
            throw pEx;

        } catch (Exception ex) {
            throw new ParseException(ex.getMessage(), line);

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
        if (exitCode != 0 && exitCode != 1) {
            throw new ExecutionException("exitcode != 0 && != 1"); //$NON-NLS-1$
        }
    }
}
