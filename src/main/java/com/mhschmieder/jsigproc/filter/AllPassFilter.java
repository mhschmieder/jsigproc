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
package com.mhschmieder.jsigproc.filter;

import com.mhschmieder.jacoustics.FrequencySignalUtilities;
import com.mhschmieder.jmath.MathConstants;
import com.mhschmieder.jmath.MathUtilities;
import com.mhschmieder.jsigproc.dsp.DigitalFilterUtilities;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

// This class models a generic All Pass Filter (cf. Robert Bristow-Johnson's
// BiquadFilterCoefficients.rtf for details on formulae and coefficients).
public final class AllPassFilter extends DigitalFilter {

    // All Pass Filters are bypassed by default as they are never flat.
    protected static final boolean BYPASSED_DEFAULT = true;

    // Center frequency, range 20Hz to 20000Hz
    protected static final double  F_DEFAULT        = 100d;

    // Bandwidth, range 1.1 to 0.1 octave
    private static final double    O_DEFAULT        = 1.0d;

    private boolean                _bypassed;
    private double                 _f;
    private double                 _o;

    // Declare equation domain parameters (computed from f/o, not
    // user-specified).
    private Complex                _w;
    private Complex                _q;

    // This is the generic default constructor for a single filter; it sets all
    // instance variables to default values.
    public AllPassFilter() {
        this( F_DEFAULT );
    }

    // This is the preferred default constructor for multiple filters; it sets
    // all instance variables to default values, except for frequency.
    public AllPassFilter( final double f ) {
        this( BYPASSED_DEFAULT, f, O_DEFAULT );
    }

    // This is the preferred constructor, when all initial values are known.
    public AllPassFilter( final boolean allPassBypassed, final double f, final double o ) {
        _bypassed = allPassBypassed;
        _f = f;
        _o = o;

        // Compute the associated equation domain parameters "W" and "Q".
        // TODO: Find out why the opposite convention is used here for which
        //  part is real and which part is imaginary vs.
        //  convertFrequencyToSDomain()
        _w = new Complex( FrequencySignalUtilities.getAngularFrequencyRadians( f ), 0.0d );
        _q = new Complex( o, 0.0d );
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public AllPassFilter( final AllPassFilter allPassFilter ) {
        this( allPassFilter.isBypassed(), allPassFilter.getF(), allPassFilter.getO() );
    }

    // NOTE: Cloning is disabled as it is dangerous; use the copy constructor
    //  instead.
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public double getF() {
        return _f;
    }

    // This instance method returns the All Pass Filter value at a given
    // frequency (in Hertz), using the stored current parameters.
    @Override
    public Complex getH( final double f ) {
        if ( _bypassed ) {
            return Complex.ONE;
        }

        // The sampling frequency should be set in advance to match exactly what
        // is done in any hardware or software that uses this filter algorithm.
        // The pre-warping affects the linearity of high frequencies so we must 
        // respect the set sampling frequency.
        //
        // The sampling frequency of the filter is independent of the other
        // sampling frequencies, as we are passing the analog frequency to the
        // filter method in which we want to get the complex response.
        Complex h = Complex.ONE;

        // If f == 0 we have divisions by 0 and therefore NaNs. Avoid it.
        final double fAdjusted = FastMath.max( f, MathConstants.EPSILON_SMALL );

        final Complex z = DigitalFilterUtilities
                .convertFrequencyToZDomain( fAdjusted, samplingFrequencyHz );
        final Complex zSquared = MathUtilities.sqrComplex( z );

        // Get the complex frequency variable in the s-plane.
        final Complex s = FrequencySignalUtilities.convertFrequencyToSDomain( fAdjusted );

        // Theta is the angle to the pole (radians), in the z-plane.
        final double theta = DigitalFilterUtilities
                .getPoleAngleRadians( fAdjusted, samplingFrequencyHz );

        final double Q = _q.getReal();
        final double W = _w.getReal();

        final double P = s.getImaginary() / FastMath.tan( 0.5d * theta );

        final double QW2 = Q * W * W;
        final double P2Q = P * P * Q;
        final double PW = P * W;

        final double A = P2Q + PW + QW2;
        final double B = ( P2Q - PW ) + QW2;
        final double C = ( -2d * P2Q ) + ( 2.0d * QW2 );

        final double CA = C / A;
        final double BA = B / A;

        final Complex denominator = zSquared.add( z.multiply( CA ) ).add( BA );
        final Complex numerator = zSquared.multiply( BA ).add( z.multiply( CA ) ).add( 1.0d );

        // NOTE: Avoid divide by zero exceptions!
        if ( Complex.ZERO.equals( denominator ) ) {
            return Complex.ONE;
        }

        // Result = numerator / denominator
        h = numerator.divide( denominator );

        // Return the all pass filter value at the given frequency.
        // NOTE: For now, we are returning the conjugate instead. This takes
        //  care of sign problems in the delay time in some implementations.
        return h.conjugate();
    }

    public double getO() {
        return _o;
    }

    public boolean isActiveEqMode() {
        // All pass is phase-only, so there are no gain settings; we only care
        // if the filter is bypassed or not.
        return !_bypassed;
    }

    public boolean isBypassed() {
        return _bypassed;
    }

    @SuppressWarnings("static-method")
    public boolean isEqBoostMode() {
        // All Pass is phase-only, so there are no gain settings.
        return false;
    }

    // This method detects whether any of the filter parameters have been
    // altered from their default state.
    // NOTE: Most uses of All Pass Filters will result in an initially
    //  constructed filter already being at non-default state, so maybe we should
    //  compare against initial state?
    public boolean isNonDefaultEqMode() {
        return ( isBypassed() != BYPASSED_DEFAULT ) || ( getF() != F_DEFAULT )
                || ( getO() != O_DEFAULT );
    }

    // Default pseudo-constructor
    public void reset() {
        setAllPassFilter( BYPASSED_DEFAULT, F_DEFAULT, O_DEFAULT );
    }

    // Pseudo-copy constructor
    public void setAllPassFilter( final AllPassFilter allPassFilter ) {
        setAllPassFilter( allPassFilter.isBypassed(), allPassFilter.getF(), allPassFilter.getO() );
    }

    // Fully qualified pseudo-constructor
    public void setAllPassFilter( final boolean allPassBypassed, final double f, final double o ) {
        setBypassed( allPassBypassed );
        setF( f );
        setO( o );
    }

    public void setBypassed( final boolean allPassBypassed ) {
        _bypassed = allPassBypassed;
    }

    public void setF( final double f ) {
        // Set the center frequency and simultaneously compute the associated
        // equation domain parameter "W" (for tight loop efficiency).
        // TODO: Find out why the opposite convention is used here for which
        //  part is real and which part is imaginary vs.
        //  convertFrequencyToSDomain()
        _f = f;
        _w = new Complex( FrequencySignalUtilities.getAngularFrequencyRadians( f ), 0.0d );
    }

    public void setO( final double o ) {
        // Set the octaves and simultaneously compute the associated equation
        // domain parameter "Q" (for tight loop efficiency).
        _o = o;
        _q = new Complex( o, 0.0d );
    }
}
