package com.hackzurich.documentshelper.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.activity.PrintActivity;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.sec.android.ngen.common.lib.ssp.Ssp;


/**
 * Fragment to show error related to incorrect Smart UX SDK initialization,
 * which can be caused by missed
 */
public final class InitializationErrorDialogFragment extends AppCompatDialogFragment {
    private static final String TAG = "InitErrorDialog";
    private static final String SDK_ERROR_TEXT_ARG = "ErrorText";
    private static final String PRINTER_CHANGE_ARG = "PrinterChange";

    /**
     * Create a new instance of InitializationErrorDialogFragment.
     *
     * @param context       {@link Context} to access resources
     * @param e             {@link Exception} to use as argument
     * @param changePrinter true if printer change option should be presented
     * @return created DialogFragment
     */
    public static InitializationErrorDialogFragment newInstance(final Context context, final Exception e, final boolean changePrinter) {
        InitializationErrorDialogFragment f = new InitializationErrorDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();

        if (e instanceof SsdkUnsupportedException) {
            switch (((SsdkUnsupportedException) e).getType()) {
                case SsdkUnsupportedException.LIBRARY_NOT_INSTALLED:
                case SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED:
                    args.putString(SDK_ERROR_TEXT_ARG, context.getString(R.string.sdk_support_missing));
                    break;

                default:
                    args.putString(SDK_ERROR_TEXT_ARG,
                            context.getString(R.string.unknown_error) + ((SsdkUnsupportedException) e).getType());
                    break;
            }
        } else {
            args.putString(SDK_ERROR_TEXT_ARG, e.getMessage());
        }

        args.putBoolean(PRINTER_CHANGE_ARG, changePrinter);
        f.setArguments(args);

        f.setCancelable(false);
        return f;
    }

    /**
     * Create a new instance of InitializationErrorDialogFragment.
     *
     * @param errorStr      {@link String} with init error string
     * @param changePrinter true if printer change option should be presented
     * @return created DialogFragment
     */
    public static InitializationErrorDialogFragment newInstance(final String errorStr, final boolean changePrinter) {
        InitializationErrorDialogFragment f = new InitializationErrorDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();

        args.putString(SDK_ERROR_TEXT_ARG, errorStr);
        args.putBoolean(PRINTER_CHANGE_ARG, changePrinter);
        f.setArguments(args);

        f.setCancelable(false);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (args.containsKey(SDK_ERROR_TEXT_ARG)) {
            final String errorText = args.getString(SDK_ERROR_TEXT_ARG, "Not able to execute operation");

            builder.setMessage(errorText);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    getActivity().finish();
                }
            });

            if (args.getBoolean(PRINTER_CHANGE_ARG, false)) {
                builder.setNegativeButton(R.string.select_printer, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        Ssp.Printer.openSelectionActivity(getActivity(), PrintActivity.PRINTER_SELECTION_CODE, null);
                    }
                });
            }
        }

        return builder.create();
    }
}
