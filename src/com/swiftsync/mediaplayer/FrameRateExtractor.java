package com.swiftsync.mediaplayer;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class FrameRateExtractor {
    public static double getFrameRate(String filePath) {
        IContainer container = IContainer.make();
        if (container.open(filePath, IContainer.Type.READ, null) < 0)
            return 0;

        int numStreams = container.getNumStreams();

        for (int i = 0; i < numStreams; i++) {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                return coder.getFrameRate().getDouble();
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(FrameRateExtractor.getFrameRate("C:\\Users\\noahl\\Desktop\\Thailand Drone\\DJI_0078.MP4"));
    }

}