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

package com.cyanogenmod.filemanager.ui.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument.Page;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.PrintAttributes.Margins;
import android.print.PrintAttributes.MediaSize;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.widget.Toast;

import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.model.FileSystemObject;
import com.cyanogenmod.filemanager.util.DialogHelper;
import com.cyanogenmod.filemanager.util.ExceptionUtil;
import com.cyanogenmod.filemanager.util.MimeTypeHelper;
import com.cyanogenmod.filemanager.util.MimeTypeHelper.MimeTypeCategory;
import com.cyanogenmod.filemanager.util.StringHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class with the convenience methods to print documents
 */
public final class PrintActionPolicy extends ActionsPolicy {

    private static final String TAG = "PrintActionPolicy"; //$NON-NLS-1$

    /**
     * Method that prints the passed document
     *
     * @param ctx The current context
     * @param fso The document to print
     */
    public static void printDocument(final Context ctx, FileSystemObject fso) {
        MimeTypeCategory category = MimeTypeHelper.getCategory(ctx, fso);
        if (category.equals(MimeTypeCategory.TEXT)) {
            printTextDocument(ctx, fso);
            return;
        }
        if (category.equals(MimeTypeCategory.IMAGE)) {
            printImage(ctx, fso);
            return;
        }
        DialogHelper.showToast(ctx, R.string.print_unsupported_document, Toast.LENGTH_SHORT);
    }

    /**
     * Method that prints the document as a text document
     *
     * @param ctx The current context
     * @param fso The document to print
     */
    private static void printTextDocument(final Context ctx, final FileSystemObject document) {
        final int printPageMargins = ctx.getResources().getDimensionPixelSize(
                R.dimen.print_page_margins);

        PrintManager printManager = (PrintManager) ctx.getSystemService(Context.PRINT_SERVICE);
        PrintAttributes attr = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build();
        printManager.print(document.getName(), new PrintDocumentAdapter() {
            private PrintAttributes mAttributes;
            private Paint mPaint;
            private RectF mTextBounds;
            private boolean mIsBinaryDocument;
            private List<String> mLines;
            private List<String> mAdjustedLines;

            private static final int MILS_PER_INCH = 1000;
            private static final int POINTS_IN_INCH = 72;

            @Override
            public void onStart() {
                super.onStart();

                // Create the paint used for draw text
                Typeface courier = Typeface.createFromAsset(ctx.getAssets(),
                        "fonts/Courier-Prime.ttf");
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setTypeface(courier);
                mPaint.setTextSize(ctx.getResources().getDimensionPixelSize(
                        R.dimen.print_text_size));
                mPaint.setColor(Color.BLACK);

                // Get the text width and height
                mTextBounds = new RectF();
                mTextBounds.right = mPaint.measureText(new char[]{'A'}, 0, 1);
                mTextBounds.bottom = mPaint.getFontMetrics().descent
                        - mPaint.getFontMetrics().ascent + mPaint.getFontMetrics().leading;

                mLines = new ArrayList<String>();
                readFile();
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                    CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PrintedPdfDocument pdfDocument = new PrintedPdfDocument(ctx,
                        mAttributes);
                try {
                    Rect pageContentRect = getContentRect(mAttributes);
                    int charsPerRow = (int) (pageContentRect.width() / mTextBounds.width());
                    int rowsPerPage = rowsPerPage(pageContentRect);

                    int currentPage = 0;
                    int currentLine = 0;
                    Page page = null;
                    if (mAdjustedLines.size() > 0) {
                        page = pdfDocument.startPage(currentPage++);
                        printHeader(ctx, page, pageContentRect, charsPerRow);
                    }
                    // Top (with margin) + header
                    float top = pageContentRect.top + (mTextBounds.height() * 2);
                    for (String line : mAdjustedLines) {
                        currentLine++;
                        page.getCanvas().drawText(line, pageContentRect.left,
                                top + (currentLine * mTextBounds.height()), mPaint);

                        if (currentLine >= rowsPerPage) {
                            if (page != null) {
                                printFooter(ctx, page, pageContentRect, currentPage);
                                pdfDocument.finishPage(page);
                            }
                            currentLine = 0;
                            page = pdfDocument.startPage(currentPage++);
                            printHeader(ctx, page, pageContentRect, charsPerRow);
                        }
                    }

                    // Finish the last page
                    printFooter(ctx, page, pageContentRect, currentPage);
                    pdfDocument.finishPage(page);

                    try {
                        // Write the document
                        pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));

                        // Done
                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } catch (IOException ioe) {
                        // Failed.
                        ExceptionUtil.translateException(ctx, ioe);
                        callback.onWriteFailed(null);
                    }
                } finally {
                    if (destination != null) {
                        try {
                            destination.close();
                        } catch (IOException ioe) {
                            /* ignore */
                        }
                    }
                }
            }

            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                    CancellationSignal cancellationSignal, LayoutResultCallback callback,
                    Bundle extras) {

                mAttributes = newAttributes;
                Rect pageContentRect = getContentRect(newAttributes);
                int charsPerRow = (int) (pageContentRect.width() / mTextBounds.width());
                int rowsPerPage = rowsPerPage(pageContentRect);
                adjustLines(pageContentRect, charsPerRow);

                PrintDocumentInfo info = new PrintDocumentInfo.Builder(document.getName())
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(calculatePageCount(rowsPerPage))
                    .build();
                info.setDataSize(document.getSize());
                boolean changed = !newAttributes.equals(oldAttributes);
                callback.onLayoutFinished(info, changed);
            }

