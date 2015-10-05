/*
 * Copyright (C) 2013 The CyanogenMod Project
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

package com.cyanogenmod.filemanager.ash;

import com.cyanogenmod.filemanager.ash.spi.PropertiesSyntaxHighlightProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory of <code>SyntaxHighlightProcessor</code> classes.
 */
public class SyntaxHighlightFactory {

    private static SyntaxHighlightFactory sFactory;

    private final ArrayList<SyntaxHighlightProcessor> mProcessors;

    /**
     * Constructor of <code>SyntaxHighlightFactory</code>
     */
    public SyntaxHighlightFactory() {
        super();
        this.mProcessors = new ArrayList<SyntaxHighlightProcessor>();
    }

    /**
     * Method that returns the default highlight factory instance
     *
     * @param resolver A class for allow the processor to obtain resources
     * @return SyntaxHighlightFactory The default syntax highlight factory
     */
    public static final synchronized SyntaxHighlightFactory getDefaultFactory(
            ISyntaxHighlightResourcesResolver resolver) {
        if (sFactory == null) {
            sFactory = createDefaultFactory(resolver);
        }
        return sFactory;
    }

    /**
     * Method that returns the syntax highlight processor that can handle the file
     *
     * @param file The file to process
     * @return SyntaxHighlightProcessor The syntax highlight processor
     */
    public SyntaxHighlightProcessor getSyntaxHighlightProcessor(File file) {
        int cc = this.mProcessors.size();
        for (int i = 0; i < cc; i++) {
            SyntaxHighlightProcessor processor = this.mProcessors.get(i);
            if (processor.accept(file)) {
                return processor;
            }
        }
        return null;
    }

    /**
     * Method that return all the available syntax highlight processors.
     *
     * @return List<SyntaxHighlightProcessor> the list available syntax highlight processors.
     */
    public List<SyntaxHighlightProcessor> getAvailableSyntaxHighlightProcessors() {
        return new ArrayList<SyntaxHighlightProcessor>(this.mProcessors);
    }

    /**
     * Method that create the default syntax highlight factory.
     *
     * @param resolver A class for allow the processor to obtain resources
     * @return SyntaxHighlightFactory The default factory
     */
    private static SyntaxHighlightFactory createDefaultFactory(
            ISyntaxHighlightResourcesResolver resolver) {
        // TODO Read all processors classes of the SPI package
        // For now we add all known syntax highlight processors
        SyntaxHighlightFactory factory = new SyntaxHighlightFactory();
        factory.mProcessors.add(new PropertiesSyntaxHighlightProcessor(resolver));
        return factory;
    }
}
