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
 * An enumeration of allow signals that can send to programs.
 */
public enum SIGNAL {

    /**
     * Hangup (POSIX).
     */
    SIGHUP(1),
    /**
     * Interrupt (ANSI).
     */
    SIGINT(2),
    /**
     * Quit (POSIX).
     */
    SIGQUIT(3),
    /**
     * Illegal instruction (ANSI).
     */
    SIGILL(4),
    /**
     * Trace trap (POSIX).
     */
    SIGTRAP(5),
    /**
     * Abort (ANSI).
     */
    SIGABRT(6),
    /**
     * Floating-point exception (ANSI).
     */
    SIGFPE(8),
    /**
     * Kill, unblockable (POSIX).
     */
    SIGKILL(9),
    /**
     * Segmentation violation (ANSI).
     */
    SIGSEGV(11),
    /**
     * Broken pipe (POSIX).
     */
    SIGPIPE(13),
    /**
     * Alarm clock (POSIX).
     */
    SIGALRM(14),
    /**
     * Termination (ANSI).
     */
    SIGTERM(15);

    private final int mSignal;

    /**
     * Constructor of <code>SIGNAL</code>
     *
     * @param signal The signal
     */
    private SIGNAL(int signal) {
        this.mSignal = signal;
    }

    /**
     * Method that returns the signal
     *
     * @return int The signal
     */
    public int getSignal() {
        return this.mSignal;
    }

}
