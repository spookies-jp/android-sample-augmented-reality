package jp.co.spookies.android.ar;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * ８方位のタグを管理するクラス
 * 
 */
public class DirectionTags {
	public ARTag[] directions;

	public DirectionTags(double latitude, double longitude, Bitmap bitmap) {
		directions = new ARTag[] {
				new ARTag(latitude + 0.1, longitude + 0.0, "北", bitmap),
				new ARTag(latitude + 0.1, longitude + 0.1, "北東", bitmap),
				new ARTag(latitude + 0.0, longitude + 0.1, "東", bitmap),
				new ARTag(latitude - 0.1, longitude + 0.1, "南東", bitmap),
				new ARTag(latitude - 0.1, longitude + 0.0, "南", bitmap),
				new ARTag(latitude - 0.1, longitude - 0.1, "南西", bitmap),
				new ARTag(latitude + 0.0, longitude - 0.1, "西", bitmap),
				new ARTag(latitude + 0.1, longitude - 0.1, "北西", bitmap) };
	}

	/**
	 * 現在位置から方向タグを設置しなおす
	 * 
	 * @param latitude
	 *            現在位置の緯度
	 * @param longitude
	 *            現在位置の経度
	 */
	public void setDirections(double latitude, double longitude) {
		directions[0].setLocation(latitude + 0.1, longitude + 0.0);
		directions[1].setLocation(latitude + 0.1, longitude + 0.1);
		directions[2].setLocation(latitude + 0.0, longitude + 0.1);
		directions[3].setLocation(latitude - 0.1, longitude + 0.1);
		directions[4].setLocation(latitude - 0.1, longitude + 0.0);
		directions[5].setLocation(latitude - 0.1, longitude - 0.1);
		directions[6].setLocation(latitude + 0.0, longitude - 0.1);
		directions[7].setLocation(latitude + 0.1, longitude - 0.1);
	}

	/**
	 * 方向タグの描画
	 * 
	 * @param canvas
	 *            draw先のCanvas
	 */
	public void drawDirections(Canvas canvas) {
		for (ARTag arTag : directions) {
			arTag.drawTag(canvas);
		}
	}
}
