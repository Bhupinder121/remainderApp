package com.example.remainderapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class customAdapter extends ArrayAdapter<JSONObject> {
    ArrayList<JSONObject> tasks;

    public customAdapter(@NonNull Context context, int resource, @NonNull List<JSONObject> objects) {
        super(context, resource, objects);
        tasks = (ArrayList<JSONObject>) objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.remaider_task, parent, false);
        TextView task = convertView.findViewById(R.id.task);
        Button doneButton = convertView.findViewById(R.id.done);
        TextView sno = convertView.findViewById(R.id.Sno);
        TextView ND = convertView.findViewById(R.id.ND);
        TextView date = convertView.findViewById(R.id.Date);
        sno.setText(String.valueOf(position+1));

        try {
            if(tasks.get(position).getInt("isNotDone") == 1){
                ND.setVisibility(View.VISIBLE);
            }
            task.setText(tasks.get(position).getString("TaskName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}
