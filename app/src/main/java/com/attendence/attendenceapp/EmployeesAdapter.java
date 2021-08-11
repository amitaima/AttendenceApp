package com.attendence.attendenceapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class EmployeesAdapter extends BaseAdapter {

    private final Context mContext;
    private final Employee[] employees;
    private ImageLoader imageLoader;

    // 1
    public EmployeesAdapter(Context context, Employee[] employees) {
        this.mContext = context;
        this.employees = employees;

        imageLoader = ImageLoader.getInstance();
    }

    // 2
    @Override
    public int getCount() {
        return (employees.length);
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
        if (employees[position]==null){
            Log.i("MyNameTest","ERROROROROROR POSITION: " + position);
            return (convertView);
        }
        // 1
        final Employee employee = employees[position];
        // 2
        if (convertView == null || 1==1) { // Delete 1==1 to return to normal
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.linearlayout_employee, null);
        }

        // 3
        final ImageView imageViewEmployee = (ImageView)convertView.findViewById(R.id.imageview_photo);
        final TextView nameTextView = (TextView)convertView.findViewById(R.id.textview_name);
        final ImageView imageViewFavorite = (ImageView)convertView.findViewById(R.id.imageview_favorite);
        final ImageView imageViewStatus = (ImageView)convertView.findViewById(R.id.imageview_status);
//        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.loading),64,64);
//        imageViewEmployee.setImageBitmap(thumbImage);
        imageViewEmployee.setImageResource(R.drawable.loading2);

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
//        Log.i("MyNameTest","position:" + position);
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

//        int id = (int) employee.getImgId();
        if (!employee.getImageName().contains("-")){
//            Log.i("MyPrintingTab","imgpath: " + employee.getImageName());
            String imageUri = "https://i.imgur.com/" + employee.getImageName() + ".jpg";
            imageLoader.displayImage(imageUri, imageViewEmployee);
//            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(mContext.getResources(),id),64,64);
//            imageViewEmployee.setImageBitmap(thumbImage);
//            imageViewEmployee.setImageResource(id);
        } else{
//            imageLoader.displayImage(imageUri, imageViewEmployee);
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.null1),64,64);
            imageViewEmployee.setImageBitmap(thumbImage);
//            imageViewEmployee.setImageResource(R.drawable.null1);
        }
//        String imageUri = "https://ibb.co/gVB0ts9";
//        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
//        // Load image, decode it to Bitmap and display Bitmap in ImageView (or any other view
//        //	which implements ImageAware interface)
//        imageLoader.displayImage(imageUri, imageView);
//        // Load image, decode it to Bitmap and return Bitmap to callback
//        imageLoader.loadImage(imageUri, new SimpleImageLoadingListener() {
//            @Override
//            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                // Do whatever you want with Bitmap
//            }
//        });
//        // Load image, decode it to Bitmap and return Bitmap synchronously
//        Bitmap bmp = imageLoader.loadImageSync(imageUri);


        return convertView;
    }

}

