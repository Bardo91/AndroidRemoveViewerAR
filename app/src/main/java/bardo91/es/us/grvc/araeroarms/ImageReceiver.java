package bardo91.es.us.grvc.araeroarms;

import android.provider.ContactsContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.MatOfInt;

class ImageReceiver{
    private Thread mReadingThread;

    private String mSocketIp;
    private int mSocketPort;

    private boolean mListening = false;
    private boolean mIsConnected = false;

    private Mat mLastImage;

    public ImageReceiver(String _ip, int _port){
        mSocketIp = _ip;
        mSocketPort= _port;
    }

    public boolean isConnected(){
        return mIsConnected;
    }

    public Mat lastImage(){
        return mLastImage;
    }

    public void startListening(){
        mLastImage  = new Mat();
        mListening = true;
        mReadingThread = new Thread(
                new Runnable() {
                    public void run() {
                        Socket socket = null;

                        while (mListening & socket == null) {
                            // Try starting the socket
                            try {
                                socket = new Socket(mSocketIp, mSocketPort);
                                mIsConnected = socket.isConnected();
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if(socket != null) {
                            while (mListening) {
                                // SEND REQUEST OF DATA
                                String msg = "I want an image";
                                try {
                                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                                    out.println(msg);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    mIsConnected = false;
                                    try {
                                        socket.close();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                // RECEIVE IMAGE
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];

                                int bytesRead;
                                InputStream inputStream = null;
                                try {
                                    inputStream = socket.getInputStream();
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        byteArrayOutputStream.write(buffer, byteArrayOutputStream.size(), bytesRead);
                                    }

                                    // Decode input image
                                    Imgcodecs.imdecode(mLastImage,0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }
        );

        mReadingThread.start();
    }

    public boolean stopListening(){
        mListening = false;
        while(mReadingThread.isAlive()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            mReadingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

}