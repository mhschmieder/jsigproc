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
 * This file is part of the JSigproc Library
 *
 * You should have received a copy of the MIT License along with the
 * JSigproc Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/jsigproc
 */
package com.mhschmieder.jsigproc.dsp;

import com.mhschmieder.jcommons.lang.NumberUtilities;
import com.mhschmieder.mathtoolkit.MathConstants;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

/**
 * General utilities for working with frequency signals in the digital domain.
 * These are fairly common methods related to digital filter design but also
 * used in digital signal processing for downstream analysis of many different
 * audio sources in the sciences and in various fields of engineering, so it
 * seems appropriate to provide them in a top-level commons library.
 */
public class DigitalFilterUtilities {

    public static final String FILTER_SLOPE_UNITS                   = " dB/Octave"; //$NON-NLS-1$

    public static final int    BUTTERWORTH_DB_TO_FILTER_ORDER_RATIO = 6;

    public static final int    NUMBER_OF_QUAD_COEFFICIENTS          = 3;

    public static final int    NUMBER_OF_POLES_PER_BIQUAD           = 2;

    // Convert a frequency (in Hertz) to the z-Domain (digital).
    public static Complex convertFrequencyToZDomain( final double poleFrequencyHz,
                                                     final double samplingFrequencyHz ) {
        // Theta is the angle to the pole frequency, in the z-plane.
        final double poleAngleRadians = getPoleAngleRadians( poleFrequencyHz, samplingFrequencyHz );
        final Complex z = new Complex( FastMath.cos( poleAngleRadians ), FastMath.sin( poleAngleRadians ) );
        return z;
    }

    // Get the bilinear transformation given coefficients in the analog domain.
    // See http://en.wikipedia.org/wiki/Bilinear_transform for details.
    public static Complex getBilinearTransform( final Complex z,
                                                final Complex zSquared,
                                                final double w,
                                                final int biquadSectionCount,
                                                final double[][][] analogCoefficients ) {
        // This is the most general bilinear transform for up to four biquad
        // sections (including pre-warping). It includes all twenty four
        // coefficients (three paired coefficients per biquad section).
        final double eQOmega = w;
        final double eQSin = FastMath.sin( eQOmega );
        final double eQCos = FastMath.cos( eQOmega );
        final double oneMinusCos = 1.0d - eQCos;
        final double onePlusCos = 1.0d + eQCos;

        // Calculate the frequency response for the given breakpoint.
        Complex result = Complex.ONE;
        for ( int i = 0; i < biquadSectionCount; i++ ) {
            // Get a set of digital domain biquad coefficients.
            final double[][] digitalBiquadCoefficients =
                                                       getDigitalBiquadCoefficients( eQSin,
                                                                                     oneMinusCos,
                                                                                     onePlusCos,
                                                                                     analogCoefficients[ i ] );

            // Get the digital biquad filter for this section.
            result = result
                    .multiply( getDigitalBiquadFilter( z, zSquared, digitalBiquadCoefficients ) );
        }

        return result;
    }

    // Get the filter slope order to filter slope dB mapping (relevant to
    // Butterworth-based filters; including Linkwitz-Riley filters).
    public static int getButterworthFilterSlopeDb( final short butterworthFilterSlopeOrder ) {
        final int butterworthFilterSlopeDb = butterworthFilterSlopeOrder
                * BUTTERWORTH_DB_TO_FILTER_ORDER_RATIO;
        return butterworthFilterSlopeDb;
    }

    // Get the filter slope order to filter slope label mapping (relevant to
    // Butterworth-based filters; including Linkwitz-Riley filters).
    public static String getButterworthFilterSlopeLabel( final short butterworthFilterSlopeOrder ) {
        final int butterworthFilterSlopeDb =
                                           getButterworthFilterSlopeDb( butterworthFilterSlopeOrder );
        final String butterworthFilterSlopeLabel = getFilterSlopeLabel( butterworthFilterSlopeDb );
        return butterworthFilterSlopeLabel;
    }

    // Get the filter slope dB to filter slope order mapping (relevant to
    // Butterworth-based filters; including Linkwitz-Riley filters).
    public static short getButterworthFilterSlopeOrder( final int butterworthFilterSlopeDb ) {
        final short butterworthFilterSlopeOrder = ( short ) FastMath
                .floor( butterworthFilterSlopeDb / BUTTERWORTH_DB_TO_FILTER_ORDER_RATIO );
        return butterworthFilterSlopeOrder;
    }

    // Get the filter slope label to filter slope order mapping (relevant to
    // Butterworth-based filters; including Linkwitz-Riley filters).
    public static short getButterworthFilterSlopeOrder( final String butterworthFilterSlopeLabel ) {
        final int butterworthFilterSlopeDb = getFilterSlopeDb( butterworthFilterSlopeLabel );
        final short butterworthFilterSlopeOrder =
                                                getButterworthFilterSlopeOrder( butterworthFilterSlopeDb );
        return butterworthFilterSlopeOrder;
    }

