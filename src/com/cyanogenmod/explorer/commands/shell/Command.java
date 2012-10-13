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

package com.cyanogenmod.explorer.commands.shell;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import com.android.internal.util.XmlUtils;
import com.cyanogenmod.explorer.ExplorerApplication;
import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.preferences.ExplorerSettings;
import com.cyanogenmod.explorer.preferences.Preferences;
import com.cyanogenmod.explorer.util.ShellHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * An abstract class that represents a command to be executed
 * in the underlying operating system.
 *
 * @see "command_list.xml"
 */
public abstract class Command {

    // Command list XML tags
    private static final String TAG_COMMAND_LIST = "CommandList"; //$NON-NLS-1$
    private static final String TAG_COMMAND = "command"; //$NON-NLS-1$
    private static final String TAG_STARTCODE = "startcode"; //$NON-NLS-1$
    private static final String TAG_EXITCODE = "exitcode"; //$NON-NLS-1$

    private final String mId;
    private String mCmd;
    private String mArgs;   // The real arguments
    private final Object[] mCmdArgs;  //This arguments to be formatted

    private static String sStartCodeCmd;
    private static String sExitCodeCmd;

    private boolean mTrace;

    /**
     * @Constructor of <code>Command</code>
     *
     * @param id The resource identifier of the command
     * @param args Arguments of the command (will be formatted with the arguments from
     * the command definition)
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public Command(String id, String... args) throws InvalidCommandDefinitionException {
        this(id, true, args);
    }

    /**
     * @Constructor of <code>Command</code>
     *
     * @param id The resource identifier of the command
     * @param prepare Indicates if the argument must be prepared
     * @param args Arguments of the command (will be formatted with the arguments from
     * the command definition)
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public Command(String id, boolean prepare, String... args)
            throws InvalidCommandDefinitionException {
        super();
        this.mId = id;

        //Convert and quote arguments
        this.mCmdArgs = new Object[args.length];
        int cc = args.length;
        for (int i = 0; i < cc; i++) {
            //Quote the arguments?
            if (prepare) {
                this.mCmdArgs[i] =
                        "\"" + ShellHelper.prepareArgument(args[i]) //$NON-NLS-1$
                        + "\""; //$NON-NLS-1$
            } else {
                this.mCmdArgs[i] = ShellHelper.prepareArgument(args[i]);
            }
        }

        //Load the command info
        getCommandInfo(ExplorerApplication.getInstance().getResources());

        // Get the current trace value
        reloadTrace();
    }

    /**
     * Method that return id the command had to trace his operations
     *
     * @return boolean If the command had to trace
     */
    public boolean isTrace() {
        return this.mTrace;
    }

    /**
     * Method that reload the status of trace setting
     */
    public final void reloadTrace() {
        this.mTrace = Preferences.getSharedPreferences().getBoolean(
                ExplorerSettings.SETTINGS_SHOW_TRACES.getId(),
                ((Boolean)ExplorerSettings.SETTINGS_SHOW_TRACES.getDefaultValue()).booleanValue());
    }

    /**
     * Method that checks if the result code of the execution was successfully.
     *
     * @param exitCode Program exit code
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws ExecutionException If the operation returns a invalid exit code
     * @hide
     */
    public abstract void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException,
            ExecutionException;

    /**
     * Method that returns the resource identifier of the command.
     *
     * @return String The resource identifier of the command
     */
    public String getId() {
        return this.mId;
    }

    /**
     * This method must returns the name of the full qualified command path.<br />
     * <br />
     * This method always must returns a full qualified path, and not an
     * abbreviation to the command to avoid security problems.<br />
     * In the same way, a command not must contains any type of arguments.
     * Arguments must be passed through method {@link #getArguments()}
     *
     * @return String The full qualified command path
     * @see #getArguments()
     */
    public String getCommand() {
        return this.mCmd;
    }

    /**
     * This method can return the list of arguments to be executed along
     * with the command.
     *
     * @return String A list of individual arguments
     */
    public String getArguments() {
        return this.mArgs;
    }

