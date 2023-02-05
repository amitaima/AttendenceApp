package com.attendence.attendenceapp;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class Factory extends Fragment {

    View myView;
    GridView gvNamesFactory;
    public Employee[] employeesFactory;
    public Employee[] empsMissing, empsLate, empsOnTime,empsPresent;
    EmployeesAdapter employeeAdapter;
    ArrayList<String> listItems;
    ArrayList<String> listItemsWeek;
    ArrayList<String> listItemsMonth;
    listViewAdapter adapter;
    int currId;
    int firstClickW=0, firstClickM=0;
    Dialog myDialog;
    private ImageLoader imageLoader;
    Thread runThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_factory, container, false);
        getActivity().setTitle("ייצור - עכו");
        setHasOptionsMenu(true);
        imageLoader = ImageLoader.getInstance();

        int i=0;
        for (Employee employee : (((MainActivity) getActivity()).employees)) { //
            if (employee == null) {break;}
            if (employee.getDepartment() == 2) { // Checks number of employees in department to create array.
                i++;
            }
        }
        employeesFactory = new Employee[i];
        i=0;
        for (Employee employee : (((MainActivity) getActivity()).employees)) {
            if (employee == null) {break;}
            if (employee.getDepartment() == 2) { // Gets only the needed employees
                employeesFactory[i] = employee;
                i++;
            }
        }

        startTimer();
        gvNamesFactory = (GridView) myView.findViewById(R.id.gridviewNamesFactory);
        EmployeesAdapter employeeAdapter = new EmployeesAdapter(getActivity(), employeesFactory);
        gvNamesFactory.setAdapter(employeeAdapter);
        gvNamesFactory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Snackbar.make(v, "מספר עובד: " +  + employeesFactory[position].getId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                history_dialog(employeesFactory[position].getId(),employeesFactory[position]);
            }
        });
        return myView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int m=0,l=0,o=0,p=0;
        for (Employee employee : employeesFactory) { //
            if (employee == null) {break;}
            if (employee.getStatus() == 0 || employee.getStatus()==3) {
                m++;
            } else if (employee.getStatus() == 1) {
                l++;
            } else if (employee.getStatus() == 2) {
                o++;
            }
        }
        empsMissing = new Employee[m];
        empsPresent = new Employee[l+o];
        empsLate = new Employee[l];
        empsOnTime = new Employee[o];
        l=0;
        m=0;
        o=0;
        p=0;
        for (Employee employee : employeesFactory) {
            if (employee == null) {break;}
            if (employee.getStatus() == 0 || employee.getStatus()==3) {
                empsMissing[m] = employee;
                m++;
            } else if (employee.getStatus() == 1) {
                empsLate[l] = employee;
                empsPresent[p] = employee;
                p++;
                l++;
            } else if (employee.getStatus() == 2) {
                empsOnTime[o] = employee;
                empsPresent[p] = employee;
                p++;
                o++;
            }
        }
        switch (item.getItemId()) {

            case R.id.action_missing:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsMissing);
                gvNamesFactory.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_present:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsPresent);
                gvNamesFactory.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_late:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsLate);
                gvNamesFactory.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_on_time:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsOnTime);
                gvNamesFactory.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_all:
                employeeAdapter = new EmployeesAdapter(getActivity(), employeesFactory);
                gvNamesFactory.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged(); // not updating!!
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Dialog to pick font size
    public void history_dialog(int id,Employee employee) {
        myDialog = new Dialog(getActivity());
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.history_dialog);
        myDialog.setTitle("היסטוריית עובד");
        firstClickW = 0;
        firstClickM = 0;

        listItems = new ArrayList<String>();
        final Button weekButton = (Button) myDialog.findViewById(R.id.week_button);
        final Button monthButton = (Button) myDialog.findViewById(R.id.month_button);
        Button exitButton = (Button) myDialog.findViewById(R.id.exit_button);
        final ListView historyList = (ListView) myDialog.findViewById(R.id.history_listView);
        adapter=new listViewAdapter(getActivity(), android.R.layout.simple_list_item_1, listItems);
        historyList.setAdapter(adapter);
        currId = id;

        ImageView employeeImg = (ImageView) myDialog.findViewById(R.id.employee_image);
        TextView employeeId = (TextView) myDialog.findViewById(R.id.employee_id);
        TextView employeeName = (TextView) myDialog.findViewById(R.id.employee_name);
        TextView employeeDepartment = (TextView) myDialog.findViewById(R.id.employee_department);
        if (!employee.getImageName().contains("-")){
            String imageUri = "https://i.imgur.com/" + employee.getImageName() + ".jpg";
            imageLoader.displayImage(imageUri, employeeImg);
        } else {
            employeeImg.setImageResource(R.drawable.null1);
        }
        employeeId.setText("מספר זהות: " + employee.getId());
        employeeName.setText("שם: " + employee.getName());
        employeeDepartment.setText("מחלקה: ייצור");

        Thread weekThread = new Thread(new weekThread());
        weekThread.start();
        try {
            weekThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        firstClickW = 1;
        listItemsWeek = (ArrayList<String>)listItems.clone();


        myDialog.show();

        weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                monthButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                listItems.clear();
                if (firstClickW == 0) {
                    Thread weekThread = new Thread(new weekThread());
                    weekThread.start();
                    try {
                        weekThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                    firstClickW = 1;
                } else {
                    adapter=new listViewAdapter(getActivity(), android.R.layout.simple_list_item_1, listItemsWeek);
                    historyList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                monthButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                listItems.clear();
                if (firstClickM==0){
                    Thread monthThread = new Thread(new monthThread());
                    monthThread.start();
                    try {
                        monthThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                    listItemsMonth = (ArrayList<String>)listItems.clone();
                    firstClickM = 1;
                } else {
                    adapter=new listViewAdapter(getActivity(), android.R.layout.simple_list_item_1, listItemsMonth);
                    historyList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.cancel();
            }
        });
    }

    public class listViewAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final ArrayList<String> values;
        public listViewAdapter(Context context, int layoutResource, ArrayList<String> values) {
            super(context, layoutResource, values);
            this.context = context;
            this.values = values;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = super.getView(position,convertView,parent);
            String s = this.values.get(position);
            String[] splitedStr, timeInSplit, timeOutSplit;
            int i, dep;
            splitedStr = s.split(" ");
            timeInSplit = splitedStr[3].split(":");
            timeOutSplit = splitedStr[6].split(":");
            for (i = 0; i < ((MainActivity) getActivity()).employees.length; i++)
                if (((MainActivity) getActivity()).employees[i].getId() == currId)
                    break;
            dep = ((MainActivity) getActivity()).employees[i].getDepartment();
            if (dep == 0 || dep == 3) {
                if (Integer.parseInt(timeInSplit[0]) >= 8 && Integer.parseInt(timeInSplit[1]) >= 0) {
                    //change color of item background to red
                    v.setBackgroundResource(R.color.subtleColorRED);
                } else if (Integer.parseInt(timeOutSplit[0]) < 16) {
                    //change color of item background to red
                    v.setBackgroundResource(R.color.subtleColorBlue);
                } else {
                    v.setBackgroundColor(Color.WHITE);
                }
            } else {
                if (Integer.parseInt(timeInSplit[0]) >= 7 && Integer.parseInt(timeInSplit[1]) >= 0) {
                    //change color of item background to red
                    v.setBackgroundResource(R.color.subtleColorRED);
                } else if (Integer.parseInt(timeOutSplit[0]) < 16) {
                    //change color of item background to red
                    v.setBackgroundResource(R.color.subtleColorBlue);
                } else {
                    v.setBackgroundColor(Color.WHITE);
                }
            }

            return v;
        }
    }

    class weekThread implements  Runnable {

        @Override
        public void run() {
            try {
                ((MainActivity) getActivity()).socket =
                        new Socket(((MainActivity) getActivity()).IP, ((MainActivity) getActivity()).PORT);
                String message;
                String recievedMsg = "";
                ((MainActivity) getActivity()).dos = new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
                message = "get week " + Integer.toString(currId);
                ((MainActivity) getActivity()).dos.writeUTF(message);
                byte[] buffer = new byte[20];
                String[] splitedStr;
                int i;
                String date,timeIn,timeOut,addString;
                while (!recievedMsg.toLowerCase().contains("done")){
                    try {
                        // Getting next message:
                        ((MainActivity) getActivity()).is = ((MainActivity) getActivity()).socket.getInputStream();
                        ((MainActivity) getActivity()).dos =
                                new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
                        ((MainActivity) getActivity()).is.read(buffer);
                        recievedMsg = new String(buffer, "UTF-8");
                        if (recievedMsg.toLowerCase().contains("done")) {
                            ((MainActivity) getActivity()).dos.writeUTF("closing s");
                            ((MainActivity) getActivity()).dos.flush();
                            ((MainActivity) getActivity()).socket.close(); // Only close socket at end of program
                            ((MainActivity) getActivity()).dos.close();
                            break;
                        }
                        splitedStr = recievedMsg.split(" ");
                        date = splitedStr[0];
                        timeIn = splitedStr[1];
                        timeOut = splitedStr[2];
                        addString = date + " - in: " + timeIn + " , out: " + timeOut;
                        listItems.add(addString);
                        ((MainActivity) getActivity()).dos.writeUTF("recieved");
                        ((MainActivity) getActivity()).dos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    class monthThread implements  Runnable {

        @Override
        public void run() {
            try {
                ((MainActivity) getActivity()).socket =
                        new Socket(((MainActivity) getActivity()).IP, ((MainActivity) getActivity()).PORT);
                String message;
                String recievedMsg = "";
                ((MainActivity) getActivity()).dos = new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
                message = "get month " + Integer.toString(currId);
                ((MainActivity) getActivity()).dos.writeUTF(message);
                byte[] buffer = new byte[20];
                String[] splitedStr;
                int i;
                String date,timeIn,timeOut,addString;
                while (!recievedMsg.toLowerCase().contains("done")){
                    try {
                        // Getting next message:
                        ((MainActivity) getActivity()).is = ((MainActivity) getActivity()).socket.getInputStream();
                        ((MainActivity) getActivity()).dos =
                                new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
                        ((MainActivity) getActivity()).is.read(buffer);
                        recievedMsg = new String(buffer, "UTF-8");
                        if (recievedMsg.toLowerCase().contains("done")) {
                            ((MainActivity) getActivity()).dos.writeUTF("closing s");
                            ((MainActivity) getActivity()).dos.flush();
                            ((MainActivity) getActivity()).socket.close(); // Only close socket at end of program
                            ((MainActivity) getActivity()).dos.close();
                            break;
                        }
                        splitedStr = recievedMsg.split(" ");
                        date = splitedStr[0];
                        timeIn = splitedStr[1];
                        timeOut = splitedStr[2];
                        addString = date + " - in: " + timeIn + " , out: " + timeOut;
                        listItems.add(addString);
                        ((MainActivity) getActivity()).dos.writeUTF("recieved");
                        ((MainActivity) getActivity()).dos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    @Override
    public void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    private void startTimer(){
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run(){
                        runThread = new Thread(new alwaysRunThread());
                        runThread.start();
                        try {
                            runThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        mTimer1.schedule(mTt1, 1, 60000);
    }

    class alwaysRunThread implements  Runnable {

        @Override
        public void run() {
            try {
                ((MainActivity) getActivity()).socket =
                        new Socket(((MainActivity) getActivity()).IP, ((MainActivity) getActivity()).PORT);
                String message;
                String recievedMsg = "";
                ((MainActivity) getActivity()).dos = new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
//                message = "run attendence thread";
                message = "get attendence";
                ((MainActivity) getActivity()).dos.writeUTF(message);
                byte[] buffer = new byte[10];
                String[] splitedStr;
                int id, status, i;
                while (!recievedMsg.toLowerCase().contains("done")){
                    try {
                        // Getting next message:
                        ((MainActivity) getActivity()).is = ((MainActivity) getActivity()).socket.getInputStream();
                        ((MainActivity) getActivity()).dos =
                                new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
                        ((MainActivity) getActivity()).is.read(buffer);
                        recievedMsg = new String(buffer, "UTF-8");
                        if (recievedMsg.toLowerCase().contains("done")) {
                            ((MainActivity) getActivity()).dos.writeUTF("closing s");
                            ((MainActivity) getActivity()).dos.flush();
                            ((MainActivity) getActivity()).socket.close(); // Only close socket at end of program
                            ((MainActivity) getActivity()).dos.close();
                            break;
                        }
                        splitedStr = recievedMsg.split("\\s+");
                        id = Integer.parseInt(splitedStr[0]);
                        status = Integer.parseInt(splitedStr[1]);
                        for(int j =0; j < ((MainActivity) getActivity()).employees.length; j++) {
                            if (((MainActivity) getActivity()).employees[j].getId() == id) {
                                ((MainActivity) getActivity()).employees[j].setStatus(status);
                                break;
                            }
                        }
                        for (i = 0; i < employeesFactory.length; i++) {
                            if (employeesFactory[i].getId() == id) {
                                employeesFactory[i].setStatus(status);
                                break;
                            }
                        }
                        ((MainActivity) getActivity()).dos.writeUTF("recieved");
                        ((MainActivity) getActivity()).dos.flush();
                        employeeAdapter = new EmployeesAdapter(getActivity(), employeesFactory);
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                employeeAdapter.notifyDataSetChanged();
                                gvNamesFactory.setAdapter(employeeAdapter);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
