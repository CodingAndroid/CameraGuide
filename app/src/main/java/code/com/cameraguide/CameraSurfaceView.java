package code.com.cameraguide;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

/**
 * Created by lihui1 on 2017/12/28.
 */

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback{

    private Context mContext;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mScreenWidth;
    private int mScreenHeight;

    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getScreenMetrix(context);
        initView();
    }

    private void initView() {
        mHolder = getHolder();/*获得SurfaceHolder的引用*/
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);/*设置类型*/
    }

    private void getScreenMetrix(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mCamera == null){
            mCamera = Camera.open();/*开启相机*/
            try {
                mCamera.setPreviewDisplay(mHolder);/*摄像头画面显示在surface上*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();/*停止预览*/
        mCamera.release();/*释放相机资源*/
        mCamera = null;
        mHolder = null;
    }

    /**
     * 设置相机参数
     * @param camera
     * @param width
     * @param height
     */
    private void setCameraParams(Camera camera, int width, int height){
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : sizeList){

        }

        Camera.Size picSize = getProperSize(sizeList, ((float) height/width));
        if (null == picSize){
            picSize = parameters.getPictureSize();
        }
        float w = picSize.width;
        float h = picSize.height;
        parameters.setPictureSize(picSize.width, picSize.height);
        this.setLayoutParams(new FrameLayout.LayoutParams((int)(height*(h/w)), height));

        List<Camera.Size> previewList = parameters.getSupportedPreviewSizes();
        for (Camera.Size size:previewList){

        }

        Camera.Size preSize = getProperSize(previewList, ((float)height)/width);
        if (null != preSize){
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        parameters.setJpegQuality(100);/*设置照片质量*/
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); /*连续对焦模式*/
        }
        mCamera.cancelAutoFocus();/*自动对焦*/
        mCamera.setDisplayOrientation(90);/*设置previewDisplay的方向*/
        mCamera.setParameters(parameters);
    }

    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio){
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList){
            float currentRatio = ((float)size.width) / size.height;
            if (currentRatio - screenRatio == 0){
                result = size;
                break;
            }
        }
        if (null == result){
            for (Camera.Size size : pictureSizeList){
                float curRatio = ((float)size.width) / size.height;
                if (curRatio == 4f / 3){
                    result = size;
                    break;
                }
            }
        }
        return result;
    }
}
