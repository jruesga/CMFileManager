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

/**
 * An exception thrown when an exception occurs allocating a console.
 */
public class ConsoleAllocException extends Exception {

    private static final long serialVersionUID = -4907729346065481798L;

    /**
     * Constructor of <code>ConsoleAllocException</code>.
     *
     * @param detailMessage Message associated to the exception
     */
    public ConsoleAllocException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructor of <code>ConsoleAllocException</code>.
     *
     * @param detailMessage Message associated to the exception
     * @param throwable The cause of the exception
     */
    public ConsoleAllocException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