    // Get the digital biquad coefficients for a Butterworth filter.
    // See http://en.wikipedia.org/wiki/Butterworth_filter for details.
    public static double[][] getDigitalBiquadCoefficients( final double eQSin,
                                                           final double oneMinusCos,
                                                           final double onePlusCos,
                                                           final double[][] analogCoefficients ) {
        // Calculate a full set of digital domain biquad coefficients.
        final double[][] digitalBiquadCoefficients =
                                                   new double[ NUMBER_OF_POLES_PER_BIQUAD ][ NUMBER_OF_QUAD_COEFFICIENTS ];

        for ( int i = 0; i < NUMBER_OF_POLES_PER_BIQUAD; i++ ) {
            digitalBiquadCoefficients[ i ] =
                                           getDigitalBiquadPoleCoefficients( eQSin,
                                                                             oneMinusCos,
                                                                             onePlusCos,
                                                                             analogCoefficients[ i ] );
        }

        return digitalBiquadCoefficients;
    }

    // Get the bilinear transformation given coefficients in the digital domain.
    // See http://en.wikipedia.org/wiki/Digital_biquad_filter for details.
    public static Complex getDigitalBiquadFilter( final Complex z,
                                                  final Complex zSquared,
                                                  final double[][] biquadCoefficients ) {
        // Calculate the bilinear transform for the given break point.
        // NOTE: The convention used in this code is an/bn, which is opposite
        // the one from Matlab, Audio EQ Cookbook, and various Wiki sources,
        // where bn/an is more common.
        final Complex numerator = getQuadraticFactor( z, zSquared, biquadCoefficients[ 0 ] );
        final Complex denominator = getQuadraticFactor( z, zSquared, biquadCoefficients[ 1 ] );

        // NOTE: Avoid divide by zero exceptions!
        if ( Complex.ZERO.equals( denominator ) ) {
            return Complex.ONE;
        }

        // Result = numerator / denominator
        final Complex result = numerator.divide( denominator );

        return result;
    }

    // Get the digital biquad coefficients for a Butterworth filter pole.
    // See http://en.wikipedia.org/wiki/Butterworth_filter for details.
    public static double[] getDigitalBiquadPoleCoefficients( final double eQSin,
                                                             final double oneMinusCos,
                                                             final double onePlusCos,
                                                             final double[] analogCoefficients ) {
        // Calculate a full set of digital domain biquad pole coefficients.
        // NOTE: The redundant assignments are for ease of verifying against
        // textbook implementations of this algorithm.
        final double[] digitalBiquadPoleCoefficients = new double[ NUMBER_OF_QUAD_COEFFICIENTS ];

        final double A = analogCoefficients[ 0 ];
        final double B = analogCoefficients[ 1 ];
        final double C = analogCoefficients[ 2 ];

        final double a0 = ( ( A * onePlusCos ) - ( B * eQSin ) ) + ( C * oneMinusCos );
        final double a1 = ( -2d * A * onePlusCos ) + ( 2.0d * C * oneMinusCos );
        final double a2 = ( A * onePlusCos ) + ( B * eQSin ) + ( C * oneMinusCos );

        digitalBiquadPoleCoefficients[ 0 ] = a0;
        digitalBiquadPoleCoefficients[ 1 ] = a1;
        digitalBiquadPoleCoefficients[ 2 ] = a2;

        return digitalBiquadPoleCoefficients;
    }

    // Get the filter slope label to filter slope dB mapping (all filters).
    public static int getFilterSlopeDb( final String filterSlopeLabel ) {
        final int filterSlopeDb = NumberUtilities
                .parseInteger( filterSlopeLabel.split( FILTER_SLOPE_UNITS )[ 0 ] );
        return filterSlopeDb;
    }

    // Get the filter slope dB to filter slope label mapping (all filters).
    public static String getFilterSlopeLabel( final int filterSlopeDb ) {
        final String filterSlopeLabel = Integer.toString( filterSlopeDb ) + FILTER_SLOPE_UNITS;
        return filterSlopeLabel;
    }

    // Get the angle to the pole (radians), in the z-plane.
    public static double getPoleAngleRadians( final double poleFrequencyHz,
                                              final double samplingFrequencyHz ) {
        final double frequencyRatio = poleFrequencyHz / samplingFrequencyHz;
        final double poleAngleRadians = MathConstants.TWO_PI * frequencyRatio;
        return poleAngleRadians;
    }

    // Get a quadratic factor (numerator or denominator, depending on the
    // specific biquad pole coefficients) of a linear transform, given
    // coefficients in the digital domain.
    // See http://en.wikipedia.org/wiki/Digital_biquad_filter for details.
    public static Complex getQuadraticFactor( final Complex z,
                                              final Complex zSquared,
                                              final double[] digitalQuadCoefficients ) {
        // Calculate the linear transform for the given break point.
        // NOTE: Most software uses negative powers of z, but some firmware
        // implementations (usually in C) use positive powers of z due to
        // older conventions in FPGA and PAL chip architecture. Coefficients
        // must be swapped if this code is copy-pasted to such firmware.
        // NOTE: The redundant assignments are for ease of verifying against
        // textbook implementations of this algorithm.
        final double a0 = digitalQuadCoefficients[ 0 ];
        final double a1 = digitalQuadCoefficients[ 1 ];
        final double a2 = digitalQuadCoefficients[ 2 ];

        final Complex result = new Complex( a0 ).divide( zSquared )
                .add( new Complex( a1 ).divide( z ) ).add( new Complex( a2 ) );

        return result;
    }

}
