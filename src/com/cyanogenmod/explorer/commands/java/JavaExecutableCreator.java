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

package com.cyanogenmod.explorer.commands.java;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.commands.AsyncResultListener;
import com.cyanogenmod.explorer.commands.ChangeCurrentDirExecutable;
import com.cyanogenmod.explorer.commands.ChangeOwnerExecutable;
import com.cyanogenmod.explorer.commands.ChangePermissionsExecutable;
import com.cyanogenmod.explorer.commands.CompressExecutable;
import com.cyanogenmod.explorer.commands.CopyExecutable;
import com.cyanogenmod.explorer.commands.CreateDirExecutable;
import com.cyanogenmod.explorer.commands.CreateFileExecutable;
import com.cyanogenmod.explorer.commands.CurrentDirExecutable;
import com.cyanogenmod.explorer.commands.DeleteDirExecutable;
import com.cyanogenmod.explorer.commands.DeleteFileExecutable;
import com.cyanogenmod.explorer.commands.DiskUsageExecutable;
import com.cyanogenmod.explorer.commands.EchoExecutable;
import com.cyanogenmod.explorer.commands.ExecExecutable;
import com.cyanogenmod.explorer.commands.ExecutableCreator;
import com.cyanogenmod.explorer.commands.FindExecutable;
import com.cyanogenmod.explorer.commands.FolderUsageExecutable;
import com.cyanogenmod.explorer.commands.GroupsExecutable;
import com.cyanogenmod.explorer.commands.IdentityExecutable;
import com.cyanogenmod.explorer.commands.LinkExecutable;
import com.cyanogenmod.explorer.commands.ListExecutable;
import com.cyanogenmod.explorer.commands.ListExecutable.LIST_MODE;
import com.cyanogenmod.explorer.commands.MountExecutable;
import com.cyanogenmod.explorer.commands.MountPointInfoExecutable;
import com.cyanogenmod.explorer.commands.MoveExecutable;
import com.cyanogenmod.explorer.commands.ParentDirExecutable;
import com.cyanogenmod.explorer.commands.ProcessIdExecutable;
import com.cyanogenmod.explorer.commands.QuickFolderSearchExecutable;
import com.cyanogenmod.explorer.commands.ReadExecutable;
import com.cyanogenmod.explorer.commands.ResolveLinkExecutable;
import com.cyanogenmod.explorer.commands.SIGNAL;
import com.cyanogenmod.explorer.commands.SendSignalExecutable;
import com.cyanogenmod.explorer.commands.UncompressExecutable;
import com.cyanogenmod.explorer.commands.WriteExecutable;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.java.JavaConsole;
import com.cyanogenmod.explorer.model.Group;
import com.cyanogenmod.explorer.model.MountPoint;
import com.cyanogenmod.explorer.model.Permissions;
import com.cyanogenmod.explorer.model.Query;
import com.cyanogenmod.explorer.model.User;
import com.cyanogenmod.explorer.preferences.CompressionMode;

/**
 * A class for create shell {@link "Executable"} objects.
 */
public class JavaExecutableCreator implements ExecutableCreator {

    private final JavaConsole mConsole;

