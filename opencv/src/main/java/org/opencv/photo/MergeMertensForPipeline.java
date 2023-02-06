//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.photo;

import org.opencv.core.Mat;

// C++: class MergeMertensForPipeline

public class MergeMertensForPipeline {

    protected final long nativeObj;
    protected MergeMertensForPipeline(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static MergeMertensForPipeline __fromPtr__(long addr) { return new MergeMertensForPipeline(addr); }

    //
    // C++:  void cv::MergeMertensForPipeline::push(Mat image)
    //

    public void push(Mat image) {
        push_0(nativeObj, image.nativeObj);
    }


    //
    // C++:  void cv::MergeMertensForPipeline::pop()
    //

    public void pop() {
        pop_0(nativeObj);
    }


    //
    // C++:  void cv::MergeMertensForPipeline::process(Mat& dst)
    //

    public void process(Mat dst) {
        process_0(nativeObj, dst.nativeObj);
    }


    //
    // C++:  void cv::MergeMertensForPipeline::release()
    //

    public void release() {
        release_0(nativeObj);
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  void cv::MergeMertensForPipeline::push(Mat image)
    private static native void push_0(long nativeObj, long image_nativeObj);

    // C++:  void cv::MergeMertensForPipeline::pop()
    private static native void pop_0(long nativeObj);

    // C++:  void cv::MergeMertensForPipeline::process(Mat& dst)
    private static native void process_0(long nativeObj, long dst_nativeObj);

    // C++:  void cv::MergeMertensForPipeline::release()
    private static native void release_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
