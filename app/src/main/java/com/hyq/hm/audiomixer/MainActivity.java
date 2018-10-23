package com.hyq.hm.audiomixer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<AudioHolder> list = new ArrayList<>();
    private String[] denied;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioHolder catHolder = new AudioHolder();
        catHolder.setBitRate(128);
        catHolder.setChannelCount(2);
        catHolder.setSampleRate(44100);
        catHolder.setEnd(14.47);
        catHolder.setStart(0);
        catHolder.setFile("/storage/emulated/0/DCIM/Camera/8116e8478fabc40d00fed1fffa22a69b.mp4");
        catHolder.setName("猫");
        list.add(catHolder);
        AudioHolder bagHolder = new AudioHolder();
        bagHolder.setBitRate(1411);
        bagHolder.setSampleRate(44100);
        bagHolder.setChannelCount(2);
        bagHolder.setEnd(14.47);
        bagHolder.setStart(0);
        bagHolder.setFile("/storage/emulated/0/Music/胡彦斌 - 有梦好甜蜜.wav");
        bagHolder.setName("背景");
        list.add(bagHolder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    denied[i] = list.get(i);
                    ActivityCompat.requestPermissions(this, denied, 5);
                }

            } else {
                init();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 5) {
            boolean isDenied = false;
            for (int i = 0; i < denied.length; i++) {
                String permission = denied[i];
                for (int j = 0; j < permissions.length; j++) {
                    if (permissions[j].equals(permission)) {
                        if (grantResults[j] != PackageManager.PERMISSION_GRANTED) {
                            isDenied = true;
                            break;
                        }
                    }
                }
            }
            if (isDenied) {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            } else {
                init();

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private AudioTrack audioTrack;
    private void init(){
        AudioMerge audioMerge = new AudioMerge();
        audioMerge.setEncoderListener(new AudioMerge.OnAudioEncoderListener() {
            @Override
            public void onDecoder(AudioHolder decoderHolder, int progress) {
                Log.d("=================",decoderHolder.getName()+":"+progress);
            }

            @Override
            public void onEncoder(int progress) {

            }

            @Override
            public void onStart(int sampleRate, int channelCount, int bitRate) {
                if(audioTrack == null){
                    int bufferSize = AudioTrack.getMinBufferSize(
                            sampleRate,
                            channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
                    if (bufferSize < channelCount * sampleRate * 2) {
                        bufferSize = channelCount * sampleRate * 2;
                    }
                    audioTrack = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            sampleRate,
                            channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize,
                            AudioTrack.MODE_STREAM);
                    audioTrack.play();
                }

            }

            @Override
            public void onWrite(byte[] audioData, int offsetInBytes, int sizeInBytes, MediaCodec.BufferInfo info) {
                if(audioTrack != null){
                    audioTrack.write(audioData,offsetInBytes,sizeInBytes);
                }
            }

            @Override
            public void onOver(String path) {
                if(audioTrack != null){
                    audioTrack.flush();
                }
            }
        });
        audioMerge.start("",list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(audioTrack != null){
            audioTrack.stop();
            audioTrack.release();
        }
    }
}
