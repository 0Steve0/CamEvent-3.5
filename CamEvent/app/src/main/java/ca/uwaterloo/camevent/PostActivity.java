package ca.uwaterloo.camevent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.firebase.database.FirebaseDatabase.getInstance;


public class PostActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;


    private Spinner spinnerBuilding;
    private Spinner spinnerType;
    private EditText capacity;
    private EditText fromDateEtxt;
    private EditText toDateEtxt;
    private EditText fromTimeEtxt;
    private EditText toTimeEtxt;

    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private TimePickerDialog fromTimePickerDialog;
    private TimePickerDialog toTimePickerDialog;
    private Button mSubmitButton;
    private SimpleDateFormat dateFormatter;

    private int mHour, mMinute;
    private EditText mTitleField;
    private EditText mDesField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mDatabase = getInstance().getReference();

        mTitleField = (EditText) findViewById(R.id.title);
        mDesField = (EditText) findViewById(R.id.description);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        mSubmitButton =(Button) findViewById(R.id.post);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        findViewsById();
        showSpinner();
        setDateTimeField();
    }

    private void findViewsById() {
        fromDateEtxt = (EditText) findViewById(R.id.etxt_fromdate);
        fromDateEtxt.setInputType(InputType.TYPE_NULL);
        fromDateEtxt.setFocusable(false);
        //fromDateEtxt.requestFocus();
        spinnerBuilding = (Spinner) findViewById(R.id.spinnerBuilding);

    }

    private void showSpinner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterBuilding = ArrayAdapter.createFromResource(this,
                R.array.spinnerBuilding, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBuilding.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBuilding.setAdapter(adapterBuilding);

    }

    private void setDateTimeField() {
        fromDateEtxt.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fromDateEtxt.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(new Date().getTime());
    }

    private void submitPost() {
//        String uid, String author, String EventTitle, String EventDescriptionRow,
// String eventLocationName,String eventLatitude,String eventLongitude,String eventLink,String eventDate){
// title, building, date, time, type, des, capacity
        final String title = mTitleField.getText().toString();
        final String des = mDesField.getText().toString();
        final String loc = spinnerBuilding.getSelectedItem().toString();
        final String cap = capacity.getText().toString();
        final String fromDate = fromDateEtxt.getText().toString();
        final String fromTime = fromTimeEtxt.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(des)) {
            mDesField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(PostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, title, des,loc,cap,fromDate,fromTime);

                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        post(mSubmitButton);
        // [END single_value_read]
    }
    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mDesField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }
    private void writeNewPost(String uid, String author, String title, String desc,String location,String capacity,String fromDate,String fromTime){
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        PostEvent eventinfo = new PostEvent(uid, author, title, desc,location,capacity,fromDate,fromTime, null, null, null);
        Map<String, Object> postValues = eventinfo.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + uid + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onClick(View view) {
        fromDatePickerDialog.show();
    }

    public void post(View view){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

}



