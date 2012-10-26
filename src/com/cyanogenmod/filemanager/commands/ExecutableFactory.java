/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License") throws CommandNotFoundException;
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
 * A class that represents a factory for creating {@link Executable} objects.
 */
public abstract class ExecutableFactory {

    /**
     * Method that creates a new {@link ExecutableCreator} for create
     * {@link Executable} objects.
     *
     * @return ExecutableCreator The delegated {@link Executable} creator
     */
    public abstract ExecutableCreator newCreator();
}
