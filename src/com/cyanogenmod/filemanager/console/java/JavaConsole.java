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

package com.cyanogenmod.filemanager.console.java;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.cyanogenmod.filemanager.commands.Executable;
import com.cyanogenmod.filemanager.commands.ExecutableFactory;
import com.cyanogenmod.filemanager.commands.SIGNAL;
import com.cyanogenmod.filemanager.commands.java.JavaExecutableFactory;
import com.cyanogenmod.filemanager.commands.java.Program;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.Console;
import com.cyanogenmod.filemanager.console.ConsoleAllocException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.console.OperationTimeoutException;
import com.cyanogenmod.filemanager.console.ReadOnlyFilesystemException;
import com.cyanogenmod.filemanager.model.AID;
import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.model.Identity;
import com.cyanogenmod.filemanager.model.User;
import com.cyanogenmod.filemanager.util.AIDHelper;

import java.util.ArrayList;

/**
 * An implementation of a {@link Console} based on a java implementation.<br/>
 * <br/>
 * This console is a non-privileged console an many of the functionality is not implemented
 * because can't be obtain from java api.
 */
public final class JavaConsole extends Console {

    private static final String TAG = "JavaConsole"; //$NON-NLS-1$

    private boolean mActive;

    private final Context mCtx;
    private final int mBufferSize;

    /**
     * Constructor of <code>JavaConsole</code>
     *
     * @param ctx The current context
     * @param bufferSize The buffer size
     */
    public JavaConsole(Context ctx, int bufferSize) {
        super();
        this.mCtx = ctx;
        this.mBufferSize = bufferSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alloc() throws ConsoleAllocException {
        try {
            if (isTrace()) {
                Log.v(TAG, "Allocating Java console"); //$NON-NLS-1$
            }
            this.mActive = true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to allocate Java console", e); //$NON-NLS-1$
            throw new ConsoleAllocException("failed to build console", e); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dealloc() {
        if (isTrace()) {
            Log.v(TAG, "Deallocating Java console"); //$NON-NLS-1$
        }
        this.mActive = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void realloc() throws ConsoleAllocException {
        dealloc();
        alloc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutableFactory getExecutableFactory() {
        return new JavaExecutableFactory(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity getIdentity() {
        AID aid = AIDHelper.getAID(Process.myUid());
        if (aid == null) return null;
        return new Identity(
                new User(aid.getId(), aid.getName()),
                new Group(aid.getId(), aid.getName()),
                new ArrayList<Group>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivileged() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return this.mActive;
    }

    /**
     * Method that returns the current context
     *
     * @return Context The current context
     */
    public Context getCtx() {
        return this.mCtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void execute(Executable executable) throws ConsoleAllocException,
                                InsufficientPermissionsException, NoSuchFileOrDirectory,
                                OperationTimeoutException, ExecutionException,
                                CommandNotFoundException, ReadOnlyFilesystemException {
        // Check that the program is a java program
        try {
            Program p = (Program)executable;
            p.isTrace();
        } catch (Throwable e) {
            Log.e(TAG, String.format("Failed to resolve program: %s", //$NON-NLS-1$
                    executable.getClass().toString()), e);
            throw new CommandNotFoundException("executable is not a program", e); //$NON-NLS-1$
        }

        //Auditing program execution
        if (isTrace()) {
            Log.v(TAG, String.format("Executing program: %s", //$NON-NLS-1$
                    executable.getClass().toString()));
        }

        // Execute the program
        final Program program = (Program)executable;
        program.setTrace(isTrace());
        program.setBufferSize(this.mBufferSize);
        if (program.isAsynchronous()) {
            // Execute in a thread
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        program.execute();
                    } catch (Exception e) {
                        // Program must use onException to communicate exceptions
                        Log.v(TAG,
                                String.format("Async execute failed program: %s", //$NON-NLS-1$
                                program.getClass().toString()));
                    }
                }
            };
            t.start();

        } else {
            // Synchronous execution
            program.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCancel() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSendSignal(SIGNAL signal) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onEnd() {
        return false;
    }

}