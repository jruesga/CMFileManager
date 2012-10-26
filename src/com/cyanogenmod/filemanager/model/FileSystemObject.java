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

package com.cyanogenmod.filemanager.model;

import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.util.FileHelper;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * A class that represents an abstract file system object.
 *
 * @see RegularFile
 * @see Directory
 * @see Symlink
 * @see SystemFile
 */
public abstract class FileSystemObject implements Serializable, Comparable<FileSystemObject> {

    private static final long serialVersionUID = -8527561430880927320L;

    //Resource identifier for default icon
    private static final int RESOURCE_ICON_DEFAULT = R.drawable.ic_fso_default;

    private int mResourceIconId;
    private String mName;
    private String mParent;
    private User mUser;
    private Group mGroup;
    private Permissions mPermissions;
    private Date mLastModifiedTime;
    private long mSize;

    /**
     * Constructor of <code>FileSystemObject</code>.
     *
     * @param name The name of the object
     * @param parent The parent folder of the object
     * @param user The user proprietary of the object
     * @param group The group proprietary of the object
     * @param permissions The permissions of the object
     * @param lastModifiedTime The last time that the object was modified
     * @param size The size in bytes of the object
     */
    public FileSystemObject(String name, String parent, User user, Group group,
            Permissions permissions, Date lastModifiedTime, long size) {
        super();
        this.mName = name;
        this.mParent = parent;
        this.mUser = user;
        this.mGroup = group;
        this.mPermissions = permissions;
        this.mLastModifiedTime = lastModifiedTime;
        this.mSize = size;
        this.mResourceIconId = RESOURCE_ICON_DEFAULT;
    }

    /**
     * Method that returns the character that identifies the object in unix.
     *
     * @return char The character that identifies the object in unix
     */
    public abstract char getUnixIdentifier();

    /**
     * Method that returns the name of the object.
     *
     * @return String The name of the object
     */
    public String getName() {
        return this.mName;
    }

    /**
     * Method that sets the name of the object.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * Method that returns the parent folder of the object.
     *
     * @return String The parent folder of the object
     */
    public String getParent() {
        return this.mParent;
    }

    /**
     * Method that sets the parent folder of the object.
     *
     * @param parent The parent folder of the object
     */
    public void setParent(String parent) {
        this.mParent = parent;
    }

    /**
     * Method that returns the user proprietary of the object.
     *
     * @return User The user proprietary of the object
     */
    public User getUser() {
        return this.mUser;
    }

    /**
     * Method that sets the user proprietary of the object.
     *
     * @param user The user proprietary of the object
     */
    public void setUser(User user) {
        this.mUser = user;
    }

    /**
     * Method that returns the group proprietary of the object.
     *
     * @return Group The group proprietary of the object
     */
    public Group getGroup() {
        return this.mGroup;
    }

    /**
     * Method that sets the group proprietary of the object.
     *
     * @param group The group proprietary of the object
     */
    public void setGroup(Group group) {
        this.mGroup = group;
    }

    /**
     * Method that returns the permissions of the object.
     *
     * @return Permissions The permissions of the object
     */
    public Permissions getPermissions() {
        return this.mPermissions;
    }

    /**
     * Method that sets the permissions of the object.
     *
     * @param permissions The permissions of the object
     */
    public void setPermissions(Permissions permissions) {
        this.mPermissions = permissions;
    }

    /**
     * Method that returns the last time that the object was modified.
     *
     * @return Date The last time that the object was modified
     */
    public Date getLastModifiedTime() {
        return this.mLastModifiedTime;
    }

