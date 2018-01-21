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
import java.nio.ByteBuffer;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import static org.opencv.core.CvType.CV_8UC1;

class ImageReceiver{
    private Thread mReadingThread;

    private String mSocketIp;
    private int mSocketPort;

    private boolean mListening = false;
    private boolean mIsConnected = false;

    private Mat mLastImage;

    private ReentrantLock mSecureLock = new ReentrantLock();

    public ImageReceiver(String _ip, int _port){
        mSocketIp = _ip;
        mSocketPort= _port;
    }

    public boolean isConnected(){
        return mIsConnected;
    }

    public Mat lastImage(){
        mSecureLock.lock();
        Mat image = mLastImage.clone();
        mSecureLock.unlock();
        return image;
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
                                mIsConnected = true;
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

                                try {
                                    InputStream inputStream = socket.getInputStream();

                                    // READ SIZE OF IMAGE
                                    int sizeImage;
                                    int bytesRead = 0;
                                    if((bytesRead = inputStream.read(buffer)) != -1){
                                        //char []sizeImageStr = new char[bytesRead];
                                        //for(int i = 0; i < bytesRead; i++){
                                        //    sizeImageStr[i] = '0' + buffer[i];
                                        //}
                                        buffer[bytesRead] = '\0';
                                        String sizeImageStr = new String(buffer, "UTF-8");
                                        sizeImage =  Integer.parseInt(sizeImageStr.substring(0, bytesRead));
                                    }else{
                                        continue;
                                    }

                                    msg = "Got size, sent it now";
                                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                                    out.println(msg);

                                    // READ IMAGE
                                    int bytesRecv = 0;
                                    int totalBytes = 0;
                                    while (totalBytes < sizeImage && (bytesRecv = inputStream.read(buffer)) != -1) {
                                        totalBytes += bytesRecv;
                                        if(bytesRecv == 1024){
                                            byteArrayOutputStream.write(buffer);
                                        }else{
                                            byte[] portion =  Arrays.copyOfRange(buffer, 0, bytesRecv);
                                            byteArrayOutputStream.write(portion);
                                        }
                                    }
                                    if(totalBytes > 0) {
                                        // Decode input image
                                        Mat encodedMat = new Mat(totalBytes, 1, CV_8UC1);
                                        encodedMat.put(0, 0, byteArrayOutputStream.toByteArray());
                                        Mat decodedImage = Imgcodecs.imdecode(encodedMat, 0);

                                        mSecureLock.lock();
                                        mLastImage = decodedImage.clone();
                                        mSecureLock.unlock();
                                    }

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
        mIsConnected = false;
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