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

import com.mhschmieder.acousticstoolkit.FrequencySignalUtilities;
import com.mhschmieder.mathtoolkit.MathConstants;
import com.mhschmieder.mathtoolkit.MathUtilities;
import com.mhschmieder.sigproctoolkit.dsp.DigitalFilterUtilities;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

// This class models a generic Parametric Filter (cf. Robert Bristow-Johnson's
// BiquadFilterCoefficients.rtf for details on formulae and coefficients).
//
// https://webaudio.github.io/Audio-EQ-Cookbook/Audio-EQ-Cookbook.txt
//
// TODO: Declare min/max for range as well, and use these (rounded to the
//  nearest integer) to initialize new-style Java Swing spinner controls.
public final class ParametricFilter extends DigitalFilter {

    // Parametric Filters are enabled by default, as they are initially flat.
    private static final boolean BYPASSED_DEFAULT = false;

    // Center frequency, range 20Hz to 20000Hz
    private static final double  F_DEFAULT        = 1000d;

    // Bandwidth, range 0.1 to 1.1 octave
    private static final double  O_DEFAULT        = 1.0d;

    // Cut/boost (gain), range -15dB to +15dB
    private static final double  C_DEFAULT        = 0.0d;

    private boolean              _bypassed;
    private double               _f;
    private double               _o;
    private double               _c;

    // Declare equation domain parameters (computed from f/o/c, not
    // user-specified).
    // NOTE: We take the absolute value of the gain before logging it, to
    //  compensate for the equation flip for H in the ParametricFilters class.
    private Complex              _w;
    private Complex              _q;
    private Complex              _g;

    // Declare flag for whether or not to flip the frequency response result.
    private boolean              _invertH         = false;

    // Pre-cached parametric coefficients.
    private Complex              _a0              = Complex.ONE;
    private Complex              _a1              = Complex.ONE;
    private Complex              _a2              = Complex.ONE;
    private Complex              _b0              = Complex.ONE;
    private Complex              _b1              = Complex.ONE;
    private Complex              _b2              = Complex.ONE;

    // This is the preferred default constructor for a single filter; it sets
    // all instance variables to default values.
    public ParametricFilter() {
        this( F_DEFAULT );
    }

    // This is the preferred default constructor for multiple filters; it sets
    // all instance variables to default values, except for frequency.
    public ParametricFilter( final double f ) {
        this( BYPASSED_DEFAULT, f, O_DEFAULT, C_DEFAULT );
    }

    // This is the preferred constructor, when all initial values are known.
    private ParametricFilter( final boolean bypassed,
                              final double f,
                              final double o,
                              final double c ) {
        // Always call the superclass constructor first!
        super();
        
        _bypassed = bypassed;
        _f = f;
        _o = o;
        _c = c;

        // Compute the associated equation domain parameters "W", "Q" and "G".
        // TODO: Find out why the opposite convention is used here for which
        //  part is real and which part is imaginary vs.
        //  convertFrequencyToSDomain()
        _w = new Complex( FrequencySignalUtilities.getAngularFrequencyRadians( f ), 0.0d );
        _q = new Complex( FrequencySignalUtilities.convertBandwidthToQ( o ), 0.0d );
        _g = new Complex( FrequencySignalUtilities.getVoltageRatio( FastMath.abs( c ) ), 0.0d );
        _invertH = ( c < 0f );

        // Update the equation parameters any time the base values change.
        calculateEqCoefficients();
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public ParametricFilter( final ParametricFilter parametricFilter ) {
        this( parametricFilter.isBypassed(),
              parametricFilter.getF(),
              parametricFilter.getO(),
              parametricFilter.getC() );
    }

    // NOTE: Cloning is disabled as it is dangerous; use the copy constructor
    //  instead.
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public double getC() {
        return _c;
    }

    public double getF() {
        return _f;
    }

    // This instance method returns the Parametric Filter value at a given
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

        final Complex d0 = _a0;
        final Complex d1 = _a1.divide( z );
        final Complex d2 = _a2.divide( zSquared );
        final Complex denominator = d0.add( d1 ).add( d2 );

        final Complex n0 = _b0;
        final Complex n1 = _b1.divide( z );
        final Complex n2 = _b2.divide( zSquared );
        final Complex numerator = n0.add( n1 ).add( n2 );

        // NOTE: Avoid divide by zero exceptions!
        if ( Complex.ZERO.equals( denominator ) ) {
            return Complex.ONE;
        }

        // Result = numerator / denominator
        h = numerator.divide( denominator );

        // Return the parametric filter value at the given frequency.
        // NOTE: For now, we are returning the conjugate instead. This takes
        //  care of sign problems in the delay time in some implementations.
        return h.conjugate();
    }

    public double getO() {
        return _o;
    }

    public boolean isActiveEqMode() {
        // Iterate through the individual parametric filters values, and if any
        // of them are non-zero, activate the EQ Mode.
        boolean activeEqMode = false;

        if ( !_bypassed ) {
            // Only check for non-unity gain within a reasonable precision.
            // TODO: Review this comparison and make it a generic math utility.
            final float fuzzyC = ( float ) _c;
            activeEqMode |= ( ( fuzzyC <= -0.001f ) || ( fuzzyC >= 0.001f ) );
        }

        return activeEqMode;
    }

    public boolean isBypassed() {
        return _bypassed;
    }

