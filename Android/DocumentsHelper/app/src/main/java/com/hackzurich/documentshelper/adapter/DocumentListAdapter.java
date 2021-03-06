package com.hackzurich.documentshelper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hackzurich.documentshelper.R;
import com.hackzurich.documentshelper.model.Part;

import java.util.List;

/**
 *
 */
public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Part> mParts;
    private final PartListener mListener;

    public DocumentListAdapter(@NonNull Context context, @NonNull List<Part> parts, @NonNull PartListener listener) {
        mParts = parts;
        mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_part, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Part current = mParts.get(position);

        holder.mNameCheckbox.setText(current.getName());

        if(TextUtils.isEmpty(current.getDescription())) {
            holder.mDescription.setVisibility(View.GONE);
        } else {
            holder.mDescription.setVisibility(View.VISIBLE);
            holder.mDescription.setText(current.getDescription());
        }

        int start = current.getPages().get(0);
        int end = current.getPages().get(1);

        String pagesRange = mContext.getString(R.string.item_parts_pages, String.valueOf(start), String.valueOf(end));
        holder.mPages.setText(pagesRange);

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < current.getKeys().size(); i++) {
            String key = current.getKeys().get(i);
            builder.append(key);

            if(i < (current.getKeys().size() - 1)) {
                builder.append(", ");
            }
        }
        holder.mKeywords.setText(builder.toString());

        //in some cases, it will prevent unwanted situations
        holder.mNameCheckbox.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        holder.mNameCheckbox.setChecked(current.isSelected());

        holder.mNameCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                current.setSelected(b);
                mListener.onPartChecked(current, b);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mParts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox mNameCheckbox;
        private TextView mDescription;
        private TextView mPages;
        private TextView mKeywords;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameCheckbox = (CheckBox) itemView.findViewById(R.id.item_part_name_checkmark);
            mDescription = (TextView) itemView.findViewById(R.id.item_part_description);
            mPages = (TextView) itemView.findViewById(R.id.item_part_pages);
            mKeywords = (TextView) itemView.findViewById(R.id.item_part_keywords);
        }
    }

    public interface PartListener {
        void onPartChecked(Part part, boolean checked);
    }
}
