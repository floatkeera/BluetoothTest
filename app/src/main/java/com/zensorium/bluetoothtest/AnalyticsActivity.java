package com.zensorium.bluetoothtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.zensorium.bluetoothtest.MainActivity.DataRowStress;

public class AnalyticsActivity extends ActionBarActivity {

    Handler mHandler = new Handler();
    SharedPreferences sp;

    String url = "";
    String userid;
    String syncperiod;
    public String a = null;
    public JSONObject obj1;
    SwipeRefreshLayout mSwipeRefreshLayout;

    int number = 0;

    PieChart chart;
    GraphView graph;
    public ArrayList<DataRowStress> data = new ArrayList();
    public ArrayList<Entry> entries = new ArrayList<>();

    LineGraphSeries<DataPoint> series ;
    ArrayList<String> labels = new ArrayList<String>();

    TextView txt;
    TextView txt2;
    long min = 0;
    long max= 0;

    int excited;
    int calm;
    int normal;
    Date current;
    Date hr24;

    int stressed;



    upload mUpload;

    AsyncTask task;
    boolean checkload = false;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        checkload = true;
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Calendar cal = Calendar.getInstance();
        current = cal.getTime();

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        userid = sp.getString("example_text", "hi");
        syncperiod = sp.getString("sync_period", "hi");

