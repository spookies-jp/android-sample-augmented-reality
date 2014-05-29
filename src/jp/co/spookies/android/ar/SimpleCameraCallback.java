package jp.co.spookies.android.ar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.SurfaceHolder;

public class SimpleCameraCallback implements SurfaceHolder.Callback {
	protected Camera camera = null;
	private Camera.Parameters params = null;
	private boolean timerLock = false;
	private Context context = null;
	private final File path = new File(
			Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
			"SimpleCamera");
	private final SimpleDateFormat format = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS'.jpg'");
	private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (jpegCallback != null) {
				jpegCallback.onPictureTaken(data, camera);
			}
			timerLock = false;
		}
	};
	private Camera.PictureCallback jpegCallback;
	private final Camera.PictureCallback defaultJpegCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File file = new File(path, format.format(new Date(System
					.currentTimeMillis())));
			try {
				path.mkdirs();
				FileOutputStream out = new FileOutputStream(file);
				out.write(data);
				out.close();
				MediaScannerConnection.scanFile(context,
						new String[] { file.toString() }, null, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (camera != null) {
				camera.startPreview();
			}
			timerLock = false;
		}
	};

	public SimpleCameraCallback(Context context) {
		this.context = context;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		params = camera.getParameters();

		// プレビューサイズ設定
		List<Camera.Size> supportedPreviewSizes = params
				.getSupportedPreviewSizes();
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size s1, Camera.Size s2) {
				return s1.width - s2.width;
			}
		});
		Camera.Size previewSize = supportedPreviewSizes.get(0);
		for (Camera.Size size : supportedPreviewSizes) {
			if (size.width > width) {
				break;
			}
			previewSize = size;
		}
		params.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(params);
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (camera == null) {
			camera = Camera.open();
		}
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			camera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public void takePicture() {
		takePicture(null, null, defaultJpegCallback, 0, null);
	}

	public void takePicture(Camera.ShutterCallback shutter,
			Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
		takePicture(shutter, raw, jpeg, 0, null);
	}

	public void takePicture(int time, ISelfTimerCallback callback) {
		takePicture(null, null, defaultJpegCallback, time, callback);
	}

	public void takePicture(Camera.ShutterCallback shutter,
			Camera.PictureCallback raw, Camera.PictureCallback jpeg, int time,
			ISelfTimerCallback callback) {
		if (timerLock) {
			return;
		}
		timerLock = true;
		for (int i = 0; i < time; i++) {
			if (callback != null) {
				callback.countDown(time - i);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (callback != null) {
			callback.countDown(0);
		}
		if (camera != null) {
			jpegCallback = jpeg;
			camera.takePicture(shutter, raw, pictureCallback);
		}
	}

	public List<String> getSupportedColorEffects() {
		return params.getSupportedColorEffects();
	}

	public void setColorEffect(String value) {
		if (getSupportedColorEffects().contains(value)) {
			params.setColorEffect(value);
			camera.setParameters(params);
		}
	}

	public List<String> getSupportedFlashModes() {
		return params.getSupportedFlashModes();
	}

	public void setFlashMode(String value) {
		if (getSupportedFlashModes().contains(value)) {
			params.setFlashMode(value);
			camera.setParameters(params);
		}
	}

	public List<String> getSupportedFocusModes() {
		return params.getSupportedFocusModes();
	}

	public void setFocusMode(String value) {
		if (getSupportedFocusModes().contains(value)) {
			params.setFocusMode(value);
			camera.setParameters(params);
		}
	}

	public List<String> getSupportedSceneModes() {
		return params.getSupportedSceneModes();
	}

	public void setSceneMode(String value) {
		if (getSupportedSceneModes().contains(value)) {
			params.setSceneMode(value);
			camera.setParameters(params);
		}
	}

	public List<String> getSupportedWhiteBalance() {
		return params.getSupportedWhiteBalance();
	}

	public void setWhiteBalance(String value) {
		if (getSupportedWhiteBalance().contains(value)) {
			params.setWhiteBalance(value);
			camera.setParameters(params);
		}
	}

	public float getHorizontalViewAngle() {
		return params.getHorizontalViewAngle();
	}

	/*
	 * セルフタイマーのカウントダウン用コールバックインターフェース
	 */
	public interface ISelfTimerCallback {
		public void countDown(int time);
	}
}
