package com.project.elisabet.appDoctor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.skyfishjy.library.RippleBackground;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ChatWindow extends AppCompatActivity {

    private static final String TAG = "ChatWindow";
    private MediaRecorder mRecorder;
    private ImageButton RecordingPauseButton;
    private MediaPlayer mMediaPlayer;
    private static boolean isRecording = false;
    private static boolean isRecorded = false;
    private File audiofile;
    OutputStream outputStream;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streaming_activity);

        RecordingPauseButton = (ImageButton) findViewById(R.id.recording_button);

        RecordingPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording && !isRecorded){
                    mRecorder.stop();
                    RecordingPauseButton.setImageResource(R.drawable.play);
                    isRecorded = true;
                }
                else if(!isRecording && !isRecorded) {
                    startRecording();
                    RecordingPauseButton.setImageResource(R.drawable.pausa);
                    isRecording = true;
                }
                else if(isRecorded){
                    Uri uri = Uri.parse(audiofile.getAbsolutePath());
                    mMediaPlayer = MediaPlayer.create(v.getContext(),uri);
                    mMediaPlayer.start();
                }
            }
        });

        Socket socket = SocketHandler.getSocket();

        try {
            outputStream = socket.getOutputStream();
            Log.e("OUTPUT_SOCKET", "Éxit");
            startService(new Intent(getApplicationContext(), AudioStreamingService.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.stop();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        File dir = Environment.getExternalStorageDirectory();
        try {
            audiofile = File.createTempFile("sound", ".3gp", dir);
        } catch (IOException e) {
            Log.e(TAG, "Error al escriure el fitxer");
            return;
        }

        mRecorder.setOutputFile(audiofile);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "La grabació ha fallat");
        }
        mRecorder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecorder.stop();
    }
}
