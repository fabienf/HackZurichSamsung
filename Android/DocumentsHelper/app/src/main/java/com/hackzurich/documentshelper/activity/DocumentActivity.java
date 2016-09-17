package com.hackzurich.documentshelper.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.adapter.DocumentListAdapter;
import com.hackzurich.documentshelper.model.Document;
import com.hackzurich.documentshelper.model.Part;

import java.util.HashSet;
import java.util.Set;

public class DocumentActivity extends AppCompatActivity {

    private RecyclerView mPartsList;
    private Document mActiveDocument;
    private Set<Part> mSelectedForPrinting = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        String document = getIntent().getStringExtra(MainActivity.DOCUMENT_KEY);
        Gson gson = new Gson();
        mActiveDocument = gson.fromJson(document, Document.class);

        final Button printButton = (Button) findViewById(R.id.documents_activity_print_btn);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Pass pages for printing

                Intent intent = new Intent(DocumentActivity.this, PrintActivity.class);
                startActivity(intent);
            }
        });

        mPartsList = (RecyclerView)findViewById(R.id.documents_activity_parts_list);
        mPartsList.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mPartsList.setLayoutManager(layoutManager);

        DocumentListAdapter adapter = new DocumentListAdapter(this, mActiveDocument.getParts(), new DocumentListAdapter.PartListener() {
            @Override
            public void onPartChecked(Part part, boolean checked) {
                if(checked) {
                    mSelectedForPrinting.add(part);
                } else {
                    mSelectedForPrinting.remove(part);
                }

                printButton.setEnabled(!mSelectedForPrinting.isEmpty());
            }
        });
        mPartsList.setAdapter(adapter);

    }


}
