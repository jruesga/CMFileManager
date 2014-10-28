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

import android.text.TextUtils;

import com.android.internal.util.HexDump;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * A helper class with useful methods for deal with strings.
 */
public final class StringHelper {

    private static final char[] VALID_NON_PRINTABLE_CHARS = {' ', '\t', '\r', '\n'};

    /**
     * Method that check if a character is valid printable character
     *
     * @param c The character to check
     * @return boolean If the character is printable
     */
    public static boolean isPrintableCharacter(char c) {
        int cc = VALID_NON_PRINTABLE_CHARS.length;
        for (int i = 0; i < cc; i++) {
            if (c == VALID_NON_PRINTABLE_CHARS[i]) {
                return true;
            }
        }
        return TextUtils.isGraphic(c);
    }

    public static boolean isBinaryData(byte[] data) {
        int lastByteTranslated = 0;
        final int read = Math.min(10 * 1024, data.length);
        final long max = ((5 * read) / 100); // 5% percent of binary bytes
        int hits = 0;
        for (int i = 0; i < read; i++) {
            final byte b = data[i];
            int ub = b & (0xff);  // unsigned
            int utf8value = lastByteTranslated + ub;
            lastByteTranslated = (ub) << 8;

            if (ub == 0x09 /*(tab)*/
                    || ub == 0x0A /*(line feed)*/
                    || ub == 0x0C /*(form feed)*/
                    || ub == 0x0D /*(carriage return)*/
                    || (ub >= 0x20 && ub <= 0x7E) /* Letters, Numbers and other "normal symbols" */
                    || (ub >= 0xA0 && ub <= 0xEE) /* Symbols of Latin-1 */
                    || (utf8value >= 0x2E2E && utf8value <= 0xC3BF)) { /* Latin-1 in UTF-8 encoding */
                // ok
            } else {
                // binary
                hits++;
            }
        }
        return hits > max;
    }

    /**
     * Method that converts to a visual printable hex string
     *
     * @param string The string to check
     */
    public static String toHexPrintableString(byte[] data) {
        String hexLineSeparator =  UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String hex = toHexDump(data, hexLineSeparator);

        // Remove characters without visual representation
        final String REPLACED_SYMBOL = "."; //$NON-NLS-1$
        final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
        String printable = hex.replaceAll("\\p{Cntrl}", REPLACED_SYMBOL); //$NON-NLS-1$
        printable = printable.replaceAll("[^\\p{Print}]", REPLACED_SYMBOL); //$NON-NLS-1$
        printable = printable.replaceAll("\\p{C}", REPLACED_SYMBOL); //$NON-NLS-1$
        printable = printable.replaceAll(hexLineSeparator, NEWLINE);
        return printable;
    }

    /**
     * Create a hex dump of the data while show progress to user
     *
     * @param data The data to hex dump
     * @param hexLineSeparator Internal line separator
     * @return StringBuilder The hex dump buffer
     */
    private static String toHexDump(byte[] data, String hexLineSeparator) {
        final int DISPLAY_SIZE = 16;  // Bytes per line
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        byte[] line = new byte[DISPLAY_SIZE];
        int read = 0;
        int offset = 0;
        StringBuilder sb = new StringBuilder();
        while ((read = bais.read(line, 0, DISPLAY_SIZE)) != -1) {
            //offset   dump(16)   data\n
            String linedata = new String(line, 0, read);
            sb.append(HexDump.toHexString(offset));
            sb.append(" "); //$NON-NLS-1$
            String hexDump = HexDump.toHexString(line, 0, read);
            if (hexDump.length() != (DISPLAY_SIZE * 2)) {
                char[] array = new char[(DISPLAY_SIZE * 2) - hexDump.length()];
                Arrays.fill(array, ' ');
                hexDump += new String(array);
            }
            sb.append(hexDump);
            sb.append(" "); //$NON-NLS-1$
            sb.append(linedata);
            sb.append(hexLineSeparator);
            offset += DISPLAY_SIZE;
        }

        return sb.toString();
    }
}