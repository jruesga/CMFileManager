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

package com.cyanogenmod.filemanager.commands.java;

import android.util.Log;

import com.cyanogenmod.filemanager.commands.CopyExecutable;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.console.NoSuchFileOrDirectory;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.util.MountPointHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * A class for copy a file or directory.
 */
public class CopyCommand extends Program implements CopyExecutable {

    private static final String TAG = "CopyCommand"; //$NON-NLS-1$

    private final String mSrc;
    private final String mDst;

    /**
     * Constructor of <code>CopyCommand</code>.
     *
     * @param src The name of the file or directory to be copied
     * @param dst The name of the file or directory in which copy the source file or directory
     */
    public CopyCommand(String src, String dst) {
        super();
        this.mSrc = src;
        this.mDst = dst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getResult() {
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
            throws InsufficientPermissionsException, NoSuchFileOrDirectory, ExecutionException {
        if (isTrace()) {
            Log.v(TAG,
                    String.format("Moving from %s to %s", //$NON-NLS-1$
                            this.mSrc, this.mDst));
        }

        File s = new File(this.mSrc);
        File d = new File(this.mDst);
        if (!s.exists()) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. NoSuchFileOrDirectory"); //$NON-NLS-1$
            }
            throw new NoSuchFileOrDirectory(this.mSrc);
        }

        //Copy recursively
        if (!copyRecursive(s, d)) {
            if (isTrace()) {
                Log.v(TAG, "Result: FAIL. InsufficientPermissionsException"); //$NON-NLS-1$
            }
            throw new InsufficientPermissionsException();
        }

        if (isTrace()) {
            Log.v(TAG, "Result: OK"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MountPoint getWritableMountPoint() {
        return MountPointHelper.getMountPointFromDirectory(this.mDst);
    }

    /**
     * Method that copies recursively to the destination
     *
     * @param src The source file or folder
     * @param dst The destination file or folder
     * @return boolean If the operation complete successfully
     * @throws ExecutionException If a problem was detected in the operation
     */
    public boolean copyRecursive(final File src, final File dst) throws ExecutionException {
        if (src.isDirectory()) {
            // Create the directory
            if (dst.exists() && !dst.isDirectory()) {
                Log.e(TAG,
                        String.format("Failed to check destionation dir: %s", dst)); //$NON-NLS-1$
                throw new ExecutionException("the path exists but is not a folder"); //$NON-NLS-1$
            }
            if (!dst.exists()) {
                if (!dst.mkdir()) {
                    Log.e(TAG, String.format("Failed to create directory: %s", dst)); //$NON-NLS-1$
                    return false;
                }
            }
            File[] files = src.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!copyRecursive(files[i], new File(dst, files[i].getName()))) {
                        return false;
                    }
                }
            }
        } else {
            // Copy the directory
            if (!bufferedCopy(src, dst)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method that copies a file
     *
     * @param src The source file
     * @param dst The destination file
     * @return boolean If the operation complete successfully
     */
    public boolean bufferedCopy(final File src, final File dst) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(src), getBufferSize());
            bos = new BufferedOutputStream(new FileOutputStream(dst), getBufferSize());
            int read = 0;
            byte[] data = new byte[getBufferSize()];
            while ((read = bis.read(data, 0, getBufferSize())) != -1) {
                bos.write(data, 0, read);
            }
            return true;

        } catch (Throwable e) {
            Log.e(TAG,
                    String.format(TAG, "Failed to copy from %s to %d", src, dst), e); //$NON-NLS-1$
            return false;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Throwable e) {/**NON BLOCK**/}
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Throwable e) {/**NON BLOCK**/}
        }

    }
}
