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
 * An interface that represents an executable for uncompress file system objects.
 */
public interface UncompressExecutable extends AsyncResultExecutable {

    /**
     * Method that returns the result of the operation
     *
     * @return Boolean The result of the operation
     */
    Boolean getResult();

    /**
     * Method that returns the path out uncompressed file or folder
     *
     * @return String The path of the uncompressed file or folder
     */
    String getOutUncompressedFile();

    /**
     * Method that returns if the uncompress process will create a file or a folder
     *
     * @return boolean If the uncompress process will create a file or a folder
     */
    boolean IsArchive();
}
