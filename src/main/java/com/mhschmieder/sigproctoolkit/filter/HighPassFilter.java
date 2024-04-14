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

public final class HighPassFilter extends HighLowPassFilter {

    // High Pass frequency, range 16 Hz to 10 kHz (40 Hz default).
    private static final double FC_HIGH_PASS_DEFAULT = 40d;

    // Default for which High Pass filter type to use.
    public static final HighLowPassFilterType FILTER_TYPE_HIGH_PASS_DEFAULT =
                                                                            HighLowPassFilterType.SECOND_ORDER_HIGH_PASS;

    // ///////////////////////////////////////////////////////////////
    // Constructors
    // This is the generic default constructor; it sets all instance variables
    // to default values.
    public HighPassFilter() {
        this( FC_HIGH_PASS_DEFAULT );
    }

    // This is the default constructor; it sets all instance variables to
    // default values, but sets a supplied bypassed status in advance.
    public HighPassFilter( final boolean highPassBypassed ) {
        this( highPassBypassed, FC_HIGH_PASS_DEFAULT, FILTER_TYPE_HIGH_PASS_DEFAULT );
    }

    // This is the preferred specific constructor, when all initial values are
    // known.
    public HighPassFilter( final boolean highPassBypassed,
                           final double fc,
                           final HighLowPassFilterType highLowPassFilterType ) {
        super( highPassBypassed, fc, ElectronicFilterType.HIGH_PASS, highLowPassFilterType );
    }

    // This is the preferred default constructor; it sets all instance variables
    // to default values, except for frequency.
    private HighPassFilter( final double fc ) {
        this( BYPASSED_DEFAULT, fc, FILTER_TYPE_HIGH_PASS_DEFAULT );
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public HighPassFilter( final HighPassFilter highPassFilter ) {
        this( highPassFilter.isBypassed(),
              highPassFilter.getFc(),
              highPassFilter.getHighLowPassFilterType() );
    }

    // This method detects whether any of the filter parameters have been
    // altered from their default state.
    @Override
    public boolean isNonDefaultEqMode() {
        return ( isBypassed() != BYPASSED_DEFAULT ) || ( getFc() != FC_HIGH_PASS_DEFAULT )
                || ( getElectronicFilterType() != ElectronicFilterType.HIGH_PASS )
                || ( getHighLowPassFilterType() != FILTER_TYPE_HIGH_PASS_DEFAULT );
    }

    // Default pseudo-constructor
    @Override
    public void reset() {
        setHighLowPassFilter( BYPASSED_DEFAULT,
                              FC_HIGH_PASS_DEFAULT,
                              ElectronicFilterType.HIGH_PASS,
                              FILTER_TYPE_HIGH_PASS_DEFAULT );
    }
}
