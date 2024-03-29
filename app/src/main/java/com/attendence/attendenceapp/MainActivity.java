package com.attendence.attendenceapp;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /*public Employee[] employees = {
            new Employee("Amitai Malka", R.drawable.pic_1, 0, 1 , false),
            new Employee("Amitai Malka 2", R.drawable.pic_2, 1, 2 , false),
            new Employee("Amitai Malka 3", R.drawable.pic_3, 1, 3 , false),
            new Employee("Erel Friedman", R.drawable.pic_4, 1, 4 , false),
            new Employee("Yoav Malka", R.drawable.pic_5, 2, 5 , false),
            new Employee("Ofir Malka", R.drawable.pic_6, 0, 6 , false),
            new Employee("Eitan Kadosh", R.drawable.pic_7, 0, 7 , false),
            new Employee("Shoham Kadosh", R.drawable.pic_8, 2, 8 , false),
            new Employee("אופיר מלכה", R.drawable.pic_9, 2, 9 , false),
            new Employee("רועי ולד", R.drawable.pic_10, 1, 10 , false)
    };*/
    public Employee[] employees;
    public Boolean[] favEmployeesBefore;
    public int favoritesEdited = 0;
    public int currentFragment, connectionSuccess=0;
    private static final String favoritedEmployeeNamesKey = "favoritedEmployeeNamesKey";
//    public final String IP = "192.168.43.43"; // Internet Phone
//    public final String IP = "10.100.102.116"; // Internet Home
//    public final String IP = "46.116.86.71"; // Home Router
//    public final String IP = "62.219.229.28"; // Gvanim Router
    public final String IP = "192.168.5.200";
    public final int PORT = 443;
    public Socket socket;
    public DataOutputStream dos;
    private Thread changeT;
    public Thread mainT, statusThread;
    public boolean threadRun = true;
    InputStream is;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainT = new Thread(new MainThread());
        mainT.start();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addFavorites);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add To Favorites Button", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        android.app.FragmentManager fragmentManager = getFragmentManager();
        try {
            mainT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(connectionSuccess==0){
            Snackbar.make(findViewById(R.id.constraintLayout), "The app could'nt connect to the server\nPlease try again later",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 4000);
        }
        else {
            for (int i = 0; i < employees.length; i++) {
                for (int j = i + 1; j < employees.length; j++) {
                    if (employees[i].getName().compareTo(employees[j].getName()) > 0) {
                        Employee temp = employees[i];
                        employees[i] = employees[j];
                        employees[j] = temp;
                        Boolean tempFav = favEmployeesBefore[i];
                        favEmployeesBefore[i] = favEmployeesBefore[j];
                        favEmployeesBefore[j] = tempFav;
                    }
                }
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new Favorites())
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // construct a list of employees you've favorited
        final ArrayList<String> favoritedEmployeeNames = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.getIsFavorite()) {
                favoritedEmployeeNames.add(employee.getName());
            }
        }
