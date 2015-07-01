package org.opencv.samples.imagemanipulations;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class ExperimentsActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG = "ImageProcessing";

    public static final int VIEW_MODE_CAMERA = 0;
    public static final int VIEW_MODE_HOMOGRAPHY = VIEW_MODE_CAMERA + 1;

    private MenuItem mItemPreviewCamera;
    private MenuItem mItemPreviewHomography;

    private CameraBridgeViewBase mOpenCvCameraView;

    // support structures
    private Mat homographyMat;
    private MatOfPoint2f originalHomoPoints;
    private MatOfPoint2f transformedHomoPoints;

    // current view mode
    public static int viewMode = VIEW_MODE_CAMERA;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ExperimentsActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.experiments_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewCamera = menu.add("Camera");
        mItemPreviewHomography = menu.add("Homography");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewCamera)
            viewMode = VIEW_MODE_CAMERA;
        else if (item == mItemPreviewHomography)
            viewMode = VIEW_MODE_HOMOGRAPHY;
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        // initialize support structures

        // TODO: currently homography points are fixed. we could add some sliders to do this
        // dynamically
        List<Point> originalList = new ArrayList<Point>();
        originalList.add(new Point(0,0));
        originalList.add(new Point(0, height));
        originalList.add(new Point(width, height));
        originalList.add(new Point(width, 0));

        List<Point> transformedList = new ArrayList<Point>();
        transformedList.add(new Point(100, 100));
        transformedList.add(new Point(100, height - 100));
        transformedList.add(new Point(width, height));
        transformedList.add(new Point(width, 0));

        originalHomoPoints = new MatOfPoint2f();
        originalHomoPoints.fromList(originalList);

        transformedHomoPoints = new MatOfPoint2f();
        transformedHomoPoints.fromList(transformedList);

        homographyMat = new Mat();
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (homographyMat != null)
            homographyMat.release();

        homographyMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat rgba = inputFrame.rgba();

        switch (ExperimentsActivity.viewMode) {
            case ExperimentsActivity.VIEW_MODE_CAMERA:
                break;
            case ExperimentsActivity.VIEW_MODE_HOMOGRAPHY:

                Mat hg = Calib3d.findHomography(originalHomoPoints, transformedHomoPoints);

                Imgproc.warpPerspective(rgba, homographyMat,hg, rgba.size());

                rgba = homographyMat;
                break;
        }

        return rgba;
    }
}
