/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License") throws CommandNotFoundException;
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

package com.cyanogenmod.filemanager.commands;

import com.cyanogenmod.filemanager.commands.ListExecutable.LIST_MODE;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.model.Permissions;
import com.cyanogenmod.filemanager.model.Query;
import com.cyanogenmod.filemanager.model.User;
import com.cyanogenmod.filemanager.preferences.CompressionMode;

/**
 * A interface that defines methods for create {@link Executable} objects.
 */
public interface ExecutableCreator {

    /**
     * Method that creates an executable for change the current directory.
     *
     * @param dir The absolute path of the new directory to establish as current directory
     * @return ChangeCurrentDirExecutable A {@link ChangeCurrentDirExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ChangeCurrentDirExecutable createChangeCurrentDirExecutable(
            String dir) throws CommandNotFoundException;

    /**
     * Method that creates an executable for change the owner of a file system object.
     *
     * @param fso The absolute path to the source file system object
     * @param newUser The new user of the file system object
     * @param newGroup The new group of the file system object
     * @return ChangeOwnerExecutable A {@link ChangeOwnerExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ChangeOwnerExecutable createChangeOwnerExecutable(
            String fso, User newUser, Group newGroup) throws CommandNotFoundException;

    /**
     * Method that creates an executable for change the permissions of a file system object.
     *
     * @param fso The absolute path to the source file system object
     * @param newPermissions The new permissions of the file system object
     * @return ChangePermissionsExecutable A {@link ChangePermissionsExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ChangePermissionsExecutable createChangePermissionsExecutable(
            String fso, Permissions newPermissions) throws CommandNotFoundException;

    /**
     * Method that creates an executable for copy a file system object to
     * other file system object.
     *
     * @param src The absolute path to the source file system object
     * @param dst The absolute path to the destination file system object
     * @return CopyExecutable A {@link CopyExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    CopyExecutable createCopyExecutable(String src, String dst) throws CommandNotFoundException;

    /**
     * Method that creates an executable for create a new directory.
     *
     * @param dir The absolute path of the new directory
     * @return CreateDirExecutable A {@link CreateDirExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    CreateDirExecutable createCreateDirectoryExecutable(String dir)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for create a new file.
     *
     * @param file The absolute path of the new file
     * @return CreateFileExecutable A {@link CreateFileExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    CreateFileExecutable createCreateFileExecutable(String file) throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve the current directory.
     *
     * @return CurrentDirExecutable A {@link CurrentDirExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    CurrentDirExecutable createCurrentDirExecutable() throws CommandNotFoundException;

    /**
     * Method that creates an executable for delete a directory.
     *
     * @param dir The absolute path to the directory to be deleted
     * @return DeleteDirExecutable A {@link DeleteDirExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    DeleteDirExecutable createDeleteDirExecutable(String dir) throws CommandNotFoundException;

    /**
     * Method that creates an executable for delete a file.
     *
     * @param file The absolute path to the file to be deleted
     * @return DeleteFileExecutable A {@link DeleteFileExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    DeleteFileExecutable createDeleteFileExecutable(String file) throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve the disk usage.
     * for all filesystems
     *
     * @return DiskUsageExecutable A {@link DiskUsageExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    DiskUsageExecutable createDiskUsageExecutable() throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve the disk usage.
     * of the filesystem of a directory
     *
     * @param dir The absolute path to the directory
     * @return DiskUsageExecutable A {@link DiskUsageExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    DiskUsageExecutable createDiskUsageExecutable(String dir) throws CommandNotFoundException;

    /**
     * Method that creates an executable for expanding environment variables.
     * in a message string
     *
     * @param msg The message to expand
     * @return EchoExecutable A {@link EchoExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    EchoExecutable createEchoExecutable(String msg) throws CommandNotFoundException;

    /**
     * Method that execute a command
     *
     * @param cmd The command to execute
     * @param asyncResultListener The listener where to return partial results
     * @return ExecExecutable A {@link ExecExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ExecExecutable createExecExecutable(
            String cmd, AsyncResultListener asyncResultListener) throws CommandNotFoundException;

    /**
     * Method that creates an executable for make searches over the filesystem.
     *
     * @param directory The directory where to search
     * @param query The term of the query
     * @param asyncResultListener The listener where to return partial results
     * @return FindExecutable A {@link FindExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    FindExecutable createFindExecutable(
            String directory, Query query, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for compute the disk usage of a folder.
     *
     * @param directory The directory where to search
     * @param asyncResultListener The listener where to return partial results
     * @return FolderUsageExecutable A {@link FolderUsageExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    FolderUsageExecutable createFolderUsageExecutable(
            String directory, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve the groups of the current user.
     *
     * @return GroupsExecutable A {@link GroupsExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    GroupsExecutable createGroupsExecutable()
            throws com.cyanogenmod.filemanager.console.CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve identity information of the current user.
     *
     * @return IdentityExecutable A {@link IdentityExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    IdentityExecutable createIdentityExecutable() throws CommandNotFoundException;

    /**
     * Method that creates a symlink of an other file system object.
     *
     * @param src The absolute path to the source fso
     * @param link The absolute path to the link fso
     * @return LinkExecutable A {@link LinkExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    LinkExecutable createLinkExecutable(
            String src, String link) throws CommandNotFoundException;

    /**
     * Method that creates an executable for list files of a directory.
     *
     * @param src The directory where to do the listing
     * @return ListExecutable A {@link ListExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     * @see LIST_MODE
     */
    ListExecutable createListExecutable(String src)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve information of a file
     *
     * @param src The directory where to do the listing
     * @param followSymlinks If follow the symlink
     * @return ListExecutable A {@link ListExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     * @see LIST_MODE
     */
    ListExecutable createFileInfoExecutable(String src, boolean followSymlinks)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve identity information of the current user.
     *
     * @param mp The mount point to mount
     * @param rw Indicates if the operation mount the device as read-write
     * @return MountExecutable A {@link MountExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    MountExecutable createMountExecutable(
            MountPoint mp, boolean rw) throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve identity information of the current user.
     *
     * @return MountPointInfoExecutable A {@link MountPointInfoExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    MountPointInfoExecutable createMountPointInfoExecutable() throws CommandNotFoundException;

    /**
     * Method that creates an executable for move a file system object to
     * other file system object.
     *
     * @param src The absolute path to the source file system object
     * @param dst The absolute path to the destination file system object
     * @return MoveExecutable A {@link MoveExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    MoveExecutable createMoveExecutable(String src, String dst) throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve the parent directory
     * of a file system object.
     *
     * @param fso The absolute path to the file system object
     * @return ParentDirExecutable A {@link ParentDirExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ParentDirExecutable createParentDirExecutable(String fso) throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve operating system process identifier of a
     * shell.
     *
     * @return ProcessIdExecutable A {@link ProcessIdExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ProcessIdExecutable createShellProcessIdExecutable() throws CommandNotFoundException;

    /**
     * Method that creates an executable for retrieve operating system process identifier of a
     * process.
     *
     * @param pid The shell process id where the process is running
     * @param processName The process name
     * @return ProcessIdExecutable A {@link ProcessIdExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ProcessIdExecutable createProcessIdExecutable(
            int pid, String processName) throws CommandNotFoundException;

    /**
     * Method that creates an executable for quickly retrieve the name of directories
     * that matches a string.
     *
     * @param regexp The regular expression
     * @return ProcessIdExecutable A {@link ProcessIdExecutable} executable implementation
     * reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    QuickFolderSearchExecutable createQuickFolderSearchExecutable(
            String regexp) throws CommandNotFoundException;

    /**
     * Method that creates an executable for read data from disk.
     *
     * @param file The file where to read the data
     * @param asyncResultListener The listener where to return partial results
     * @return ReadExecutable A {@link ReadExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ReadExecutable createReadExecutable(
            String file, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for resolves the real
     * path of a symlink or file system object.
     *
     * @param fso The absolute path to the file system object
     * @return ResolveLinkExecutable A {@link ResolveLinkExecutable} executable
     * implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    ResolveLinkExecutable createResolveLinkExecutable(String fso) throws CommandNotFoundException;

    /**
     * Method that creates an executable for send a signal to the current process.
     *
     * @param process The process which to send the signal
     * @param signal The signal to send
     * @return SendSignalExecutable A {@link SendSignalExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    SendSignalExecutable createSendSignalExecutable(
            int process, SIGNAL signal) throws CommandNotFoundException;

    /**
     * Method that creates an executable for send a kill signal to the current process.
     *
     * @param process The process which to send the signal
     * @param signal The signal to send
     * @return SendSignalExecutable A {@link SendSignalExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    SendSignalExecutable createKillExecutable(
            int process) throws CommandNotFoundException;

    /**
     * Method that creates an executable for write data to disk.
     *
     * @param file The file where to write the data
     * @param asyncResultListener The listener where to return partial results
     * @return WriteExecutable A {@link WriteExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    WriteExecutable createWriteExecutable(
            String file, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for archive-compress file system objects.
     *
     * @param mode The compression mode
     * @param dst The destination compressed file
     * @param src The array of source files to compress
     * @param asyncResultListener The listener where to return partial results
     * @return CompressExecutable A {@link CompressExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    CompressExecutable createCompressExecutable(
            CompressionMode mode, String dst, String[] src,
            AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for compress a file system object.
     *
     * @param mode The compression mode
     * @param src The file to compress
     * @param asyncResultListener The listener where to return partial results
     * @return CompressExecutable A {@link CompressExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    CompressExecutable createCompressExecutable(
            CompressionMode mode, String src, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

    /**
     * Method that creates an executable for uncompress file system objects.
     *
     * @param src The compressed file
     * @param dst The destination file of folder (if null this method resolve with the best
     * fit based on the src)
     * @param asyncResultListener The listener where to return partial results
     * @return UncompressExecutable A {@link UncompressExecutable} executable implementation reference
     * @throws CommandNotFoundException If the executable can't be created
     */
    UncompressExecutable createUncompressExecutable(
            String src, String dst, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException;

}
