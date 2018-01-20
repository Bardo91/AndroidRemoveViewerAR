package bardo91.es.us.grvc.araeroarms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

class ImageReceiver{
    private Thread mReadingThread;

    private String mSocketIp;
    private int mSocketPort;

    private boolean mListening = false;

    public ImageReceiver(String _ip, int _port){
        mSocketIp = _ip;
        mSocketPort= _port;
    }

    public void startListening(){
        mListening = true;
        mReadingThread = new Thread(
                new Runnable() {
                    public void run() {
                        Socket socket = null;

                        while (mListening & socket == null) {
                            // Try starting the socket
                            try {
                                socket = new Socket(mSocketIp, mSocketPort);
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
                                    //MatOfInt params = new MatOfInt(size);
                                    //int[] data = {Highgui.IMWRITE_JPEG_QUALITY, 40};
                                    //params.put(0, 0, data);
                                    //Highgui.imencode(".jpg", image, buf, params);
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