        Log.i("BluetoothLeService",syncperiod );
        url = "http://128.199.189.195:8086/query?db=stressdb&q=SELECT%20*%20FROM%20stressdata%20where%20userid=%27" + userid + "%27";





        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Color.BLUE);

        task = new upload().execute(url);


        txt = (TextView)findViewById(R.id.textView5);
        txt2 = (TextView)findViewById(R.id.textView6);

        chart = (PieChart) findViewById(R.id.chart);
        graph = (GraphView) findViewById(R.id.graph);
        series =  new LineGraphSeries<DataPoint>();

        switch(syncperiod){
            case "1":
                cal.add(Calendar.HOUR, -24);
                hr24 = cal.getTime();
                txt.setText("Last 24 hours");
                txt2.setText("Last 24 hours");
                break;
            case "3":
                cal.add(Calendar.HOUR, -72);
                hr24 = cal.getTime();
                txt.setText("Last 3 days");
                txt2.setText("Last 3 days");
                break;
            case "7":
                cal.add(Calendar.HOUR, -168);
                hr24 = cal.getTime();
                txt.setText("Last week");
                txt2.setText("Last week");
                break;
            case"30":
                cal.add(Calendar.HOUR, -720);
                hr24 = cal.getTime();
                txt.setText("Last month");
                txt2.setText("Last month");
                break;

        }


        //Gson gson = new Gson();
        //gson.fromJson(a, result);
        //JSONArray;

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()

        {
            @Override
            public void onRefresh () {

                syncperiod = sp.getString("sync_period", "hi");

                Calendar cal = Calendar.getInstance();
                current = cal.getTime();

                switch(syncperiod){
                    case "1":
                        cal.add(Calendar.HOUR, -24);
                        hr24 = cal.getTime();
                        txt.setText("Last 24 hours");
                        txt2.setText("Last 24 hours");
                        break;
                    case "3":
                        cal.add(Calendar.HOUR, -72);
                        hr24 = cal.getTime();
                        txt.setText("Last 3 days");
                        txt2.setText("Last 3 days");
                        break;
                    case "7":
                        cal.add(Calendar.HOUR, -168);
                        hr24 = cal.getTime();
                        txt.setText("Last week");
                        txt2.setText("Last week");
                        break;
                    case "30":
                        cal.add(Calendar.HOUR, -720);
                        hr24 = cal.getTime();
                        txt.setText("Last month");
                        txt2.setText("Last month");
                        break;

                }


                data.clear();
                entries.clear();
                userid = sp.getString("example_text", "hi");
                url = "http://128.199.189.195:8086/query?db=stressdb&q=SELECT%20*%20FROM%20stressdata%20where%20userid=%27" + userid + "%27";
                task.cancel(true);
                task = new upload().execute(url);


            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_analytics, menu);
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
            Intent in = new Intent(getApplicationContext(), SettingsActivity.class);


            overridePendingTransition(R.anim.slideinright, R.anim.slideoutleft);
            startActivity(in);


            return true;
        }

        switch (item.getItemId()) {
            case R.id.connect:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public class upload extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... strings) {


            for (int i = 0; i < strings.length; i++) {


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
                        Log.i("BluetoothLeService", a);
                    } else {
                        a = "";
                    }


                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block]
                    Toast.makeText(getApplicationContext(), "Sync failed. Check your internet connection.", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sync failed. Check your internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }


            }

            return a;

        }


        protected void onPreExecute() {



        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {


            excited = 0;
            calm = 0;
            normal = 0;
            stressed = 0;
            number = 0;

            boolean exc = false;
            boolean cal = false;
            boolean norm = false;
            boolean stress = false;

            entries.clear();
            labels.clear();


            try {

                obj1 = new JSONObject(a);


                Log.d("BluetoothLeService", obj1.toString());

                JSONArray b = obj1.getJSONArray("results");
                Log.d("BluetoothLeService", b.toString());
                JSONObject obj2 = b.getJSONObject(0);
                Log.d("BluetoothLeService", obj2.toString());
                JSONArray c = obj2.getJSONArray("series");
                JSONObject obj3 = c.getJSONObject(0);
                Log.d("BluetoothLeService", obj3.toString());
                JSONArray d = obj3.getJSONArray("values");
                Log.d("BluetoothLeService", d.toString());
                for (int i = 0; i < d.length(); i++) {
                    JSONArray e = d.getJSONArray(i);
                    Log.d("BluetoothLeService", e.toString());
                    String a = e.getString(0);
                    a.replaceAll("\"", "");
                    SimpleDateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = format.parse(a);
                    SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yy HH:mm");
                    String realdate = format2.format(date).toString();


                    DataRowStress dr = new DataRowStress(realdate, e.getString(1), e.getString(7), e.getString(4), e.getString(5), e.getString(2), e.getString(6), e.getString(8), e.getString(3));
                    data.add(dr);
                }






                DateFormat a;
                if(syncperiod.equals("30")){
                    a = new SimpleDateFormat("d MMM");
                } else {
                    a = new SimpleDateFormat("E h a");
                }
                DateAsXAxisLabelFormatter df = new DateAsXAxisLabelFormatter(getApplicationContext(), a);

                graph.getGridLabelRenderer().setLabelFormatter(df);
                graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

                graph.getViewport().setMinX(hr24.getTime());
                graph.getViewport().setMaxX(current.getTime());

                graph.getViewport().setMaxY(200);
                graph.getViewport().setMinY(0);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setYAxisBoundsManual(true);



                graph.addSeries(series);




                series.resetData(addtograph(data));
                series.setColor(Color.RED);










                int count = 0;



                for(int i = 0; i < data.size(); i++) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                    try {
                        Date x = dateFormat.parse(data.get(i).timeStamp);
                        if (x.getTime() > hr24.getTime()) {
                            int mood = Integer.parseInt(data.get(i).Quadrant);

                            switch (mood) {
                                case 1:
                                    excited++;
                                    count++;
                                    break;
                                case 2:
                                    calm++;
                                    count++;
                                    break;
                                case 3:
                                    normal++;
                                    count++;
                                    break;
                                case 4:
                                    stressed++;
                                    count++;
                                    break;
                            }

                        }
                    } catch (Exception e) {
                    }
                }





                float excited1 = (excited * 100)/count ;
                int excited2 = (int)(excited1);
                if(excited2!=0) exc = true;


                float calm1 = (calm * 100)/count;
                int calm2 = (int)(calm1);
                if(calm2!=0) cal = true;

                float normal1 = (normal * 100)/count ;
                int normal2 = (int)(normal1);
                if(normal2!=0) norm = true;


                float stressed1 = (stressed* 100)/count ;
                int stressed2 = (int)(stressed1);
                if(stressed2!=0) stress = true;



                if(exc == true) entries.add(new Entry(excited2, 0));
                if(cal == true)entries.add(new Entry(calm2, 1));
                if(norm == true)entries.add(new Entry(normal2, 2));
                if(stress == true)entries.add(new Entry(stressed2 , 3));

                if(exc == true)labels.add("Excited");
                if(cal == true)labels.add("Calm");
                if(norm == true)labels.add("Normal");
                if(stress == true)labels.add("Stressed");

                chart.setHoleRadius(45f);
                chart.setHoleColorTransparent(false);
                chart.setDrawHoleEnabled(true);
                chart.setUsePercentValues(true);
                chart.setHoleColor(Color.WHITE);

                chart.setCenterText("");



                ArrayList<Integer> colors = new ArrayList<Integer>();

                for (int color : ColorTemplate.VORDIPLOM_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.JOYFUL_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.COLORFUL_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.LIBERTY_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.PASTEL_COLORS)
                    colors.add(color);

                colors.add(ColorTemplate.getHoloBlue());

                ArrayList<Integer> colors1 = new ArrayList<>();
                if(exc == true)colors1.add(Color.rgb(250,255,63));
                if(cal == true)colors1.add(Color.rgb(73,184,248));
                if(norm == true)colors1.add(Color.rgb(77,166,92));
                if(stress == true)colors1.add(Color.rgb(159,82,181));


                PieDataSet dataset = new PieDataSet(entries, "%");
                dataset.setColors(colors1);

                dataset.setValueFormatter(new MyValueFormatter());
                chart.setDescription("");

                chart.setDrawSliceText(true);




                PieData data2 = new PieData(labels, dataset);

                data2.setValueTextSize(13f);
                data2.setValueTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                chart.setDescriptionTextSize(13f);

                chart.setData(data2);

                chart.getLegend().setEnabled(false);

                chart.invalidate();
                mHandler.postDelayed(z, 5);

                chart.setCenterTextSize(24f);
                chart.setCenterTextTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
                chart.animateY(2000, Easing.EasingOption.EaseInOutQuad);
                //JSONArray obj3 = obj2.getJSONArray(0);
                //for(int i = 0; i < obj3.length(); i++){

                //  Log.d("BluetoothLeService", obj3.get(i).toString());
                // }





            } catch (Throwable t) {
                Log.e("BluetoothLeService", "Could not parse malformed JSON");
            }

            mSwipeRefreshLayout.setRefreshing(false);



        }


    }

    private DataPoint[] addtograph(ArrayList<DataRowStress> a) {

        int count = data.size();

        DataPoint[] values = new DataPoint[count];





        for (int i = 0; i < count; i++) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
            SimpleDateFormat time = new SimpleDateFormat("E HH:mm");

            try {
                Date x = dateFormat.parse(a.get(i).timeStamp);

                if (i ==0){
                    min = x.getTime();
                    max = x.getTime();
                } else{
                    if(x.getTime() < min){
                        min = x.getTime();
                    }

                    if(x.getTime() > max){
                        max = x.getTime();
                    }
                }


                Double y = Double.parseDouble(a.get(i).HR);
                DataPoint v = new DataPoint(x, y);
                values[i] = v;
            } catch (Exception e) {
                Log.i("BluetoothLeService", "error");
            }


        }
        return values;
    }

    public class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("##############"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value) + "%"; // append a dollar-sign
        }
    }

    Runnable z = new Runnable() {
        @Override
        public void run() {
            number = number + 1;
            chart.setCenterText(String.valueOf(number) + "%");
            chart.invalidate();
            if(number < 100) mHandler.postDelayed(z, 5);
        }
    };
}
