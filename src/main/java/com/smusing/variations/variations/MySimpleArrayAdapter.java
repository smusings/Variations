package com.smusing.variations.variations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] array;
    private final String[] nArray;

    public MySimpleArrayAdapter(Context context, String[] array, String[] nArray) {
        super(context, R.layout.row, array);
        this.context = context;
        this.array = array;
        this.nArray = nArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rView = inflater.inflate(R.layout.row, parent, false);
        TextView textView = (TextView) rView.findViewById(R.id.secondLine);
        TextView tView = (TextView) rView.findViewById(R.id.thirdLine);
        textView.setText(nArray[position]);
        tView.setText(array[position]);
        return rView;
    }
}