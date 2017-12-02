package com.example.hp.smss;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class MainActivity extends AppCompatActivity {
    String msgData=null;
    ArrayList<String> numbers;
    ArrayList<String> messages;
    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    public static MainActivity instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsListView = (ListView) findViewById(R.id.SMSList);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);


        // Add SMS Read Permision At Runtime
        // Todo : If Permission Is Not GRANTED
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {

            // Todo : If Permission Granted Then Show SMS
            refreshSmsInbox();

        } else {
            // Todo : Then Set Permission
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }




    }
    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");

        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) {
            msgData=new String();
            numbers=new ArrayList<String>();
            messages=new ArrayList<String>();

            arrayAdapter.clear();
            do {
                String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                        "\n" + smsInboxCursor.getString(indexBody) + "\n";
                arrayAdapter.add(str);
            } while (smsInboxCursor.moveToNext());
            if (msgData != null) {
                taskSorting obj = new taskSorting();
                obj.setData(numbers, messages);
                obj.execute();

            }
        }
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    class taskSorting extends AsyncTask<Void,Void,Void>
    {
        ArrayList<String> numbers;
        ArrayList<String> messages;

        ArrayList<String> sortedNumbers;//to contain just numbers that are appeared with you while chatting

        ArrayList<ArrayList<String>> sortedMessages;// contain messages each index contain array, and each array contains messages recieved corresponding to number stored at same index on numbers array
        public void setData(ArrayList<String> numbers,ArrayList<String> messages)
        {
            this.numbers=numbers;
            this.messages=messages;
        }
        @Override
        protected Void doInBackground(Void... voids) {


            //to remove duplications from numbers array

            LinkedHashSet<String> tempNumbers=new LinkedHashSet<>();
            tempNumbers.addAll(numbers);
            sortedNumbers.addAll(tempNumbers);

            //to get messages from same number
            ArrayList<String> messagesUnderSameNumber=new ArrayList<>();
            for(int i=0;i<sortedNumbers.size();i++)
            {
                String currentNumber=sortedNumbers.get(i);
                messagesUnderSameNumber=new ArrayList<String>();
                for(int j=0;j<messages.size();j++)
                {
                    if(numbers.get(j)==currentNumber)
                    {
                        messagesUnderSameNumber.add(messages.get(j));
                    }
                }
                sortedMessages.add(messagesUnderSameNumber);

            }

            return null;
        }
    }

}