    /**
     * Method that sets the last time that the object was modified.
     *
     * @param lastModifiedTime The last time that the object was modified
     */
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.mLastModifiedTime = lastModifiedTime;
    }

    /**
     * Method that returns the size in bytes of the object.
     *
     * @return long The size in bytes of the object
     */
    public long getSize() {
        return this.mSize;
    }

    /**
     * Method that sets the size in bytes of the object.
     *
     * @param size The size in bytes of the object
     */
    public void setSize(long size) {
        this.mSize = size;
    }

    /**
     * Method that returns of the object is hidden object.
     *
     * @return boolean If the object is hidden object
     */
    public boolean isHidden() {
        return this.mName.startsWith("."); //$NON-NLS-1$
    }

    /**
     * Method that returns the identifier of the drawable icon associated
     * to the object.
     *
     * @return int The identifier of the drawable icon
     * @hide
     */
    public int getResourceIconId() {
        return this.mResourceIconId;
    }

    /**
     * Method that sets the identifier of the drawable icon associated
     * to the object.
     *
     * @param resourceIconId The identifier of the drawable icon
     * @hide
     */
    protected void setResourceIconId(int resourceIconId) {
        this.mResourceIconId = resourceIconId;
    }

    /**
     * Method that returns the full path of the file system object.
     *
     * @return String The full path of the file system object
     */
    public String getFullPath() {
        if (FileHelper.isRootDirectory(this)) {
            return FileHelper.ROOT_DIRECTORY;
        } else if (FileHelper.isParentRootDirectory(this)) {
            if (this.mParent == null) {
                return FileHelper.ROOT_DIRECTORY + this.mName;
            }
            return this.mParent + this.mName;
        }
        return this.mParent + File.separator + this.mName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(FileSystemObject another) {
        String o1 = this.getFullPath();
        String o2 = another.getFullPath();
        return o1.compareTo(o2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.mGroup == null) ? 0 : this.mGroup.hashCode());
        result = prime * result + ((this.mLastModifiedTime == null)
                ? 0
                : this.mLastModifiedTime.hashCode());
        result = prime * result + ((this.mName == null) ? 0 : this.mName.hashCode());
        result = prime * result + ((this.mParent == null) ? 0 : this.mParent.hashCode());
        result = prime * result + ((this.mPermissions == null) ? 0 : this.mPermissions.hashCode());
        result = prime * result + this.mResourceIconId;
        result = prime * result + (int) (this.mSize ^ (this.mSize >>> 32));
        result = prime * result + ((this.mUser == null) ? 0 : this.mUser.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileSystemObject other = (FileSystemObject) obj;
        if (this.mGroup == null) {
            if (other.mGroup != null) {
                return false;
            }
        } else if (!this.mGroup.equals(other.mGroup)) {
            return false;
        }
        if (this.mLastModifiedTime == null) {
            if (other.mLastModifiedTime != null) {
                return false;
            }
        } else if (!this.mLastModifiedTime.equals(other.mLastModifiedTime)) {
            return false;
        }
        if (this.mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!this.mName.equals(other.mName)) {
            return false;
        }
        if (this.mParent == null) {
            if (other.mParent != null) {
                return false;
            }
        } else if (!this.mParent.equals(other.mParent)) {
            return false;
        }
        if (this.mPermissions == null) {
            if (other.mPermissions != null) {
                return false;
            }
        } else if (!this.mPermissions.equals(other.mPermissions)) {
            return false;
        }
        if (this.mResourceIconId != other.mResourceIconId) {
            return false;
        }
        if (this.mSize != other.mSize) {
            return false;
        }
        if (this.mUser == null) {
            if (other.mUser != null) {
                return false;
            }
        } else if (!this.mUser.equals(other.mUser)) {
            return false;
        }
        return true;
    }

    /**
     * Method that returns the unix string representation of the type and permissions of the
     * file system object.
     *
     * @return String The string representation
     */
    public String toRawString() {
        return String.format("%s%s", //$NON-NLS-1$
                String.valueOf(getUnixIdentifier()),
                getPermissions().toRawString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FileSystemObject [resourceIconId=" + this.mResourceIconId //$NON-NLS-1$
                + ", name=" + this.mName //$NON-NLS-1$
                + ", parent=" + this.mParent + ", user=" + this.mUser //$NON-NLS-1$ //$NON-NLS-2$
                + ", group=" + this.mGroup  + ", permissions=" //$NON-NLS-1$ //$NON-NLS-2$
                + this.mPermissions + ", lastModifiedTime=" + this.mLastModifiedTime //$NON-NLS-1$
                + ", size=" + this.mSize + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }

}
