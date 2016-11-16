package ca.uwaterloo.camevent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by serena on 2016-11-15.
 */
public class Frag_login extends Fragment {

    EditText editText_username;
    EditText editText_password;
    Button button_signIn;
    TextView textView_forgotPassword;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_login, container, false);
        editText_username = (EditText)rootView.findViewById(R.id.editText_username);
        editText_password = (EditText)rootView.findViewById(R.id.editText_password);
        button_signIn = (Button)rootView.findViewById(R.id.button_signIn);
        textView_forgotPassword = (TextView)rootView.findViewById(R.id.textView_forgotPassword);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());

        // click button "sign in"
        button_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = editText_username.getText().toString();
                final String password = editText_password.getText().toString();
                login(username, password);
            }
        });

        // click "forgot password"
        textView_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), RetrievePasswordActivity.class));
                getActivity().finish();
            }
        });

        return rootView;
    }

    protected void login(String username, String password) {
        if(checkInput(username, password)) {
            progressDialog.setMessage("Logging In...");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                progressDialog.dismiss();
                                startActivity(new Intent(getActivity(), MeActivity.class));
                                getActivity().finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Could not log in. Please try again!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    protected boolean checkInput(String username, String password) {
        String email_regex = "^[a-z0-9A-Z]+[a-z0-9A-Z_-]*@[a-z0-9A-Z]+[a-z0-9A-Z\\.-]*\\.[a-z0-9A-Z]+$";
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Username or password is empty. You should fill out all fields.",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(!username.matches(email_regex)) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Invalid email address. Please try again!",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
