package com.example.malaria_detector;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.support.common.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class classifierActivity extends AsyncTask<Activity, Void, float[][]> {
    private MappedByteBuffer tflitemodel;
    private List<String> labels;
    static Bitmap bitmap;
    private float[][] resultArray;
    static float[] ans;

    @Override
    protected float[][] doInBackground(Activity... activitys) {
        for(int i=0;i<activitys.length;i++)
            classifier(activitys[i]);
        return resultArray;
    }

    protected void onPostExecute(float[][] result){
        ans=resultArray[0];
    }

    private final MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor assetFileDescriptor = activity.getAssets().openFd("model.tflite");
        AssetFileDescriptor fileDescriptor = assetFileDescriptor;
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void classifier(Activity activity){
        resultArray = new float[1][];
        float[] var9 = new float[2];
        resultArray[0] = var9;

        try {
            tflitemodel = loadModelFile(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Interpreter.Options tfliteOptions = new Interpreter.Options();
        Interpreter tflite = new Interpreter(tflitemodel);
 //       try {
//            labels = FileUtil.loadLabels(activity,"tflitelabel.txt");
//        } catch (IOException e) {
//            e.printStackTrace();
   //     }

        ByteBuffer modelInput=loadImage();
        tflite.run(modelInput,resultArray);
        System.out.println("Test Accuracy:"+resultArray[0][0]);
        System.out.println("Test Accuracy:"+resultArray[0][1]);

        /**int imageTensorIndex = 0;
         int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape();
         imageSizeX =imageShape[1];
         imageSizeY = imageShape[2];
         DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
         int probabilityTensorIndex = 0;
         int[] probabilityShape =
         tflite.getOutputTensor(probabilityTensorIndex).shape();
         DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();
         inputImageBuffer = new TensorImage(imageDataType);

         outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape,probabilityDataType);

         probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0.0f,1.0f)).build();
         */
    }

    private ByteBuffer loadImage(){

        final int channelSize = 3;
        int inputImageWidth = 100;
        int inputImageHeight = 100;
        int modelInputSize = 100*100*3;

        //inputImageBuffer.load(bitmap);

        //int cropSize = Math.min(bitmap.getWidth(),bitmap.getHeight());
        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
        ByteBuffer modelInput = convertBitmapToByteBuffer(resizedImage);


        /**ImageProcessor imageProcessor = new ImageProcessor.Builder()
         .add(new ResizeWithCropOrPadOp(cropSize,cropSize))
         .add(new ResizeOp(imageSizeX,imageSizeY,ResizeMethod.BILINEAR))
         .add(new Rot90Op(0))
         .add(new NormalizeOp(127.5f,127.5f))
         .build();*/
        return modelInput;
    }


    private final ByteBuffer convertBitmapToByteBuffer(Bitmap newBitmap) {

        int DIM_IMG_SIZE_X = 100;
        final int DIM_IMG_SIZE_Y = 100;
        int DIM_BATCH_SIZE = 1;
        int DIM_PIXEL_SIZE = 3;
        int IMAGE_MEAN = 128;
        float IMAGE_STD = 128.0F;
        int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * DIM_BATCH_SIZE * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        //byteBuffer.rewind();
        newBitmap.getPixels(intValues, 0, newBitmap.getWidth(), 0, 0, newBitmap.getWidth(), newBitmap.getHeight());
        int pixel = 0;

        for(byte i = 0;i < DIM_IMG_SIZE_X; ++i) {
            for(byte j = 0;j < DIM_IMG_SIZE_Y; ++j) {
                int currPixel = intValues[pixel++];

                byteBuffer.putFloat((float)((currPixel >> 16 & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                byteBuffer.putFloat((float)((currPixel >> 8 & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                byteBuffer.putFloat((float)((currPixel & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            }
        }
        return byteBuffer;
    }
}
