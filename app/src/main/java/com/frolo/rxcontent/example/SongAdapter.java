package com.frolo.rxcontent.example;


import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class SongAdapter extends BaseAdapter {

    private List<Song> mItems;

    public void setItems(List<Song> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public Song getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(20, 20, 20, 20);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            convertView = textView;
        }

        TextView textView = (TextView) convertView;
        textView.setText(getItem(position).title);

        return textView;
    }
}
