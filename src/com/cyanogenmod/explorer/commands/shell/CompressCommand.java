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

import com.cyanogenmod.explorer.commands.AsyncResultListener;
import com.cyanogenmod.explorer.commands.CompressExecutable;
import com.cyanogenmod.explorer.commands.SIGNAL;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;

/**
 * A class for compress file system objects
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?tar"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?gzip"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?bzip2"}
 */
public class CompressCommand extends AsyncResultProgram implements CompressExecutable {

    /**
     * An enumeration of implemented compression modes.
     */
    public enum CompressionMode {
        /**
         * Without compression
         */
        NONE("", "", ""), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        /**
         * Compress using Gzip algorithm
         */
        GZIP(GZIP_ID, "z", "gz"), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Compress using Bzip algorithm
         */
        BZIP(BZIP_ID, "j", "bz2"); //$NON-NLS-1$ //$NON-NLS-2$

        String mId;
        String mFlag;
        String mExtension;

        /**
         * Constructor of <code>CompressionMode</code>
         *
         * @param id The command identifier
         * @param flag The tar compression flag
         * @param extension The file extension
         */
        private CompressionMode(String id, String flag, String extension) {
            this.mId = id;
            this.mFlag = flag;
            this.mExtension = extension;
        }
    }

    private static final String TAR_ID = "compress"; //$NON-NLS-1$
    private static final String GZIP_ID = "gzip"; //$NON-NLS-1$
    private static final String BZIP_ID = "bzip"; //$NON-NLS-1$

    private Boolean mResult;
    private String mPartial;

    private final String mOutFile;

    /**
     * Constructor of <code>CompressCommand</code>. This method creates an archive-compressed
     * file from one or various file system objects.
     *
     * @param mode The compression mode
     * @param dst The absolute path of the new compress file
     * @param src An array of file system objects to compress
     * @param asyncResultListener The partial result listener
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public CompressCommand(
            CompressionMode mode, String dst, String[] src, AsyncResultListener asyncResultListener)
            throws InvalidCommandDefinitionException {
        super(TAR_ID, asyncResultListener, new String[]{mode.mFlag, dst});
        addExpandedArguments(src, true);

        // Create the output file
        this.mOutFile = dst;
    }

    /**
     * Constructor of <code>CompressCommand</code>. This method creates a compressed
     * file from one file.
     *
     * @param mode The compression mode
     * @param src The file to compress
     * @param asyncResultListener The partial result listener
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public CompressCommand(
            CompressionMode mode, String src, AsyncResultListener asyncResultListener)
            throws InvalidCommandDefinitionException {
        super(mode.mId, asyncResultListener, resolveArguments(mode, src));

        // Create the output file
        this.mOutFile = resolveOutputFile(mode, src);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartParsePartialResult() {
        this.mResult = Boolean.FALSE;
        this.mPartial = ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEndParsePartialResult(boolean canceled) {
        // Send the last partial data
        if (this.mPartial != null && this.mPartial.length() > 0) {
            if (getAsyncResultListener() != null) {
                getAsyncResultListener().onPartialResult(this.mPartial);
            }
        }
        this.mPartial = ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onParsePartialResult(final String partialIn) {
        if (partialIn == null || partialIn.length() ==0) return;
        boolean endsWithNewLine = partialIn.endsWith("\n"); //$NON-NLS-1$
        String[] lines = partialIn.split("\n"); //$NON-NLS-1$

        // Append the pending data to the first line
        lines[0] = this.mPartial + lines[0];

        // Return all the lines, except the last
        for (int i = 0; i < lines.length-1; i++) {
            if (getAsyncResultListener() != null) {
                getAsyncResultListener().onPartialResult(lines[i]);
            }
        }

        // Return the last line?
        if (endsWithNewLine) {
            if (getAsyncResultListener() != null) {
                getAsyncResultListener().onPartialResult(lines[lines.length-1]);
            }
            this.mPartial = ""; //$NON-NLS-1$
        } else {
            // Save the partial for next calls
            this.mPartial = lines[lines.length-1];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onParseErrorPartialResult(String partialErr) {/**NON BLOCK**/}

    /**
     * {@inheritDoc}
     */
    @Override
    public SIGNAL onRequestEnd() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getResult() {
        return this.mResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {

        //Ignore exit code 143 (canceled)
        //Ignore exit code 137 (kill -9)
        if (exitCode != 0 && exitCode != 1 && exitCode != 143 && exitCode != 137) {
            throw new ExecutionException(
                        "exitcode != 0 && != 143 && != 137"); //$NON-NLS-1$
        }

        // Correct
        this.mResult = Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutCompressedFile() {
        return this.mOutFile;
    }

    /**
     * Method that resolves the arguments for the compression
     *
     * @return String[] The arguments
     */
    private static String[] resolveArguments(CompressionMode mode, String src) {
        switch (mode) {
            case GZIP:
            case BZIP:
                return new String[]{src};
            default:
                return new String[]{};
        }
    }

    /**
     * Method that resolves the output path of the compressed file
     *
     * @return String The output path of the compressed file
     */
    private static String resolveOutputFile(CompressionMode mode, String src) {
        return String.format("%s.%s", src, mode.mExtension); //$NON-NLS-1$
    }
}