    /**
     * Method that loads the resource command list xml and
     * inflate the internal variables.
     *
     * @param resources The application resource manager
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    private void getCommandInfo(Resources resources) throws InvalidCommandDefinitionException {

        //Read the command list xml file
        XmlResourceParser parser = resources.getXml(R.xml.command_list);

        try {
            //Find the root element
            XmlUtils.beginDocument(parser, TAG_COMMAND_LIST);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    break;
                }

                if (TAG_COMMAND.equals(element)) {
                    CharSequence id = parser.getAttributeValue(R.styleable.Command_commandId);
                    if (id != null && id.toString().compareTo(this.mId) == 0) {
                        CharSequence path =
                                parser.getAttributeValue(R.styleable.Command_commandPath);
                        CharSequence args =
                                parser.getAttributeValue(R.styleable.Command_commandArgs);
                        if (path == null) {
                            throw new InvalidCommandDefinitionException(
                                    this.mId + ": path is null"); //$NON-NLS-1$
                        }
                        if (args == null) {
                            throw new InvalidCommandDefinitionException(
                                    this.mId + ": args is null"); //$NON-NLS-1$
                        }

                        //Save paths
                        this.mCmd = path.toString();
                        this.mArgs = args.toString();
                        //Format the arguments of the process with the command arguments
                        if (this.mArgs != null && this.mArgs.length() > 0
                                && this.mCmdArgs != null && this.mCmdArgs.length > 0) {
                            this.mArgs = String.format(this.mArgs, this.mCmdArgs);
                        }

                        return;
                    }
                }
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            parser.close();
        }

        //Command not found
        throw new InvalidCommandDefinitionException(this.mId);
    }

    /**
     * Method that returns the exit code command info.
     *
     * @param resources The application resource manager
     * @return String The exit code command info
     * @throws InvalidCommandDefinitionException If the command is not present or has an
     * invalid definition
     */
    public static synchronized String getStartCodeCommandInfo(
            Resources resources) throws InvalidCommandDefinitionException {
        //Singleton
        if (sStartCodeCmd != null) {
            return new String(sStartCodeCmd);
        }

        //Read the command list xml file
        XmlResourceParser parser = resources.getXml(R.xml.command_list);

        try {
            //Find the root element
            XmlUtils.beginDocument(parser, TAG_COMMAND_LIST);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    break;
                }

                if (TAG_STARTCODE.equals(element)) {
                    CharSequence path = parser.getAttributeValue(R.styleable.Command_commandPath);
                    if (path == null) {
                        throw new InvalidCommandDefinitionException(
                                TAG_STARTCODE + ": path is null"); //$NON-NLS-1$
                    }

                    //Save paths
                    sStartCodeCmd = path.toString();
                    return new String(sStartCodeCmd);
                }
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            parser.close();
        }

        //Command not found
        throw new InvalidCommandDefinitionException(TAG_STARTCODE);
    }

    /**
     * Method that returns the exit code command info.
     *
     * @param resources The application resource manager
     * @return String The exit code command info
     * @throws InvalidCommandDefinitionException If the command is not present or has an
     * invalid definition
     */
    public static synchronized String getExitCodeCommandInfo(
            Resources resources) throws InvalidCommandDefinitionException {
        //Singleton
        if (sExitCodeCmd != null) {
            return new String(sExitCodeCmd);
        }

        //Read the command list xml file
        XmlResourceParser parser = resources.getXml(R.xml.command_list);

        try {
            //Find the root element
            XmlUtils.beginDocument(parser, TAG_COMMAND_LIST);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    break;
                }

                if (TAG_EXITCODE.equals(element)) {
                    CharSequence path = parser.getAttributeValue(R.styleable.Command_commandPath);
                    if (path == null) {
                        throw new InvalidCommandDefinitionException(
                                TAG_EXITCODE + ": path is null"); //$NON-NLS-1$
                    }

                    //Save paths
                    sExitCodeCmd = path.toString();
                    return new String(sExitCodeCmd);
                }
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            parser.close();
        }

        //Command not found
        throw new InvalidCommandDefinitionException(TAG_EXITCODE);
    }
}
