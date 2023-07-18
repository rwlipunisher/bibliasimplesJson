package rwl.bibliassimples.bibliaacf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences bibleJsonString = getSharedPreferences("biblia", Context.MODE_PRIVATE);

        if( bibleJsonString.getString("jsonString", null) == null) {
            String jsonString;
            try {
                InputStream is = getAssets().open("acf.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                jsonString = new String(buffer, "UTF-8");
                bibleJsonString.edit().putString("jsonString", jsonString).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_1, new Principal());
        transaction.commit();
    }
}