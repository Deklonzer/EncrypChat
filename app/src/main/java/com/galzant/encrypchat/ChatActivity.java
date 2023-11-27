package com.galzant.encrypchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
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

public class ChatActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    Spinner spinner;
    String choice = "";
    final char[] alphabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    final char[] ALPHABET = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    int caesarCount = 0;
    private byte[] encryptionKey = {41, 52, 32, 23, 12, 50, 52, -24, 58, 123, 35, -11, 34, 25, 31, 67};
    private Cipher encrypt, decrypt;
    private SecretKeySpec secretKeySpec;
    private TextView tv;
    static Dialog d ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            encrypt = Cipher.getInstance("AES");
            decrypt = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");



        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);
        spinner = (Spinner) findViewById(R.id.ciphers_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ciphers_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://galzant.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://galzant.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    if(choice.equals("RAIL-FENCE"))
                    {
                        messageText = decipher(messageText);
                    }
                    else if (!choice.equals(""))
                    {
                        messageText = cipher(messageText);
                    }

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });


        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choice = (String) parent.getItemAtPosition(position);
                if(choice.equals("CAESARIAN") || choice.equals("RAIL-FENCE"))
                    show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spinner.setOnItemSelectedListener(onItemSelectedListener);
        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox(message, 1);
                }
                else{
                    addMessageBox(message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public String decipher(String msg)
    {
        StringBuilder decrypted = new StringBuilder();

        //So far only rail-fence has a diff decrypt than encrypt

        switch(choice)
        {
            case "RAIL-FENCE":

                // create the matrix to cipher plain text 
                // caesarCount = rows , length(text) = columns 
                char [][] rail = new char[caesarCount][msg.length()];

                // filling the rail matrix to distinguish filled 
                // spaces from blank ones
                for (int i=0; i < caesarCount; i++)
                    for (int j=0; j < msg.length(); j++)
                        rail[i][j] = '\n';

                // to find the direction 
                boolean dir_down = false;

                int row = 0, col = 0;

                // mark the places with '*' 
                for (int i=0; i < msg.length(); i++)
                {
                    // check the direction of flow 
                    if (row == 0)
                        dir_down = true;
                    if (row == caesarCount-1)
                        dir_down = false;

                    // place the marker 
                    rail[row][col++] = '*';

                    // find the next row using direction flag 
                    if (dir_down) {
                        row++;
                    } else {
                        row--;
                    }
                }

                // now we can construct the fill the rail matrix 
                int index = 0;
                for (int i=0; i<caesarCount; i++)
                    for (int j=0; j<msg.length(); j++)
                        if (rail[i][j] == '*' && index<msg.length())
                            rail[i][j] = msg.charAt(index++);//[index++];


                // now read the matrix in zig-zag manner to construct 
                // the resultant text 
                //string result;

                row = 0;
                col = 0;
                for (int i=0; i< msg.length(); i++)
                {
                    // check the direction of flow 
                    if (row == 0)
                        dir_down = true;
                    if (row == caesarCount-1)
                        dir_down = false;

                    // place the marker 
                    if (rail[row][col] != '*')
                        decrypted.append(rail[row][col++]);

                    // find the next row using direction flag 
                    if (dir_down) {
                        row++;
                    } else {
                        row--;
                    }
                }
                //return new String(decrypted);
                msg = new String(decrypted);
                break;

            default:
                break;
        }
        return msg;

    }

    public String cipher(String msg)
    {
        StringBuilder encrypted = new StringBuilder();

        switch(choice)
        {
            case "ROT13":
                for (int i = 0; i < msg.length(); i++)
                {
                    char letter = msg.charAt(i);
                    if((letter >= 78 && letter <= 90) || (letter >= 110 && letter <= 122 ))
                    {
                        char a = (char)(letter - 13);
                        encrypted.append(a);
                    }
                    else if((letter >= 65 && letter <= 77) || (letter >= 97 && letter <= 109 ) )
                    {
                        char a = (char)(letter + 13);
                        encrypted.append(a);
                    }
                    else
                        encrypted.append(letter);
                }
                msg = new String(encrypted);
                break;
            case "ATBASH":
                for (int i = 0; i < msg.length(); i++)
                {
                    char letter = msg.charAt(i);
                    if(letter >= 78 && letter <= 90)
                    {
                        char a = (char)(letter - (25 - (('Z' - letter) * 2)));
                        encrypted.append(a);
                        continue;
                    }
                    if(letter >= 110 && letter <= 122 )
                    {
                        char a = (char)(letter - (25 - (('z' - letter) * 2)));
                        encrypted.append(a);
                        continue;
                    }
                    if (letter >= 65 && letter <= 77)
                    {
                        char a = (char)(letter + (25 - ((letter - 'A') * 2)));
                        encrypted.append(a);
                        continue;
                    }
                    if(letter >= 97 && letter <= 109)
                    {
                        char a = (char)(letter + (25 - ((letter - 'a') * 2)));
                        encrypted.append(a);
                    }
                    else
                        encrypted.append(letter);
                }
                msg = new String(encrypted);
                break;
            case "CAESARIAN":
                for (int i = 0; i < msg.length(); i++)
                {
                    char letter = msg.charAt(i);

                    if(letter >= 65 && letter <= 90)
                    {
                        char a = letter + caesarCount > 90 ? (char)(64 + (caesarCount - (90 - letter))) : (char)(letter + caesarCount);
                        encrypted.append(a);
                    }
                    else if(letter >= 97 && letter <= 122)
                    {
                        char a = letter + caesarCount > 122 ? (char)(96 + (caesarCount - (122 - letter))) : (char)(letter + caesarCount);;
                        encrypted.append(a);
                    }
                    else
                        encrypted.append(letter);
                }
                msg = new String(encrypted);
                break;
            case "RAIL-FENCE":
               /* //char[] encrypted = new char[msg.length()];
                int n = 0;


                for(int k = 0 ; k < caesarCount; k ++) {
                    int index = k;
                    boolean down = true;
                    while(index < msg.length() ) {
                        //System.out.println(k + " " + index+ " "+ n );
                        encrypted.append(msg.charAt(index));
//                        encrypted[n++] = msg.charAt(index);

                        if(k == 0 || k == caesarCount - 1) {
                            index = index + 2 * (caesarCount - 1);
                        }
                        else if(down) {
                            index = index +  2 * (caesarCount - k - 1);
                            down = !down;
                        }
                        else {
                            index = index + 2 * k;
                            down = !down;
                        }
                    }
                }
                msg = new String(encrypted);
                break;*/
                //char rail[caesarCount][(msg.length())];
                char [][] rail = new char[caesarCount][msg.length()];
                // filling the rail matrix to distinguish filled
                // spaces from blank ones
                for (int i=0; i < caesarCount; i++)
                    for (int j = 0; j < msg.length(); j++)
                        rail[i][j] = '\n';

                // to find the direction
                boolean dir_down = false;
                int row = 0, col = 0;

                for (int i=0; i < msg.length(); i++)
                {
                    // check the direction of flow
                    // reverse the direction if we've just
                    // filled the top or bottom rail
                    if (row == 0 || row == caesarCount-1)
                        dir_down = !dir_down;

                    // fill the corresponding alphabet
                    rail[row][col++] = msg.charAt(i);

                    // find the next row using direction flag
                    if (dir_down) {
                        row++;
                    } else {
                        row--;
                    }
                }

                //now we can construct the cipher using the rail matrix
               // String result = new String();
                for (int i=0; i < caesarCount; i++)
                    for (int j=0; j < msg.length(); j++)
                        if (rail[i][j]!='\n')
                            encrypted.append(rail[i][j]);
//                            result.push_back(rail[i][j]);
                msg = new String(encrypted);
            default:
                break;
        }
        return msg;
    }


    public void addMessageBox(final String message, int type) {
        TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);
        textView.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 7.0f;

        if(type == 1) {
            lp2.gravity = Gravity.END;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bubble_out);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!choice.equals(""))
                    {
                        Toast.makeText(getApplicationContext(), cipher(message), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //Do something with this value
    }


    public void show()
    {

        final Dialog d = new Dialog(ChatActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.number_dialog);
        Button setButton = d.findViewById(R.id.set_button);
        Button cancelButton = d.findViewById(R.id.cancel_button);
        final NumberPicker np = d.findViewById(R.id.caesarian_Picker);
        np.setMaxValue(25);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        np.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caesarCount = np.getValue();
                d.dismiss();
                Log.e("caesarcount", String.valueOf(caesarCount));
            }
        });
        setButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                caesarCount = np.getValue();
                //tv.setText(String.valueOf(np.getValue())); //set the value to textview
                d.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();


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
        //        return returnString;
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
//        return decryptedString;
    }

}
