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

package org.androidsoft.coloring.ui.widget;

import android.os.Handler;
import android.os.Message;

public final class Progress {
	public static final int MAX = 100;

	public static final int MESSAGE_INCREMENT_PROGRESS = 1;
	public static final int MESSAGE_DONE_OK = 2;
	public static final int MESSAGE_DONE_ERROR = 3;

	public static final void sendIncrementProgress(Handler h, int diff) {
		Message m = Message.obtain(h, Progress.MESSAGE_INCREMENT_PROGRESS, diff, 0);
		h.sendMessage(m);
	}
}
