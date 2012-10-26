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

package com.cyanogenmod.filemanager.preferences;

/**
 * An enumeration of the search result sort modes.
 */
public enum SearchSortResultMode implements ObjectStringIdentifier {

    /**
     * No sort results.
     */
    NONE("0"), //$NON-NLS-1$
    /**
     * Sort results by name
     */
    NAME("1"), //$NON-NLS-1$
    /**
     * Sort results by relevance
     */
    RELEVANCE("2"); //$NON-NLS-1$

    private String mId;

    /**
     * Constructor of <code>SearchSortResultMode</code>.
     *
     * @param id The unique identifier of the enumeration
     */
    private SearchSortResultMode(String id) {
        this.mId = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns an instance of {@link SearchSortResultMode} from its
     * unique identifier.
     *
     * @param id The unique identifier
     * @return SearchSortResultMode The search result sort mode
     */
    public static SearchSortResultMode fromId(String id) {
        SearchSortResultMode[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId.compareTo(id) == 0) {
                return values[i];
            }
        }
        return null;
    }

}
