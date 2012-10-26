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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface that represents an executable for write data to the disk.
 */
public interface WriteExecutable extends AsyncResultExecutable {

    /**
     * Method that returns the stream where write the data.<br/>
     * <br/>
     * NOTE: Don't close this buffer. It is internally closed.
     *
     * @return OutputStream The stream where write the data
     * @throws IOException If the buffer couldn't be created
     */
    public OutputStream createOutputStream() throws IOException;
}
