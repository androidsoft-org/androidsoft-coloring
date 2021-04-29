/*
 * Copyright (C) 2010 Peter Dornbach.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.androidsoft.coloring.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import eu.quelltext.images.BlackAndWhiteConversion;

public class FloodFill extends BitmapConverter {
	public static int BORDER_COLOR = Color.BLACK;
	public static int BACKGROUND_COLOR = Color.WHITE;
	private final int color;
	private final eu.quelltext.images.FloodFill floodFill;

	public FloodFill(Bitmap bitmap, int color) {
		super(bitmap);
		this.color = color;
		floodFill = new eu.quelltext.images.FloodFill(getPixelsOfBitmap(), getWidth(), getHeight(), BORDER_COLOR);
	}

	public static Bitmap fill(Bitmap bitmap, int x, int y, int color) {
		FloodFill fill = new FloodFill(bitmap, color);
		fill.fillAt(x, y);
		return fill.getNewBitmap();
	}

	private void fillAt(int x, int y) {
		floodFill.fillAt(x, y, color);
	}

	@Override
	public int[] getPixelsForNewBitmap() {
		return super.getPixelsForNewBitmap();
	}

	public static Bitmap asBlackAndWhite(Bitmap bitmap) {
		BlackAndWhite bin = new BlackAndWhite(bitmap);
		return bin.getNewBitmap();
	}

	public static class BlackAndWhite extends BitmapConverter {

		private final BlackAndWhiteConversion bw;

		public BlackAndWhite(Bitmap bitmap) {
			super(bitmap);
			bw = new BlackAndWhiteConversion(BACKGROUND_COLOR, BORDER_COLOR);
		}

		@Override
		public int[] getPixelsForNewBitmap() {
			int[] pixels = super.getPixelsForNewBitmap();
			bw.toBlackAndWhite(pixels);
			return pixels;
		}
	}
}
