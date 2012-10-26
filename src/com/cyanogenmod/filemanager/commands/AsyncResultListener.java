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

package com.cyanogenmod.filemanager.commands;


/**
 * An interface for communicate partial results.
 */
public interface AsyncResultListener {
    /**
     * Method invoked when the partial data has initialized.
     */
    void onAsyncStart();

    /**
     * Method invoked when the partial data has finalized.
     *
     * @param cancelled Indicates if the program was cancelled
     */
    void onAsyncEnd(boolean cancelled);

    /**
     * Method invoked when the program is ended.
     *
     * @param exitCode The exit code of the program
     */
    void onAsyncExitCode(int exitCode);

    /**
     * Method invoked when new partial data are ready.
     *
     * @param result New data result
     */
    void onPartialResult(Object result);

    /**
     * Method invoked when an exception occurs while executing the program.
     *
     * @param cause The cause that raise the exception
     */
    void onException(Exception cause);
}
