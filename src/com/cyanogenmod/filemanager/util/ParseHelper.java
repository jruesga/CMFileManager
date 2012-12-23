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

package com.cyanogenmod.filemanager.util;

import android.os.Process;

import com.cyanogenmod.filemanager.model.BlockDevice;
import com.cyanogenmod.filemanager.model.CharacterDevice;
import com.cyanogenmod.filemanager.model.Directory;
import com.cyanogenmod.filemanager.model.DiskUsage;
import com.cyanogenmod.filemanager.model.DomainSocket;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.model.Group;
import com.cyanogenmod.filemanager.model.GroupPermission;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.model.NamedPipe;
import com.cyanogenmod.filemanager.model.OthersPermission;
import com.cyanogenmod.filemanager.model.Permission;
import com.cyanogenmod.filemanager.model.Permissions;
import com.cyanogenmod.filemanager.model.RegularFile;
import com.cyanogenmod.filemanager.model.Symlink;
import com.cyanogenmod.filemanager.model.User;
import com.cyanogenmod.filemanager.model.UserPermission;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class with useful methods for deal with parse of results.
 */
public final class ParseHelper {

    private static final String DATE_PATTERN =
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"; //$NON-NLS-1$
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$

    /**
     * Constructor of <code>ParseHelper</code>.
     */
    private ParseHelper() {
        super();
    }

    /**
     * Method that parses and creates a {@link FileSystemObject} references from
     * a unix string style line.
     *
     * @param parent The parent of the object
     * @param src The unix string style line
     * @return FileSystemObject The file system object reference
     * @throws ParseException If the line can't be parsed
     * @see #toFileSystemObject(String, String, boolean)
     */
    public static FileSystemObject toFileSystemObject(
            final String parent, final String src) throws ParseException {
        return toFileSystemObject(parent, src, false);
    }

    /**
     * Method that parses and creates a {@link FileSystemObject} references from
     * a unix string style line.
     *
     * @param parent The parent of the object
     * @param src The unix string style line
     * @param quick Do not resolve data (User and Group doesn't have a valid reference)
     * @return FileSystemObject The file system object reference
     * @throws ParseException If the line can't be parsed
     */
    //
    //<permission> <user> <group> <size> <last modified>  <name>
    //-rw-r--r--   root   root       229 2012-05-04 01:51 boot.txt
    //drwxr-xr-x   root   root           2012-05-04 01:51 acct
    //lrwxrwxrwx   root   root           2012-05-04 01:51 etc -> /system/etc
    //crw-rw-rw-   system system 10, 243 2012-05-04 01:51 HPD
    //brw-------   root   root    7,   0 2012-05-04 01:51 loop0
    //srw-------   root   root         0 2012-05-04 01:51 socket
    //prw-------   root   root         0 2012-05-04 01:51 pipe
    //
    //
    //<permissions>: http://en.wikipedia.org/wiki/File_system_permissions
    //-rw-r--r--
    //
    //(char 1)
    //  - denotes a regular file
    //  d denotes a directory
    //  b denotes a block special file
    //  c denotes a character special file
    //  l denotes a symbolic link
    //  p denotes a named pipe
    //  s denotes a domain socket
    //(char 2-10)
    //  r if the read bit is set, - if it is not.
    //  w if the write bit is set, - if it is not.
    //  x if the execute bit is set, - if it is not.
    //(char 4)
    //  s if the setuid bit and executable bit are set
    //  S if the setuid bit is set, but not executable bit
    //(char 7)
    //  s if the setgid bit and executable bit are set
    //  S if the setgid bit is set, but not executable bit
    //(char 10)
    //  t if the sticky bit and executable bit are set
    //  T if the sticky bit is set, but not executable bit
    //
    //<user>: User proprietary of the file
    //<group>: Group proprietary of the file
    //<last modified>:
    //<size>:
    //  - if object is a type regular file, size of the file in bytes
    //  - if object is a block or a character device, mayor and minor device number (7, 0)
    //  - if object is a pipe or a socket, always is 0
    //  - if object is a directory or symlink, no value is present
    //
    //<last modification>: Last file modification (in Android always yyyy-MM-dd HH:mm.
    //  Can ensure this?)
    //<name>:
    //  - if object is a symlink, the value must be "link name -> real name"
    //  - If the name is void, then assume that it is the root directory (/)
    //
    public static FileSystemObject toFileSystemObject(
            final String parent, final String src, final boolean quick) throws ParseException {

        String raw = src;

        //0.- Object Type
        char type = raw.charAt(0);

        //1.- Extract permissions
        String szPermissions = raw.substring(0, 10);
        Permissions oPermissions = parsePermission(szPermissions);
        raw = raw.substring(11);

        //2.- Extract the last modification date
        Pattern pattern = Pattern.compile(DATE_PATTERN);
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.find()) {
            throw new ParseException(
                    "last modification date not found in " + raw, 0); //$NON-NLS-1$
        }
        Date dLastModified = null;
        try {
            dLastModified = DATE_FORMAT.parse(matcher.group());
        } catch (ParseException pEx) {
            throw new ParseException(pEx.getMessage(), 0);
        }
        String szStartLine = raw.substring(0, matcher.start()).trim();
        String szEndLine = raw.substring(matcher.end()).trim();

