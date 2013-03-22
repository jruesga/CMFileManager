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

package com.cyanogenmod.filemanager.console;

import android.os.AsyncTask;

import com.cyanogenmod.filemanager.commands.SyncResultExecutable;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception that determines that an operation should be re-executed.
 */
public abstract class RelaunchableException extends Exception {

    private static final long serialVersionUID = -3897597978154453512L;

    private final List<SyncResultExecutable> mExecutables;
    private AsyncTask<Object, Integer, Boolean> mTask;

    /**
     * Constructor of <code>RelaunchableException</code>.
     *
     * @param executable The executable that should be re-executed
     */
    public RelaunchableException(SyncResultExecutable executable) {
        super();
        this.mExecutables = new ArrayList<SyncResultExecutable>();
        if (executable != null) {
            addExecutable(executable);
        }
    }

    /**
     * Constructor of <code>RelaunchableException</code>.
     *
     * @param detailMessage Message associated to the exception
     * @param executable The executable that should be re-executed
     */
    public RelaunchableException(String detailMessage, SyncResultExecutable executable) {
        super(detailMessage);
        this.mExecutables = new ArrayList<SyncResultExecutable>();
        if (executable != null) {
            addExecutable(executable);
        }
    }

    /**
     * Constructor of <code>RelaunchableException</code>.
     *
     * @param detailMessage Message associated to the exception
     * @param throwable The cause of the exception
     * @param executable The executable that should be re-executed
     */
    public RelaunchableException(
            String detailMessage, Throwable throwable, SyncResultExecutable executable) {
        super(detailMessage, throwable);
        this.mExecutables = new ArrayList<SyncResultExecutable>();
        if (executable != null) {
            addExecutable(executable);
        }
    }

    /**
     * Method that returns the executable that should be re-executed.
     *
     * @return SyncResultExecutable The list of executable that should be re-executed
     */
    public List<SyncResultExecutable> getExecutables() {
        return this.mExecutables;
    }

    /**
     * Method that add a new executable to the queue of command to be re-executed.
     *
     * @param executable The executable to add
     */
    public void addExecutable(SyncResultExecutable executable) {
        this.mExecutables.add(executable);
    }

    /**
     * Method that returns the task to execute when the re-execution ends.
     *
     * @return AsyncTask<Object, Integer, Boolean> The task to execute when the re-execution ends
     */
    public AsyncTask<Object, Integer, Boolean> getTask() {
        return this.mTask;
    }

    /**
     * Method that set the task to execute when the re-execution ends.
     *
     * @param task The task to execute when the re-execution ends
     */
    public void setTask(AsyncTask<Object, Integer, Boolean> task) {
        this.mTask = task;
    }

    /**
     * Method that returns he resource identifier of the question to translate to the user.
     *
     * @return int The resource identifier of the question to translate to the user.
     */
    public abstract int getQuestionResourceId();

}
