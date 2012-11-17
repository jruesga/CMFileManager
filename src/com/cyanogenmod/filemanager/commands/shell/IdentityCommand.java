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

package com.cyanogenmod.filemanager.commands.shell;

import com.cyanogenmod.filemanager.commands.IdentityExecutable;
import com.cyanogenmod.filemanager.console.CommandNotFoundException;
import com.cyanogenmod.filemanager.console.ExecutionException;
import com.cyanogenmod.filemanager.console.InsufficientPermissionsException;
import com.cyanogenmod.filemanager.model.AID;
import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.model.Identity;
import com.cyanogenmod.filemanager.model.User;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * A class for retrieve identity information of the current user
 * (user associated to the active shell).
 *
 * {@link "http://unixhelp.ed.ac.uk/CGI/man-cgi?id"}
 */
//
//<user>          <group>         <groups>
//uid=2000(shell) gid=2000(shell) groups=1003(graphics),1004(input),...
//
public class IdentityCommand extends SyncResultProgram implements IdentityExecutable {

    private static final String ID = "id";  //$NON-NLS-1$
    private Identity mIdentity;

    private static final String UID = "uid";  //$NON-NLS-1$
    private static final String GID = "gid";  //$NON-NLS-1$
    private static final String GROUPS = "groups";  //$NON-NLS-1$

    /**
     * Constructor of <code>IdentityCommand</code>.
     *
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     */
    public IdentityCommand() throws InvalidCommandDefinitionException {
        super(ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(String in, String err) throws ParseException {
        //Release the return object
        this.mIdentity = null;

        // Check the in buffer to extract information
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(in));
            String szLine = br.readLine();
            if (szLine == null) {
                throw new ParseException("no information", 0); //$NON-NLS-1$
            }

            szLine = szLine.replaceAll(" ", FileHelper.NEWLINE); //$NON-NLS-1$
            Properties p = new Properties();
            p.load(new StringReader(szLine));

            //At least uid and gid must be present
            if (!p.containsKey(UID) && !p.containsKey(GID)) {
                throw new ParseException(
                        String.format(
                                "no %s or %s present in %s", UID, GID, szLine), 0); //$NON-NLS-1$
            }

            //1.- Extract user
            User user = (User)createAID(p.getProperty(UID), User.class);

            //2.- Extract group
            Group group = (Group)createAID(p.getProperty(GID), Group.class);

            //3.- Extract groups
            List<Group> groups = new ArrayList<Group>();
            String szGroups = p.getProperty(GROUPS);
            if (szGroups != null && szGroups.length() > 0) {
                String[] aGroups = szGroups.split(","); //$NON-NLS-1$
                int cc = aGroups.length;
                for (int i = 0; i < cc; i++) {
                    groups.add((Group)createAID(aGroups[i], Group.class));
                }
            }

            //Now the string is parsed into a reference
            this.mIdentity = new Identity(user, group, groups);

        } catch (IOException ioEx) {
            throw new ParseException(ioEx.getMessage(), 0);

        } catch (ParseException pEx) {
            throw pEx;

        } catch (Exception ex) {
            throw new ParseException(ex.getMessage(), 0);

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Throwable ex) {
                /**NON BLOCK**/
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity getResult() {
        return this.mIdentity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExitCode(int exitCode)
            throws InsufficientPermissionsException, CommandNotFoundException, ExecutionException {
        if (exitCode != 0) {
            throw new ExecutionException("exitcode != 0"); //$NON-NLS-1$
        }
    }

    /**
     * Method that creates the {@link AID} from the parsed string.
     *
     * @param src The string to parsed into a {@link AID}
     * @param clazz The {@link User} or {@link Group} class from which create the AID object
     * @return AID The identity reference
     * @throws ParseException If can't create the {@link AID} reference from the string
     * @throws NoSuchMethodException If the constructor can not be found.
     * @exception InstantiationException If the class cannot be instantiated
     * @exception IllegalAccessException If this constructor is not accessible
     * @exception InvocationTargetException If an exception was thrown by the invoked constructor
     */
    @SuppressWarnings({ "unchecked", "static-method", "boxing" })
    private AID createAID(String src, Class<?> clazz)
            throws ParseException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        int id = 0;
        try {
            id = Integer.parseInt(src.substring(0, src.lastIndexOf('(')).trim());
        } catch (NumberFormatException nfEx) {
            throw new ParseException(String.format("not valid AID id: %s", src), 0); //$NON-NLS-1$
        }
        String szName = src.substring(src.lastIndexOf('(') + 1, src.lastIndexOf(')'));

        Constructor<AID> constructor =
                (Constructor<AID>)clazz.getConstructor(Integer.TYPE, String.class);
        return constructor.newInstance(id, szName);
    }

}
