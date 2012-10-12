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

package com.cyanogenmod.explorer.commands.shell;

import java.io.File;
import java.io.FileWriter;

import com.cyanogenmod.explorer.commands.AsyncResultListener;
import com.cyanogenmod.explorer.util.CommandHelper;

/**
 * A class for testing exec command.
 *
 * @see ExecCommand
 */
public class ExecCommandTest extends AbstractConsoleTest {

    private static final String EXEC_CMD = "/sdcard/source.sh"; //$NON-NLS-1$
    private static final String EXEC_PROGRAM =
            "#!/system/bin/sh\necho \"List of files:\"\nls -la\n"; //$NON-NLS-1$

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
     * Method that performs a test over known search results.
     *
     * @throws Exception If test failed
     */
    public void testFindWithPartialResult() throws Exception {
        try {
            // Create the test program
            FileWriter fw = new FileWriter(new File(EXEC_CMD));
            fw.write(EXEC_PROGRAM);
            fw.close();

            // Execute the test program
            this.mNewPartialData = false;
            CommandHelper.exec(getContext(), EXEC_CMD, new AsyncResultListener() {
                public void onAsyncStart() {
                    /**NON BLOCK**/
                }
                public void onAsyncEnd(boolean cancelled) {
                    synchronized (ExecCommandTest.this.mSync) {
                        ExecCommandTest.this.mSync.notifyAll();
                    }
                }
                public void onException(Exception cause) {
                    fail(cause.toString());
                }
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
