#include <jni.h>
#include <vector>
#include "opencv2/core.hpp"

using namespace cv;

inline static unsigned int calculateLuminance(const Point3_<uint8_t>* pixel) {
    return (int)(0.299 * (int)pixel->x + 0.587 * (int)pixel->y + (int)0.114 * (int)pixel->z);
}

static
void Mat_to_vector_Mat(Mat &mat, std::vector<Mat> &v_mat) {
    v_mat.clear();
    if (mat.type() == CV_32SC2 && mat.cols == 1) {
        v_mat.reserve(mat.rows);
        for (int i = 0; i < mat.rows; i++) {
            Vec<int, 2> a = mat.at<Vec<int, 2> >(i, 0);
            long long addr = (((long long) a[0]) << 32) | (a[1] & 0xffffffff);
            Mat &m = *((Mat *) addr);
            v_mat.push_back(m);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_dan_timelapse_images_ImageTools_00024Companion_mergeLightestPixelsNative(JNIEnv *env,
                                                                                  jobject thiz,
                                                                                  jlong images_nativeObj,
                                                                                  jlong output_nativeObj) {
    Mat &imagesAsMat = *((Mat *) images_nativeObj);
    std::vector<Mat> images;
    Mat_to_vector_Mat(imagesAsMat, images);
    int size = images.size();

    Mat &output = *((Mat *) output_nativeObj);

    output.create(images[0].size(), images[0].type());

    output.forEach<Point3_<uint8_t>>(
    [images, size](Point3_<uint8_t>& pixel, const int *position) -> void {
        const Point3_<uint8_t> *srcPixel = NULL;
        int luminance;
        const Point3_<uint8_t> *bestSrcPixel = (const Point3_<uint8_t>*)images[0].ptr(position);
        int bestLuminance = calculateLuminance(bestSrcPixel);

        for(int imageIndex = 1; imageIndex < size; imageIndex++) {
            srcPixel = (const Point3_<uint8_t>*)images[imageIndex].ptr(position);
            luminance = calculateLuminance(srcPixel);
            if (luminance > bestLuminance) {
                bestLuminance = luminance;
                bestSrcPixel = srcPixel;
            }
        }

        pixel = *bestSrcPixel;
    });
}