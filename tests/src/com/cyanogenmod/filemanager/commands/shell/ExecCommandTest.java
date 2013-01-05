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

import android.os.Environment;
import android.test.suitebuilder.annotation.MediumTest;

import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.commands.WriteExecutable;
import com.cyanogenmod.filemanager.model.Permissions;
import com.cyanogenmod.filemanager.util.CommandHelper;

import java.io.OutputStream;

/**
 * A class for testing exec command.
 *
 * @see ExecCommand
 */
public class ExecCommandTest extends AbstractConsoleTest {

    private static final String EXEC_CMD =
            Environment.getDataDirectory().getAbsolutePath() + "/source.sh"; //$NON-NLS-1$
    private static final String EXEC_PROGRAM =
            "#!/system/bin/sh\necho \"List of files:\"\nls -la\n"; //$NON-NLS-1$
    private static final String EXEC_CMD_PERMISSIONS = "0755"; //$NON-NLS-1$

    /**
     * @hide
     */
    final Object mSync = new Object();
    /**
     * @hide
     */
    boolean mNewPartialData;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a test over known executable program.
     *
     * @throws Exception If test failed
     */
    @MediumTest
    public void testExecWithPartialResult() throws Exception {
        try {
            // Create the test program
            WriteExecutable writeCmd =
                    CommandHelper.write(getContext(), EXEC_CMD, null, getConsole());
            OutputStream os = writeCmd.createOutputStream();
            os.write(EXEC_PROGRAM.getBytes());
            writeCmd.end();

            // Enable execute permission
            CommandHelper.changePermissions(
                    getContext(),
                    EXEC_CMD,
                    Permissions.fromOctalString(EXEC_CMD_PERMISSIONS),
                    getConsole());

            // Execute the test program
            this.mNewPartialData = false;
            CommandHelper.exec(getContext(), EXEC_CMD, new AsyncResultListener() {
                @Override
                public void onAsyncStart() {
                    /**NON BLOCK**/
                }
                @Override
                public void onAsyncEnd(boolean cancelled) {
                    synchronized (ExecCommandTest.this.mSync) {
                        ExecCommandTest.this.mSync.notify();
                    }
                }
                @Override
                public void onAsyncExitCode(int exitCode) {
                    /**NON BLOCK**/
                }
                @Override
                public void onException(Exception cause) {
                    fail(String.valueOf(cause));
                }
                @Override
                public void onPartialResult(Object results) {
                    ExecCommandTest.this.mNewPartialData = true;
                }
            }, getConsole());
            synchronized (ExecCommandTest.this.mSync) {
                ExecCommandTest.this.mSync.wait(15000L);
            }
            assertTrue("no new partial data", this.mNewPartialData); //$NON-NLS-1$

        } finally {
            try {
                CommandHelper.deleteFile(getContext(), EXEC_CMD, getConsole());
            } catch (Exception e) {/**NON BLOCK**/}
        }
    }

}
