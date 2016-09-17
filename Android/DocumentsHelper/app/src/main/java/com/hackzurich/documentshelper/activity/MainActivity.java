package com.hackzurich.documentshelper.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.model.Document;
import com.hackzurich.documentshelper.network.ServiceClient;

import java.io.File;
import java.io.FileInputStream;
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
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DocumentHelper";

    private static final String FILENAME = "fileToPrint";

    private static final int FILE_SELECT_CODE = 1;

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
                    // Get the Uri of the selected file
                    Uri uri = data.getData();

                    uploadFile(uri)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Document>() {
                                @Override
                                public void call(Document document) {
                                    // TODO Save document response here
                                    // TODO Proceed further
                                    Toast.makeText(MainActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    // TODO Handle error
                                    Toast.makeText(MainActivity.this, "Fail: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

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
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file = null;
        try {
            // read this file into InputStream
//            inputStream = new FileInputStream(uri);

            inputStream = getContentResolver().openInputStream(uri);

            // write the inputStream to a FileOutputStream
            file = new File(getFilesDir(), FILENAME);
//            outputStream = new FileOutputStream(file);

            outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);

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


}
