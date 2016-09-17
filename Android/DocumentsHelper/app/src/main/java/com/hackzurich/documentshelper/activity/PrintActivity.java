package com.hackzurich.documentshelper.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import com.hackzurich.documentshelper.JobCompleteReceiver;
import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.fragments.InitializationErrorDialogFragment;
import com.hackzurich.documentshelper.fragments.PrintConfigureFragment;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.sec.android.ngen.common.lib.ssp.CapabilitiesExceededException;
import com.sec.android.ngen.common.lib.ssp.DeviceNotReadyException;
import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.SpsCause;
import com.sec.android.ngen.common.lib.ssp.Ssp;
import com.sec.android.ngen.common.lib.ssp.job.JobService;
import com.sec.android.ngen.common.lib.ssp.job.JobletAttributes;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes.AutoFit;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes.ColorMode;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes.Duplex;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributesCaps;
import com.sec.android.ngen.common.lib.ssp.printer.PrinterService;
import com.sec.android.ngen.common.lib.ssp.printer.Printlet;
import com.sec.android.ngen.common.lib.ssp.printer.PrintletAttributes;

/**
 * Main activity for Print Sample.
 * The activity shows the following interactions:<br>
 * <ol>
 * <li>How to initialize Smart UX SDK</li>
 * <li>How to get the print service to get attribute capability details</li>
 * <li>How to launch print job on MFP</li>
 * </ol>
 */
public final class PrintActivity extends AppCompatActivity {
    private static final String TAG = "PrintSampleActivity";

    public static final String ACTION_PRINT_COMPLETED = "com.sec.android.ssp.sample.print.ACTION_PRINT_COMPLETED";

    private static final String ERROR_DIALOG_FRAGMENT = "errorDialogFragment";

    /** Code for printer selection launch */
    public static final int PRINTER_SELECTION_CODE = 1;

    private static final String FILE_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";

    private SharedPreferences mPrefs = null;
    private PrintObserver mObserver = null;

    /** Smart UX SDK initialization errors dialog */
    private InitializationErrorDialogFragment mDialog;

    /** Fragment to display attributes configuration UI */
    private PrintConfigureFragment mFragment = null;

    /** Main layout container */
    private View mContainer;
    /** Background task for SmartUX initialization */
    private InitializationTask mInitializationTask;

    /**
     * Observer for Print result and progress.
     */
    private class PrintObserver extends PrinterService.AbstractPrintletObserver {

        private int mJobId = 0;

        public PrintObserver(final Handler handler) {
            super(handler);
        }

        @Override
        public void onCancel(final String rid) {
            Log.d(TAG, "Received Print Cancel");
            showToast("Print cancelled!");
        }

