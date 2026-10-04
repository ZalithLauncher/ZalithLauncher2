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

package com.movtery.zalithlauncher.utils.file;

public class InvalidFilenameException extends RuntimeException {
    private final FilenameErrorType type;
    private String illegalCharacters = null;
    private int invalidLength = -1;

    public InvalidFilenameException(String message, String illegalCharacters) {
        super(message);
        this.type = FilenameErrorType.CONTAINS_ILLEGAL_CHARACTERS;
        this.illegalCharacters = illegalCharacters;
    }

    public InvalidFilenameException(String message, int invalidLength) {
        super(message);
        this.type = FilenameErrorType.INVALID_LENGTH;
        this.invalidLength = invalidLength;
    }

    public InvalidFilenameException(String message, @SuppressWarnings("unused") boolean isLeadingOrTrailingSpace) {
        super(message);
        this.type = FilenameErrorType.LEADING_OR_TRAILING_SPACE;
    }

    public boolean containsIllegalCharacters() {
        return type == FilenameErrorType.CONTAINS_ILLEGAL_CHARACTERS;
    }

    public String getIllegalCharacters() {
        return illegalCharacters;
    }

    public boolean isInvalidLength() {
        return type == FilenameErrorType.INVALID_LENGTH;
    }

    public int getInvalidLength() {
        return invalidLength;
    }

    public boolean isLeadingOrTrailingSpace() {
        return type == FilenameErrorType.LEADING_OR_TRAILING_SPACE;
    }

    private enum FilenameErrorType {
        /**
         * 包含非法字符
         */
        CONTAINS_ILLEGAL_CHARACTERS,
        /**
         * 长度不合法
         */
        INVALID_LENGTH,
        /**
         * 以空格开头或结尾
         */
        LEADING_OR_TRAILING_SPACE
    }
}
