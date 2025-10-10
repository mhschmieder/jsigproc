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

import com.mhschmieder.sigproctoolkit.dsp.DigitalFilterUtilities;

import java.util.Locale;

public enum HighLowPassFilterType {
    SECOND_ORDER_HIGH_PASS,
    ELLIPTICAL_HIGH_PASS,
    BUTTERWORTH_1_HIGH_PASS,
    BUTTERWORTH_2_HIGH_PASS,
    BUTTERWORTH_3_HIGH_PASS,
    BUTTERWORTH_4_HIGH_PASS,
    BUTTERWORTH_5_HIGH_PASS,
    BUTTERWORTH_6_HIGH_PASS,
    BUTTERWORTH_7_HIGH_PASS,
    BUTTERWORTH_8_HIGH_PASS,
    LINKWITZ_RILEY_2_HIGH_PASS,
    LINKWITZ_RILEY_4_HIGH_PASS,
    LOW_PASS,
    BUTTERWORTH_1_LOW_PASS,
    BUTTERWORTH_2_LOW_PASS,
    BUTTERWORTH_3_LOW_PASS,
    BUTTERWORTH_4_LOW_PASS,
    BUTTERWORTH_5_LOW_PASS,
    BUTTERWORTH_6_LOW_PASS,
    BUTTERWORTH_7_LOW_PASS,
    BUTTERWORTH_8_LOW_PASS,
    LINKWITZ_RILEY_2_LOW_PASS,
    LINKWITZ_RILEY_4_LOW_PASS;

    public static HighLowPassFilterType defaultValue() {
        return LOW_PASS;
    }

    public static HighLowPassFilterType presentationValueOf( final String highLowPassFilterType,
                                                             final boolean highPass ) {
        // Cover legacy cases, as we changed terminology at some point.
        if ( highLowPassFilterType == null ) {
            return defaultValue();
        }
        else if ( "2nd order high pass"
                .equalsIgnoreCase( highLowPassFilterType )
                || "highpass".equalsIgnoreCase( highLowPassFilterType ) ) {
            return SECOND_ORDER_HIGH_PASS;
        }
        else if ( "elliptical high pass"
                .equalsIgnoreCase( highLowPassFilterType )
                || "ellipticalhighpass"
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return ELLIPTICAL_HIGH_PASS;
        }
        else if ( "low pass".equalsIgnoreCase( highLowPassFilterType )
                || "lowpass".equalsIgnoreCase( highLowPassFilterType ) ) {
            return LOW_PASS;
        }
        else if ( ( "Butterworth 6"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_1_HIGH_PASS : BUTTERWORTH_1_LOW_PASS;
        }
        else if ( ( "Butterworth 12"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_2_HIGH_PASS : BUTTERWORTH_2_LOW_PASS;
        }
        else if ( ( "Butterworth 18"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_3_HIGH_PASS : BUTTERWORTH_3_LOW_PASS;
        }
        else if ( ( "Butterworth 24"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_4_HIGH_PASS : BUTTERWORTH_4_LOW_PASS;
        }
        else if ( ( "Butterworth 30"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_5_HIGH_PASS : BUTTERWORTH_5_LOW_PASS;
        }
        else if ( ( "Butterworth 36"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_6_HIGH_PASS : BUTTERWORTH_6_LOW_PASS;
        }
        else if ( ( "Butterworth 42"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_7_HIGH_PASS : BUTTERWORTH_7_LOW_PASS;
        }
        else if ( ( "Butterworth 48"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? BUTTERWORTH_8_HIGH_PASS : BUTTERWORTH_8_LOW_PASS;
        }
        else if ( ( "Linkwitz-Riley 12"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? LINKWITZ_RILEY_2_HIGH_PASS : LINKWITZ_RILEY_2_LOW_PASS;
        }
        else if ( ( "Linkwitz-Riley 24"
                + DigitalFilterUtilities.FILTER_SLOPE_UNITS )
                        .equalsIgnoreCase( highLowPassFilterType ) ) {
            return highPass ? LINKWITZ_RILEY_4_HIGH_PASS : LINKWITZ_RILEY_4_LOW_PASS;
        }
        else {
            return valueOf( highLowPassFilterType.toUpperCase( Locale.ENGLISH ) );
        }
    }

    public final String toPresentationString() {
        switch ( this ) {
        case SECOND_ORDER_HIGH_PASS:
            return "2nd Order High Pass";
        case ELLIPTICAL_HIGH_PASS:
            return "Elliptical High Pass";
        case LOW_PASS:
            return "Low Pass";
        case BUTTERWORTH_1_LOW_PASS:
        case BUTTERWORTH_1_HIGH_PASS:
            return "Butterworth 6"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_2_LOW_PASS:
        case BUTTERWORTH_2_HIGH_PASS:
            return "Butterworth 12"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_3_LOW_PASS:
        case BUTTERWORTH_3_HIGH_PASS:
            return "Butterworth 18"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_4_LOW_PASS:
        case BUTTERWORTH_4_HIGH_PASS:
            return "Butterworth 24"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_5_LOW_PASS:
        case BUTTERWORTH_5_HIGH_PASS:
            return "Butterworth 30"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_6_LOW_PASS:
        case BUTTERWORTH_6_HIGH_PASS:
            return "Butterworth 36"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_7_LOW_PASS:
        case BUTTERWORTH_7_HIGH_PASS:
            return "Butterworth 42"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case BUTTERWORTH_8_LOW_PASS:
        case BUTTERWORTH_8_HIGH_PASS:
            return "Butterworth 48"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case LINKWITZ_RILEY_2_LOW_PASS:
        case LINKWITZ_RILEY_2_HIGH_PASS:
            return "Linkwitz-Riley 12"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
        case LINKWITZ_RILEY_4_LOW_PASS:
        case LINKWITZ_RILEY_4_HIGH_PASS:
            return "Linkwitz-Riley 24"
                    + DigitalFilterUtilities.FILTER_SLOPE_UNITS;
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
