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

public final class GeneralAllPassFilters extends AllPassFilters {

    public static final int      NUMBER_OF_FILTERS  = 3;

    // Declare the full list of center frequencies for General All Pass Filters.
    public static final String[] CENTER_FREQUENCIES = {
                                                        "32",               //$NON-NLS-1$
                                                        "64",               //$NON-NLS-1$
                                                        "128",              //$NON-NLS-1$
                                                        "256" };            //$NON-NLS-1$

    // This is the default constructor; it sets all instance variables to
    // default values.
    public GeneralAllPassFilters() {
        super( NUMBER_OF_FILTERS, CENTER_FREQUENCIES );
    }

    // This is the default constructor; it sets all instance variables to
    // default values, but sets a supplied bypassed status in advance.
    public GeneralAllPassFilters( final boolean allPassFiltersBypassed ) {
        this( allPassFiltersBypassed, null );
    }

    // This is the fully qualified constructor.
    public GeneralAllPassFilters( final boolean allPassFiltersBypassed,
                                  final AllPassFilter[] allPassFilters ) {
        super( allPassFiltersBypassed, NUMBER_OF_FILTERS, allPassFilters );
    }

    // This is the copy constructor.
    public GeneralAllPassFilters( final GeneralAllPassFilters generalAllPassFilters ) {
        super( generalAllPassFilters );
    }

    // Default pseudo-constructor
    public void reset() {
        setDefaults( NUMBER_OF_FILTERS, CENTER_FREQUENCIES );
    }

    // Pseudo-copy constructor.
    public void setGeneralAllPassFilters( final GeneralAllPassFilters generalAllPassFilters ) {
        setAllPassFilters( generalAllPassFilters );
    }
}
