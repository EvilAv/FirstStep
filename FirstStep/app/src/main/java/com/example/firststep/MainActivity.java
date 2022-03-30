package com.example.firststep;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firststep.databinding.ActivityMainBinding;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class MainActivity extends AppCompatActivity implements  TransactionEvents{

    ActivityResultLauncher activityResultLauncher;
    private String pin;
    int money = 10;

    // Used to load the 'firststep' library on application startup.
    static {
        System.loadLibrary("firststep");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int res = initRng();

        TextView sum = findViewById(R.id.total_sum);
        sum.setText(Integer.toString(money)+" RUB");


        activityResultLauncher  = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // instead of anonymous class
                (ActivityResult result) -> {  // rewrite method 'onActivityResult' with lambda
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        pin = data.getStringExtra("pin");
                        synchronized (MainActivity.this) {
                            MainActivity.this.notifyAll();
                        }
                    }
                });

    }

    @Override
    public String enterPin(int ptc, String amount) {
        pin = new String();
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                //todo: log error
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        TextView sum = findViewById(R.id.total_sum);
        TextView test = findViewById(R.id.rnd_elem);
        byte[] rnd = randomBytes(10);
        runOnUiThread(()-> {
            test.setText( result ? "Some random numbers: "+Byte.toString( rnd[0])+" , "+String.format("%d", rnd[1]) : "");
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();

            if (result)
                money--;
            sum.setText(money + " RUB");

        });
    }

    public void onButtonClick(View v)
    {
        byte[] trd = stringToHex("9F0206000000000100");

        if (money <= 0)
            Toast.makeText(MainActivity.this, "You have no money :(", Toast.LENGTH_SHORT).show();
        else
            transaction(trd);
    }

    public static byte[] stringToHex(String s)
    {
        byte[] hex;
        try
        {
            hex = Hex.decodeHex(s.toCharArray());
        }
        catch (DecoderException ex)
        {
            hex = null;
        }
        return hex;
    }
    /**
     * A native method that is implemented by the 'firststep' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);
    public native boolean transaction(byte[] trd);
}