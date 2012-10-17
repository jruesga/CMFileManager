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
import com.cyanogenmod.explorer.commands.SIGNAL;
import com.cyanogenmod.explorer.commands.UncompressExecutable;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.util.FileHelper;

import java.io.File;

/**
 * A class for uncompress file system objects.
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?tar"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?unzip"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?gunzip"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?bunzip2"}
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?xz"}
 */
public class UncompressCommand extends AsyncResultProgram implements UncompressExecutable {

    /**
     * An enumeration of implemented uncompression modes.
     */
    public enum UncompressionMode {
        /**
         * Uncompress using Tar algorithm
         */
        A_UNTAR(UNTAR_ID, "", "tar", true), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Tar algorithm
         */
        A_UNZIP(UNZIP_ID, "", "zip", true), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Gzip algorithm
         */
        AC_GUNZIP(GUNZIP_ID, "z", "tar.gz", true), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Gzip algorithm
         */
        AC_GUNZIP2(GUNZIP_ID, "z", "tgz", true), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Bzip algorithm
         */
        AC_BUNZIP(BUNZIP_ID, "j", "tar.bz2", true), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Lzma algorithm
         */
        AC_UNLZMA(UNLZMA_ID, "a", "tar.lzma", true), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Gzip algorithm
         */
        C_GUNZIP(GUNZIP_ID, "", "gz", false), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Bzip algorithm
         */
        C_BUNZIP(BUNZIP_ID, "", "bz2", false), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Lzma algorithm
         */
        C_UNLZMA(UNLZMA_ID, "", "lzma", false), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Unix compress algorithm
         */
        C_UNCOMPRESS(UNCOMPRESS_ID, "", ".Z", false), //$NON-NLS-1$ //$NON-NLS-2$
        /**
         * Uncompress using Unix compress algorithm
         */
        C_UNXZ(UNXZ_ID, "", ".xz", false); //$NON-NLS-1$ //$NON-NLS-2$

        String mId;
        String mFlag;
        String mExtension;
        boolean mArchive;

        /**
         * Constructor of <code>UncompressionMode</code>
         *
         * @param id The command identifier
         * @param flag The tar compression flag
         * @param extension The file extension
         * @param archive If the file is an archive or archive-compressed
         */
        private UncompressionMode(String id, String flag, String extension, boolean archive) {
            this.mId = id;
            this.mFlag = flag;
            this.mExtension = extension;
            this.mArchive = archive;
        }
    }

    private static final String UNTAR_ID = "untar"; //$NON-NLS-1$
    private static final String UNZIP_ID = "unzip"; //$NON-NLS-1$
    private static final String GUNZIP_ID = "gunzip"; //$NON-NLS-1$
    private static final String BUNZIP_ID = "bunzip"; //$NON-NLS-1$
    private static final String UNLZMA_ID = "unlzma"; //$NON-NLS-1$
    private static final String UNCOMPRESS_ID = "uncompress"; //$NON-NLS-1$
    private static final String UNXZ_ID = "unxz"; //$NON-NLS-1$

    private Boolean mResult;
    private String mPartial;

    private final String mOutFile;
    private final boolean mIsArchive;

    /**
     * Constructor of <code>UncompressCommand</code>.<br/>
     * <br/>
     * <ul>
     * <li>For archive and archive-compressed files, the file is extracted in a directory
     * of the current location of the file with the name of the file without the extension.</li>
     * <li>For compressed files, the file is extracted in the same directory in a file without
     * the extension, and the source file is deleted.</li>
     * </ul>
     *
     * @param src The archive-compressed file
     * @param asyncResultListener The partial result listener
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public UncompressCommand(
            String src, AsyncResultListener asyncResultListener)
            throws InvalidCommandDefinitionException {
        super(resolveId(src), asyncResultListener, resolveArguments(src));

        // Check that have a valid
        UncompressionMode mode = getMode(src);
        if (mode == null) {
            throw new InvalidCommandDefinitionException(
                            "Unsupported uncompress mode"); //$NON-NLS-1$
        }

        // Retrieve information about the uncompress process
        this.mOutFile = resolveOutputFile(src);
        this.mIsArchive = mode.mArchive;
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
        if (exitCode != 0 && exitCode != 143 && exitCode != 137) {
            throw new ExecutionException(
                        "exitcode != 0 && != 1 && != 143 && != 137"); //$NON-NLS-1$
        }

        // Correct
        this.mResult = Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutUncompressedFile() {
        return this.mOutFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean IsArchive() {
        return this.mIsArchive;
    }

    /**
     * Method that resolves the identifier to use as command
     *
     * @param src The compressed file
     * @return String The identifier of the command
     */
    private static String resolveId(String src) {
        UncompressionMode mode = getMode(src);
        if (mode != null) {
            return mode.mId;
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Method that resolves the arguments for the uncompression
     *
     * @return String[] The arguments
     */
    private static String[] resolveArguments(String src) {
        String name = FileHelper.getName(src);
        File dst = new File(new File(src).getParent(), name);
        UncompressionMode mode = getMode(src);
        if (mode != null) {
            switch (mode) {
                case A_UNTAR:
                case AC_GUNZIP:
                case AC_GUNZIP2:
                case AC_BUNZIP:
                case AC_UNLZMA:
                    return new String[]{mode.mFlag, dst.getAbsolutePath(), src};

                case A_UNZIP:
                    return new String[]{dst.getAbsolutePath(), src};

                case C_GUNZIP:
                case C_BUNZIP:
                case C_UNLZMA:
                case C_UNCOMPRESS:
                case C_UNXZ:
                    return new String[]{src};

                default:
                    break;
            }
        }
        return new String[]{};
    }

    /**
     * Method that resolves the output path of the uncompressed file
     *
     * @return String The output path of the uncompressed file
     */
    private static String resolveOutputFile(String src) {
        String name = FileHelper.getName(src);
        File dst = new File(new File(src).getParent(), name);
        return dst.getAbsolutePath();
    }

    /**
     * Method that returns the uncompression mode from the compressed file
     *
     * @param src The compressed file
     * @return UncompressionMode The uncompression mode. <code>null</code> if no mode found
     */
    private static UncompressionMode getMode(String src) {
        String extension = FileHelper.getExtension(src);
        UncompressionMode[] modes = UncompressionMode.values();
        for (int i = 0; i < modes.length; i++) {
            UncompressionMode mode = modes[i];
            if (mode.mExtension.compareTo(extension) == 0) {
                return mode;
            }
        }
        return null;
    }
}
