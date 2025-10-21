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

public final class LowPassFilter extends HighLowPassFilter {

    // Low Pass frequency, range 32 Hz to 20 kHz (160 Hz default).
    private static final double FC_LOW_PASS_DEFAULT = 160d;

    // Default for which Low Pass filter type to use.
    public static final HighLowPassFilterType FILTER_TYPE_LOW_PASS_DEFAULT =
                                                                           HighLowPassFilterType.LOW_PASS;

    // ///////////////////////////////////////////////////////////////
    // Constructors
    // This is the generic default constructor; it sets all instance variables
    // to default values.
    public LowPassFilter() {
        this( FC_LOW_PASS_DEFAULT );
    }

    // This is the default constructor; it sets all instance variables to
    // default values, but sets a supplied bypassed status in advance.
    public LowPassFilter( final boolean lowPassBypassed ) {
        this( lowPassBypassed, FC_LOW_PASS_DEFAULT, FILTER_TYPE_LOW_PASS_DEFAULT );
    }

    // This is the preferred specific constructor, when all initial values are
    // known.
    public LowPassFilter( final boolean lowPassBypassed,
                          final double fc,
                          final HighLowPassFilterType highLowPassFilterType ) {
        super( lowPassBypassed, fc, ElectronicFilterType.LOW_PASS, highLowPassFilterType );
    }

    // This is the preferred default constructor; it sets all instance variables
    // to default values, except for frequency.
    private LowPassFilter( final double fc ) {
        this( BYPASSED_DEFAULT, fc, FILTER_TYPE_LOW_PASS_DEFAULT );
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public LowPassFilter( final LowPassFilter lowPassFilter ) {
        this( lowPassFilter.isBypassed(),
              lowPassFilter.getFc(),
              lowPassFilter.getHighLowPassFilterType() );
    }

    // This method detects whether any of the filter parameters have been
    // altered from their default state.
    @Override
    public boolean isNonDefaultEqMode() {
        return ( isBypassed() != BYPASSED_DEFAULT ) || ( getFc() != FC_LOW_PASS_DEFAULT )
                || ( getElectronicFilterType() != ElectronicFilterType.LOW_PASS )
                || ( getHighLowPassFilterType() != FILTER_TYPE_LOW_PASS_DEFAULT );
    }

    // Default pseudo-constructor
    @Override
    public void reset() {
        setHighLowPassFilter( BYPASSED_DEFAULT,
                              FC_LOW_PASS_DEFAULT,
                              ElectronicFilterType.LOW_PASS,
                              FILTER_TYPE_LOW_PASS_DEFAULT );
    }
}