//        changeT.start();
//        try {
//            changeT.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // save that list to outState for later
        outState.putStringArrayList(favoritedEmployeeNamesKey, favoritedEmployeeNames);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get our previously saved list of favorited employees
        final ArrayList<String> favoritedEmployeeNames =
                savedInstanceState.getStringArrayList(favoritedEmployeeNamesKey);

        // look at all of your employees and figure out which are the favorites
        for (String employeeName : favoritedEmployeeNames) {
            for (Employee employee : employees) {
                if (employee.getName().equals(employeeName)) {
                    employee.setIsFavorite(true);
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Settings has been clicked", Toast.LENGTH_SHORT).show();
            return true; // switch here with navigation to settings panel!
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fragmentManager = getFragmentManager();


//        if (statusThread!=null) {
//            statusThread.interrupt();
//        }
        threadRun = false;

//        new Thread(new updateFavoritesThread()).start();
        changeT = new Thread(new changeThreadTest());
        changeT.start();
        try {
            changeT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadRun = true;
        if (id == R.id.nav_favorites) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new Favorites())
                    .commit();
        } else if (id == R.id.nav_office) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new Office())
                    .commit();
        } else if (id == R.id.nav_plastic) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new plastic())
                    .commit();
        } else if (id == R.id.nav_factory) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new Factory())
                    .commit();
        } /*else if (id == R.id.nav_factory_afula) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new FactoryAfula())
                    .commit();
        } */else if (id == R.id.nav_all) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFrame,
                            new All())
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    class MainThread implements Runnable {

        @Override
        public void run() {
            try {
                int i;
//                socket = new Socket(IP, PORT);
                socket = new Socket();
                socket.connect(new InetSocketAddress(IP,PORT),5000);
                socket.setSoTimeout(5000);
                if (socket == null) {
                    connectionSuccess = 0;
                    return;
                }
                connectionSuccess=1;
                dos = new DataOutputStream(socket.getOutputStream());
                String message = "get employees";
                dos.writeUTF(message);
                dos.flush();
                byte[] bufferLen = new byte[10];
                String recievedMsg = "", recievedMsgLong="";
                is = socket.getInputStream();
                is.read(bufferLen);
                recievedMsg = new String(bufferLen, "UTF-8");
                if (!recievedMsg.contains("recieved")){
                    System.out.print("error: could'nt get 'recieved' from server after sending 'get employees'.");
                }
                if(socket.getInputStream()!=null) {
                    recievedMsg = "";
                    is = socket.getInputStream();
                    byte[] buffer;
                    int read; // array of binary of the given messgage!
                    String[] splitedStr,splitedList = new String[0];
                    Boolean isFav = false;
                    i=0;
                    dos = new DataOutputStream(socket.getOutputStream());
                    while (true) {
                        bufferLen = new byte[1];
                        is.read(bufferLen);
                        recievedMsg = new String(bufferLen, "UTF-8"); //length of length
                        if (recievedMsg.equals("0")) {
                            dos.writeUTF("recieved");
                            dos.flush();
                            break;
                        }
                        bufferLen = new byte[Integer.parseInt(recievedMsg)]; //length of chars to receive
                        dos.writeUTF("recieved");
                        dos.flush();
                        is.read(bufferLen);
                        recievedMsg = new String(bufferLen, "UTF-8");
                        buffer = new byte[Integer.parseInt(recievedMsg)]; // chars to receive
                        dos.writeUTF("recieved");
                        dos.flush();
                        is.read(buffer);
                        recievedMsg = new String(buffer, "UTF-8");
                        recievedMsgLong+=recievedMsg;
//                        while (is.available() > 0) {
//                            is.read(buffer);
//                            recievedMsg = new String(buffer, "UTF-8");
////                            while (!recievedMsgLong.contains(recievedMsg.split(",")[i])) {
////                                recievedMsgLong+=recievedMsg.split(",")[i] + ",";
////                                i++;
////                            }
//                            for (i=0;i<recievedMsg.split(",").length;i++) {
//                                if(!recievedMsgLong.contains(recievedMsg.split(",")[i])) {
//                                    recievedMsgLong += recievedMsg.split(",")[i] + ",";
//                                } else{break;}
//                            }
//                        }
                        if (recievedMsgLong.contains("/")){ //Checks if "/" in reveivedmsg then stop receiving
                            recievedMsgLong = recievedMsgLong.split("/")[0];
                            while (is.available() > 0) {
                                is.read(buffer);
                            }
                        } else {
                            while (is.available() > 0) {
                                is.read(buffer);
                                recievedMsg = new String(buffer, "UTF-8");
                                for (i = 0; i < recievedMsg.split(",").length; i++) {
                                    if (recievedMsgLong.contains("/")) {
                                        recievedMsgLong = recievedMsgLong.split("/")[0];
                                        break;
                                    }
                                }
                            }
                        }
                        Log.i("MyPrintingTab", Integer.toString(recievedMsg.length()));
                        is.skip(is.available());
                        dos.writeUTF("recieved");
                        dos.flush();
                    }
                    splitedList = recievedMsgLong.split(",");
                    for(i=0;i<splitedList.length;i++){
                        splitedStr = splitedList[i].split("\\s+");
                        if (splitedStr.length!=7) {
                            List<String> list = new ArrayList<String>(Arrays.asList(splitedList));
                            list.remove(splitedList[i]);
                            splitedList = list.toArray(new String[0]);
                        }
                    }
                    employees = new Employee[splitedList.length];
                    favEmployeesBefore = new Boolean[splitedList.length];
                    for(i=0;i<splitedList.length;i++){
                        splitedStr = splitedList[i].split("\\s+");
                        String imgName = "e"+splitedStr[0];
                        int imgId = getResources().getIdentifier(imgName, "drawable", getPackageName());
//                        Log.i("imgId"," " + imgId);
                        Log.i("MyPrintingTab",splitedList[i]);
                        if (Integer.parseInt(splitedStr[3]) != 0) {
                            isFav = true;
                        } else {
                            isFav = false;
                        }
                        if (splitedStr.length==7) {
                            employees[i] = new Employee(
                                    splitedStr[1] + " " + splitedStr[2],
                                    splitedStr[6],
                                    Integer.parseInt(splitedStr[4]),
                                    Integer.parseInt(splitedStr[0]),
                                    isFav,
                                    Integer.parseInt(splitedStr[5]),
                                    imgId);
                            favEmployeesBefore[i] = isFav;
                        }
                    }
                    dos.writeUTF("recieved");
                    dos.flush();
                    is.close();
                    dos.writeUTF("closing s");
                    dos.flush();
                    socket.close(); // Only close socket at end of program
                    dos.close();
                } else if (socket.getInputStream() == null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    message = in.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    class MainThread implements Runnable {
//
//        @Override
//        public void run() {
//            try {
//                int i;
////                socket = new Socket(IP, PORT);
//                socket = new Socket();
//                socket.connect(new InetSocketAddress(IP,PORT),5000);
//                socket.setSoTimeout(5000);
//                if (socket == null) {
//                    connectionSuccess = 0;
//                    return;
//                }
//                connectionSuccess=1;
//                dos = new DataOutputStream(socket.getOutputStream());
//                String message = "get num employees";
//                dos.writeUTF(message);
//                dos.flush();
//                byte[] bufferLen = new byte[5];
//                String recievedMsg = "";
//                is = socket.getInputStream();
//                is.read(bufferLen);
//                recievedMsg = new String(bufferLen, "UTF-8");
//                employees = new Employee[Integer.parseInt(recievedMsg)];
//                favEmployeesBefore = new Boolean[Integer.parseInt(recievedMsg)];
//                dos = new DataOutputStream(socket.getOutputStream());
//                message = "get employees";
//                dos.writeUTF(message);
//                dos.flush();
//                is.read(bufferLen);
//                recievedMsg = new String(bufferLen, "UTF-8");
//                if (!recievedMsg.contains("recieved")){
//                    System.out.print("error: could'nt get 'recieved' from server after sending 'get employees'.");
//                }
//                if(socket.getInputStream()!=null)
//                {
//                    recievedMsg = "";
//                    is = socket.getInputStream();
//                    bufferLen = new byte[2];
//                    byte[] buffer;
//                    int read; // array of binary of the given messgage!
//                    String[] splitedStr;
//                    Boolean isFav = false;
//                    i=0;
//                    while(!recievedMsg.toLowerCase().contains("done")){
//                        // Getting len of next message:
//                        try {
//                            is = socket.getInputStream();
//                            dos = new DataOutputStream(socket.getOutputStream());
//                            is.read(bufferLen);
//                            recievedMsg = new String(bufferLen, "UTF-8");
//                            buffer = new byte[Integer.parseInt(recievedMsg)];
//                            dos.writeUTF("recieved");
//                            dos.flush();
//                        } catch (Exception e){
//                            e.printStackTrace();
//                            buffer = new byte[40];
//                        }
//                        try {
//                            // Getting next message:
//                            is = socket.getInputStream();
//                            dos = new DataOutputStream(socket.getOutputStream());
//                            //read = is.read(buffer);
//                            is.read(buffer);
//                            recievedMsg = new String(buffer, "UTF-8");
//                            if (recievedMsg.toLowerCase().contains("done")) {
//                                dos.writeUTF("closing s");
//                                dos.flush();
//                                socket.close(); // Only close socket at end of program
//                                dos.close();
//                                break;
//                            }
//                            /*for (i = 0; i<read; i++)
//                            {
//                                recievedMsg += Character.toString ((char) buffer[i]);
//                            }*/
//                            splitedStr = recievedMsg.split("\\s+");
//                            String imgName = "e"+splitedStr[0];
//                            int imgId = getResources().getIdentifier(imgName, "drawable", getPackageName());
//                            if (Integer.parseInt(splitedStr[3]) != 0) {
//                                isFav = true;
//                            } else {
//                                isFav = false;
//                            }
//                            employees[i] = new Employee(
//                                    splitedStr[1] + " " + splitedStr[2],
//                                    splitedStr[0],
//                                    Integer.parseInt(splitedStr[4]),
//                                    Integer.parseInt(splitedStr[0]),
//                                    isFav,
//                                    Integer.parseInt(splitedStr[5]),
//                                    imgId);
//                            dos.writeUTF("recieved");
//                            dos.flush();
//                            favEmployeesBefore[i] = isFav;
//                            i++;
//                        } catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                    is.close();
//                } else if (socket.getInputStream() == null) {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    message = in.toString();
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

    class changeThreadTest implements Runnable {

        @Override
        public void run() {
            try {
                int i;
                socket = new Socket(IP, PORT);
                String recievedMsg = "";
                is = socket.getInputStream();
                byte[] buffer = new byte[9];
                i=0;
                try {
                    is = socket.getInputStream();
                    dos = new DataOutputStream(socket.getOutputStream());
                    for (Employee employee : employees) {
                        if (employee == null) {break;}
                        if (favEmployeesBefore[i] != employee.getIsFavorite()) {
                            if (employee.getIsFavorite()) { // Gets only the needed employees
                                dos.writeUTF("change favorites " + employee.getId() + " 1");
                            } else {
                                dos.writeUTF("change favorites " + employee.getId() + " 0");
                            }
                            is.read(buffer);
                            recievedMsg = new String(buffer, "UTF-8");
                            if (!recievedMsg.contains("recieved")) {
                                System.out.print("error: could'nt get 'recieved' from server after sending 'change favorites'.");
                            }
                            favEmployeesBefore[i] = employee.getIsFavorite();
                        }
                        i++;
                    }
                    dos.writeUTF("closing s");
                    dos.flush();
                    is.read(buffer);
                    recievedMsg = new String(buffer, "UTF-8");
                } catch (Exception e){
                    e.printStackTrace();
                }
                dos.close();
                is.close();
                socket.close(); // Only close socket at end of program
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
