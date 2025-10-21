/*
 * MIT License
 *
 * Copyright (c) 2020, 2025 Mark Schmieder
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
package com.mhschmieder.jsigproc.filter;

import com.mhschmieder.jsigproc.dsp.DigitalFilterUtilities;
import com.mhschmieder.mathtoolkit.MathConstants;
import com.mhschmieder.mathtoolkit.MathUtilities;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

// This class models a custom High/Low Pass Filter (cf. Robert Bristow-Johnson's
// BiquadFilterCoefficients.rtf for details on formulae and coefficients).
public class HighLowPassFilter extends DigitalFilter {

    // High/Low Pass Filters are bypassed by default as they are never flat.
    protected static final boolean            BYPASSED_DEFAULT                  = true;

    // High/Low Pass frequency, range 40 Hz to 160 Hz (100 Hz default).
    private static final double               FC_HIGH_LOW_PASS_DEFAULT          = 100d;

    // Default for which High/Low Pass filter type to use.
    public static final HighLowPassFilterType FILTER_TYPE_HIGH_LOW_PASS_DEFAULT =
                                                                                HighLowPassFilterType.LOW_PASS;

    // Define constants to define the digital domain coefficient dimensions.
    private static final int                  NUMBER_OF_BIQUAD_COEFFICIENTS     = 6;
    private static final int                  NUMBER_OF_BIQUAD_SETS             = 4;

    // Sub-product constants for legacy analog-derived High/Low Pass filter types.
    protected static final double             LOW_PASS_AA                       = 0.107d;
    protected static final double             LOW_PASS_BB                       = 0.893d;
    protected static final double             LOW_PASS_CC                       = 0.9d;

    protected static final double             SECOND_ORDER_HIGH_PASS_AA         = 0.0d;
    protected static final double             SECOND_ORDER_HIGH_PASS_BB         = 1.0d;
    protected static final double             SECOND_ORDER_HIGH_PASS_CC         = 0.9d;

    protected static final double             ELLIPTICAL_HIGH_PASS_AA           = 0.107d;
    protected static final double             ELLIPTICAL_HIGH_PASS_BB           = 0.893d;
    protected static final double             ELLIPTICAL_HIGH_PASS_CC           = 0.9d;

    // Butterworth fourth through eighth order filter pole constants.
    // NOTE: These are pre-computed and cached, as it is expensive to
    //  redundantly re-derive these constants inside tight computational loops
    //  that get refreshed frequently during GUI syncing operations as well.
    protected static final double             BUTTERWORTH_4_E                   = 2.0d
            * FastMath.cos( MathConstants.THREE_PI / 8.0d );
    protected static final double             BUTTERWORTH_4_K                   =
                                                              2.0d * FastMath.cos( FastMath.PI / 8.0d );
    protected static final double             BUTTERWORTH_5_E                   =
                                                              2.0d * FastMath.cos( FastMath.PI / 5.0d );
    protected static final double             BUTTERWORTH_5_K                   = 2.0d
            * FastMath.cos( MathConstants.TWO_PI / 5.0d );
    protected static final double             BUTTERWORTH_6_E                   = 2.0d
            * FastMath.cos( MathConstants.FIVE_PI / 12d );
    protected static final double             BUTTERWORTH_6_K                   = 2.0d
            * FastMath.cos( MathConstants.FIVE_PI / 12d );
    protected static final double             BUTTERWORTH_6_Q                   = 2.0d
            * FastMath.cos( MathConstants.THREE_PI / 12d );
    protected static final double             BUTTERWORTH_7_E                   = 2.0d
            * FastMath.cos( MathConstants.THREE_PI / 7.0d );
    protected static final double             BUTTERWORTH_7_K                   = 2.0d
            * FastMath.cos( MathConstants.TWO_PI / 7.0d );
    protected static final double             BUTTERWORTH_7_Q                   =
                                                              2.0d * FastMath.cos( FastMath.PI / 7.0d );
    protected static final double             BUTTERWORTH_8_E                   = 2.0d
            * FastMath.cos( MathConstants.SEVEN_PI / 16d );
    protected static final double             BUTTERWORTH_8_K                   = 2.0d
            * FastMath.cos( MathConstants.FIVE_PI / 16d );
    protected static final double             BUTTERWORTH_8_Q                   = 2.0d
            * FastMath.cos( MathConstants.THREE_PI / 16d );
    protected static final double             BUTTERWORTH_8_W                   =
                                                              2.0d * FastMath.cos( FastMath.PI / 16d );

    private boolean                           _bypassed;
    private double                            _fc;
    private ElectronicFilterType              _electronicFilterType;
    private HighLowPassFilterType             _highLowPassFilterType;

    // Declare the angle to the pole (radians), in the z-plane.
    private double                            _w;

    // Four sets of pre-cached biquad coefficients (digital domain).
    private final double[][]                  _coeffs;

    // This is the generic default constructor for a single filter; it sets all
    // instance variables to default values.
    public HighLowPassFilter() {
        this( FC_HIGH_LOW_PASS_DEFAULT );
    }

    // This is the preferred default constructor for multiple filters; it sets
    // all instance variables to default values, except for frequency.
    private HighLowPassFilter( final double f ) {
        this( BYPASSED_DEFAULT, f, FILTER_TYPE_HIGH_LOW_PASS_DEFAULT );
    }

    // This is the preferred specific constructor for legacy analog-derived
    // High/Low Pass, when all initial values are known.
    protected HighLowPassFilter( final boolean highLowPassBypassed,
                                 final double fc,
                                 final HighLowPassFilterType highLowPassFilterType ) {
        this( highLowPassBypassed, fc, ElectronicFilterType.HIGH_LOW_PASS, highLowPassFilterType );
    }

    // This is the preferred generalized constructor for all High/Low Pass
    // cases, when all initial values are known.
    protected HighLowPassFilter( final boolean highLowPassBypassed,
                                 final double fc,
                                 final ElectronicFilterType electronicFilterType,
                                 final HighLowPassFilterType highLowPassFilterType ) {
        _bypassed = highLowPassBypassed;
        _fc = fc;
        _electronicFilterType = electronicFilterType;
        _highLowPassFilterType = highLowPassFilterType;

        // Compute the associated equation domain parameter "W", which is the
        // angle to the pole (radians), in the z-plane.
        _w = DigitalFilterUtilities.getPoleAngleRadians( fc, samplingFrequencyHz );

        // Allocate the static array for digital domain coefficients.
        _coeffs = new double[ NUMBER_OF_BIQUAD_COEFFICIENTS ][ NUMBER_OF_BIQUAD_SETS ];

        // Update the equation parameters any time the base values change.
        calculateEqCoefficients();
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public HighLowPassFilter( final HighLowPassFilter highLowPassFilter ) {
        this( highLowPassFilter.isBypassed(),
              highLowPassFilter.getFc(),
              highLowPassFilter.getHighLowPassFilterType() );
    }

    // NOTE: Cloning is disabled as it is dangerous; use the copy constructor
    //  instead.
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final Complex getBiQuadResult( final Complex zMinusOne, final Complex zMinusTwo ) {
        // Apply the first set of biquad coefficients.
        Complex numeratorFactor1 = new Complex( _coeffs[ 0 ][ 0 ] );
        Complex numeratorFactor2 = zMinusOne.multiply( _coeffs[ 1 ][ 0 ] );
        Complex numeratorFactor3 = zMinusTwo.multiply( _coeffs[ 2 ][ 0 ] );
        Complex numerator = numeratorFactor1.add( numeratorFactor2 ).add( numeratorFactor3 );

        Complex denominatorFactor1 = new Complex( _coeffs[ 3 ][ 0 ] );
        Complex denominatorFactor2 = zMinusOne.multiply( _coeffs[ 4 ][ 0 ] );
        Complex denominatorFactor3 = zMinusTwo.multiply( _coeffs[ 5 ][ 0 ] );
        Complex denominator =
                            denominatorFactor1.add( denominatorFactor2 ).add( denominatorFactor3 );

        // NOTE: Avoid divide by zero exceptions!
        if ( Complex.ZERO.equals( denominator ) ) {
            return Complex.ONE;
        }

        // Result = numerator / denominator
        Complex biquadResult = numerator.divide( denominator );

        // Combine the remaining three sets of biquad coefficients.
        for ( int biquadSetIndex = 1; biquadSetIndex < NUMBER_OF_BIQUAD_SETS; biquadSetIndex++ ) {
            numeratorFactor1 = new Complex( _coeffs[ 0 ][ biquadSetIndex ] );
            numeratorFactor2 = zMinusOne.multiply( _coeffs[ 1 ][ biquadSetIndex ] );
            numeratorFactor3 = zMinusTwo.multiply( _coeffs[ 2 ][ biquadSetIndex ] );
            numerator = numeratorFactor1.add( numeratorFactor2 ).add( numeratorFactor3 );

            denominatorFactor1 = new Complex( _coeffs[ 3 ][ biquadSetIndex ] );
            denominatorFactor2 = zMinusOne.multiply( _coeffs[ 4 ][ biquadSetIndex ] );
            denominatorFactor3 = zMinusTwo.multiply( _coeffs[ 5 ][ biquadSetIndex ] );
            denominator = denominatorFactor1.add( denominatorFactor2 ).add( denominatorFactor3 );

            final Complex result = numerator.divide( denominator );

            biquadResult = biquadResult.multiply( result );
        }

        return biquadResult;
    }

    public final ElectronicFilterType getElectronicFilterType() {
        return _electronicFilterType;
    }

    public final double getFc() {
        return _fc;
    }

    // This method returns the High Pass or Low Pass Filter value at a given
    // frequency (in Hertz), using the stored current parameters.
    @Override
    public final Complex getH( final double f ) {
        // Due to loop efficiency issues and cascaded Complex arithmetic
        // operations, the bypassed flag is checked here vs. in the invoker, and
        // is guaranteed to have no effect on the final composite filter value.
        if ( _bypassed ) {
            return Complex.ONE;
        }

        // If f == 0 we have divisions by 0 and therefore NaNs. Avoid it.
        final double fAdjusted = FastMath.max( f, MathConstants.EPSILON_SMALL );

        // Switch from the analog to digital domain for the main calculations.
        //
        // The sampling frequency should be set in advance to match exactly what
        // is done in any hardware or software that uses this filter algorithm.
        // The pre-warping affects the linearity of high frequencies so we must 
        // respect the set sampling frequency.
        //
        // The sampling frequency of the filter is independent of the other
        // sampling frequencies, as we are passing the analog frequency to the
        // filter method in which we want to get the complex response.
        final Complex z = DigitalFilterUtilities
                .convertFrequencyToZDomain( fAdjusted, samplingFrequencyHz );
        final Complex zMinusOne = z.reciprocal();
        final Complex zMinusTwo = MathUtilities.sqrComplex( zMinusOne );

        // The biquad calculation code is ported and adapted from textbook examples.
        final Complex h = getBiQuadResult( zMinusOne, zMinusTwo );

        // Return the High/Low Pass filter value at the given frequency.
        // NOTE: For now, we are returning the conjugate instead. This takes
        //  care of sign problems in the delay time on the server.
        return h.conjugate();
    }

    public final HighLowPassFilterType getHighLowPassFilterType() {
        return _highLowPassFilterType;
    }

    public final boolean isActiveEqMode() {
        // Both High and Low Pass use idealized cut/boost curves; we only care
        // if the filter is bypassed or not.
        return !_bypassed;
    }

    public final boolean isBypassed() {
        return _bypassed;
    }

    @SuppressWarnings("static-method")
    public final boolean isEqBoostMode() {
        // Both High and Low Pass use idealized cut/boost curves, but the boost
        // is inconsequential and highly localized.
        return false;
    }

    // This method detects whether any of the filter parameters have been
    // altered from their default state.
    public boolean isNonDefaultEqMode() {
        return ( isBypassed() != BYPASSED_DEFAULT ) || ( getFc() != FC_HIGH_LOW_PASS_DEFAULT )
                || ( getElectronicFilterType() != ElectronicFilterType.HIGH_LOW_PASS )
                || ( getHighLowPassFilterType() != FILTER_TYPE_HIGH_LOW_PASS_DEFAULT );
    }

    // Default pseudo-constructor
    public void reset() {
        setHighLowPassFilter( BYPASSED_DEFAULT,
                              FC_HIGH_LOW_PASS_DEFAULT,
                              ElectronicFilterType.HIGH_LOW_PASS,
                              FILTER_TYPE_HIGH_LOW_PASS_DEFAULT );
    }

    public final void setBypassed( final boolean highLowPassBypassed ) {
        _bypassed = highLowPassBypassed;
    }

    public final void setElectronicFilterType( final ElectronicFilterType electronicFilterType ) {
        _electronicFilterType = electronicFilterType;
    }

    public final void setFc( final double fc, final boolean updateEquationParameters ) {
        // Set the cutoff frequency.
        _fc = fc;

        // Compute the associated equation domain parameter "W", which is the
        // angle to the pole (radians), in the z-plane.
        _w = DigitalFilterUtilities.getPoleAngleRadians( fc, samplingFrequencyHz );

        // Update the equation parameters any time the base values change.
        if ( updateEquationParameters ) {
            calculateEqCoefficients();
        }
    }

    // Fully qualified pseudo-constructor
    public final void setHighLowPassFilter( final boolean highLowPassBypassed,
                                            final double fc,
                                            final ElectronicFilterType electronicFilterType,
                                            final HighLowPassFilterType highLowPassFilterType ) {
        setBypassed( highLowPassBypassed );
        setFc( fc, false );
        setElectronicFilterType( electronicFilterType );
        setHighLowPassFilterType( highLowPassFilterType, false );

        // Update the equation parameters any time the base values change.
        calculateEqCoefficients();
    }

    // Pseudo-copy constructor.
    public final void setHighLowPassFilter( final HighLowPassFilter highLowPassFilter ) {
        setHighLowPassFilter( highLowPassFilter.isBypassed(),
                              highLowPassFilter.getFc(),
                              highLowPassFilter.getElectronicFilterType(),
                              highLowPassFilter.getHighLowPassFilterType() );
    }

    public final void setHighLowPassFilterType( final HighLowPassFilterType highLowPassFilterType,
                                                final boolean updateEquationParameters ) {
        _highLowPassFilterType = highLowPassFilterType;

        // Update the equation parameters any time the base values change.
        if ( updateEquationParameters ) {
            calculateEqCoefficients();
        }
    }

    // TODO: Enforce this method on all filters via an interface.
    public final void calculateEqCoefficients() {
        // Analog domain coefficients, per dual-pole biquad section.
        double a = 0.0d;
        double b = 0.0d;
        double c = 1.0d;
        double d = 0.0d;
        double e = 0.0d;
        double f = 1.0d;

        double g = 0.0d;
        double h = 0.0d;
        double i = 1.0d;
        double j = 0.0d;
        double k = 0.0d;
        double l = 1.0d;

        double m = 0.0d;
        double n = 0.0d;
        double o = 1.0d;
        double p = 0.0d;
        double q = 0.0d;
        double r = 1.0d;

        double s = 0.0d;
        double t = 0.0d;
        double u = 1.0d;
        double v = 0.0d;
        double w = 0.0d;
        double x = 1.0d;

        // Pre-cache the analog domain coefficients, for tight loop efficiency.
        // NOTE: See http://alignment.hep.brandeis.edu/Lab/Filter/Filter.html
        //  for an excellent write-up that explains the Butterworth filter stages
        //  and Pole Locus calculations. Note that filters with an odd number of
        //  poles require only an R-C network to implement the solitary pole on
        //  the negative real axis.
        switch ( _highLowPassFilterType ) {
        case SECOND_ORDER_HIGH_PASS:
            a = SECOND_ORDER_HIGH_PASS_AA + SECOND_ORDER_HIGH_PASS_BB;
            b = SECOND_ORDER_HIGH_PASS_AA / SECOND_ORDER_HIGH_PASS_CC;
            c = SECOND_ORDER_HIGH_PASS_AA;
            d = 1.0d;
            e = 1.0d / SECOND_ORDER_HIGH_PASS_CC;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 0.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case ELLIPTICAL_HIGH_PASS:
            a = ELLIPTICAL_HIGH_PASS_AA + ELLIPTICAL_HIGH_PASS_BB;
            b = ELLIPTICAL_HIGH_PASS_AA / ELLIPTICAL_HIGH_PASS_CC;
            c = ELLIPTICAL_HIGH_PASS_AA;
            d = 1.0d;
            e = 1.0d / ELLIPTICAL_HIGH_PASS_CC;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 0.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_1_HIGH_PASS:
            a = 0.0d;
            b = 1.0d;
            c = 0.0d;
            d = 0.0d;
            e = 1.0d;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 0.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_2_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = MathConstants.SQRT_TWO;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 0.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_3_HIGH_PASS:
            a = 0.0d;
            b = 1.0d;
            c = 0.0d;
            d = 0.0d;
            e = 1.0d;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = 1.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_4_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = BUTTERWORTH_4_E;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = BUTTERWORTH_4_K;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_5_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = BUTTERWORTH_5_E;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = BUTTERWORTH_5_K;
            l = 1.0d;

            m = 0.0d;
            n = 1.0d;
            o = 0.0d;
            p = 0.0d;
            q = 1.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_6_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = BUTTERWORTH_6_E;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = BUTTERWORTH_6_K;
            l = 1.0d;

            m = 1.0d;
            n = 0.0d;
            o = 0.0d;
            p = 1.0d;
            q = BUTTERWORTH_6_Q;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_7_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = BUTTERWORTH_7_E;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = BUTTERWORTH_7_K;
            l = 1.0d;

            m = 1.0d;
            n = 0.0d;
            o = 0.0d;
            p = 1.0d;
            q = BUTTERWORTH_7_Q;
            r = 1.0d;

            s = 0.0d;
            t = 1.0d;
            u = 0.0d;
            v = 0.0d;
            w = 1.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_8_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = BUTTERWORTH_8_E;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = BUTTERWORTH_8_K;
            l = 1.0d;

            m = 1.0d;
            n = 0.0d;
            o = 0.0d;
            p = 1.0d;
            q = BUTTERWORTH_8_Q;
            r = 1.0d;

            s = 1.0d;
            t = 0.0d;
            u = 0.0d;
            v = 1.0d;
            w = BUTTERWORTH_8_W;
            x = 1.0d;

            break;
        case LINKWITZ_RILEY_2_HIGH_PASS:
            a = 0.0d;
            b = 1.0d;
            c = 0.0d;
            d = 0.0d;
            e = 1.0d;
            f = 1.0d;

            g = 0.0d;
            h = 1.0d;
            i = 0.0d;
            j = 0.0d;
            k = 1.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case LINKWITZ_RILEY_4_HIGH_PASS:
            a = 1.0d;
            b = 0.0d;
            c = 0.0d;
            d = 1.0d;
            e = MathConstants.SQRT_TWO;
            f = 1.0d;

            g = 1.0d;
            h = 0.0d;
            i = 0.0d;
            j = 1.0d;
            k = MathConstants.SQRT_TWO;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case LOW_PASS:
            // Compute the clamped equation domain parameter "W2", which is the
            // angle to the pole (radians), in the z-plane.
            final double clampedFrequency = _fc <= 160d ? 447d : ( ( 447d / 160d ) * _fc );
            final double w2 = DigitalFilterUtilities
                    .getPoleAngleRadians( clampedFrequency, samplingFrequencyHz );

            a = LOW_PASS_AA;
            b = LOW_PASS_AA / LOW_PASS_CC;
            c = LOW_PASS_AA + LOW_PASS_BB;
            d = 1.0d;
            e = 1.0d / LOW_PASS_CC;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = w2;
            j = 0.0d;
            k = _w;
            l = w2;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_1_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 0.0d;
            e = 1.0d;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 0.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_2_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = MathConstants.SQRT_TWO;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 0.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_3_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 0.0d;
            e = 1.0d;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = 1.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_4_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = BUTTERWORTH_4_E;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = BUTTERWORTH_4_K;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_5_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = BUTTERWORTH_5_E;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = BUTTERWORTH_5_K;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 1.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_6_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = BUTTERWORTH_6_E;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = BUTTERWORTH_6_K;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 1.0d;
            q = BUTTERWORTH_6_Q;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_7_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = BUTTERWORTH_7_E;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = BUTTERWORTH_7_K;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 1.0d;
            q = BUTTERWORTH_7_Q;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 1.0d;
            x = 1.0d;

            break;
        case BUTTERWORTH_8_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = BUTTERWORTH_8_E;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = BUTTERWORTH_8_K;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 1.0d;
            q = BUTTERWORTH_8_Q;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 1.0d;
            w = BUTTERWORTH_8_W;
            x = 1.0d;

            break;
        case LINKWITZ_RILEY_2_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 0.0d;
            e = 1.0d;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 0.0d;
            k = 1.0d;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        case LINKWITZ_RILEY_4_LOW_PASS:
            a = 0.0d;
            b = 0.0d;
            c = 1.0d;
            d = 1.0d;
            e = MathConstants.SQRT_TWO;
            f = 1.0d;

            g = 0.0d;
            h = 0.0d;
            i = 1.0d;
            j = 1.0d;
            k = MathConstants.SQRT_TWO;
            l = 1.0d;

            m = 0.0d;
            n = 0.0d;
            o = 1.0d;
            p = 0.0d;
            q = 0.0d;
            r = 1.0d;

            s = 0.0d;
            t = 0.0d;
            u = 1.0d;
            v = 0.0d;
            w = 0.0d;
            x = 1.0d;

            break;
        default:
            break;
        }

        // Pre-cache equation constants that don't depend on adjusted frequency
        // (to avoid divide-by-zero errors) or other dynamic parameters.
        //
        // This is the most general bilinear transform for four biquads sections
        // including pre-warping. It includes all of the twenty four basic
        // coefficients.
        final double eqOmega = _w;
        final double eqCos = FastMath.cos( eqOmega );
        final double eqSin = FastMath.sin( eqOmega );
        final double oneMinusEqCos = ( 1.0d - eqCos );
        final double onePlusEqCos = ( 1.0d + eqCos );

        // First set of pre-cached biquad coefficients.
        // NOTE: The modern convention is now b/a, as in Matlab and The
        // Audio EQ Cookbook.
        _coeffs[ 0 ][ 0 ] = ( a * onePlusEqCos ) + ( b * eqSin ) + ( c * oneMinusEqCos );
        _coeffs[ 1 ][ 0 ] = ( -2d * a * onePlusEqCos ) + ( 2.0d * c * oneMinusEqCos );
        _coeffs[ 2 ][ 0 ] = ( ( a * onePlusEqCos ) - ( b * eqSin ) ) + ( c * oneMinusEqCos );
        _coeffs[ 3 ][ 0 ] = ( d * onePlusEqCos ) + ( e * eqSin ) + ( f * oneMinusEqCos );
        _coeffs[ 4 ][ 0 ] = ( -2d * d * onePlusEqCos ) + ( 2.0d * f * oneMinusEqCos );
        _coeffs[ 5 ][ 0 ] = ( ( d * onePlusEqCos ) - ( e * eqSin ) ) + ( f * oneMinusEqCos );

        // Second set of pre-cached biquad coefficients.
        // NOTE: The modern convention is now b/a, as in Matlab and The
        // Audio EQ Cookbook.
        _coeffs[ 0 ][ 1 ] = ( g * onePlusEqCos ) + ( h * eqSin ) + ( i * oneMinusEqCos );
        _coeffs[ 1 ][ 1 ] = ( -2d * g * onePlusEqCos ) + ( 2.0d * i * oneMinusEqCos );
        _coeffs[ 2 ][ 1 ] = ( ( g * onePlusEqCos ) - ( h * eqSin ) ) + ( i * oneMinusEqCos );
        _coeffs[ 3 ][ 1 ] = ( j * onePlusEqCos ) + ( k * eqSin ) + ( l * oneMinusEqCos );
        _coeffs[ 4 ][ 1 ] = ( -2d * j * onePlusEqCos ) + ( 2.0d * l * oneMinusEqCos );
        _coeffs[ 5 ][ 1 ] = ( ( j * onePlusEqCos ) - ( k * eqSin ) ) + ( l * oneMinusEqCos );

        // Third set of pre-cached biquad coefficients.
        // NOTE: The modern convention is now b/a, as in Matlab and The
        // Audio EQ Cookbook.
        _coeffs[ 0 ][ 2 ] = ( m * onePlusEqCos ) + ( n * eqSin ) + ( o * oneMinusEqCos );
        _coeffs[ 1 ][ 2 ] = ( -2d * m * onePlusEqCos ) + ( 2.0d * o * oneMinusEqCos );
        _coeffs[ 2 ][ 2 ] = ( ( m * onePlusEqCos ) - ( n * eqSin ) ) + ( o * oneMinusEqCos );
        _coeffs[ 3 ][ 2 ] = ( p * onePlusEqCos ) + ( q * eqSin ) + ( r * oneMinusEqCos );
        _coeffs[ 4 ][ 2 ] = ( -2d * p * onePlusEqCos ) + ( 2.0d * r * oneMinusEqCos );
        _coeffs[ 5 ][ 2 ] = ( ( p * onePlusEqCos ) - ( q * eqSin ) ) + ( r * oneMinusEqCos );

        // Fourth set of pre-cached biquad coefficients.
        // NOTE: The modern convention is now b/a, as in Matlab and The
        // Audio EQ Cookbook.
        _coeffs[ 0 ][ 3 ] = ( s * onePlusEqCos ) + ( t * eqSin ) + ( u * oneMinusEqCos );
        _coeffs[ 1 ][ 3 ] = ( -2d * s * onePlusEqCos ) + ( 2.0d * u * oneMinusEqCos );
        _coeffs[ 2 ][ 3 ] = ( ( s * onePlusEqCos ) - ( t * eqSin ) ) + ( u * oneMinusEqCos );
        _coeffs[ 3 ][ 3 ] = ( v * onePlusEqCos ) + ( w * eqSin ) + ( x * oneMinusEqCos );
        _coeffs[ 4 ][ 3 ] = ( -2d * v * onePlusEqCos ) + ( 2.0d * x * oneMinusEqCos );
        _coeffs[ 5 ][ 3 ] = ( ( v * onePlusEqCos ) - ( w * eqSin ) ) + ( x * oneMinusEqCos );

        // Finally, normalize the biquads by a0, due to limited dynamic range.
        for ( int biquadSetIndex = 0; biquadSetIndex < NUMBER_OF_BIQUAD_SETS; biquadSetIndex++ ) {
            final double a0 = _coeffs[ 3 ][ biquadSetIndex ];
            for ( int biquadCoefficientIndex =
                                             0; biquadCoefficientIndex < NUMBER_OF_BIQUAD_COEFFICIENTS; biquadCoefficientIndex++ ) {
                _coeffs[ biquadCoefficientIndex ][ biquadSetIndex ] /= a0;
            }
        }
    }
}
