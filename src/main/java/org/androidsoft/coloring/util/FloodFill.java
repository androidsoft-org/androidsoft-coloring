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

import java.util.LinkedList;
import java.util.Queue;

public class FloodFill {
	public static final int BACKGROUND_COLOR = Color.WHITE;
	private static final int BINARY_COLOR_THRESHOLD = 3 * 0xff / 2;
	public static int BORDER_COLOR = Color.BLACK;
	private final int width;
	private final int color;
	private final Queue<Pixel> queue = new LinkedList<>();
	private final int height;
	private final int[] pixels;

	public FloodFill(Bitmap bitmap, int color) {
		// fill a pixel in a bitmap https://stackoverflow.com/a/5916506
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		this.color = color;
	}

	public static Bitmap fill(Bitmap bitmap, int x, int y, int color) {
		FloodFill fill = new FloodFill(bitmap, color);
		fill.fillAt(x, y);
		return fill.createBitmap();
	}

	private Bitmap createBitmap() {
		// create a new bitmap
		// see https://stackoverflow.com/a/10180908
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	private void fillAt(int x, int y) {
		fillPixel(x, y);
		while (!queue.isEmpty()) {
			Pixel p = queue.remove();
			fillPixel(p.x + 1, p.y);
			fillPixel(p.x - 1, p.y);
			fillPixel(p.x, p.y + 1);
			fillPixel(p.x, p.y - 1);
		}
	}

	private void fillPixel(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return;
		}
		int pixelColor = pixels[x + y * width];
		if (pixelColor == color || pixelColor == BORDER_COLOR) {
			return;
		}
		queue.add(new Pixel(x, y));
		pixels[x + y * width] = color;
	}


	private static class Pixel {
		public Pixel(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int x;
		public int y;
	}

	public static Bitmap binarize(Bitmap bitmap) {
		FloodFill fill = new FloodFill(bitmap, BACKGROUND_COLOR);
		fill.binarize();
		return fill.createBitmap();
	}

	private void binarize() {
		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];
			int brightness = (pixel & 0xff) + ((pixel >> 8) & 0xff) + ((pixel >> 16) & 0xff);
			pixels[i] = brightness > BINARY_COLOR_THRESHOLD ? color : BORDER_COLOR;
		}
	}
}