        //3.- Extract user (user name has no spaces.
        int pos = szStartLine.indexOf(" "); //$NON-NLS-1$
        String szUser = szStartLine.substring(0, pos).trim();
        szStartLine = szStartLine.substring(pos).trim();
        User oUser = null;
        if (!quick) {
            oUser = new User(Process.getUidForName(szUser), szUser);
        } else {
            oUser = new User(-1, szUser);
        }

        //4.- Extract group (group name has no spaces.
        pos = szStartLine.indexOf(" "); //$NON-NLS-1$
        String szGroup = szStartLine.substring(0, (pos == -1) ? szStartLine.length() : pos).trim();
        szStartLine = szStartLine.substring((pos == -1) ? szStartLine.length() : pos).trim();
        Group oGroup = null;
        if (!quick) {
            oGroup = new Group(Process.getGidForName(szGroup), szGroup);
        } else {
            oGroup = new Group(-1, szGroup);
        }

        //5.- Extract size
        long lSize = 0;
        if (szStartLine.length() != 0) {
            //At this moment only size of files is interesting. Mayor/minor block
            //devices are no required
            if (type == RegularFile.UNIX_ID) {
                try {
                    lSize = Long.parseLong(szStartLine);
                } catch (NumberFormatException nfEx) {
                    throw new ParseException(nfEx.getMessage(), 0);
                }

            }
        }

        //6.- Extract object name
        String szName = szEndLine;
        if (szName.trim().length() == 0) {
            // Assume that the object name is the root folder
            szName = FileHelper.ROOT_DIRECTORY;
        }
        String szLink = null;
        if (type == Symlink.UNIX_ID) {
            //"link name -> real name"
            String[] names = szEndLine.split(" -> "); //$NON-NLS-1$
            szName = names[0].trim();
            szLink = names[1].trim();
        }

