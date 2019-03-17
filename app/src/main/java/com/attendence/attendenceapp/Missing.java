package com.attendence.attendenceapp;


import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


public class Missing extends Fragment {

    View myView;
    GridView gvNamesMissing;
    public Employee[] employeesMissing;
    EmployeesAdapter employeeAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_missing, container, false);
        getActivity().setTitle("חסרים");

        int i=0;
        for (Employee employee : (((MainActivity) getActivity()).employees)) { //
            if (employee == null) {break;}
            if (employee.getStatus() == 0 || employee.getStatus() == 3) { // Checks number of employees in department to create array.
                i++;
            }
        }
        employeesMissing = new Employee[i];//(((MainActivity) getActivity()).employees).length
        i=0;
        for (Employee employee : (((MainActivity) getActivity()).employees)) { //
            if (employee == null) {break;}
            if (employee.getStatus() == 0 || employee.getStatus() == 3) { // Gets only the needed employees
                employeesMissing[i] = employee;
                i++;
            }
        }
        //new Thread(new statusThread()).start();
        Thread attendenceThread = new Thread(new alwaysRunThread());
        attendenceThread.start();
        try {
            attendenceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gvNamesMissing = (GridView) myView.findViewById(R.id.gridviewNamesMissing);
        EmployeesAdapter employeeAdapter = new EmployeesAdapter(getActivity(), employeesMissing);
        gvNamesMissing.setAdapter(employeeAdapter);
        gvNamesMissing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Snackbar.make(v, "pressed image id: " +  + employeesMissing[position].getId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        return myView;
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
                        onTime = Integer.parseInt(splitedStr[1]);
                        inOrOut = Integer.parseInt(splitedStr[2]);
                        for (i = 0; i < employeesMissing.length; i++) {
                            if (employeesMissing[i].getId() == id) {
                                if (inOrOut == 1) {
                                    employeesMissing[i].setStatus(onTime + 1);
                                } else {
                                    if (onTime == 1)
                                        employeesMissing[i].setStatus(0);
                                    else
                                        employeesMissing[i].setStatus(3);
                                }
                                break;
                            }
                        }
                        for (i = 0; i < ((MainActivity) getActivity()).employees.length; i++) {
                            if (((MainActivity) getActivity()).employees[i].getId() == id) {
                                if (inOrOut == 1) {
                                    ((MainActivity) getActivity()).employees[i].setStatus(onTime + 1);
                                } else {
                                    if (onTime == 1)
                                        ((MainActivity) getActivity()).employees[i].setStatus(0);
                                    else
                                        ((MainActivity) getActivity()).employees[i].setStatus(3);
                                }
                                break;
                            }
                        }
                        employeeAdapter = new EmployeesAdapter(getActivity(), employeesMissing);
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                employeeAdapter.notifyDataSetChanged();
                                gvNamesMissing.setAdapter(employeeAdapter);
                            }
                        });
                        ((MainActivity) getActivity()).dos.writeUTF("recieved");
                        ((MainActivity) getActivity()).dos.flush();
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
