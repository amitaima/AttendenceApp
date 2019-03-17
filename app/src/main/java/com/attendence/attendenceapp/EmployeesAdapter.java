package com.attendence.attendenceapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class EmployeesAdapter extends BaseAdapter {

    private final Context mContext;
    private final Employee[] employees;

    // 1
    public EmployeesAdapter(Context context, Employee[] employees) {
        this.mContext = context;
        this.employees = employees;
    }

    // 2
    @Override
    public int getCount() {
        return employees.length;
    }

    // 3
    @Override
    public long getItemId(int position) {
        return 0;
    }

    // 4
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1
        final Employee employee = employees[position];

        // 2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.linearlayout_employee, null);
        }

        // 3
        final ImageView imageViewEmployee = (ImageView)convertView.findViewById(R.id.imageview_photo);
        final TextView nameTextView = (TextView)convertView.findViewById(R.id.textview_name);
        final ImageView imageViewFavorite = (ImageView)convertView.findViewById(R.id.imageview_favorite);
        final ImageView imageViewStatus = (ImageView)convertView.findViewById(R.id.imageview_status);

        imageViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                employee.toggleFavorite();
                imageViewFavorite.setImageResource(
                        employee.getIsFavorite() ? R.drawable.star_enabled : R.drawable.star_disabled);
            }
        });

        // 4
//        imageViewEmployee.setImageResource(employee.getImageResource());
        //nameTextView.setText(mContext.getString(employee.getName()));
        nameTextView.setText(employee.getName());
        imageViewFavorite.setImageResource(
                employee.getIsFavorite() ? R.drawable.star_enabled : R.drawable.star_disabled);
        if(employee.getStatus()==1) {
            imageViewStatus.setImageResource(R.drawable.orange_dot);
        } else if(employee.getStatus()==2) {
            imageViewStatus.setImageResource(R.drawable.green_dot);
        } else if(employee.getStatus()==3) {
            imageViewStatus.setImageResource(R.drawable.blue_dot);
        } else {
            imageViewStatus.setImageResource(R.drawable.red_dot);
        }

        Context context = imageViewEmployee.getContext();
        String imgName = "e"+employee.getImageName();
        int id = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
        if (id!=0){
            imageViewEmployee.setImageResource(id);
        } else{
            imageViewEmployee.setImageResource(R.drawable.null1);
        }


        return convertView;
    }

}