        // All the line is parsed now. Create the object
        FileSystemObject fso = createObject(
                            parent, type, szName, szLink, oUser, oGroup,
                            oPermissions, dLastModified, lSize);
        return fso;
    }

    /**
     * Method that parses and extracts the permissions from a unix string format.
     *
     * @param permissions The raw permissions
     * @return Permissions An object with all the permissions
     * @throws ParseException If the permissions can't be parsed
     * @{link "http://en.wikipedia.org/wiki/File_system_permissions"}
     */
    public static Permissions parsePermission(String permissions) throws ParseException {
        if (permissions.length() != 10) {
            throw new ParseException("permission length() != 10", 0); //$NON-NLS-1$
        }
        UserPermission up = new UserPermission(
                permissions.charAt(1) == Permission.READ,
                permissions.charAt(2) == Permission.WRITE,
                permissions.charAt(3) ==  Permission.EXECUTE
                    || permissions.charAt(3) == UserPermission.SETUID_E,
                permissions.charAt(3) == UserPermission.SETUID_E
                    || permissions.charAt(3) == UserPermission.SETUID);
        GroupPermission gp = new GroupPermission(
                permissions.charAt(4) == Permission.READ,
                permissions.charAt(5) == Permission.WRITE,
                permissions.charAt(6) == Permission.EXECUTE
                    || permissions.charAt(6) == GroupPermission.SETGID_E,
                permissions.charAt(6) == GroupPermission.SETGID_E
                    || permissions.charAt(6) == GroupPermission.SETGID);
        OthersPermission op = new OthersPermission(
                permissions.charAt(7) == Permission.READ,
                permissions.charAt(8) == Permission.WRITE,
                permissions.charAt(9) == Permission.EXECUTE
                    || permissions.charAt(9) == OthersPermission.STICKY_E,
                permissions.charAt(9) == OthersPermission.STICKY_E
                    || permissions.charAt(9) == OthersPermission.STICKY);
        return new Permissions(up, gp, op);
    }

    /**
     * Method that creates the appropriate file system object.
     *
     * @param parentDir The parent directory
     * @param type The raw char type of the file system object
     * @param name The name of the object
     * @param link The real file that this symlink is point to
     * @param user The user proprietary of the object
     * @param group The group proprietary of the object
     * @param permissions The permissions of the object
     * @param lastModifiedTime The last time that the object was modified
     * @param size The size in bytes of the object
     * @return FileSystemObject The file system object reference
     * @throws ParseException If type couldn't be translate into a reference
     * file system object
     */
    private static FileSystemObject createObject(
            String parentDir, char type, String name, String link, User user,
            Group group, Permissions permissions, Date lastModifiedTime, long size)
            throws ParseException {

        if (type == RegularFile.UNIX_ID) {
            return new RegularFile(
                    name, parentDir, user, group, permissions, lastModifiedTime, size);
        }
        if (type == Directory.UNIX_ID) {
            return new Directory(name, parentDir, user, group, permissions, lastModifiedTime);
        }
        if (type == Symlink.UNIX_ID) {
            return new Symlink(name, link, parentDir, user, group, permissions, lastModifiedTime);
        }
        if (type == BlockDevice.UNIX_ID) {
            return new BlockDevice(name, parentDir, user, group, permissions, lastModifiedTime);
        }
        if (type == CharacterDevice.UNIX_ID) {
            return new CharacterDevice(name, parentDir, user, group, permissions, lastModifiedTime);
        }
        if (type == NamedPipe.UNIX_ID) {
            return new NamedPipe(name, parentDir, user, group, permissions, lastModifiedTime);
        }
        if (type == DomainSocket.UNIX_ID) {
            return new DomainSocket(name, parentDir, user, group, permissions, lastModifiedTime);
        }
        throw new ParseException("no file system object", 0); //$NON-NLS-1$
    }

    /**
     * Method that parse a disk usage line.
     *
     * @param src The disk usage line
     * @return DiskUsage The disk usage information
     * @throws ParseException If the line can't be parsed
     */
    // Filesystem             Size   Used   Free   Blksize
    // /dev                   414M    48K   414M   4096
    // /mnt/asec              414M     0K   414M   4096
    // /mnt/secure/asec: Permission denied
    public static DiskUsage toDiskUsage(final String src) throws ParseException {

        try {
            final int fields = 5;

            //Permission denied or invalid statistics
            if (src.indexOf(":") != -1) { //$NON-NLS-1$
                throw new ParseException(String.format("Non allowed: %s", src), 0); //$NON-NLS-1$
            }

            //Extract all the info
            String line = src;
            String[] data = new String[fields];
            for (int i = 0; i < fields; i++) {
                int pos = line.indexOf(" "); //$NON-NLS-1$
                data[i] = line.substring(0, pos != -1 ? pos : line.length());
                if (pos != -1) {
                    line = line.substring(pos).trim();
                }
            }

            //Return the disk usage
            return new DiskUsage(data[0], toBytes(data[1]), toBytes(data[2]), toBytes(data[3]));

        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    /**
     * Method that parse a {@link "/proc/mounts"} line.
     *
     * @param src The mount point line
     * @return MountPoint The mount point information
     * @throws ParseException If the line can't be parsed
     */
    // rootfs / rootfs ro,relatime 0 0
    // tmpfs /dev tmpfs rw,nosuid,relatime,mode=755 0 0
    // devpts /dev/pts devpts rw,relatime,mode=600 0 0
    // /dev/block/vold/179:25 /mnt/emmc vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000, \
    // gid=1015,fmask=0702,dmask=0702,allow_utime=0020,codepage=cp437,iocharset=iso8859-1, \
    // shortname=mixed,utf8,errors=remount-ro 0 0
    public static MountPoint toMountPoint(final String src) throws ParseException {

        try {

            //Extract all the info
            String line = src;
            int pos = line.lastIndexOf(" "); //$NON-NLS-1$
            int pass = Integer.parseInt(line.substring(pos + 1));
            line = line.substring(0, pos).trim();
            pos = line.lastIndexOf(" "); //$NON-NLS-1$
            int dump = Integer.parseInt(line.substring(pos + 1));
            line = line.substring(0, pos).trim();
            pos = line.indexOf(" "); //$NON-NLS-1$
            String device = line.substring(0, pos).trim();
            line = line.substring(pos).trim();
            pos = line.lastIndexOf(" "); //$NON-NLS-1$
            String options = line.substring(pos + 1).trim();
            line = line.substring(0, pos).trim();
            pos = line.lastIndexOf(" "); //$NON-NLS-1$
            String type = line.substring(pos + 1).trim();
            String mountPoint = line.substring(0, pos).trim();


            //Return the mount point
            return new MountPoint(mountPoint, device, type, options, dump, pass);

        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    /**
     * Method that converts to bytes the string representation
     * of a size (10M, 1G, 0K, ...).
     *
     * @param size The size as a string representation
     * @return long The size in bytes
     */
    private static long toBytes(String size) {
        long bytes = Long.parseLong(size.substring(0, size.length() - 1));
        String unit = size.substring(size.length() - 1);
        if (unit.compareToIgnoreCase("G") == 0) { //$NON-NLS-1$
            return bytes * 1024 * 1024 * 1024;
        }
        if (unit.compareToIgnoreCase("M") == 0) { //$NON-NLS-1$
            return bytes * 1024 * 1024;
        }
        if (unit.compareToIgnoreCase("K") == 0) { //$NON-NLS-1$
            return bytes * 1024;
        }

        //Don't touch
        return bytes;
    }

}