            private Rect getContentRect(PrintAttributes attributes) {
                MediaSize mediaSize = attributes.getMediaSize();

                // Compute the size of the target canvas from the attributes.
                int pageWidth = (int) (((float) mediaSize.getWidthMils() / MILS_PER_INCH)
                        * POINTS_IN_INCH);
                int pageHeight = (int) (((float) mediaSize.getHeightMils() / MILS_PER_INCH)
                        * POINTS_IN_INCH);

                // Compute the content size from the attributes.
                Margins minMargins = attributes.getMinMargins();
                final int marginLeft = (int) (((float) minMargins.getLeftMils() / MILS_PER_INCH)
                        * POINTS_IN_INCH);
                final int marginTop = (int) (((float) minMargins.getTopMils() / MILS_PER_INCH)
                        * POINTS_IN_INCH);
                final int marginRight = (int) (((float) minMargins.getRightMils() / MILS_PER_INCH)
                        * POINTS_IN_INCH);
                final int marginBottom = (int) (((float) minMargins.getBottomMils() / MILS_PER_INCH)
                        * POINTS_IN_INCH);
                return new Rect(
                        Math.max(marginLeft, printPageMargins),
                        Math.max(marginTop, printPageMargins),
                        pageWidth - Math.max(marginRight, printPageMargins),
                        pageHeight - Math.max(marginBottom, printPageMargins));
            }

            private void printHeader(Context ctx, Page page, Rect pageContentRect,
                    int charsPerRow) {
                String header = ctx.getString(R.string.print_document_header, document.getName());
                if (header.length() >= charsPerRow) {
                    header = header.substring(header.length() - 3) + "...";
                }
                page.getCanvas().drawText(header,
                        (int) (pageContentRect.width() / 2) - (mPaint.measureText(header) / 2),
                        pageContentRect.top + mTextBounds.height(), mPaint);
            }

            private void printFooter(Context ctx, Page page, Rect pageContentRect, int pageNumber) {
                String footer = ctx.getString(R.string.print_document_footer, pageNumber);
                page.getCanvas().drawText(footer,
                        (int) (pageContentRect.width() / 2) - (mPaint.measureText(footer) / 2),
                        pageContentRect.bottom - mTextBounds.height(), mPaint);
            }

            private void adjustLines(Rect pageRect, int charsPerRow) {
                if (mIsBinaryDocument) {
                    return;
                }
                mAdjustedLines = new ArrayList<String>(mLines);
                for (int i = 0; i < mAdjustedLines.size(); i++) {
                    String line = mAdjustedLines.get(i);
                    if (line.length() > charsPerRow) {
                        int prevSpace = line.lastIndexOf(" ", charsPerRow);
                        if (prevSpace != -1) {
                            // Split in the previous word
                            String currentLine = line.substring(0, prevSpace + 1);
                            String nextLine = line.substring(prevSpace + 1);
                            mAdjustedLines.set(i, currentLine);
                            mAdjustedLines.add(i + 1, nextLine);
                        } else {
                            // Just split at margin
                            String currentLine = line.substring(0, charsPerRow);
                            String nextLine = line.substring(charsPerRow);
                            mAdjustedLines.set(i, currentLine);
                            mAdjustedLines.add(i + 1, nextLine);
                        }
                    }
                }
            }

            private int calculatePageCount(int rowsPerPage) {
                int pages = mAdjustedLines.size() / rowsPerPage;
                return pages <= 0 ? PrintDocumentInfo.PAGE_COUNT_UNKNOWN : pages;
            }

            private int rowsPerPage(Rect pageContentRect) {
                // Text height - header - footer
                return (int) ((pageContentRect.height() / mTextBounds.height()) - 4);
            }

            private void readFile() {
                mIsBinaryDocument = isBinaryDocument();
                if (mIsBinaryDocument) {
                    readHexDumpDocumentFile();
                } else {
                    readDocumentFile();
                }
            }