    public boolean isEqBoostMode() {
        // Iterate through the individual parametric filters values, and if any
        // of them are positive non-zero, activate the EQ Boost Mode.
        boolean eqBoostMode = false;

        if ( !_bypassed ) {
            // Only check for positive non-unity gain within reasonable
            // precision.
            // TODO: Review this comparison and make it a generic math utility.
            final float fuzzyC = ( float ) _c;
            eqBoostMode |= ( fuzzyC >= 0.001f );
        }

        return eqBoostMode;
    }

    // This method detects whether any of the filter parameters have been
    // altered from their default state.
    // NOTE: Most uses of Parametric Filters will result in an initially
    //  constructed filter already being at non-default state, so maybe we should
    //  compare against initial state?
    public boolean isNonDefaultEqMode() {
        return ( isBypassed() != BYPASSED_DEFAULT ) || ( getF() != F_DEFAULT )
                || ( getO() != O_DEFAULT ) || ( getC() != C_DEFAULT );
    }

    // Default pseudo-constructor.
    public void reset() {
        setParametricFilter( BYPASSED_DEFAULT, F_DEFAULT, O_DEFAULT, C_DEFAULT );
    }

    public void setBypassed( final boolean bypassed ) {
        _bypassed = bypassed;
    }

    public void setC( final double c, final boolean updateEquationParameters ) {
        // Set the gain (cut/boost) and simultaneously compute the associated
        // equation domain parameter "G" (for tight loop efficiency).
        // NOTE: We take the absolute value of the gain before logging it, to
        //  compensate for the equation inversion for H.
        _c = c;
        _g = new Complex( FrequencySignalUtilities.getVoltageRatio( FastMath.abs( c ) ), 0.0d );
        _invertH = ( c < 0.0d );

        // Update the equation parameters any time the base values change.
        if ( updateEquationParameters ) {
            calculateEqCoefficients();
        }
    }

    public void setF( final double f, final boolean updateEquationParameters ) {
        // Set the center frequency and simultaneously compute the associated
        // equation domain parameter "W" (for tight loop efficiency).
        // TODO: Find out why the opposite convention is used here for which
        //  part is real and which part is imaginary vs. the
        //  convertFrequencyToSDomain() method.
        _f = f;
        _w = new Complex( FrequencySignalUtilities.getAngularFrequencyRadians( f ), 0.0d );

        // Update the equation parameters any time the base values change.
        if ( updateEquationParameters ) {
            calculateEqCoefficients();
        }
    }

    public void setO( final double o, final boolean updateEquationParameters ) {
        // Set the octaves and simultaneously compute the associated equation
        // domain parameter "Q" (for tight loop efficiency).
        _o = o;
        _q = new Complex( FrequencySignalUtilities.convertBandwidthToQ( o ), 0.0d );

        // Update the equation parameters any time the base values change.
        if ( updateEquationParameters ) {
            calculateEqCoefficients();
        }
    }

    // Fully qualified pseudo-constructor.
    void setParametricFilter( final boolean bypassed,
                              final double f,
                              final double o,
                              final double c ) {
        setBypassed( bypassed );
        setF( f, false );
        setO( o, false );
        setC( c, false );

        // Update the equation parameters any time the base values change.
        calculateEqCoefficients();
    }

    // Pseudo-copy constructor.
    public void setParametricFilter( final ParametricFilter parametricFilter ) {
        setParametricFilter( parametricFilter.isBypassed(),
                             parametricFilter.getF(),
                             parametricFilter.getO(),
                             parametricFilter.getC() );
    }

    // TODO: Enforce this method on all filters via an interface.
    public void calculateEqCoefficients() {
        // Theta is the angle to the pole frequency (radians), in the z-plane.
        final double theta = DigitalFilterUtilities
                .getPoleAngleRadians( _f, samplingFrequencyHz );

        final double G = _g.getReal();
        final double Q = _q.getReal();
        final double W = _w.getReal();

        final double P = W / FastMath.tan( 0.5d * theta );

        final double P2Q = P * P * Q;
        final double GPW = G * P * W;
        final double QW2 = Q * W * W;
        final double PW = P * W;

        final double B0 = P2Q + GPW + QW2; // A
        final double B1 = ( -2d * P2Q ) + ( 2.0d * QW2 ); // C
        final double B2 = ( P2Q - GPW ) + QW2; // D

        final double A0 = P2Q + PW + QW2; // B
        final double A1 = B1; // C
        final double A2 = ( P2Q - PW ) + QW2; // E

        // Use cross-constructors from double to Complex as downstream
        // algorithms need Complex numbers but we are essentially in real number
        // space here.
        if ( !_invertH ) {
            _b0 = new Complex( B0 / A0 );
            _b1 = new Complex( B1 / A0 );
            _b2 = new Complex( B2 / A0 );
            _a0 = new Complex( 1.0d ); // A0/A0;
            _a1 = new Complex( B1 / A0 );
            _a2 = new Complex( A2 / A0 );
        }
        else {
            _b0 = new Complex( A0 / B0 );
            _b1 = new Complex( A1 / B0 );
            _b2 = new Complex( A2 / B0 );
            _a0 = new Complex( 1.0d ); // B0/B0;
            _a1 = new Complex( B1 / B0 );
            _a2 = new Complex( B2 / B0 );
        }
    }
}