        @Override
        public void onComplete(final String rid, final Bundle bundle) {
            Log.d(TAG, "Received Print Complete");
            showToast("Print completed!");

            Log.d(TAG, "onComplete: with data \n" +
                    "  KEY_IMAGE_COUNT: " + bundle.getInt(Printlet.Keys.KEY_IMAGE_COUNT, 0) + "\n" +
                    "  KEY_SET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SET_COUNT, 0) + "\n" +
                    "  KEY_SHEET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SHEET_COUNT, 0));
        }

        @Override
        public void onFail(final String rid, final Result result) {
            Log.e(TAG, "Received Print Fail, Result " + result.mCode);
            showToast("Print failed! " + result);

            if (result.mCode == Result.RESULT_WS_FAILURE) {
                final Result.WSCause cause = Result.getWSCause(result);

                if (cause != null) {
                    Log.w(TAG, cause.toString());
                } else {
                    Log.w(TAG, "Failed without any cause");
                }

                Toast.makeText(PrintActivity.this, "Web service cause: " + cause, Toast.LENGTH_SHORT).show();
            } else {
                final List<SpsCause> causes = result.getSpsCause();

                Toast.makeText(PrintActivity.this, "Sps results: " + causes, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onProgress(final String rid, final Bundle bundle) {
            if (bundle.containsKey(Printlet.Keys.KEY_JOBID)) {
                mJobId = bundle.getInt(Printlet.Keys.KEY_JOBID);
                Log.d(TAG, "onProgress: Received jobID as " + mJobId);
                showToast("Job ID is " + mJobId);

                if (mPrefs.getBoolean(PrintConfigureFragment.PREF_MONITORING_JOB, false)) {
                    final Intent intent = new Intent(getApplicationContext(), JobCompleteReceiver.class);

                    intent.setAction(ACTION_PRINT_COMPLETED);
                    Log.d(TAG, "MonitorJob " + mJobId);
                    // Store Job Id in order to verify it in the Broadcast Receiver
                    mPrefs.edit().putInt(PrintConfigureFragment.CURRENT_JOB_ID, mJobId).apply();

                    final boolean showProgress =
                            mPrefs.getBoolean(PrintConfigureFragment.PREF_SHOW_JOB_PROGRESS, true);

                    // Monitor the job completion
                    final JobletAttributes taskAttributes =
                            new JobletAttributes.Builder().setShowUi(showProgress).build();
                    final String jrid = JobService.monitorJobInForeground(PrintActivity.this, mJobId,
                            taskAttributes, intent);

                    Log.d(TAG, "MonitorJob request: " + jrid);
                }

                Log.d(TAG, "onProgress: with data \n" +
                        "  KEY_IMAGE_COUNT: " + bundle.getInt(Printlet.Keys.KEY_IMAGE_COUNT, 0) + "\n" +
                        "  KEY_SET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SET_COUNT, 0) + "\n" +
                        "  KEY_SHEET_COUNT: " + bundle.getInt(Printlet.Keys.KEY_SHEET_COUNT, 0));
            }
        }
    }

    /**
     * Shows toast
     *
     * @param text {@link String} toasts text
     */
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Async task to request printers capabilities and launch Print.
     */
    private static final class PrintAsyncTask extends AsyncTask<Void, Void, Void> {
        /** Observer to be used as callback */
        private final WeakReference<PrintObserver> mObserver;
        /** Application Context */
        private final Context mContext;
        /** Preferences to obtain Print Settings */
        private final SharedPreferences mPrefs;
        /** Error Message string to provide to the user */
        private String mErrorMsg = null;

        PrintAsyncTask(final Context context, final PrintObserver observer) {
            mObserver = new WeakReference<>(observer);
            mContext = context;
            mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }

        @Override
        protected Void doInBackground(final Void... params) {
            final PrintAttributesCaps caps = requestCaps(mContext);
            final Resources res = mContext.getResources();

            if (null == caps) {
                mErrorMsg = "Not able to obtain printers capabilities";
                return null;
            }

            try {
                final Duplex duplex = Duplex.valueOf(
                        mPrefs.getString(PrintConfigureFragment.PREF_DUPLEX_MODE,
                                Duplex.DEFAULT.name()));
                Log.i(TAG, "Selected Duplex:" + duplex.name());

                final ColorMode cm = ColorMode.valueOf(
                        mPrefs.getString(PrintConfigureFragment.PREF_COLOR_MODE,
                                         ColorMode.DEFAULT.name()));
                Log.i(TAG, "Selected Color Mode:" + cm.name());

                final String saf = mPrefs.getString(PrintConfigureFragment.PREF_AUTOFIT,
                        AutoFit.DEFAULT.name());
                final AutoFit af = AutoFit.valueOf(saf);
                Log.i(TAG, "Selected af: " + saf);

                final int copies = Integer.valueOf(
                        mPrefs.getString(PrintConfigureFragment.PREF_COPIES, "1"));

                String filePath = mPrefs.getString(PrintConfigureFragment.PREF_FILENAME, "");

                if (!TextUtils.isEmpty(filePath) && !filePath.startsWith("/")) {
                    filePath = FILE_PATH + filePath;
                } else {
                    Log.i(TAG, "Absolute path has been entered: " + filePath);
                }
                Log.i(TAG, "Selected path: " + filePath);

                // Build PrintAttributes based on preferences values
                final PrintAttributes attributes =
                        new PrintAttributes.PrintFromStorageBuilder(Uri.fromFile(new File(filePath)))
                        .setColorMode(cm)
                        .setDuplex(duplex)
                        .setAutoFit(af)
                        .setCopies(copies)
                        .build(caps);

                // Clean stored job id if any
                if (mObserver.get() != null) {
                    // Reset old job id
                    mObserver.get().mJobId = 0;
                }

                final PrintletAttributes taskAttribs = new PrintletAttributes.Builder()
                        .setShowSettingsUi(mPrefs.getBoolean(PrintConfigureFragment.PREF_SHOW_SETTINGS, false))
                        .build();

                // Submit the job
                PrinterService.submit(mContext, attributes, taskAttribs);
            } catch (final CapabilitiesExceededException e) {
                Log.e(TAG, "Caps were exceeded: ", e);
                mErrorMsg = "CapabilitiesExceededException: " + e.getMessage();
            } catch (final IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument was provided: ", e);
                mErrorMsg = e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);

            if (mErrorMsg != null) {
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Async task to request printers capabilities and launch Print.
     */
    private static final class LoadCapabilitiesAsyncTask extends AsyncTask<Void, Void, Void> {
        /** Application Context */
        private final Context mContext;
        /** Error Message string to provide to the user */
        private String mErrorMsg = null;

        private PrintAttributesCaps mCaps = null;
        private PrintConfigureFragment mFragment = null;

        LoadCapabilitiesAsyncTask(final Context context, PrintConfigureFragment fragment) {
            mContext = context;
            mFragment = fragment;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mCaps = requestCaps(mContext);

            if (null == mCaps) {
                mErrorMsg = "Not able to obtain printers capabilities";
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);

            if (mErrorMsg != null) {
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            } else {
                mFragment.loadCapabilities(mCaps);
                Toast.makeText(mContext, R.string.loaded_caps, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mObserver = new PrintObserver(new Handler());

        // Set listener for Print execution
        findViewById(R.id.printButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                executePrint();
            }
        });

        // Set listener for Load
        findViewById(R.id.loadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadCapabilities();
            }
        });

        mContainer = findViewById(R.id.container);
    }

    /**
     * Exception in could be because of following reasons
     * <ol>
     * <li>Library is not installed</li>
     * <li>Library update is needed</li>
     * <li>Version issue, unsupported</li>
     * <li> Permission is not granted properly</li>
     * </ol>
     * 
     * @param e {@link Exception}
     */
    private void handleInitException(final Exception e) {
        mDialog = InitializationErrorDialogFragment.newInstance(getApplicationContext(), e, false);
        mDialog.show(getSupportFragmentManager(), ERROR_DIALOG_FRAGMENT);
    }

    /**
     * Launches capabilities loading async task
     */
    private void loadCapabilities() {
        // Pass application context
        new LoadCapabilitiesAsyncTask(getApplicationContext(), mFragment).execute();
    }

    /**
     * Launches Print job
     */
    private void executePrint() {
        // Pass application context
        new PrintAsyncTask(getApplicationContext(), mObserver).execute();
    }

    /**
     * Requests printer Print capabilities
     *
     * @param context {@link Context} to obtain data
     *
     * @return {@link PrintAttributesCaps}
     */
    private static PrintAttributesCaps requestCaps(final Context context) {
        final Result result = new Result();
        final PrintAttributesCaps caps = PrinterService.getCapabilities(context, result);

        if (caps != null) {
            Log.d(TAG, "Received Caps as:" +
                    "AutoFit: " + caps.getAutoFitList() +
                    ", ColorMode: " + caps.getColorModeList() +
                    ", Max Copies: " + caps.getMaxCopies() +
                    ", Duplex: " + caps.getDuplexList());
        }

        return caps;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mObserver.register(getApplicationContext());

        // For mobile case connection to printer can take significant time, so do initialization in background
        mContainer.setEnabled(false);
        mInitializationTask = new InitializationTask();
        mInitializationTask.execute();

        mFragment = (PrintConfigureFragment)getFragmentManager().findFragmentById(R.id.data_container);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mInitializationTask.cancel(true);
        mInitializationTask = null;

        // remove error dialog on Pause
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        mObserver.unregister(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (!Ssp.Platform.IS_PRINTER_DEVICE) {
            menu.add(Menu.NONE, R.id.select_printer, 0, R.string.select_printer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item != null && item.getItemId() == R.id.select_printer) {
            Ssp.Printer.openSelectionActivity(this, PRINTER_SELECTION_CODE, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == PRINTER_SELECTION_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Log.d(TAG, "Printer connected");
                    break;

                default:
                    Log.d(TAG, "Printer connection was cancelled / failed");
                    break;
            }

            if (mFragment != null) {
                mFragment.resetConfiguration();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Simple init statuses enum for internal usage
     */
    private enum InitStatus {
        INIT_EXCEPTION,
        NOT_SUPPORTED,
        NO_ERROR
    }

    /**
     * Simple initialization task, for mobile context is recommended
     * to be done in separate thread / AsyncTask
     */
    private class InitializationTask extends AsyncTask<Void, Void, InitStatus> {

        private Exception mException = null;

        @Override
        protected InitStatus doInBackground(final Void... params) {
            InitStatus status = InitStatus.NO_ERROR;

            try {
                // initialize the SSP with app context
                Ssp.getInstance().initialize(getApplicationContext());
            } catch (final SsdkUnsupportedException e) {
                Log.e(TAG, "SDK is not supported!", e);
                mException = e;
                status = InitStatus.INIT_EXCEPTION;
            } catch (final SecurityException e) {
                Log.e(TAG, "Security exception!", e);
                mException = e;
                status = InitStatus.INIT_EXCEPTION;
            } catch (final DeviceNotReadyException e) {
                Log.e(TAG, "DeviceNotReadyException exception", e);
                mException = e;
                status = InitStatus.INIT_EXCEPTION;
            }

            // Check if Copy is supported
            if (status == InitStatus.NO_ERROR
                    && Ssp.Printer.isConnected(getContentResolver())
                    && !PrinterService.isSupported(getApplicationContext())) {
                status = InitStatus.NOT_SUPPORTED;
            }

            return status;
        }

        @Override
        protected void onPostExecute(final InitStatus status) {
            if (status == InitStatus.NO_ERROR || status == null) {
                Log.d(TAG, "SmartUX initialized");
                mContainer.setEnabled(true);
                return;
            }

            switch (status) {
                case INIT_EXCEPTION:
                    handleInitException(mException);
                    break;

                case NOT_SUPPORTED:
                    mDialog = InitializationErrorDialogFragment.newInstance(getString(R.string.print_not_supported),
                            !Ssp.Platform.IS_PRINTER_DEVICE);
                    mDialog.show(getSupportFragmentManager(), ERROR_DIALOG_FRAGMENT);
                    break;

                default:
                    mContainer.setEnabled(true);
                    Log.e(TAG, "Invalid init status received");
                    break;
            }
        }
    }

}
