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

package com.cyanogenmod.explorer.console.shell;

import android.util.Log;

import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.commands.AsyncResultExecutable;
import com.cyanogenmod.explorer.commands.Executable;
import com.cyanogenmod.explorer.commands.ExecutableFactory;
import com.cyanogenmod.explorer.commands.GroupsExecutable;
import com.cyanogenmod.explorer.commands.IdentityExecutable;
import com.cyanogenmod.explorer.commands.ProcessIdExecutable;
import com.cyanogenmod.explorer.commands.shell.AsyncResultProgram;
import com.cyanogenmod.explorer.commands.shell.Command;
import com.cyanogenmod.explorer.commands.shell.InvalidCommandDefinitionException;
import com.cyanogenmod.explorer.commands.shell.Program;
import com.cyanogenmod.explorer.commands.shell.Shell;
import com.cyanogenmod.explorer.commands.shell.ShellExecutableFactory;
import com.cyanogenmod.explorer.commands.shell.SyncResultProgram;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.Console;
import com.cyanogenmod.explorer.console.ConsoleAllocException;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.console.NoSuchFileOrDirectory;
import com.cyanogenmod.explorer.console.OperationTimeoutException;
import com.cyanogenmod.explorer.console.ReadOnlyFilesystemException;
import com.cyanogenmod.explorer.model.Identity;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.FileHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of a {@link Console} based in the execution of shell commands.<br/>
 * <br/>
 * This class holds a <code>shell bash</code> program associated with the application, being
 * a wrapper to execute all other programs (like shell does in linux), capturing the
 * output (stdin and stderr) and the exit code of the program executed.
 */
public abstract class ShellConsole extends Console implements Program.ProgramListener {

    private static final String TAG = "ShellConsole"; //$NON-NLS-1$

    private static final long DEFAULT_TIMEOUT = 20000L;

    private static final int DEFAULT_BUFFER = 512;

    //Shell References
    private final Shell mShell;
    private final String mInitialDirectory;
    private Identity mIdentity;

    //Process References
    private final Object mSync = new Object();
    private final Object mPartialSync = new Object();
    /**
     * @hide
     */
    boolean mActive = false;
    /**
     * @hide
     */
    boolean mReady = false;
    private boolean mFinished = true;
    private Process mProc = null;
    /**
     * @hide
     */
    Program mActiveCommand = null;
    /**
     * @hide
     */
    boolean mCanceled;
    /**
     * @hide
     */
    boolean mStarted;

    //Buffers
    private InputStream mIn = null;
    private InputStream mErr = null;
    private OutputStream mOut = null;
    /**
     * @hide
     */
    StringBuffer mSbIn = null;
    /**
     * @hide
     */
    StringBuffer mSbErr = null;

    private final SecureRandom mRandom;
    private String mStartControlPattern;
    private String mEndControlPattern;

    /**
     * @hide
     */
    int mBufferSize;

    private final ShellExecutableFactory mExecutableFactory;

    /**
     * Constructor of <code>ShellConsole</code>.
     *
     * @param shell The shell used to execute commands
     * @throws FileNotFoundException If the default initial directory not exists
     * @throws IOException If initial directory can't not be resolved
     */
    public ShellConsole(Shell shell) throws FileNotFoundException, IOException {
        this(shell, Preferences.getSharedPreferences().getString(
                            ExplorerSettings.SETTINGS_INITIAL_DIR.getId(),
                            (String)ExplorerSettings.SETTINGS_INITIAL_DIR.getDefaultValue()));
    }

