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
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.cyanogenmod.filemanager.commands.AsyncResultListener;
import com.cyanogenmod.filemanager.commands.WriteExecutable;
import com.cyanogenmod.filemanager.util.CommandHelper;

import java.io.OutputStream;
import java.util.Random;

/**
 * A class for testing write command.
 *
 * @see WriteCommand
 */
public class WriteCommandTest extends AbstractConsoleTest {

    private static final String WRITE_FILE_SMALL =
            Environment.getDataDirectory().getAbsolutePath() + "/write-test-s.txt"; //$NON-NLS-1$
    private static final String WRITE_FILE_LARGE =
            Environment.getDataDirectory().getAbsolutePath() + "/write-test-l.txt"; //$NON-NLS-1$
    private static final byte[] TEST_DATA = new byte[]{(byte)33, (byte)36, '\n'};

    private static final int DATA_SIZE = 4096;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootConsoleNeeded() {
        return true;
    }

    /**
     * Method that performs a small write test.
     *
     * @throws Exception If an exception occurs while executing the test
     */
    @SmallTest
    public void testSmallWriteWithPartialResult() throws Exception {
        try {
            WriteExecutable cmd =
                    CommandHelper.write(getContext(),
                    WRITE_FILE_SMALL, new AsyncResultListener() {
                            @Override
                            public void onAsyncStart() {/**NON BLOCK**/}
                            @Override
                            public void onAsyncEnd(boolean cancelled) {/**NON BLOCK**/}
                            @Override
                            public void onAsyncExitCode(int exitCode) {/**NON BLOCK**/}
                            @Override
                            public void onException(Exception cause) {
                                fail(String.valueOf(cause));
                            }
                            @Override
                            public void onPartialResult(Object results) {/**NON BLOCK**/}
                       }, getConsole());
            OutputStream os = cmd.createOutputStream();
            os.write(TEST_DATA, 0, TEST_DATA.length);
            cmd.end();

            // Wait for allow close all instrumentation data
            Thread.sleep(2500L);
        } finally {
            try {
                CommandHelper.deleteFile(getContext(), WRITE_FILE_SMALL, getConsole());
            } catch (Exception e) {/**NON BLOCK**/}
        }
    }

    /**
     * Method that performs a large write test.
     *
     * @throws Exception If an exception occurs while executing the test
     */
    @LargeTest
    public void testLargeWriteWithPartialResult() throws Exception {
        try {
            WriteExecutable cmd =
                    CommandHelper.write(getContext(),
                    WRITE_FILE_LARGE, new AsyncResultListener() {
                            @Override
                            public void onAsyncStart() {/**NON BLOCK**/}
                            @Override
                            public void onAsyncEnd(boolean cancelled) {/**NON BLOCK**/}
                            @Override
                            public void onAsyncExitCode(int exitCode) {/**NON BLOCK**/}
                            @Override
                            public void onException(Exception cause) {
                                fail(String.valueOf(cause));
                            }
                            @Override
                            public void onPartialResult(Object results) {/**NON BLOCK**/}
                       }, getConsole());
            OutputStream os = cmd.createOutputStream();
            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                byte[] data = new byte[DATA_SIZE];
                random.nextBytes(data);
                os.write(data, 0, data.length);
            }
            cmd.end();

            // Wait for allow close all instrumentation data
            Thread.sleep(2500L);
        } finally {
            try {
                CommandHelper.deleteFile(getContext(), WRITE_FILE_LARGE, getConsole());
            } catch (Exception e) {/**NON BLOCK**/}
        }
    }

}
