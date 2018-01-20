package bardo91.es.us.grvc.araeroarms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    ImageReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mReceiver = new ImageReceiver("192.168.0.164", 5005);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mReceiver.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver.startListening();
    }
}
