package com.galzant.encrypchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {
    EditText username, password, email;
    Button registerButton;
    String user, pass, e_mail;
    TextView login;
    private FirebaseAuth mAuth;

    private byte[] encryptionKey = {41, 52, 32, 23, 12, 50, 52, -24, 58, 123, 35, -11, 34, 25, 31, 67};
    private Cipher encrypt, decrypt;
    private SecretKeySpec secretKeySpec;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        try {
            encrypt = Cipher.getInstance("AES");
            decrypt = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        login = findViewById(R.id.login);

        Firebase.setAndroidContext(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                e_mail = email.getText().toString();

                if (user.equals("")) {
                    username.setError("Username cannot be blank");
                } else if (pass.equals("")) {
                    password.setError("Password cannot be blank");
                } else if (!user.matches("[A-Za-z0-9]+")) {
                    username.setError("Only alphanumeric characters are allowed");
                } else if (user.length() < 5) {

                    MongoClient mongoClient = MongoClients.create(
                            "mongodb+srv://testuser:<password>@cluster0-vlpcf.azure.mongodb.net/test?retryWrites=true&w=majority");
                    MongoDatabase database = mongoClient.getDatabase("test");

                    username.setError("Username must be at least 5 characters long");
                } else if (pass.length() < 5) {
                    password.setError("Password must be at least 5 characters long");
                } else {
                    final ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = "https://galzant.firebaseio.com/users.json";

                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Firebase reference = new Firebase("https://galzant.firebaseio.com/users");


                            /*try {
                                user = AESEncrypt(username.getText().toString());
                                pass = AESEncrypt(password.getText().toString());
                                //e_mail = AESEncrypt(email.getText().toString());
                            } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
                                e.printStackTrace();
                            }*/

                            if (s.equals("null")) {
                                reference.child(user).child("password").setValue(pass);
                                //reference.child(user).child("email").setValue(e_mail);
                                Toast.makeText(RegisterActivity.this, "Registration completed", Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    JSONObject obj = new JSONObject(s);

                                    if (!obj.has(user)) {
                                        reference.child(user).child("password").setValue(pass);
                                        //reference.child(user).child("email").setValue(e_mail);
                                        Toast.makeText(RegisterActivity.this, "Registration completed", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Username must be unique", Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            pd.dismiss();
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError);
                            pd.dismiss();
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(RegisterActivity.this);
                    rQueue.add(request);
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
    }

    public String AESEncrypt(String string) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException {
        byte[] stringBytes = string.getBytes();
        byte[] encryptedBytes = new byte[stringBytes.length];


        encrypt.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        encryptedBytes = encrypt.doFinal(stringBytes);
        String returnString = null;

        try {
            returnString = new String(encryptedBytes, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return returnString;

        return string;
    }
}