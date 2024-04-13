/**
 * MIT License
 *
 * Copyright (c) 2020, 2024 Mark Schmieder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is part of the SigprocToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * SigprocToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/sigproctoolkit
 */
package com.mhschmieder.sigproctoolkit.filter;

public enum ElectronicFilterType {
    HIGH_LOW_PASS, LOW_PASS, HIGH_PASS;

    public static ElectronicFilterType defaultValue() {
        return HIGH_LOW_PASS;
    }

    public final String toPresentationString() {
        switch ( this ) {
        case HIGH_LOW_PASS:
            return "High/Low Pass";
        case LOW_PASS:
            return "Low Pass";
        case HIGH_PASS:
            return "High Pass";
        default:
            final String errMessage = "Unexpected "
                    + this.getClass().getSimpleName() + " " + this;
            throw new IllegalArgumentException( errMessage );
        }
    }

    @Override
    public final String toString() {
        // NOTE: As of Java 6, enums include the underscore in their string
        //  representation, which is a problem for backward-compatibility with
        //  XML parsers. Thus, we need to strip the underscores and replace them
        //  with spaces, to behave like Java 5.
        final String value = super.toString();
        return value.replaceAll( "_", " " );
    }
}
