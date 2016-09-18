package com.hackzurich.documentshelper.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.model.Document;
import com.hackzurich.documentshelper.network.ServiceApi;
import com.hackzurich.documentshelper.network.ServiceClient;
import com.hackzurich.documentshelper.network.request.CheckRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DocumentHelper";

    private static final String FILENAME = "fileToPrint";

    private static final int FILE_SELECT_CODE = 1;

    public static final String DOCUMENT_KEY = "DocumentKey";

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.pickFileBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showFileChooser();
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {

                    mProgressDialog = ProgressDialog.show(this, "", getString(R.string.upload_file_progress));

                    // Get the Uri of the selected file
                    final Uri uri = data.getData();

                    String filename = getDisplayName(uri);
                    CheckRequest checkRequest = new CheckRequest(filename);

                    ServiceApi serviceApi = ServiceClient.getInstance().getServiceApi();
                    serviceApi.check(checkRequest)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(Schedulers.newThread())
                            .flatMap(new Func1<Document, Observable<Document>>() {
                                @Override
                                public Observable<Document> call(Document document) {
                                    if(!TextUtils.isEmpty(document.getFile())) {
                                        return Observable.just(document);
                                    } else {
                                        return uploadFile(uri);
                                    }
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Document>() {
                                @Override
                                public void call(Document document) {
                                    mProgressDialog.dismiss();

                                    // serialize the response and transfer to the next activity
                                    Gson gson = new Gson();
                                    String documentJson = gson.toJson(document);
                                    Intent intent = new Intent(MainActivity.this, DocumentActivity.class);
                                    intent.putExtra(DOCUMENT_KEY, documentJson);

                                    startActivity(intent);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Fail: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

//                    uploadFile(uri)
//                            .subscribeOn(Schedulers.newThread())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Action1<Document>() {
//                                @Override
//                                public void call(Document document) {
//                                    mProgressDialog.dismiss();
//
//                                    // serialize the response and transfer to the next activity
//                                    Gson gson = new Gson();
//                                    String documentJson = gson.toJson(document);
//                                    Intent intent = new Intent(MainActivity.this, DocumentActivity.class);
//                                    intent.putExtra(DOCUMENT_KEY, documentJson);
//
//                                    startActivity(intent);
//                                }
//                            }, new Action1<Throwable>() {
//                                @Override
//                                public void call(Throwable throwable) {
//                                    mProgressDialog.dismiss();
//                                    Toast.makeText(MainActivity.this, "Fail: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            });

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Observable<Document> uploadFile(Uri fileUri) {

        File file = readFile(fileUri);

        MediaType mediaType = MediaType.parse("multipart/form-data");
        RequestBody requestFile = RequestBody.create(mediaType, file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // do upload
        return ServiceClient.getInstance().getServiceApi().upload(body);
    }

    private File readFile(Uri uri) {

        String filename = getDisplayName(uri);

        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File folder = new File(path.getPath() + "/toPrint");
            if (!folder.exists()) {
                folder.mkdir();
            }

            file = new File(folder.getPath() + "/" + filename);
            outputStream = new FileOutputStream(file);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return file;
    }

    private String getDisplayName(Uri uri) {
        String displayName = FILENAME;
        if (uri.toString().startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri.toString().startsWith("file://")) {
            File myFile = new File(uri.toString());
            displayName = myFile.getName();
        }
        return displayName;
    }

}
