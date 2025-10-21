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

import com.mhschmieder.jsigproc.dsp.DspConstants;

public abstract class DigitalFilter implements AcousticalFilter {
    
    // The sampling frequency to use for pre-warping in filter algorithms.
    protected double samplingFrequencyHz;

    public DigitalFilter() {
        this( DspConstants.DEFAULT_SAMPLING_FREQUENCY_HZ );
    }

    public DigitalFilter( final double pSamplingFrequencyHz ) {
        samplingFrequencyHz = pSamplingFrequencyHz;
    }

    public double getSamplingFrequencyHz() {
        return samplingFrequencyHz;
    }
    
    public void setSamplingFrequencyHz( final double pSamplingFrequencyHz ) {
        samplingFrequencyHz = pSamplingFrequencyHz;
    }
}
