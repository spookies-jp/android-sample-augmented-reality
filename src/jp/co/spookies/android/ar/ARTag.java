package jp.co.spookies.android.ar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ARTag {
	private double latitude;
	private double longitude;
	private String name;
	private Bitmap bitmap;
	private int x = -9999, y = -9999;
	private Paint paint = new Paint();

	public ARTag(double latitude, double longitude, String name, Bitmap drawer) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.bitmap = drawer;

		paint.setAntiAlias(true);
		paint.setTextSize(12);
		paint.setColor(Color.WHITE);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	/**
	 * 経度・緯度の設定
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * タグの描画
	 * 
	 * @param canvas
	 *            draw先のCanvas
	 */
	public void drawTag(Canvas canvas) {
		canvas.drawBitmap(bitmap, x, y, null);
		canvas.drawText(name, x + bitmap.getWidth(), y + bitmap.getHeight(),
				paint);
	}
}
