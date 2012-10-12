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

package com.cyanogenmod.explorer.commands;

/**
 * An interface that defines a class as executable in an asynchronous way.
 */
public interface AsyncResultExecutable extends Executable {

    /**
     * An interface that let to request the cancellation of the current
     * execution of an {@link Executable}.
     */
    public interface OnCancelListener {
        /**
         * Invoked when a request of cancellation of the current
         * execution is started.
         *
         *  @return boolean If the execution was canceled
         */
        boolean onCancel();
    }

    /**
     * Method that return if the command is canceled.
     *
     * @return boolean Indicates if the command is canceled
     */
    boolean isCanceled();

    /**
     * Method that cancels the execution of the program.
     *
     * @return boolean If the program was canceled
     */
    boolean cancel();

    /**
     * Method that sets the cancel listener.
     *
     * @param onCancelListener The cancel listener
     */
    void setOnCancelListener(OnCancelListener onCancelListener);

    /**
     * Method that returns if program supports cancellation.
     *
     * @return boolean If the program supports cancellation
     */
    boolean isCancelable();


    /**
     * Method that returns the listener to communicate result in
     * an asynchronous way.
     *
     * @return AsyncResultListener The listener
     */
    AsyncResultListener getAsyncResultListener();
}
