package com.galzant.encrypchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {
    TextView registerUser;
    EditText username, password;
    Button loginButton;
    String user, pass;

    private byte[] encryptionKey = {41, 52, 32, 23, 12, 50, 52, -24, 58, 123, 35, -11, 34, 25, 31, 67};
    private Cipher encrypt, decrypt;
    private SecretKeySpec secretKeySpec;

    private FirebaseAuth mAuth;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        try {
            encrypt = Cipher.getInstance("AES");
            decrypt = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");


        setContentView(R.layout.activity_login);

        registerUser = findViewById(R.id.register);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();

                if(user.equals("")){
                    username.setError("Username cannot be blank");
                }
                else if(pass.equals("")){
                    password.setError("Password cannot be blank");
                }
                else{

                    DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");

                   // db.child(FirebaseAuth.getInstance().getUid())

                    String url = "https://galzant.firebaseio.com/users.json";
                    final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                    pd.setMessage("Loading...");
                    pd.show();

                   // StringRequest potato = new StringRequest(Request.Method.GET, url, )
                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){

                        @Override
                        public void onResponse(String s) {



                            // s = AESDecrypt(s);
                            if(s.equals("null")){
                                Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_LONG).show();
                            }
                            else{
                                try {
                                    JSONObject obj = new JSONObject(s);

                                    if(!obj.has(user)){
                                        Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_LONG).show();
                                    }
                                    else if(obj.getJSONObject(user).getString("password").equals(pass)){
                                        UserDetails.username = user;
                                        UserDetails.password = pass;
                                        startActivity(new Intent(LoginActivity.this, UsersActivity.class));
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            pd.dismiss();
                        }

                    },new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError);
                            pd.dismiss();
                        }

                    })
                    {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> params = new HashMap<String, String>();
                            String creds = String.format("%s:%s", user, pass);
                            String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                            params.put("Authorization", auth);
                            return params;
                        }
                    };

                    RequestQueue rQueue = Volley.newRequestQueue(LoginActivity.this);
                    rQueue.add(request);
                }

            }
        });
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
        return string;
       // return returnString;
    }

    public String AESDecrypt(String string){
        byte[] encryptedBytes = string.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString = string;

        byte[] decryption;


        try
        {
            decrypt.init(Cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decrypt.doFinal(encryptedBytes);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

return string;
        //return decryptedString;
    }
}
