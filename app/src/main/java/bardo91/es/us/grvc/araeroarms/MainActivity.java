package bardo91.es.us.grvc.araeroarms;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    ImageReceiver mReceiver;
    ImageView mDisplayer;
    private boolean mRunBoyRun = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mReceiver = new ImageReceiver("192.168.0.164", 5005);
        mDisplayer = (ImageView) findViewById(R.id.displayer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunBoyRun = false;
        mReceiver.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            mRunBoyRun = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    while(mRunBoyRun){
                        if(!mReceiver.isConnected()){
                            mReceiver.startListening();
                        }else{
                            Mat image = mReceiver.lastImage();
                            Bitmap bmp = Bitmap.createBitmap(image.width(), image.height(), Bitmap.Config.RGB_565);
                            Utils.matToBitmap(image, bmp);
                            mDisplayer.setImageBitmap(bmp);
                        }
                    }
                }
            });
        } else {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }
}
