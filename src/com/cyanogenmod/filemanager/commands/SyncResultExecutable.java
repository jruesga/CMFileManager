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
 * An interface that defines a class as executable in a synchronous way.
 */
public interface SyncResultExecutable extends Executable {

    /**
     * Method that returns the result of the program invocation.
     *
     * @return Object The result of the execution invocation (or null)
     * if the execution has no result. This object is defined by every
     * command
     */
    Object getResult();
}
