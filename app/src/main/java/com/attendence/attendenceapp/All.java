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
import android.widget.ListView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class All extends Fragment {

    View myView;
    GridView gvNamesAll;
    EmployeesAdapter employeeAdapter;
    public Employee[] empsMissing, empsLate, empsOnTime;
    ArrayList<String> listItems;
    ArrayList<String> listItemsWeek;
    ArrayList<String> listItemsMonth;
    listViewAdapter adapter;
    ListView historyList;
    int currId;
    int firstClickW=0, firstClickM=0;
    Dialog myDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_all, container, false);
        getActivity().setTitle("הכל");
        setHasOptionsMenu(true);

        startTimer();
        gvNamesAll = (GridView) myView.findViewById(R.id.gridviewNamesAll);
        EmployeesAdapter employeeAdapter = new EmployeesAdapter(getActivity(), (((MainActivity) getActivity()).employees));
        gvNamesAll.setAdapter(employeeAdapter);
        gvNamesAll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Snackbar.make(v, "pressed image id: " +  + (((MainActivity) getActivity()).employees)[position].getId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                history_dialog((((MainActivity) getActivity()).employees)[position].getId());
            }
        });

        return myView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int m=0,l=0,o=0;
        for (Employee employee : (((MainActivity) getActivity()).employees)) { //
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
        empsLate = new Employee[l];
        empsOnTime = new Employee[o];
        l=0;
        m=0;
        o=0;
        for (Employee employee : (((MainActivity) getActivity()).employees)) {
            if (employee == null) {break;}
            if (employee.getStatus() == 0 || employee.getStatus()==3) {
                empsMissing[m] = employee;
                m++;
            } else if (employee.getStatus() == 1) {
                empsLate[l] = employee;
                l++;
            } else if (employee.getStatus() == 2) {
                empsOnTime[o] = employee;
                o++;
            }
        }
        switch (item.getItemId()) {

            case R.id.action_missing:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsMissing);
                gvNamesAll.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_late:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsLate);
                gvNamesAll.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_on_time:
                employeeAdapter = new EmployeesAdapter(getActivity(), empsOnTime);
                gvNamesAll.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_all:
                employeeAdapter = new EmployeesAdapter(getActivity(), (((MainActivity) getActivity()).employees));
                gvNamesAll.setAdapter(employeeAdapter);
                employeeAdapter.notifyDataSetChanged(); // not updating!!
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Dialog to pick font size
    public void history_dialog(int id) {
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
        historyList = (ListView) myDialog.findViewById(R.id.history_listView);
        adapter=new listViewAdapter(getActivity(), android.R.layout.simple_list_item_1, listItems);
        historyList.setAdapter(adapter);
        currId = id;

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
                String[] splitedStr,timeInSplit,timeOutSplit;
                int i, dep;
                String date,timeIn,timeOut,addString;
                for(i=0;i<((MainActivity) getActivity()).employees.length;i++)
                    if(((MainActivity) getActivity()).employees[i].getId() == currId)
                        break;
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
                        timeInSplit = timeIn.split(":");
                        timeOut = splitedStr[2];
                        timeOutSplit = timeOut.split(":");
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
                String[] splitedStr,timeInSplit,timeOutSplit;
                int i, dep;
                String date,timeIn,timeOut,addString;
                for(i=0;i<((MainActivity) getActivity()).employees.length;i++)
                    if(((MainActivity) getActivity()).employees[i].getId() == currId)
                        break;
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
                        timeInSplit = timeIn.split(":");
                        timeOut = splitedStr[2];
                        timeOutSplit = timeOut.split(":");
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
                        new Thread(new alwaysRunThread()).start();
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
                byte[] buffer = new byte[12];
                String[] splitedStr;
                int id, onTime, inOrOut, i;
                while (!recievedMsg.toLowerCase().contains("done")){
                    try {
                        // Getting next message:
                        ((MainActivity) getActivity()).is = ((MainActivity) getActivity()).socket.getInputStream();
                        ((MainActivity) getActivity()).dos = new DataOutputStream(((MainActivity) getActivity()).socket.getOutputStream());
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
                        onTime = Integer.parseInt(splitedStr[1]);
                        inOrOut = Integer.parseInt(splitedStr[2]);
                        for (i = 0; i < ((MainActivity) getActivity()).employees.length; i++) {
                            if (((MainActivity) getActivity()).employees[i].getId() == id) {
                                if (inOrOut == 1) {
                                    ((MainActivity) getActivity()).employees[i].setStatus(onTime + 1);
                                } else {
                                    ((MainActivity) getActivity()).employees[i].setStatus(0);
                                }
                                break;
                            }
                        }
                        ((MainActivity) getActivity()).dos.writeUTF("recieved");
                        ((MainActivity) getActivity()).dos.flush();
                        employeeAdapter = new EmployeesAdapter(getActivity(), ((MainActivity) getActivity()).employees);
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                employeeAdapter.notifyDataSetChanged();
                                gvNamesAll.setAdapter(employeeAdapter);
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