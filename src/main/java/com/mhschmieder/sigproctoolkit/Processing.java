/**
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
 * This file is part of the SigprocToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * SigprocToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/sigproctoolkit
 */
package com.mhschmieder.sigproctoolkit;

import org.apache.commons.math3.complex.Complex;

/**
 * The <code>Processing</code> interface is an interface for setting and
 * getting processing values. It is more audio-focused than AcousticalFilter.
 */
public interface Processing {

    boolean isMuted();

    void setMuted( final boolean muted );

    double getGainDb();

    void setGainDb( final double gainDb );

    double getDelayMs();

    void setDelayMs( final double delayMs );

    boolean isParametricFilterBypassed();

    void setParametricFilterBypassed( final boolean parametricFilterBypassed );

    boolean isActiveEqMode( final boolean calculateAllEnabledFiltersOverride,
                            final boolean ignoreMutedFilters );

    boolean isEqBoostMode();

    /**
     * Return the filter values at all given frequencies (in Hertz).
     *
     * @param numberOfBins
     *            The number of bins in the list of given frequencies
     * @param calculateAllEnabledFiltersOverride
     *            Flag for whether we override other criteria as long as a
     *            specific low-level filter isn't bypassed
     * @return The Complex amplitude/phase filter values at all given
     *         frequencies
     */
    Complex[] getFilterH( final int numberOfBins,
                          final boolean calculateAllEnabledFiltersOverride );

    /**
     * Return the filter value at a given frequency (in Hertz).
     *
     * @param f
     *            The center band frequency for the filter calculation
     * @param calculateAllEnabledFiltersOverride
     *            Flag for whether we override other criteria as long as a
     *            specific low-level filter isn't bypassed
     * @return The Complex amplitude/phase filter value at the given frequency
     */
    Complex getH( final double f, final boolean calculateAllEnabledFiltersOverride );
}