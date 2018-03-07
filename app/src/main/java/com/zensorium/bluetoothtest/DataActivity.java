package com.zensorium.bluetoothtest;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class DataActivity extends ActionBarActivity {


    ListView lv;
    ListView lv1;
    Button btnBack;
    Button btnUpload;
    ArrayList<String> myStringArray2 = new ArrayList();
    ArrayList<String> myStringArray3 = new ArrayList();
    public String[] parsed;
    static boolean show = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        lv = (ListView) findViewById(R.id.listView2);
        lv1 = (ListView) findViewById(R.id.listView3);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        HashMap map = new HashMap<String, Object>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            map = (HashMap) extras.get("RESULT");
            myStringArray2 = (ArrayList) (map.get("RESULT"));
            myStringArray3 = (ArrayList) (map.get("PARSED"));

             /*
            parsed = extras.getStringArray("PARSED");
            `

            for (int i = 0; i < value.length; i++) {
                myStringArray2.add(value[i]);
                Log.i("BluetoothLeService", value[i]);
            }

            */

            parsed = new String[myStringArray3.size()];

            for (int i = 0; i < myStringArray3.size(); i++) {
                parsed[i] = myStringArray3.get(i);
                Log.i("BluetoothLeService", parsed[i]);
            }


            setAbc();
            setBCD();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MainActivity.mBluetoothGatt.close();

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override


            public void onClick(View v) {

                String a = "";
                String[] URLArray = new String[parsed.length];

                if (MainActivity.decision == 1) {



                    a = "http://10.5.10.126:8080/api/getDataInfo?a=";


                    for (int i = 0; i < parsed.length; i++) {
                        String b = parsed[i];
                        show = true;
                        String d = URLEncoder.encode(b);
                        a = a + d;
                        new upload().execute(a);
                    }
                    /*
                    a = a + "&b=" + parsed[1];
                    a = a + "&c=" + parsed[2];
                    a = a + "&d=" + parsed[3];
                    a = a + "&e=" + parsed[4];
                    a = a + "&f=" + parsed[5];
                    a = a + "&g=" + parsed[6];
                    a = a + "&h=" + parsed[7];
                    a = a + "&i=" + parsed[8];
                    a = a + "&j=" + parsed[9];
                    a = a + "&k=" + parsed[10];
                    a = a + "&l=" + parsed[11];
                    a = a + "&m=" + parsed[12];
                    a = a + "&n=" + parsed[13];
                    a = a + "&o=" + parsed[14];
                    */





                } else if (MainActivity.decision == 2) {

                    for (int i = 0; i < parsed.length; i++) {
                        String b = "http://10.5.10.126:8080/api/getDataStress?a=";
                        String c = parsed[i];
                        String d = URLEncoder.encode(c);
                        String e = b + d;
                        if(i == parsed.length - 1){
                            show = true;
                        }
                        new upload().execute(e);






                    }

                }


            }


        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data, menu);
        return true;
    }


    public void setAbc() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                myStringArray2);
        lv.setAdapter(arrayAdapter);

    }

    public void setBCD() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                myStringArray3);
        lv1.setAdapter(arrayAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class upload extends AsyncTask<String, Void, String> {

        String a;

        @Override
        protected String doInBackground(String... strings) {



            int count = strings.length;
            for (int i = 0; i < count; i++) {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(strings[i]);
                // replace with your url

                HttpResponse response;
                try {
                    response = client.execute(request);

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();

                    if (inputStream != null) {
                        BufferedReader br = null;
                        StringBuilder sb = new StringBuilder();

                        String line;
                        try {

                            br = new BufferedReader(new InputStreamReader(inputStream));
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        a = sb.toString();
                    } else {
                        a = "";
                    }


                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block]
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return a;
        }


        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {

            if (show == true) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }

        }


        private void makeGetRequest(final String a) {


            new Thread() {
                @Override
                public void run() {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(a);
                    // replace with your url

                    HttpResponse response;
                    try {
                        response = client.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream inputStream = entity.getContent();


                        Log.d("Response of GET request", response.getStatusLine().toString());
                    } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block]
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }.start();


        }
    }
}

