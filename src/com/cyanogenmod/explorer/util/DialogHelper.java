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

package com.cyanogenmod.explorer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cyanogenmod.explorer.R;

/**
 * A helper class with useful methods for deal with dialogs.
 */
public final class DialogHelper {

    /**
     * Constructor of <code>DialogHelper</code>.
     */
    private DialogHelper() {
        super();
    }

    /**
     * Method that creates a new warning {@link AlertDialog}.
     *
     * @param context The current context
     * @param message The resource identifier of the message of the alert dialog
     * @return AlertDialog The alert dialog reference
     */
    public static AlertDialog createWarningDialog(Context context, int message) {
        return createAlertDialog(
                context,
                R.drawable.ic_holo_light_warning,
                R.string.title_warning,
                message);
    }

    /**
     * Method that creates a new error {@link AlertDialog}.
     *
     * @param context The current context
     * @param message The resource identifier of the message of the alert dialog
     * @return AlertDialog The alert dialog reference
     */
    public static AlertDialog createErrorDialog(Context context, int message) {
        return createAlertDialog(
                context,
                R.drawable.ic_holo_light_error,
                R.string.title_error,
                message);
    }

    /**
     * Method that creates a new {@link AlertDialog}.
     *
     * @param context The current context
     * @param icon The icon resource
     * @param title The resource identifier of the title of the alert dialog
     * @param message The resource identifier of the message of the alert dialog
     * @return AlertDialog The alert dialog reference
     */
    public static AlertDialog createAlertDialog(Context context, int icon, int title, int message) {
        //Create the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCustomTitle(createTitle(context, icon, context.getString(title)));
        builder.setView(createMessage(context, message));
        builder.setPositiveButton(context.getString(R.string.ok), null);
        return builder.create();
    }

    /**
     * Method that creates a new YES/NO {@link AlertDialog}.
     *
     * @param context The current context
     * @param message The resource identifier of the message of the alert dialog
     * @param onClickListener The listener where returns the button pressed
     * @return AlertDialog The alert dialog reference
     */
    public static AlertDialog createYesNoDialog(
            Context context, int message, OnClickListener onClickListener) {
        //Create the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCustomTitle(
                createTitle(
                        context,
                        R.drawable.ic_holo_light_question,
                        context.getString(R.string.title_question)));
        builder.setView(createMessage(context, message));
        AlertDialog dialog = builder.create();
        dialog.setButton(
                DialogInterface.BUTTON_POSITIVE, context.getString(R.string.yes), onClickListener);
        dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.no), onClickListener);
        return dialog;
    }

    /**
     * Method that creates a new {@link AlertDialog}.
     *
     * @param context The current context
     * @param icon The icon resource
     * @param title The resource identifier of the title of the alert dialog
     * @param content The content layout
     * @return AlertDialog The alert dialog reference
     */
    public static AlertDialog createDialog(Context context, int icon, int title, View content) {
        //Create the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCustomTitle(createTitle(context, icon, context.getString(title)));
        builder.setView(content);
        return builder.create();
    }

    /**
     * Method that creates a new {@link AlertDialog}.
     *
     * @param context The current context
     * @param icon The icon resource
     * @param title The title of the alert dialog
     * @param content The content layout
     * @return AlertDialog The alert dialog reference
     */
    public static AlertDialog createDialog(Context context, int icon, String title, View content) {
        //Create the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCustomTitle(createTitle(context, icon, title));
        builder.setView(content);
        return builder.create();
    }

    /**
     * Method that creates and returns the title of the dialog.
     *
     * @param context The current context
     * @param icon The icon resource
     * @param title The resource identifier of the title of the alert dialog
     * @return The title view
     */
    private static View createTitle(Context context, int icon, String title) {
        //Inflate the dialog layouts
        LayoutInflater li =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lyTitle = li.inflate(R.layout.dialog_title, null);
        ImageView vIcon = (ImageView)lyTitle.findViewById(R.id.dialog_title_icon);
        if (icon != 0) {
            vIcon.setBackgroundResource(icon);
        } else {
            vIcon.setVisibility(View.GONE);
        }
        TextView vText = (TextView)lyTitle.findViewById(R.id.dialog_title_text);
        vText.setText(title);
        return lyTitle;
    }

    /**
     * Method that creates and returns the title of the dialog.
     *
     * @param context The current context
     * @param message The resource identifier of the message of the alert dialog
     * @return The title view
     */
    private static View createMessage(Context context, int message) {
        //Inflate the dialog layouts
        LayoutInflater li =
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lyMessage = li.inflate(R.layout.dialog_message, null);
        TextView vMsg = (TextView)lyMessage.findViewById(R.id.dialog_message);
        vMsg.setText(message);
        return lyMessage;
    }

    /**
     * Method that creates and returns a {@list ListPopupWindow} reference.
     *
     * @param context The current context
     * @param adapter The adapter to associate with the popup
     * @param anchor The view that is used as an anchor to show the popup
     * @return ListPopupWindow The {@list ListPopupWindow} reference
     */
    public static ListPopupWindow createListPopupWindow(
            Context context, final ListAdapter adapter, View anchor) {
        final ListPopupWindow popup = new ListPopupWindow(context);
        popup.setAdapter(adapter);
        popup.setContentWidth(context.getResources().getDimensionPixelSize(R.dimen.popup_width));
        popup.setAnchorView(anchor);
        popup.setModal(true);
        return popup;
    }

    /**
     * Method that creates and display a toast dialog.
     *
     * @param context The context to use.
     * @param msg The message to display.
     * @param duration How long to display the message.
     */
    public static void showToast(Context context, String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }

    /**
     * Method that creates and display a toast dialog.
     *
     * @param context The context to use.
     * @param msgResourceId The resource id of the string resource to use.
     * @param duration How long to display the message.
     */
    public static void showToast(Context context, int msgResourceId, int duration) {
        Toast.makeText(context, msgResourceId, duration).show();
    }

}
