package com.example.remainderapp;

import android.Manifest;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class customAdapter extends ArrayAdapter<JSONObject> {
    ArrayList<JSONObject> tasks;
    int todayTasks;
    String[]monthName={"Jan","Feb","Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public customAdapter(@NonNull Context context, int resource, @NonNull List<JSONObject> objects, int todayTasks) {
        super(context, resource, objects);
        tasks = (ArrayList<JSONObject>) objects;
        this.todayTasks = todayTasks;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.remaider_task, parent, false);

        String task_date = "today";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss a");

        TextView task = convertView.findViewById(R.id.task);
        Button doneButton = convertView.findViewById(R.id.done);
        TextView sno = convertView.findViewById(R.id.Sno);
        TextView NDT = convertView.findViewById(R.id.NDT);
        TextView ND = convertView.findViewById(R.id.ND);
        TextView date = convertView.findViewById(R.id.Date);

        sno.setText(String.valueOf(position+1));

        try {
            if(position - todayTasks == 0){
                ND.setVisibility(View.VISIBLE);
                NDT.setVisibility(View.VISIBLE);
            }

            Date taskDate = format.parse(tasks.get(position).getString("taskAddDate"));
            Date currentTaskDate = format.parse(tasks.get(0).getString("taskAddDate"));

            if (taskDate.getDate() == currentTaskDate.getDate() + 1 && taskDate.getMonth() == currentTaskDate.getMonth()) {
                task_date = "tomorrow";
            } else if (taskDate.getDate() != currentTaskDate.getDate() || taskDate.getMonth() != currentTaskDate.getMonth()) {
                task_date = taskDate.getDate() + " " + monthName[taskDate.getMonth()];
            }

            date.setText(task_date);
            task.setText(tasks.get(position).getString("taskName"));
            if(tasks.get(position).getInt("isDone") == 1){
                task.setPaintFlags(task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                doneButton.setEnabled(false);
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("isDone", tasks.get(position).getInt("taskID"));
                    obj.put("isNotDone", tasks.get(position).getInt("isNotDone"));
                    MainActivity.connection.sendData(obj);
                    tasks.get(position).put("isDone", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                task.setPaintFlags(task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                doneButton.setEnabled(false);
            }
        });

        try {
            if(tasks.get(position).getInt("isNotDone") == 0) {
                task.setOnLongClickListener(v -> {

                    JSONObject obj = new JSONObject();
                    try {
                        int permanent = tasks.get(position).getInt("isPermanent");
                        if(permanent == 0){
                            permanent = 1;
                        }
                        else {
                            permanent = 0;
                        }
                        obj.put("taskName", task.getText());
                        obj.put("isPermanent", permanent);
                        tasks.get(position).put("isPermanent", permanent);
                        MainActivity.connection.sendData(obj);
                        String mess = "Now Permanent";
                        if(permanent == 0){
                            mess = "Not Permanent";
                        }
                        Utils.showMes(getContext(), mess);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return false;
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public void updateData(ArrayList<JSONObject> objects, int Tasks){
        tasks = objects;
        this.todayTasks = Tasks;
        this.notifyDataSetChanged();
    }

}
