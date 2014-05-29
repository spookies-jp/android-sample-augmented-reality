package jp.co.spookies.android.ar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jp.co.spookies.android.ar.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

public class TagView extends View implements LocationListener,
		SensorEventListener {
	// 水平視野
	private static final float ANGLE = (float) (Math.toRadians(51.2));
	private int width, height;
	private Paint paint;
	private Paint paintAddressBg;
	Bitmap bitmap;
	private Location location;
	private String address;

	float[] inR = new float[9];
	float[] outR = new float[9];
	float[] I = new float[9];
	private float[] orientation = new float[3];
	private float[] magneticFieldValues = new float[3];
	private float[] accelerometerValues = new float[3];

	private DirectionTags directionTags = null;
	private List<ARTag> tagList = new ArrayList<ARTag>();

	public TagView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(16);
		paint.setColor(Color.WHITE);
		paintAddressBg = new Paint();
		paintAddressBg.setColor(Color.BLACK);
		bitmap = BitmapFactory
				.decodeResource(getResources(), R.drawable.compus);
		initTag();
	}

	/**
	 * タグの設置
	 */
	private void initTag() {

		try {
			InputStream input = getResources().getAssets()
					.open("landmarks.csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					input));
			String line;
			String[] values;
			Bitmap point = BitmapFactory.decodeResource(getResources(),
					R.drawable.point);
			while ((line = buffer.readLine()) != null) {
				values = line.split(",");
				if (values.length == 3) {
					tagList.add(new ARTag(Double.parseDouble(values[1]), Double
							.parseDouble(values[2]), values[0], point));
				}
			}
			buffer.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (address != null) {
			canvas.drawRect(0, 0, width, 24, paintAddressBg);
			canvas.drawText("現在地：　　" + address, 50, 20, paint);
		}
		if (directionTags != null) {
			directionTags.drawDirections(canvas);
		}

		for (ARTag arTag : tagList) {
			arTag.drawTag(canvas);
		}
	}

	/**
	 * ARタグと画面の傾きから画面表示位置の計算
	 * 
	 * @param arTag
	 *            ARTag
	 * @param location
	 *            現在地
	 * @param azimuth
	 *            画面の傾き(azimuth)
	 * @param pitch
	 *            画面の傾き(pitch)
	 */
	private void calcXY(ARTag arTag, Location location, float azimuth,
			float pitch) {
		float[] distance = new float[3];
		// 距離取得
		Location.distanceBetween(location.getLatitude(),
				location.getLongitude(), arTag.getLatitude(),
				arTag.getLongitude(), distance);
		// 角度取得
		double theta = Math.toRadians(distance[1])
				- (azimuth < 0 ? azimuth + Math.PI * 2 : azimuth);
		if (Math.abs(theta) > Math.PI) {
			theta = theta + (theta > 0 ? -2 * Math.PI : 2 * Math.PI);
		}
		// 画面での座標計算
		int x = (int) (width / 2 + width / ANGLE * theta);
		int y = 2 * height / 3 - (int) (distance[0] * Math.tan(pitch) / 800);
		arTag.setPosition(x, y);
	}

	public void onOrientationChanged(float azimuth, float pitch, float roll) {
		if (this.location == null) {
			return;
		}
		final Location location = this.location;

		for (ARTag arTag : directionTags.directions) {
			calcXY(arTag, location, azimuth, pitch);
		}
		for (ARTag arTag : tagList) {
			calcXY(arTag, location, azimuth, pitch);
		}

		invalidate();
	}

	@Override
	public void onLocationChanged(Location location) {
		synchronized (this) {
			if (location == null) {
				return;
			}

			try {
				// 座標から住所の候補取得
				List<Address> addresses = (new Geocoder(getContext()))
						.getFromLocation(location.getLatitude(),
								location.getLongitude(), 1);
				// 住所取得
				address = addressesListToString(addresses);
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.location = location;

			if (directionTags == null) {
				directionTags = new DirectionTags(location.getLatitude(),
						location.getLongitude(), bitmap);
			} else {
				// 方向タグ更新
				directionTags.setDirections(location.getLatitude(),
						location.getLongitude());
			}
		}
	}

	/**
	 * 現在地の住所名の整形
	 * 
	 * @param addresses
	 *            AddressのList
	 * @return 整形後の文字列
	 */
	private String addressesListToString(List<Address> addresses) {
		if (addresses == null || addresses.isEmpty()) {
			return "";
		}
		Address address = addresses.get(0);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
			b.append(address.getAddressLine(i));
		}
		return b.toString();
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.width = w;
		this.height = h;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			for (int i = 0; i < 3; i++) {
				magneticFieldValues[i] = event.values[i] * 0.1f
						+ magneticFieldValues[i] * 0.9f;
			}
			break;
		case Sensor.TYPE_ACCELEROMETER:
			for (int i = 0; i < 3; i++) {
				accelerometerValues[i] = event.values[i] * 0.1f
						+ accelerometerValues[i] * 0.9f;
			}
			break;
		}

		// 傾きセンサーへ変換
		if (SensorManager.getRotationMatrix(inR, I, accelerometerValues,
				magneticFieldValues)) {
			SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_Z,
					SensorManager.AXIS_MINUS_X, outR);
			SensorManager.getOrientation(outR, orientation);
			onOrientationChanged(orientation[0], orientation[1], orientation[2]);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
