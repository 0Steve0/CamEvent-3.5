package ca.uwaterloo.camevent;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by serena on 2016-11-15.
 */
public class Frag_signup extends Fragment {

    EditText editText_username;
    EditText editText_password;
    EditText editText_cmPassword;
    Button button_register;

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_signup, container, false);
        editText_username = (EditText)rootView.findViewById(R.id.editText_username);
        editText_password = (EditText)rootView.findViewById(R.id.editText_password);
        editText_cmPassword = (EditText)rootView.findViewById(R.id.editText_cmPassword);
        button_register = (Button)rootView.findViewById(R.id.button_register);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editText_username.getText().toString();
                String password = editText_password.getText().toString();
                String cmPassword = editText_cmPassword.getText().toString();
                registerUser(username, password, cmPassword);
            }
        });

        return rootView;
    }

    protected void registerUser(String username, String password, String cmPassword) {
        if(checkInput(username, password, cmPassword)) {
            progressDialog.setMessage("Registering User...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()) {
                                onAuthSuccess(task.getResult().getUser());
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Register Successfully",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Could not register. Please try again!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    protected boolean checkInput(String username, String password, String cmPassword) {
        String email_regex = "^[a-z0-9A-Z]+[a-z0-9A-Z_-]*@[a-z0-9A-Z]+[a-z0-9A-Z\\.-]*\\.[a-z0-9A-Z]+$";
        if(username.isEmpty() || password.isEmpty() || cmPassword.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Some fields are empty. You should fill out all the fields.",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(!username.matches(email_regex)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Invalid email address. Please try again!",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(!password.equals(cmPassword)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "The passwords you've typed are not matched. Try again!",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        String email = user.getEmail();
        String username = email.substring(0, email.indexOf("@"));

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
