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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class FloodFill {
	public interface PixelMatcher {
		// Return true if the pixel at x,y should be filled. It must be prepared
		// to handle x < 0, y < 0. It must return false for a pixel already set.
		boolean match(int x, int y);
	}

	public interface PixelSetter {
		// Set the a row of pixels from x1 until x2 in row y.
		// x1 included, x2 excluded.
		void set(int x1, int x2, int y);
	}

	// The "nice" fill that works with any PixelMatcher and PixelSetter.
	public static void fill(int x, int y, PixelMatcher matcher, PixelSetter setter) {
		Queue<Pixel> queue = new LinkedList<Pixel>();
		queue.add(new Pixel(x, y));
		while (!queue.isEmpty()) {
			Pixel p = queue.remove();
			int px1 = p._x;
			int px2 = p._x;
			int py = p._y;
			if (matcher.match(px1, py)) {
				while (matcher.match(px1, py))
					px1--;
				px1++;
				while (matcher.match(px2, py))
					px2++;
				boolean prevMatchUp = false;
				boolean prevMatchDn = false;
				setter.set(px1, px2, py);
				for (int px = px1; px < px2; px++) {
					boolean matchUp = matcher.match(px, py - 1);
					if (matchUp && !prevMatchUp)
						queue.add(new Pixel(px, py - 1));
					boolean matchDn = matcher.match(px, py + 1);
					if (matchDn && !prevMatchDn)
						queue.add(new Pixel(px, py + 1));
					prevMatchUp = matchUp;
					prevMatchDn = matchDn;
				}
			}
		}
	}

	// The plain dumb ugly fill - but because it's fast we use this one.
	// @param mask 1 if the pixel can be filled, 0 if it cannot. Must contain
	//             exactly width * height pixels.
	// @param pixels the array where we fill matching pixels with color.
	// @param x, y we start the fill here.
	public static void fillRaw(int x, int y, int width, int height, byte[] mask,
			int[] pixels, int color) {
		Queue<Pixel> queue = new LinkedList<Pixel>();
		queue.add(new Pixel(x, y));
		while (!queue.isEmpty()) {
			Pixel p = queue.remove();
			int px1 = p._x;
			int px2 = p._x;
			int py = p._y;
			int pp = py * width;

			if (mask[pp + px1] != 0) {
				while (px1 >= 0 && mask[pp + px1] != 0)
					px1--;
				px1++;
				while (px2 < width && mask[pp + px2] != 0)
					px2++;
				Arrays.fill(pixels, pp + px1, pp + px2, color);
				Arrays.fill(mask, pp + px1, pp + px2, (byte) 0);
				boolean prevMatchUp = false;
				boolean prevMatchDn = false;
				int ppUp = pp - width;
				int ppDn = pp + width;
				for (int px = px1; px < px2; px++) {
					if (py > 0) {
						boolean matchUp = mask[ppUp + px] != 0;
						if (matchUp && !prevMatchUp)
							queue.add(new Pixel(px, py - 1));
						prevMatchUp = matchUp;
					}
					if (py + 1 < height) {
						boolean matchDn = mask[ppDn + px] != 0;
						if (matchDn && !prevMatchDn)
							queue.add(new Pixel(px, py + 1));
						prevMatchDn = matchDn;
					}
				}
			}
		}
	}

	private static class Pixel {
		public Pixel(int x, int y) {
			_x = x;
			_y = y;
		}

		public int _x;
		public int _y;
	}
}
