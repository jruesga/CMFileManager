package com.cyanogenmod.filemanager.ui.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.cyanogenmod.filemanager.R;
import com.cyanogenmod.filemanager.model.MountPoint;
import com.cyanogenmod.filemanager.tasks.FilesystemAsyncTask;
import com.cyanogenmod.filemanager.ui.ThemeManager;
import com.cyanogenmod.filemanager.util.MountPointHelper;

public class DialogBreadcrumbView extends BreadcrumbView {
    public DialogBreadcrumbView(Context context) {
        super(context);
    }

    public DialogBreadcrumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DialogBreadcrumbView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void applyTheme() {
        ThemeManager.Theme theme = ThemeManager.getCurrentTheme(getContext());

        //- Breadcrumb
        if (this.mBreadcrumbBar != null) {
            int cc = this.mBreadcrumbBar.getChildCount();
            for (int i = 0; i < cc; i++) {
                // There are 2 types: Breadcrumb items and separators
                View v = this.mBreadcrumbBar.getChildAt(i);
                if (v instanceof BreadcrumbItem) {
                    // Breadcrumb item
                    theme.setTextColor(
                            getContext(), (BreadcrumbItem)v, "text_color"); //$NON-NLS-1$
                } else if (v instanceof ImageView) {
                    // Divider drawable
                    theme.setImageDrawable(
                            getContext(),
                            (ImageView)v, "breadcrumb_divider_drawable"); //$NON-NLS-1$
                }
            }
        }
        if (this.mDiskUsageInfo != null) {
            Drawable dw = theme.getDrawable(getContext(), "horizontal_progress_bar"); //$NON-NLS-1$
            this.mDiskUsageInfo.setProgressDrawable(dw);
        }
        final ImageView fsInfo = (ImageView)findViewById(R.id.ab_filesystem_info);
        if (fsInfo != null) {
            MountPoint mp = (MountPoint) fsInfo.getTag();
            if (mp == null) {
                theme.setImageDrawable(getContext(), fsInfo, "filesystem_dialog_warning_drawable");
            } else {
                String resource =
                        MountPointHelper.isReadOnly(mp)
                                ? "filesystem_dialog_locked_drawable"
                                : "filesystem_dialog_unlocked_drawable";
                theme.setImageDrawable(getContext(), fsInfo, resource);
            }
        }
    }

    @Override
    public synchronized void updateMountPointInfo() {
        //Cancel the current execution (if any) and launch again
        if (this.mFilesystemAsyncTask != null && this.mFilesystemAsyncTask.isRunning()) {
            this.mFilesystemAsyncTask.cancel(true);
        }
        final ImageView filesystemInfo = (ImageView)findViewById(R.id.ab_filesystem_info);
        final ProgressBar diskUsageInfo = (ProgressBar)findViewById(R.id.breadcrumb_diskusage);
        this.mFilesystemAsyncTask =
                new FilesystemAsyncTask(
                        getContext(), filesystemInfo,
                        diskUsageInfo, this.mFreeDiskSpaceWarningLevel, true);
        this.mFilesystemAsyncTask.execute(this.mCurrentPath);
    }
}
