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

import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.commands.UncompressExecutable;
import com.cyanogenmod.filemanager.util.CommandHelper;

/**
 * A class for testing the uncompression of file system objects.
 *
 * @see UncompressCommand
 */
public class UncompressCommandTest extends AbstractConsoleTest {

    private static final String TAG = "UncompressCommandTest"; //$NON-NLS-1$

    /**
     * @hide
     */
    final Object mSync = new Object();
    /**
     * @hide
     */
    boolean mNewPartialData;
    /**
     * @hide
     */
    boolean mNormalEnd;
    /**
     * @hide
     */
    boolean mResult;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs the test of uncompress data in TAR format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnTAR() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in ZIP format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnZIP() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in GZIP format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnTarGZIP() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in BZIP format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnTarBZIP() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in LZMA format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnTarLZMA() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in GZIP format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnGZIP() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in BZIP format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnBZIP() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in LZMA format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnLZMA() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in Unix compress format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnCompress() throws Exception {
        // Can't not reproduce without sample compress file
    }

    /**
     * Method that performs the test of uncompress data in XZ format.
     *
     * @throws Exception If test failed
     */
    @LargeTest
    @SuppressWarnings("unused")
    public void testArchiveUnXZ() throws Exception {
        // Can't not reproduce without sample compress file
    }


    /**
     * Method that uncompress data.
     *
     * @param src The compressed file
     * @param expectOutput If the test need to expect output in the uncompress operation
     * @throws Exception If test failed
     */
    @SuppressWarnings("unused")
    private void testUncompress(String src, boolean expectOutput) throws Exception {
        UncompressExecutable cmd = null;
        try {
            this.mNewPartialData = false;
            this.mNormalEnd = false;
            cmd =
                CommandHelper.uncompress(
                    getContext(), src, null, new AsyncResultListener() {
                        @Override
                        public void onAsyncStart() {
                            /**NON BLOCK**/
                        }
                        @Override
                        public void onAsyncEnd(boolean cancelled) {
                            synchronized (UncompressCommandTest.this.mSync) {
                                UncompressCommandTest.this.mNormalEnd = true;
                                UncompressCommandTest.this.mSync.notify();
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
                        public void onPartialResult(Object result) {
                            UncompressCommandTest.this.mNewPartialData = true;
                            Log.d(TAG, (String)result);
                        }
                   }, getConsole());
            synchronized (UncompressCommandTest.this.mSync) {
                UncompressCommandTest.this.mSync.wait(60000L);
            }
            try {
                if (!this.mNormalEnd && cmd != null && cmd.isCancellable() && !cmd.isCancelled()) {
                    cmd.cancel();
                }
            } catch (Exception e) {/**NON BLOCK**/}

            // Wait for result
            Thread.sleep(500L);

            if (expectOutput) {
                assertTrue("no new partial data", this.mNewPartialData); //$NON-NLS-1$
            }
            assertNotNull("cmd != null", cmd); //$NON-NLS-1$
            if (cmd != null) {
                assertTrue("return != true", cmd.getResult().booleanValue()); //$NON-NLS-1$
            }
        } finally {
            try {
                CommandHelper.deleteFile(getContext(), src, getConsole());
            } catch (Exception e) {/**NON BLOCK**/}
            try {
                if (cmd != null) {
                    if (cmd.IsArchive()) {
                        CommandHelper.deleteDirectory(
                                getContext(), cmd.getOutUncompressedFile(), getConsole());
                    } else {
                        CommandHelper.deleteFile(
                                getContext(), cmd.getOutUncompressedFile(), getConsole());
                    }
                }
            } catch (Exception e) {/**NON BLOCK**/}
        }
    }

}
