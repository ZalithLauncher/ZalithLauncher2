/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.bridge;

import androidx.annotation.Keep;

/**
 * Singleton class made to log on one file
 * The singleton part can be removed but will require more implementation from the end-dev
 * <a href="https://github.com/PojavLauncherTeam/PojavLauncher/blob/f1cb9e6/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Logger.java">Modified from PojavLauncher</a>
 */
@Keep
public final class LoggerBridge {
    /** Reset the log file, effectively erasing any previous logs */
    @Keep public static native void start(String filePath);

    /** Print the text to the log file if not censored */
    @Keep public static native void append(String log);

    /** Link a log listener to the logger */
    @Keep public static native void setListener(EventLogListener listener);

    /** Small listener for anything listening to the log */
    @Keep
    public interface EventLogListener {
        @Keep
        void onEventLogged(String text);
    }

    public static void appendTitle(String title) {
        String logText = "==================== " + title + " ====================";
        append(logText);
    }

    public static void appendInfo(String info) {
        String logText = "▷ " + info;
        append(logText);
    }

    static {
        NativeLibraryLoader.loadPojavLib();
    }
}