    /**
     * Constructor of <code>JavaExecutableCreator</code>.
     *
     * @param console A shell console that use for create objects
     */
    JavaExecutableCreator(JavaConsole console) {
        super();
        this.mConsole = console;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeCurrentDirExecutable createChangeCurrentDirExecutable(String dir)
            throws CommandNotFoundException {
        return new ChangeCurrentDirCommand(this.mConsole, dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeOwnerExecutable createChangeOwnerExecutable(
            String fso, User newUser, Group newGroup) throws CommandNotFoundException {
        return new ChangeOwnerCommand(fso, newUser, newGroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangePermissionsExecutable createChangePermissionsExecutable(
            String fso, Permissions newPermissions) throws CommandNotFoundException {
        return new ChangePermissionsCommand(fso, newPermissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyExecutable createCopyExecutable(String src, String dst)
            throws CommandNotFoundException {
        return new CopyCommand(src, dst);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateDirExecutable createCreateDirectoryExecutable(String dir)
            throws CommandNotFoundException {
        return new CreateDirCommand(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateFileExecutable createCreateFileExecutable(String file)
            throws CommandNotFoundException {
        return new CreateFileCommand(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CurrentDirExecutable createCurrentDirExecutable() throws CommandNotFoundException {
        return new CurrentDirCommand(this.mConsole);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteDirExecutable createDeleteDirExecutable(String dir)
            throws CommandNotFoundException {
        return new DeleteDirCommand(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteFileExecutable createDeleteFileExecutable(String file)
            throws CommandNotFoundException {
        return new DeleteFileCommand(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskUsageExecutable createDiskUsageExecutable() throws CommandNotFoundException {
        String mountsFile = this.mConsole.getCtx().getString(R.string.mounts_file);
        return new DiskUsageCommand(mountsFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskUsageExecutable createDiskUsageExecutable(String dir)
            throws CommandNotFoundException {
        String mountsFile = this.mConsole.getCtx().getString(R.string.mounts_file);
        return new DiskUsageCommand(mountsFile, dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EchoExecutable createEchoExecutable(String msg) throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecExecutable createExecExecutable(
            String cmd, AsyncResultListener asyncResultListener) throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindExecutable createFindExecutable(
            String directory, Query query, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new FindCommand(directory, query, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("FindCommand", icdEx); //$NON-NLS-1$
//        }
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FolderUsageExecutable createFolderUsageExecutable(
            String directory, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new FolderUsageCommand(directory, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("FolderUsageCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupsExecutable createGroupsExecutable() throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityExecutable createIdentityExecutable() throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkExecutable createLinkExecutable(String src, String link)
            throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ListExecutable createListExecutable(String src)
            throws CommandNotFoundException {
        return new ListCommand(src, LIST_MODE.DIRECTORY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListExecutable createFileInfoExecutable(String src, boolean followSymlinks)
            throws CommandNotFoundException {
        return new ListCommand(src, LIST_MODE.FILEINFO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountExecutable createMountExecutable(MountPoint mp, boolean rw)
            throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPointInfoExecutable createMountPointInfoExecutable()
            throws CommandNotFoundException {
        String mountsFile = this.mConsole.getCtx().getString(R.string.mounts_file);
        return new MountPointInfoCommand(mountsFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MoveExecutable createMoveExecutable(String src, String dst)
            throws CommandNotFoundException {
        return new MoveCommand(src, dst);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParentDirExecutable createParentDirExecutable(String fso)
            throws CommandNotFoundException {
        return new ParentDirCommand(fso);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessIdExecutable createShellProcessIdExecutable() throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessIdExecutable createProcessIdExecutable(int pid, String processName)
            throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuickFolderSearchExecutable createQuickFolderSearchExecutable(String regexp)
            throws CommandNotFoundException {
//        try {
//            return new QuickFolderSearchCommand(regexp);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("QuickFolderSearchCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadExecutable createReadExecutable(
            String file, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new ReadCommand(file, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("ReadCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResolveLinkExecutable createResolveLinkExecutable(String fso)
            throws CommandNotFoundException {
        return new ResolveLinkCommand(fso);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendSignalExecutable createSendSignalExecutable(int process, SIGNAL signal)
            throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendSignalExecutable createKillExecutable(int process)
            throws CommandNotFoundException {
        throw new CommandNotFoundException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteExecutable createWriteExecutable(
            String file, AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new WriteCommand(file, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("WriteCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompressExecutable createCompressExecutable(
            CompressionMode mode, String dst, String[] src,
            AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new CompressCommand(mode, dst, src, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("CompressCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompressExecutable createCompressExecutable(
            CompressionMode mode, String src,
            AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new CompressCommand(mode, src, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("CompressCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UncompressExecutable createUncompressExecutable(
            String src, String dst,
            AsyncResultListener asyncResultListener)
            throws CommandNotFoundException {
//        try {
//            return new UncompressCommand(src, dst, asyncResultListener);
//        } catch (InvalidCommandDefinitionException icdEx) {
//            throw new CommandNotFoundException("UncompressCommand", icdEx); //$NON-NLS-1$
//        }
        return null;
    }

}