            private boolean isBinaryDocument() {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(document.getFullPath()));
                    char[] data = new char[50];
                    int read = br.read(data);
                    for (int i = 0; i < read; i++) {
                        if (!StringHelper.isPrintableCharacter(data[i])) {
                            return true;
                        }
                    }
                } catch (IOException ex) {
                    //Ignore
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException ex) {
                            //Ignore
                        }
                    }
                }
                return false;
            }

            private void readDocumentFile() {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(document.getFullPath()));
                    String line = null;
                    while((line = br.readLine()) != null) {
                        mLines.add(line);
                    }
                } catch (IOException ex) {
                    mLines.clear();
                    Log.e(TAG, "Failed to read file " + document.getFullPath(), ex);
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException ex) {
                            //Ignore
                        }
                    }
                }
            }

            private void readHexDumpDocumentFile() {
                InputStream is = null;
                ByteArrayOutputStream baos;
                try {
                    int bufferSize = ctx.getResources().getInteger(R.integer.buffer_size);

                    baos = new ByteArrayOutputStream();
                    is = new BufferedInputStream(new FileInputStream(document.getFullPath()));
                    byte[] data = new byte[bufferSize];
                    int read = 0;
                    while((read = is.read(data, 0, bufferSize)) != -1) {
                        baos.write(data, 0, read);
                    }
                } catch (IOException ex) {
                    mLines.clear();
                    Log.e(TAG, "Failed to read file " + document.getFullPath(), ex);
                    return;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            //Ignore
                        }
                    }
                }

                // Convert the bytes to a hex printable string and free resources
                String documentBuffer = StringHelper.toHexPrintableString(baos.toByteArray());
                try {
                    baos.close();
                } catch (IOException ex) {
                    //Ignore
                }

                BufferedReader br = null;
                try {
                    br = new BufferedReader(new StringReader(documentBuffer));
                    String line = null;
                    while((line = br.readLine()) != null) {
                        mLines.add(line);
                    }
                } catch (IOException ex) {
                    mLines.clear();
                    Log.e(TAG, "Failed to read file " + document.getFullPath(), ex);
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException ex) {
                            //Ignore
                        }
                    }
                }

                // Use the final array and clear the original (we don't use it anymore)
                mAdjustedLines = new ArrayList<String>(mLines);
                mLines.clear();
            }

        }, attr);
    }

    /**
     * Method that prints the document as an image
     *
     * @param ctx The current context
     * @param fso The image to print
     */
    private static void printImage(final Context ctx, final FileSystemObject image) {
        // Check that the image is supported by Android
        if (isValidImageDocument(image.getFullPath())) {
            DialogHelper.showToast(ctx, R.string.print_unsupported_image, Toast.LENGTH_SHORT);
            return;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        final Bitmap bitmap = BitmapFactory.decodeFile(image.getFullPath(), options);

        PrintManager printManager = (PrintManager) ctx.getSystemService(Context.PRINT_SERVICE);
        PrintAttributes.MediaSize mediaSize = PrintAttributes.MediaSize.UNKNOWN_PORTRAIT;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            mediaSize = PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE;
        }
        PrintAttributes attr = new PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build();
        printManager.print(image.getName(), new PrintDocumentAdapter() {
            private PrintAttributes mAttributes;

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                    CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PrintedPdfDocument pdfDocument = new PrintedPdfDocument(ctx,
                        mAttributes);
                try {
                    Page page = pdfDocument.startPage(1);

                    RectF content = new RectF(page.getInfo().getContentRect());

                    Matrix matrix = getMatrix(bitmap.getWidth(), bitmap.getHeight(), content);

                    // Draw the bitmap.
                    page.getCanvas().drawBitmap(bitmap, matrix, null);

                    // Finish the page.
                    pdfDocument.finishPage(page);

                    try {
                        // Write the document
                        pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));

                        // Done
                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } catch (IOException ioe) {
                        // Failed.
                        ExceptionUtil.translateException(ctx, ioe);
                        callback.onWriteFailed(null);
                    }
                } finally {
                    if (pdfDocument != null) {
                        pdfDocument.close();
                    }
                    if (destination != null) {
                        try {
                            destination.close();
                        } catch (IOException ioe) {
                            /* ignore */
                        }
                    }
                }
            }

            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                    CancellationSignal cancellationSignal, LayoutResultCallback callback,
                    Bundle extras) {

                mAttributes = newAttributes;

                PrintDocumentInfo info = new PrintDocumentInfo.Builder(image.getName())
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
                    .setPageCount(1)
                    .build();
                boolean changed = !newAttributes.equals(oldAttributes);
                callback.onLayoutFinished(info, changed);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }

            private Matrix getMatrix(int imageWidth, int imageHeight, RectF content) {
                Matrix matrix = new Matrix();

                // Compute and apply scale to fill the page.
                int widthRatio = content.width() / imageWidth;
                int heightRatio = content.height() / imageHeight;
                float scale = Math.max(widthRatio, heightRatio);
                matrix.postScale(scale, scale);

                // Center the content.
                final float translateX = (content.width()
                        - imageWidth * scale) / 2;
                final float translateY = (content.height()
                        - imageHeight * scale) / 2;
                matrix.postTranslate(translateX, translateY);
                return matrix;
            }
        }, attr);
    }

    /**
     * Check if the file is a valid image document allowed by android to be printed
     *
     * @param file The image to check
     * @return boolean If the image is a valid document
     */
    private static boolean isValidImageDocument(String file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(file, options);
        if (bitmap != null) {
            bitmap.recycle();
        }
        return bitmap != null;
    }
}