    /**
     * Constructor of <code>ShellConsole</code>.
     *
     * @param shell The shell used to execute commands
     * @param initialDirectory The initial directory of the shell
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be resolved
     */
    public ShellConsole(Shell shell, String initialDirectory)
            throws FileNotFoundException, IOException {
        super();
        this.mShell = shell;
        this.mExecutableFactory = new ShellExecutableFactory(this);

        this.mBufferSize = DEFAULT_BUFFER;

        //Resolve and checks the initial directory
        File f = new File(initialDirectory);
        while (FileHelper.isSymlink(f)) {
            f = FileHelper.resolveSymlink(f);
        }
        if (!f.exists() || !f.isDirectory()) {
            throw new FileNotFoundException(f.toString());
        }
        this.mInitialDirectory = initialDirectory;

        //Restart the buffers
        this.mSbIn = new StringBuffer();
        this.mSbErr = new StringBuffer();

        //Generate an aleatory secure random generator
        try {
            this.mRandom = SecureRandom.getInstance("SHA1PRNG"); //$NON-NLS-1$
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutableFactory getExecutableFactory() {
        return this.mExecutableFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity getIdentity() {
        return this.mIdentity;
    }



    /**
     * Method that returns the buffer size
     *
     * @return int The buffer size
     */
    public int getBufferSize() {
        return this.mBufferSize;
    }

    /**
     * Method that sets the buffer size
     *
     * @param bufferSize the The buffer size
     */
    public void setBufferSize(int bufferSize) {
        this.mBufferSize = bufferSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void alloc() throws ConsoleAllocException {
        try {
            //Create command string
            List<String> cmd = new ArrayList<String>();
            cmd.add(this.mShell.getCommand());
            if (this.mShell.getArguments() != null && this.mShell.getArguments().length() > 0) {
                cmd.add(this.mShell.getArguments());
            }

            //Create the process
            Runtime rt = Runtime.getRuntime();
            this.mProc =
                    rt.exec(
                            cmd.toArray(new String[cmd.size()]),
                            null,
                            new File(this.mInitialDirectory));
            synchronized (this.mSync) {
                this.mActive = true;
            }
            if (isTrace()) {
                Log.v(TAG,
                        String.format("Create console %s, command: %s, args: %s",  //$NON-NLS-1$
                                this.mShell.getId(),
                                this.mShell.getCommand(),
                                this.mShell.getArguments()));
            }

            //Allocate buffers
            this.mIn = new BufferedInputStream(this.mProc.getInputStream(), this.mBufferSize);
            this.mErr = new BufferedInputStream(this.mProc.getErrorStream(), this.mBufferSize);
            this.mOut = new BufferedOutputStream(this.mProc.getOutputStream(), this.mBufferSize);
            if (this.mIn == null || this.mErr == null || this.mOut == null) {
                try {
                    dealloc();
                } catch (Throwable ex) {
                    /**NON BLOCK**/
                }
                throw new ConsoleAllocException("Console buffer allocation error."); //$NON-NLS-1$
            }

            //Starts a thread for extract output, and check timeout
            createStdInThread(this.mIn);
            createStdErrThread(this.mErr);

            //Wait for thread start
            Thread.sleep(50L);

            //Check if process its active
            checkIfProcessExits();
            synchronized (this.mSync) {
                if (!this.mActive) {
                    throw new ConsoleAllocException("Shell not started."); //$NON-NLS-1$
                }
            }

            // Retrieve the PID of the shell
            ProcessIdExecutable processIdCmd =
                    getExecutableFactory().
                        newCreator().createShellProcessIdExecutable();
            execute(processIdCmd);
            Integer pid = processIdCmd.getResult();
            if (pid == null) {
                throw new ConsoleAllocException(
                        "Can't retrieve the PID of the shell."); //$NON-NLS-1$
            }
            this.mShell.setPid(pid.intValue());

            //Retrieve identity
            IdentityExecutable identityCmd =
                    getExecutableFactory().newCreator().createIdentityExecutable();
            execute(identityCmd);
            this.mIdentity = identityCmd.getResult();
            if (this.mIdentity.getGroups().size() == 0) {
                //Try with groups
                GroupsExecutable groupsCmd =
                        getExecutableFactory().newCreator().createGroupsExecutable();
                execute(groupsCmd);
                this.mIdentity.setGroups(groupsCmd.getResult());
            }

        } catch (Exception ex) {
            try {
                dealloc();
            } catch (Throwable ex2) {
                /**NON BLOCK**/
            }
            throw new ConsoleAllocException("Console allocation error.", ex); //$NON-NLS-1$
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void dealloc() {
        synchronized (this.mSync) {
            if (this.mActive) {
                this.mProc.destroy();
                this.mActive = false;
                this.mFinished = true;

                //Close buffers
                try {
                    if (this.mIn != null) {
                        this.mIn.close();
                    }
                } catch (Throwable ex) {
                    /**NON BLOCK**/
                }
                try {
                    if (this.mErr != null) {
                        this.mErr.close();
                    }
                } catch (Throwable ex) {
                    /**NON BLOCK**/
                }
                try {
                    if (this.mOut != null) {
                        this.mOut.close();
                    }
                } catch (Throwable ex) {
                    /**NON BLOCK**/
                }
                this.mIn = null;
                this.mErr = null;
                this.mOut = null;
                this.mSbIn = null;
                this.mSbErr = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void realloc() throws ConsoleAllocException {
        dealloc();
        alloc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void execute(final Executable executable)
            throws ConsoleAllocException, InsufficientPermissionsException,
            CommandNotFoundException, NoSuchFileOrDirectory,
            OperationTimeoutException, ExecutionException, ReadOnlyFilesystemException {

        //Is a program?
        if (!(executable instanceof Program)) {
            throw new CommandNotFoundException("executable not instanceof Program"); //$NON-NLS-1$
        }

        //Asynchronous or synchronous execution?
        final Program program = (Program)executable;
        if (executable instanceof AsyncResultExecutable) {
            Thread asyncThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Synchronous execution (but asynchronous running in a thread)
                    //This way syncExecute is locked until this thread ends
                    try {
                        if (ShellConsole.this.syncExecute(program, true)) {
                            ShellConsole.this.syncExecute(program, false);
                        }
                    } catch (Exception ex) {
                        if (((AsyncResultExecutable)executable).getAsyncResultListener() != null) {
                            ((AsyncResultExecutable)executable).
                                getAsyncResultListener().onException(ex);
                        } else {
                            //Capture exception
                            Log.e(TAG, "Fail asynchronous execution", ex); //$NON-NLS-1$
                        }
                    }
                }
            });
            asyncThread.start();
        } else {
            //Synchronous execution (2 tries with 1 reallocation)
            if (syncExecute(program, true)) {
                syncExecute(program, false);
            }
        }
    }

    /**
     * Method for execute a program command in the operating system layer in a synchronous way.
     *
     * @param program The program to execute
     * @param reallocate If the console must be reallocated on i/o error
     * @return boolean If the console was reallocated
     * @throws ConsoleAllocException If the console is not allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @hide
     */
    synchronized boolean syncExecute(final Program program, boolean reallocate)
            throws ConsoleAllocException, InsufficientPermissionsException,
            CommandNotFoundException, NoSuchFileOrDirectory,
            OperationTimeoutException, ExecutionException, ReadOnlyFilesystemException {

        try {
            //Check the console status before send command
            checkConsole();

            synchronized (this.mSync) {
                if (!this.mActive) {
                    throw new ConsoleAllocException("No console allocated"); //$NON-NLS-1$
                }
            }

            //Saves the active command reference
            this.mActiveCommand = program;

            //Reset the buffers
            this.mReady = false;
            this.mStarted = false;
            this.mCanceled = false;
            this.mSbIn = new StringBuffer();
            this.mSbErr = new StringBuffer();

            //Random start/end identifiers
            String startId1 =
                    String.format("/#%d#/", Long.valueOf(this.mRandom.nextLong())); //$NON-NLS-1$
            String startId2 =
                    String.format("/#%d#/", Long.valueOf(this.mRandom.nextLong())); //$NON-NLS-1$
            String endId1 =
                    String.format("/#%d#/", Long.valueOf(this.mRandom.nextLong())); //$NON-NLS-1$
            String endId2 =
                    String.format("/#%d#/", Long.valueOf(this.mRandom.nextLong())); //$NON-NLS-1$

            //Create command string
            String cmd = program.getCommand();
            String args = program.getArguments();

            //Audit command
            if (isTrace()) {
                Log.v(TAG,
                        String.format("%s-%s, command: %s, args: %s",  //$NON-NLS-1$
                                ShellConsole.this.mShell.getId(),
                                program.getId(),
                                cmd,
                                args));
            }

            //Is asynchronous program? Then set asynchronous
            program.setProgramListener(this);
            if (program instanceof AsyncResultProgram) {
                ((AsyncResultProgram)program).setOnCancelListener(this);
                synchronized (this.mPartialSync) {
                    ((AsyncResultProgram)program).startParsePartialResult();
                }
            }

            //Send the command + a control code with exit code
            //The process has finished where control control code is present.
            //This control code is unique in every invocation and is secure random
            //generated (control code 1 + exit code + control code 2)
            try {
                this.mStartControlPattern = startId1 + "\\d{1,3}" + startId2 + "\\n"; //$NON-NLS-1$ //$NON-NLS-2$
                this.mEndControlPattern = endId1 + "\\d{1,3}" + endId2; //$NON-NLS-1$
                String startCmd =
                        Command.getStartCodeCommandInfo(
                                ExplorerApplication.getInstance().getResources());
                startCmd = String.format(
                        startCmd, "'" + startId1 +//$NON-NLS-1$
                        "'", "'" + startId2 + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                String endCmd =
                        Command.getExitCodeCommandInfo(
                                ExplorerApplication.getInstance().getResources());
                endCmd = String.format(
                        endCmd, "'" + endId1 + //$NON-NLS-1$
                        "'", "'" + endId2 + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                StringBuilder sb = new StringBuilder()
                    .append(startCmd)
                    .append(" ")  //$NON-NLS-1$
                    .append(cmd)
                    .append(" ")  //$NON-NLS-1$
                    .append(args)
                    .append(" ") //$NON-NLS-1$
                    .append(endCmd)
                    .append(FileHelper.NEWLINE);
                this.mOut.write(sb.toString().getBytes());
            } catch (InvalidCommandDefinitionException icdEx) {
                throw new CommandNotFoundException(
                        "ExitCodeCommandInfo not found", icdEx); //$NON-NLS-1$
            }

            //Now, wait for buffers to be filled
            synchronized (this.mSync) {
                if (program instanceof AsyncResultProgram) {
                    this.mSync.wait();
                } else {
                    this.mSync.wait(DEFAULT_TIMEOUT);
                    if (!this.mFinished) {
                        throw new OperationTimeoutException(DEFAULT_TIMEOUT, cmd);
                    }
                }
            }

            //End partial results?
            if (program instanceof AsyncResultProgram) {
                synchronized (this.mPartialSync) {
                    ((AsyncResultProgram)program).endParsePartialResult(this.mCanceled);
                }
            }

            //Retrieve exit code
            int exitCode = getExitCode(this.mSbIn);
            if (isTrace()) {
                Log.v(TAG,
                        String.format("%s-%s, command: %s, exitCode: %s",  //$NON-NLS-1$
                                ShellConsole.this.mShell.getId(),
                                program.getId(),
                                cmd,
                                String.valueOf(exitCode)));
            }

            // Process is not ready
            this.mReady = false;

            //Check if invocation was successfully or not
            if (!program.isIgnoreShellStdErrCheck()) {
                this.mShell.checkStdErr(this.mActiveCommand, exitCode, this.mSbErr.toString());
            }
            this.mShell.checkExitCode(exitCode);
            program.checkExitCode(exitCode);
            program.checkStdErr(exitCode, this.mSbErr.toString());

            //Parse the result? Only if not partial results
            if (program instanceof SyncResultProgram) {
                try {
                    ((SyncResultProgram)program).parse(
                            this.mSbIn.toString(), this.mSbErr.toString());
                } catch (ParseException pEx) {
                    throw new ExecutionException(
                            "SyncResultProgram parse failed", pEx); //$NON-NLS-1$
                }
            }

            //Invocation finished. Now program.getResult() has the result of
            //the operation, if any exists

        } catch (IOException ioEx) {
            if (reallocate) {
                realloc();
                return true;
            }
            throw new ExecutionException("Console allocation error.", ioEx); //$NON-NLS-1$

        } catch (InterruptedException ioEx) {
            if (reallocate) {
                realloc();
                return true;
            }
            throw new ExecutionException("Console allocation error.", ioEx); //$NON-NLS-1$

        } finally {
            //Dereference the active command
            this.mActiveCommand = null;
        }

        //Operation complete
        return false;
    }

    /**
     * Method that creates the standard input thread for read program response.
     *
     * @param in The standard input buffer
     * @return Thread The standard input thread
     */
    private Thread createStdInThread(final InputStream in) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int read = 0;
                try {
                    while (ShellConsole.this.mActive) {
                        //Read only one byte with active wait
                        final int r = in.read();
                        if (r == -1) {
                            break;
                        }
                        StringBuffer sb = new StringBuffer();
                        if (!ShellConsole.this.mCanceled) {
                            ShellConsole.this.mSbIn.append((char)r);
                            if (!ShellConsole.this.mStarted) {
                                ShellConsole.this.mStarted =
                                        isCommandStarted(ShellConsole.this.mSbIn);
                                sb.append(ShellConsole.this.mSbIn.toString());
                            } else {
                                sb.append((char)r);
                            }

                            //
                            if (ShellConsole.this.mStarted &&
                                   ShellConsole.this.mActiveCommand != null) {
                                if (!ShellConsole.this.mReady) {
                                    ShellConsole.this.mReady = true;
                                    ShellConsole.this.mActiveCommand.onProgramReady();
                                }
                                
                            }

                            //Notify asynchronous partial data
                            if (ShellConsole.this.mStarted &&
                                ShellConsole.this.mActiveCommand != null &&
                                ShellConsole.this.mActiveCommand instanceof AsyncResultProgram) {
                                AsyncResultProgram program =
                                        ((AsyncResultProgram)ShellConsole.this.mActiveCommand);
                                program.parsePartialResult(new String(new char[]{(char)r}));
                            }
                        }

                        //Has more data? Read with available as more as exists
                        //or maximum loop count is rebased
                        int count = 0;
                        while (in.available() > 0 && count < 10) {
                            count++;
                            int available = Math.min(in.available(),
                                                        ShellConsole.this.mBufferSize);
                            byte[] data = new byte[available];
                            read = in.read(data);

                            // Exit if active command is canceled
                            if (ShellConsole.this.mCanceled) continue;

                            final String s = new String(data, 0, read);
                            ShellConsole.this.mSbIn.append(s);
                            if (!ShellConsole.this.mStarted) {
                                ShellConsole.this.mStarted =
                                        isCommandStarted(ShellConsole.this.mSbIn);
                                sb.append(ShellConsole.this.mSbIn.toString());
                            } else {
                                sb.append(s);
                            }

                            //Notify asynchronous partial data
                            if (ShellConsole.this.mActiveCommand != null &&
                                ShellConsole.this.mActiveCommand  instanceof AsyncResultProgram) {
                                AsyncResultProgram program =
                                        ((AsyncResultProgram)ShellConsole.this.mActiveCommand);
                                program.parsePartialResult(s);
                            }

                            //Wait for buffer to be filled
                            try {
                                Thread.sleep(50L);
                            } catch (Throwable ex) {
                                /**NON BLOCK**/
                            }

                            //Check if the command has finished
                            if (isCommandFinished(ShellConsole.this.mSbIn)) {
                                //Notify the end
                                notifyProcessFinished();
                            }
                        }

                        //Audit (if not canceled)
                        if (!ShellConsole.this.mCanceled && isTrace()) {
                            Log.v(TAG,
                                    String.format("stdin: %s", sb.toString())); //$NON-NLS-1$
                        }

                        //Asynchronous programs can cause a lot of output, control buffers
                        //for a low memory footprint
                        if (ShellConsole.this.mActiveCommand != null &&
                                ShellConsole.this.mActiveCommand instanceof AsyncResultProgram) {
                            trimBuffer(ShellConsole.this.mSbIn);
                            trimBuffer(ShellConsole.this.mSbErr);
                        }

                        //Check if process has exited
                        checkIfProcessExits();
                    }
                } catch (IOException ioEx) {
                    notifyProcessExit(ioEx);
                }
            }
        });
        t.setName(String.format("%s", "stdin")); //$NON-NLS-1$//$NON-NLS-2$
        t.start();
        return t;
    }

    /**
     * Method that creates the standard error thread for read program response.
     *
     * @param err The standard error buffer
     * @return Thread The standard error thread
     */
    private Thread createStdErrThread(final InputStream err) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int read = 0;
                try {
                    while (ShellConsole.this.mActive) {
                        //Read only one byte with active wait
                        int r = err.read();
                        if (r == -1) {
                            break;
                        }
                        StringBuffer sb = new StringBuffer();
                        if (!ShellConsole.this.mCanceled) {
                            ShellConsole.this.mSbErr.append((char)r);
                            sb.append((char)r);

                            //Notify asynchronous partial data
                            if (ShellConsole.this.mStarted &&
                                ShellConsole.this.mActiveCommand != null &&
                                ShellConsole.this.mActiveCommand instanceof AsyncResultProgram) {
                                AsyncResultProgram program =
                                        ((AsyncResultProgram)ShellConsole.this.mActiveCommand);
                                program.parsePartialErrResult(new String(new char[]{(char)r}));
                            }
                        }

                        //Has more data? Read with available as more as exists
                        //or maximum loop count is rebased
                        int count = 0;
                        while (err.available() > 0 && count < 10) {
                            count++;
                            int available = Math.min(err.available(),
                                                        ShellConsole.this.mBufferSize);
                            byte[] data = new byte[available];
                            read = err.read(data);

                            // Exit if active command is canceled
                            if (ShellConsole.this.mCanceled) continue;

                            String s = new String(data, 0, read);
                            ShellConsole.this.mSbErr.append(s);
                            sb.append(s);

                            //Notify asynchronous partial data
                            if (ShellConsole.this.mActiveCommand != null &&
                                ShellConsole.this.mActiveCommand  instanceof AsyncResultProgram) {
                                AsyncResultProgram program =
                                        ((AsyncResultProgram)ShellConsole.this.mActiveCommand);
                                program.parsePartialErrResult(s);
                            }

                            //Wait for buffer to be filled
                            try {
                                Thread.sleep(50L);
                            } catch (Throwable ex) {
                                /**NON BLOCK**/
                            }
                        }

                        //Audit (if not canceled)
                        if (!ShellConsole.this.mCanceled && isTrace()) {
                            Log.v(TAG,
                                    String.format("stderr: %s", sb.toString())); //$NON-NLS-1$
                        }

                        //Asynchronous programs can cause a lot of output, control buffers
                        //for a low memory footprint
                        if (ShellConsole.this.mActiveCommand != null &&
                                ShellConsole.this.mActiveCommand instanceof AsyncResultProgram) {
                            trimBuffer(ShellConsole.this.mSbIn);
                            trimBuffer(ShellConsole.this.mSbErr);
                        }
                    }
                } catch (IOException ioEx) {
                    notifyProcessExit(ioEx);
                }
            }
        });
        t.setName(String.format("%s", "stderr")); //$NON-NLS-1$//$NON-NLS-2$
        t.start();
        return t;
    }

    /**
     * Method that checks the console status and restart the console
     * if this is unusable.
     *
     * @throws ConsoleAllocException If the console can't be reallocated
     */
    private void checkConsole() throws ConsoleAllocException {
        try {
            //Test write something to the buffer
            this.mOut.write(FileHelper.NEWLINE.getBytes());
            this.mOut.write(FileHelper.NEWLINE.getBytes());
        } catch (IOException ioex) {
            //Something is wrong with the buffers. Reallocate console.
            Log.w(TAG,
                "Something is wrong with the console buffers. Reallocate console.", //$NON-NLS-1$
                ioex);

            //Reallocate the damage console
            realloc();
        }
    }

    /**
     * Method that verifies if the process had exited.
     * @hide
     */
    void checkIfProcessExits() {
        try {
            if (this.mProc != null) {
                synchronized (ShellConsole.this.mSync) {
                    this.mProc.exitValue();
                }
                this.mActive = false; //Exited
            }
        } catch (IllegalThreadStateException itsEx) {
            //Not exited
        }
    }

    /**
     * Method that notifies the ending of the process.
     *
     * @param ex The exception, only if the process exit with a exception.
     * Otherwise null
     * @hide
     */
    void notifyProcessExit(Exception ex) {
        synchronized (ShellConsole.this.mSync) {
            if (this.mActive) {
                this.mSync.notify();
                this.mActive = false;
                this.mFinished = true;
                if (ex != null) {
                    Log.w(TAG, "Exits with exception", ex); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Method that notifies the ending of the command execution.
     * @hide
     */
    void notifyProcessFinished() {
        synchronized (ShellConsole.this.mSync) {
            if (this.mActive) {
                this.mReady = false;
                this.mSync.notify();
                this.mFinished = true;
            }
        }
    }

    /**
     * Method that returns if the command has started by checking the
     * standard input buffer. This method also removes the control start command
     * from the buffer, if it's present, leaving in the buffer the new data bytes.
     *
     * @param stdin The standard in buffer
     * @return boolean If the command has started
     * @hide
     */
    boolean isCommandStarted(StringBuffer stdin) {
        if (stdin == null) return false;
        Pattern pattern = Pattern.compile(this.mStartControlPattern);
        Matcher matcher = pattern.matcher(stdin.toString());
        if (matcher.find()) {
            stdin.replace(0, matcher.end(), ""); //$NON-NLS-1$
            return true;
        }
        return false;
    }

    /**
     * Method that returns if the command has finished by checking the
     * standard input buffer.
     *
     * @param stdin The standard in buffer
     * @return boolean If the command has finished
     * @hide
     */
    boolean isCommandFinished(StringBuffer stdin) {
        Pattern pattern = Pattern.compile(this.mEndControlPattern);
        if (stdin == null) return false;
        Matcher matcher = pattern.matcher(stdin.toString());
        return matcher.find();
    }

    /**
     * Method that returns the exit code of the last executed command.
     *
     * @param stdin The standard in buffer
     * @return int The exit code of the last executed command
     */
    private int getExitCode(StringBuffer stdin) {
        // If process was canceled, don't expect a exit code.
        // Returns always 143 code
        if (this.mCanceled) {
            return 143;
        }

        // Parse the stdin seeking exit code pattern
        String txt = stdin.toString();
        Pattern pattern = Pattern.compile(this.mEndControlPattern);
        Matcher matcher = pattern.matcher(txt);
        if (matcher.find()) {
            this.mSbIn = new StringBuffer(txt.substring(0, matcher.start()));
            String exitTxt = matcher.group();
            return Integer.parseInt(
                    exitTxt.substring(
                            exitTxt.indexOf("#/") + 2,  //$NON-NLS-1$
                            exitTxt.indexOf("/#", 2))); //$NON-NLS-1$
        }
        return 255;
    }

    /**
     * Method that trim a buffer, let in the buffer some
     * text to ensure that the exit code is in there.
     *
     * @param sb The buffer to trim
     * @hide
     */
    @SuppressWarnings("static-method") void trimBuffer(StringBuffer sb) {
        final int bufferSize = 200;
        if (sb.length() > bufferSize) {
            sb.delete(0, sb.length() - bufferSize);
        }
    }

    /**
     * Method that kill the current command.
     *
     * @return boolean If the program was killed
     * @hide
     */
    private boolean killCurrentCommand() {
        synchronized (this.mSync) {
            //Is synchronous program? Otherwise it can't be canceled
            if (!(this.mActiveCommand instanceof AsyncResultProgram)) {
                return false;
            }

            final AsyncResultProgram program = (AsyncResultProgram)this.mActiveCommand;
            if (program.getCommand() != null) {
                try {
                    if (program.isCancelable()) {
                        //Get the PID in background
                        Integer pid =
                                CommandHelper.getProcessId(
                                        null,
                                        this.mShell.getPid(),
                                        program.getCommand(),
                                        ExplorerApplication.getBackgroundConsole());
                        if (pid != null) {
                            android.os.Process.killProcess(pid.intValue());
                            try {
                                //Wait for process kill
                                Thread.sleep(100L);
                            } catch (Throwable ex) {
                                /**NON BLOCK**/
                            }
                            this.mCanceled = true;
                            notifyProcessFinished();
                            this.mSync.notify();
                            return this.mCanceled;
                        }
                    }
                } catch (Throwable ex) {
                    Log.w(TAG,
                            String.format("Unable to kill current program: %s", //$NON-NLS-1$
                                    program.getCommand()), ex);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCancel() {
        //Kill the current command on cancel request
        return killCurrentCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onRequestWrite(byte[] data) throws ExecutionException {
        try {
            // Method that write to the stdin the data requested by the program
            if (this.mReady && this.mOut != null) {
                this.mOut.write(data);
                return true;
            }
        } catch (Exception ex) {
            Log.w(TAG,
                    String.format("Unable to write data to program: %s", //$NON-NLS-1$
                            this.mActiveCommand.getCommand(), ex));
        }
        return false;
    }

}
