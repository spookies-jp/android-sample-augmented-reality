package jp.co.spookies.android.ar;

import jp.co.spookies.android.ar.R;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class AugmentedRealityActivity extends Activity {
	private SimpleCameraCallback camera;

	public static final int MIN_TIME = 1000;
	public static final int MIN_DISTANCE = 100;
	public static final float MIN_ACCURACY = 100f;

	private SensorManager sensorManager;
	private LocationManager locationManager;
	private TagView tagView;

	private LocationListener coarseLocationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			tagView.onLocationChanged(location);
			if (location.getAccuracy() < MIN_ACCURACY) { // 精度がよければlistenerを削除
				locationManager.removeUpdates(coarseLocationListener);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.ar);

		// センサーの準備
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// カメラの準備
		SurfaceView cameraView = (SurfaceView) findViewById(R.id.ar_camera);
		camera = new SimpleCameraCallback(this);
		cameraView.getHolder().addCallback(camera);
		cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		tagView = (TagView) findViewById(R.id.tag_view);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Sensor sensor;
		// 磁気センサー
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensor != null) {
			sensorManager.registerListener(tagView, sensor,
					SensorManager.SENSOR_DELAY_UI);
		}

		// 加速度センサー
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor != null) {
			sensorManager.registerListener(tagView, sensor,
					SensorManager.SENSOR_DELAY_UI);
		}

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// GPS
		String bestProvider = locationManager.getBestProvider(criteria, true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		// NETWORK
		String coarseProvider = locationManager.getBestProvider(criteria, true);

		// GPSのリスナー登録
		if (bestProvider != null) {
			locationManager.requestLocationUpdates(bestProvider, MIN_TIME,
					MIN_DISTANCE, tagView);
		}
		// NETWORKのリスナー登録
		if (coarseProvider != null) {
			locationManager.requestLocationUpdates(coarseProvider, 0, 0,
					coarseLocationListener);
		}

		// 使える位置情報のセンサーがないとき
		if (bestProvider == null && coarseProvider == null) {
			Toast.makeText(this, "位置情報が取得できません", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// 登録したセンサーを全て解除
		sensorManager.unregisterListener(tagView);
		locationManager.removeUpdates(tagView);
		locationManager.removeUpdates(coarseLocationListener);
	}
}