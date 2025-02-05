package com.agvahealthcare.ventilator_ext.callback;

import android.os.Process;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SerialInputOutputHIDManager implements Runnable {

    public enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    public static boolean DEBUG = false;

    private static final String TAG = "HID_CONNECT";
    private static final int BUFSIZ = 4096;

    /**
     * default read timeout is infinite, to avoid data loss with bulkTransfer API
     */

    private int mReadTimeout = 0;
    private int mWriteTimeout = 0;
    private long RECONNECT_DELAY = 5000;

    private final Object mReadBufferLock = new Object();
    private final Object mWriteBufferLock = new Object();

    private ByteBuffer mReadBuffer; // default size = getReadEndpoint().getMaxPacketSize()
    private ByteBuffer mWriteBuffer = ByteBuffer.allocate(BUFSIZ);

    private int mThreadPriority = Process.THREAD_PRIORITY_URGENT_AUDIO;
    private SerialInputOutputHIDManager.State mState = SerialInputOutputHIDManager.State.STOPPED; // Synchronized by 'this'
    private SerialInputOutputHIDManager.Listener mListener; // Synchronized by 'this'
    private final UsbSerialPort mSerialPort;

    public interface Listener {
        /**
         * Called when new incoming data is available.
         */

        void onNewHIDData(byte[] data);

        /**
         * Called when {@link SerialInputOutputHIDManager#run()} aborts due to an error.
         */
        void onRunErrorHID(Exception e);
    }

    public SerialInputOutputHIDManager(UsbSerialPort serialPort) {
        mSerialPort = serialPort;
        mReadBuffer = ByteBuffer.allocate(serialPort.getReadEndpoint().getMaxPacketSize());
    }

    public SerialInputOutputHIDManager(UsbSerialPort serialPort, SerialInputOutputHIDManager.Listener listener) {
        mSerialPort = serialPort;
        mListener = listener;
        mReadBuffer = ByteBuffer.allocate(serialPort.getReadEndpoint().getMaxPacketSize());
        Log.i(TAG, "allocation");
    }

    public synchronized void setListener(SerialInputOutputHIDManager.Listener listener) {
        mListener = listener;
    }

    public synchronized SerialInputOutputHIDManager.Listener getListener() {
        return mListener;
    }

    /**
     * setThreadPriority. By default a higher priority than UI thread is used to prevent data loss
     *
     * @param threadPriority  see {@link Process#setThreadPriority(int)}
     * */

    public void setThreadPriority(int threadPriority) {
        if (mState != SerialInputOutputHIDManager.State.STOPPED) {
            Log.i(TAG, "setThreadPriority");
            throw new IllegalStateException("threadPriority only configurable before SerialInputOutputHIDManager is started");
        }
        mThreadPriority = threadPriority;
    }

    /**
     * read/write timeout
     */

    public void setReadTimeout(int timeout) {
        // when set if already running, read already blocks and the new value will not become effective now
        if(mReadTimeout == 0 && timeout != 0 && mState != SerialInputOutputHIDManager.State.STOPPED) {
            Log.i(TAG, "settimeout");
            throw new IllegalStateException("readTimeout only configurable before SerialInputOutputHIDManager is started");
        }
        mReadTimeout = timeout;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }

    public void setWriteTimeout(int timeout) {
        mWriteTimeout = timeout;
    }

    public int getWriteTimeout() {
        return mWriteTimeout;
    }

    /**
     * read/write buffer size
     */
    public void setReadBufferSize(int bufferSize) {
        if (getReadBufferSize() == bufferSize)
            return;
        synchronized (mReadBufferLock) {
            mReadBuffer = ByteBuffer.allocate(bufferSize);
        }
    }

    public int getReadBufferSize() {
        return mReadBuffer.capacity();
    }

    public void setWriteBufferSize(int bufferSize) {
        if(getWriteBufferSize() == bufferSize)
            return;
        synchronized (mWriteBufferLock) {
            ByteBuffer newWriteBuffer = ByteBuffer.allocate(bufferSize);
            if(mWriteBuffer.position() > 0)
                newWriteBuffer.put(mWriteBuffer.array(), 0, mWriteBuffer.position());
            mWriteBuffer = newWriteBuffer;
        }
    }

    public int getWriteBufferSize() {
        return mWriteBuffer.capacity();
    }

    /**
     * when using writeAsync, it is recommended to use readTimeout != 0,
     * else the write will be delayed until read data is available
     */
    public void writeAsync(byte[] data) {
        synchronized (mWriteBufferLock) {
            mWriteBuffer.put(data);
        }
    }

    /**
     * start SerialInputOutputHIDManager in separate thread
     */

    public void start() {
        if(mState != SerialInputOutputHIDManager.State.STOPPED) {
            Log.i(TAG, "start exception");
            throw new IllegalStateException("already started");
        }
        Log.i(TAG, "start");
        new Thread(this, this.getClass().getSimpleName()).start();
    }

    /**
     * stop SerialInputOutputHIDManager thread
     * when using readTimeout == 0 (default), additionally use usbSerialPort.close() to
     * interrupt blocking read
     */

    public synchronized void stop() {
        if (getState() == SerialInputOutputHIDManager.State.RUNNING) {
            Log.i(TAG, "Stop exception");
            mState = SerialInputOutputHIDManager.State.STOPPING;
        }
    }

    public synchronized SerialInputOutputHIDManager.State getState() {
        return mState;
    }

    /**
     * Continuously services the read and write buffers until {@linkstop()} is
     * called, or until a driver exception is raised.
     */

//    @Override
//    public void run() {
//        while (true) {
//            synchronized (this) {
//                if (getState() != SerialInputOutputHIDManager.State.STOPPED) {
//                    Log.i(TAG, "run stopped");
//                    throw new IllegalStateException("Already running");
//                }
//                mState = SerialInputOutputHIDManager.State.RUNNING;
//            }
//            Log.i(TAG, "Running ...");
//            try {
//                if(mThreadPriority != Process.THREAD_PRIORITY_DEFAULT)
//                    Process.setThreadPriority(mThreadPriority);
//                while (getState() != SerialInputOutputHIDManager.State.STOPPED) {
//                    step();
//                }
//            } catch (Exception e) {
//                Log.w(TAG, "Run ending due to exception: " + e.getMessage(), e);
//                final SerialInputOutputHIDManager.Listener listener = getListener();
//                if (listener != null) {
//                    listener.onRunErrorHID(e);
//                }
//            } finally {
//                synchronized (this) {
//                    mState = SerialInputOutputHIDManager.State.STOPPED;
//                    Log.i(TAG, "Stopped");
//                }
//            }
//            // Try to reconnect
////            reconnect();
//        }
//    }


    @Override
    public void run() {
        synchronized (this) {
            if (getState() != SerialInputOutputHIDManager.State.STOPPED) {
                Log.i(TAG, "run stopped");
                throw new IllegalStateException("Already running");
            }
            mState = SerialInputOutputHIDManager.State.RUNNING;
        }
        Log.i(TAG, "Running ...");
        try {
            if(mThreadPriority != Process.THREAD_PRIORITY_DEFAULT)
                Process.setThreadPriority(mThreadPriority);
            while (true) {
                if (getState() != SerialInputOutputHIDManager.State.RUNNING) {
                    Log.i(TAG, "Stopping mState=" + getState());
                    Log.i(TAG, "run break");
//                    break;
                }
                step();
            }
        } catch (Exception e) {
            Log.w(TAG, "Run ending due to exception: " + e.getMessage(), e);
            final SerialInputOutputHIDManager.Listener listener = getListener();
            if (listener != null) {
                listener.onRunErrorHID(e);
            }
        } finally {
            synchronized (this) {
                mState = SerialInputOutputHIDManager.State.STOPPED;
                Log.i(TAG, "Stopped");
            }
        }
    }

    private void reconnect(){
        while(getState() == State.STOPPED){
            try{
                start();
                Log.i("CHECK_HID", "Reconnecting...");
            }catch (Exception e){
                Log.e("CHECK_HID", "Failed to reconnect: " + e.getMessage());
                try {
                    Thread.sleep(RECONNECT_DELAY); // Add a delay before attempting to reconnect

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
    private void step() throws IOException {
        // Handle incoming data.
        Log.i(TAG, "step");
        byte[] buffer;
        synchronized (mReadBufferLock) {
            buffer = mReadBuffer.array();
        }
        int len = mSerialPort.read(buffer, mReadTimeout);
        if (len > 0) {
            if (DEBUG) {
                Log.d(TAG, "Read data len=" + len);
            }
            final SerialInputOutputHIDManager.Listener listener = getListener();
            if (listener != null) {
                final byte[] data = new byte[len];
                System.arraycopy(buffer, 0, data, 0, len);
                listener.onNewHIDData(data);
            }
        }

        // Handle outgoing data.
        buffer = null;
        synchronized (mWriteBufferLock) {
            len = mWriteBuffer.position();
            if (len > 0) {
                buffer = new byte[len];
                mWriteBuffer.rewind();
                mWriteBuffer.get(buffer, 0, len);
                mWriteBuffer.clear();
            }
        }
        if (buffer != null) {
            if (DEBUG) {
                Log.d(TAG, "Writing data len=" + len);
            }
            mSerialPort.write(buffer, mWriteTimeout);
        }
    }

}
