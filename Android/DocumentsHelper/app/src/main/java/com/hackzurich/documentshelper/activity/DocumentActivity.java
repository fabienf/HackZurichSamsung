package com.hackzurich.documentshelper.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.adapter.DocumentListAdapter;
import com.hackzurich.documentshelper.model.Document;
import com.hackzurich.documentshelper.model.Part;
import com.hackzurich.documentshelper.network.ServiceApi;
import com.hackzurich.documentshelper.network.ServiceClient;
import com.hackzurich.documentshelper.network.request.GenerateRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DocumentActivity extends AppCompatActivity {

    private static final String TAG = "DocumentHelper";

    private static final String FILENAME = "fileToPrint";

    public static final String FILEPATH_KEY = "FilePath";

    private RecyclerView mPartsList;
    private Document mActiveDocument;
    private Set<Part> mSelectedForPrinting = new HashSet<>();

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        String document = getIntent().getStringExtra(MainActivity.DOCUMENT_KEY);
        Gson gson = new Gson();
        mActiveDocument = gson.fromJson(document, Document.class);

        setTitle(mActiveDocument.getFile());

        final Button printButton = (Button) findViewById(R.id.documents_activity_print_btn);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDocument();
            }
        });
        printButton.setEnabled(false);

        mPartsList = (RecyclerView) findViewById(R.id.documents_activity_parts_list);
        mPartsList.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mPartsList.setLayoutManager(layoutManager);

        DocumentListAdapter adapter = new DocumentListAdapter(this, mActiveDocument.getParts(), new DocumentListAdapter.PartListener() {
            @Override
            public void onPartChecked(Part part, boolean checked) {
                if (checked) {
                    mSelectedForPrinting.add(part);
                } else {
                    mSelectedForPrinting.remove(part);
                }

                printButton.setEnabled(!mSelectedForPrinting.isEmpty());
            }
        });
        mPartsList.setAdapter(adapter);

    }


    private void generateDocument() {
        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.generate_file_loading));

        List<List<Integer>> pages = new ArrayList<>();
        for (Part part : mSelectedForPrinting) {
            pages.add(part.getPages());
        }

        GenerateRequest request = new GenerateRequest(mActiveDocument.getId(), pages);
        ServiceApi serviceApi = ServiceClient.getInstance().getServiceApi();
        serviceApi.generate(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        mProgressDialog.dismiss();

                        writeResponseBodyToDisk(responseBody);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mProgressDialog.dismiss();
                        Toast.makeText(DocumentActivity.this, "Fail: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void handleDocumentDownloaded(String absolutePath) {
        Intent intent = new Intent(DocumentActivity.this, PrintActivity.class);
        intent.putExtra(FILEPATH_KEY, absolutePath);
        startActivity(intent);
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File folder = new File(path.getPath() + "/toPrint");
            if (!folder.exists()) {
                folder.mkdir();
            }

            File file = new File(folder.getPath() + "/" + FILENAME);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                handleDocumentDownloaded(file.getAbsolutePath());
                return true;
            } catch (IOException e) {
                Toast.makeText(DocumentActivity.this,
                        getString(R.string.file_download_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            Toast.makeText(DocumentActivity.this,
                    getString(R.string.file_download_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();

            return false;
        }
    }


}
