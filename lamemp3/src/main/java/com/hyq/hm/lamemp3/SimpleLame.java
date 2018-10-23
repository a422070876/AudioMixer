package com.hyq.hm.lamemp3;


/**
 * Created by clam314 on 2017/3/26
 */

public class SimpleLame {
    static {
        System.loadLibrary("SimpleLame");
    }
    /**
     * pcm文件转换mp3函数
     */
    public static native void convert(OnSimpleLameListener listener,String jwav, String jmp3,
                                      int inSampleRate, int outChannel, int outSampleRate, int outBitrate,
                                      int quality);
    public interface OnSimpleLameListener{
        void setProgress(long size,long total);
    }
}
