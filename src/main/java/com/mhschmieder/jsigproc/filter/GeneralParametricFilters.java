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

import com.mhschmieder.jcommons.lang.NumberUtilities;

public final class GeneralParametricFilters extends ParametricFilters {

    public static final int                               NUMBER_OF_FILTERS  = 10;

    // Declare the full list of center frequencies for General Parametric
    // Filters.
    @SuppressWarnings("nls") public static final String[] CENTER_FREQUENCIES = {
                                                                                 "32",
                                                                                 "63",
                                                                                 "125",
                                                                                 "250",
                                                                                 "500",
                                                                                 "1000",
                                                                                 "2000",
                                                                                 "4000",
                                                                                 "8000",
                                                                                 "16000" };

    // This is the default constructor; it sets all instance variables to
    // default values.
    public GeneralParametricFilters() {
        this( PARAMETRIC_FILTERS_BYPASSED_DEFAULT );
    }

    // This is the default constructor; it sets all instance variables to
    // default values, but sets a supplied bypassed status in advance.
    public GeneralParametricFilters( final boolean generalParametricFiltersBypassed ) {
        this( generalParametricFiltersBypassed, null, CENTER_FREQUENCIES );
    }

    // This is the fully qualified constructor.
    public GeneralParametricFilters( final boolean generalParametricFiltersBypassed,
                                     final ParametricFilter[] parametricFilters ) {
        this( generalParametricFiltersBypassed, parametricFilters, null );
    }

    // This is the superset constructor, to allow a common initialization path.
    public GeneralParametricFilters( final boolean generalParametricFiltersBypassed,
                                     final ParametricFilter[] parametricFilters,
                                     final String[] centerFrequencies ) {
        super( generalParametricFiltersBypassed,
               NUMBER_OF_FILTERS,
               parametricFilters,
               centerFrequencies );
    }

    // This is the copy constructor.
    public GeneralParametricFilters( final GeneralParametricFilters generalParametricFilters ) {
        this( generalParametricFilters.isParametricFiltersBypassed(),
              generalParametricFilters.getParametricFilters() );
    }

    // Default pseudo-constructor.
    public void reset() {
        setDefaults( NUMBER_OF_FILTERS, CENTER_FREQUENCIES );
    }

    /**
     * This method resets the upper five filters of the General Parametric
     * Filters, for when they're not in use, to avoid class-switching.
     */
    public void resetUpperParametricFilters() {
        for ( int filterIndex = 5; filterIndex < NUMBER_OF_FILTERS; filterIndex++ ) {
            _parametricFilters[ filterIndex ] = new ParametricFilter( NumberUtilities
                    .parseDouble( CENTER_FREQUENCIES[ filterIndex ] ) );
        }
    }

    // Pseudo-copy constructor.
    public void setGeneralParametricFilters( final GeneralParametricFilters pGeneralParametricFilters ) {
        setParametricFilters( pGeneralParametricFilters );
    }

    public void setGeneralParametricFilters( final GeneralParametricFilters pGeneralParametricFilters,
                                             final boolean pIgnoreIfInactive ) {
        setParametricFilters( pGeneralParametricFilters, pIgnoreIfInactive );
    }
}
