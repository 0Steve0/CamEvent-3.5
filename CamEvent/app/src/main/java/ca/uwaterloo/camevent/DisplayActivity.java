package ca.uwaterloo.camevent;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;


public class DisplayActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private String strTitle;
    //private String strDes;
    private String strDate;
    private String strLoc;
    private String strLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        String eventtitle;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                eventtitle= null;
            } else {
                eventtitle= extras.getString("Database Data");
            }
        } else {
            eventtitle= (String) savedInstanceState.getSerializable("Database Data");
        }
        EventDBHandler eventDB=new EventDBHandler(this);
        Eventinfo eventinfo=eventDB.getEvent(eventtitle);
        //System.out.println(eventinfo.getEventTitle());
        strTitle=eventinfo.getEventTitle();
        //strDes=eventinfo.getEventDescriptionRow();
        strDate=eventinfo.getEventDate();
        strLoc=eventinfo.getEventLocationName();
        //strLink=Uri.parse(eventinfo.getEventLink());
        strLink= eventinfo.getEventLink();
        //set title and description!
        TextView title = (TextView) findViewById(R.id.title);
        TextView link = (TextView) findViewById(R.id.link);
        TextView location = (TextView) findViewById(R.id.location);
        TextView time = (TextView) findViewById(R.id.time);
        title.setText(strTitle);
        link.setText(strLink);
        location.setText(strLoc);
        time.setText(strDate);


    }
}




