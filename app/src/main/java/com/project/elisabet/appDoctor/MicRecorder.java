package com.project.elisabet.appDoctor;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class MicRecorder implements Runnable {
    private static final int SAMPLE_RATE = 16000;
    public static volatile boolean keepRecording = true;

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        Log.e("AUDIO", "buffersize = "+bufferSize);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        try {
            final OutputStream outputStream = SocketHandler.getSocket().getOutputStream();

            final byte[] audioBuffer = new byte[bufferSize];

            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e("AUDIO", "La grabació de laudio no pot començar");
                return;
            }
            record.startRecording();
            Log.e("AUDIO", "Comença a grabar");

            while(keepRecording) {
                Runnable writeToOutputStream = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            outputStream.write(audioBuffer);
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread thread = new Thread(writeToOutputStream);
                thread.start();
            }

            record.stop();
            record.release();
            Log.e("AUDIO", "Streaming parat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
