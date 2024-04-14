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

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.mhschmieder.commonstoolkit.lang.NumberUtilities;

public class ParametricFilters implements AcousticalFilter {

    // Parametric Filters are enabled by default, as they are initially flat.
    protected static final boolean PARAMETRIC_FILTERS_BYPASSED_DEFAULT = false;

    private boolean              _parametricFiltersBypassed;
    private int                  _numberOfFilters;
    protected ParametricFilter[]   _parametricFilters;

    // This is the default constructor; it sets all instance variables to
    // default values.
    public ParametricFilters( final int numberOfFilters, final String[] centerFrequencies ) {
        this( PARAMETRIC_FILTERS_BYPASSED_DEFAULT, numberOfFilters, centerFrequencies );
    }

    // This is the default constructor; it sets all instance variables to
    // default values based on the supplied center frequencies per filter.
    public ParametricFilters( final boolean parametricFiltersBypassed,
                              final int numberOfFilters,
                              final String[] centerFrequencies ) {
        this( parametricFiltersBypassed, numberOfFilters, null, centerFrequencies );
    }

    // This is the fully qualified constructor.
    public ParametricFilters( final boolean parametricFiltersBypassed,
                              final int numberOfFilters,
                              final ParametricFilter[] parametricFilters ) {
        this( parametricFiltersBypassed, numberOfFilters, parametricFilters, null );
    }

    // This is the superset constructor, to allow a common initialization path.
    public ParametricFilters( final boolean parametricFiltersBypassed,
                              final int numberOfFilters,
                              final ParametricFilter[] parametricFilters,
                              final String[] centerFrequencies ) {
        _parametricFiltersBypassed = parametricFiltersBypassed;
        _numberOfFilters = numberOfFilters;

        _parametricFilters = new ParametricFilter[ _numberOfFilters ];

        // Set the array to be copies of the source array, if present.
        if ( parametricFilters != null ) {
            final int numberOfFiltersToCopy =
                    FastMath.min( parametricFilters.length, _numberOfFilters );
            for ( int filterIndex = 0; filterIndex < numberOfFiltersToCopy; filterIndex++ ) {
                _parametricFilters[ filterIndex ] =
                                                  new ParametricFilter( parametricFilters[ filterIndex ] );
            }
        }
        else if ( centerFrequencies != null ) {
            final int numberOfFiltersToSet = FastMath.min( centerFrequencies.length, _numberOfFilters );
            for ( int filterIndex = 0; filterIndex < numberOfFiltersToSet; filterIndex++ ) {
                _parametricFilters[ filterIndex ] = new ParametricFilter( NumberUtilities
                        .parseDouble( centerFrequencies[ filterIndex ] ) );
            }
        }
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public ParametricFilters( final ParametricFilters parametricFilters ) {
        this( parametricFilters.isParametricFiltersBypassed(),
              parametricFilters.getNumberOfFilters(),
              parametricFilters.getParametricFilters(),
              null );
    }

    // NOTE: Cloning is disabled as it is dangerous; use the copy constructor
    //  instead.
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final double getC( final int filterIndex ) {
        return _parametricFilters[ filterIndex ].getC();
    }

    public final double getF( final int filterIndex ) {
        return _parametricFilters[ filterIndex ].getF();
    }

    // Return the parametric filter value at a given frequency (in Hertz).
    @Override
    public final Complex getH( final double f ) {
        // Multiply all of the enabled parametric filter values together at a
        // given frequency, to compute the composite filter value.
        Complex h = Complex.ONE;

        if ( !_parametricFiltersBypassed ) {
            for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
                h = h.multiply( _parametricFilters[ filterIndex ].getH( f ) );
            }
        }

        // Return the Parametric Filter value at a given frequency.
        return h;
    }

    public final int getNumberOfFilters() {
        return _numberOfFilters;
    }

    public final double getO( final int filterIndex ) {
        return _parametricFilters[ filterIndex ].getO();
    }

    public final ParametricFilter getParametricFilter( final int filterIndex ) {
        return _parametricFilters[ filterIndex ];
    }

    protected final ParametricFilter[] getParametricFilters() {
        return _parametricFilters;
    }

    public final boolean isActiveEqMode() {
        // Iterate through the individual parametric filters, and if any of them
        // have non-unity gain, activate the EQ Mode.
        boolean activeEqMode = false;

        if ( !_parametricFiltersBypassed ) {
            for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
                if ( _parametricFilters[ filterIndex ].isActiveEqMode() ) {
                    activeEqMode = true;
                    break;
                }
            }
        }

        return activeEqMode;
    }

    public final boolean isAllParametricFiltersBypassed() {
        boolean allBypassed = true;
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            if ( !isParametricFilterBypassed( filterIndex ) ) {
                allBypassed = false;
                break;
            }
        }
        return allBypassed;
    }

    public final boolean isAllParametricFiltersEnabled() {
        boolean allEnabled = true;
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            if ( isParametricFilterBypassed( filterIndex ) ) {
                allEnabled = false;
                break;
            }
        }
        return allEnabled;
    }

    public final boolean isEqBoostMode() {
        // Iterate through the individual parametric filters, and if any of them
        // have positive non-unity gain, activate the EQ Boost Mode.
        boolean eqBoostMode = false;

        if ( !_parametricFiltersBypassed ) {
            for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
                if ( _parametricFilters[ filterIndex ].isEqBoostMode() ) {
                    eqBoostMode = true;
                    break;
                }
            }
        }

        return eqBoostMode;
    }

    public final boolean isNonDefaultEqMode() {
        // Iterate through the individual Parametric filters, and if any of them
        // have non-default values, activate the Non-Default EQ Mode.
        boolean nonDefaultEqMode = false;

        if ( isParametricFiltersBypassed() != PARAMETRIC_FILTERS_BYPASSED_DEFAULT ) {
            return true;
        }

        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            if ( _parametricFilters[ filterIndex ].isNonDefaultEqMode() ) {
                nonDefaultEqMode = true;
                break;
            }
        }

        return nonDefaultEqMode;
    }

    public final boolean isParametricFilterBypassed( final int filterIndex ) {
        return _parametricFilters[ filterIndex ].isBypassed();
    }

    public final boolean isParametricFiltersBypassed() {
        return _parametricFiltersBypassed;
    }

    public final void setAllParametricFiltersBypassed( final boolean allParametricFiltersBypassed ) {
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            setParametricFilterBypassed( filterIndex, allParametricFiltersBypassed );
        }
    }

    public final void setC( final int filterIndex,
                            final double c,
                            final boolean updateEquationParameters ) {
        _parametricFilters[ filterIndex ].setC( c, updateEquationParameters );
    }

    // Fully qualified pseudo-constructor.
    public final void setDefaults( final int numberOfFilters, final String[] centerFrequencies ) {
        _parametricFiltersBypassed = PARAMETRIC_FILTERS_BYPASSED_DEFAULT;
        _numberOfFilters = numberOfFilters;

        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            _parametricFilters[ filterIndex ] = new ParametricFilter( NumberUtilities
                    .parseDouble( centerFrequencies[ filterIndex ] ) );
        }
    }

    public final void setF( final int filterIndex,
                            final double f,
                            final boolean updateEquationParameters ) {
        _parametricFilters[ filterIndex ].setF( f, updateEquationParameters );
    }

    public final void setO( final int filterIndex,
                            final double o,
                            final boolean updateEquationParameters ) {
        _parametricFilters[ filterIndex ].setO( o, updateEquationParameters );
    }

    protected final void setParametricFilter( final int filterIndex,
                                              final ParametricFilter parametricFilter ) {
        _parametricFilters[ filterIndex ].setParametricFilter( parametricFilter );
    }

    protected final void setParametricFilter( final int filterIndex,
                                              final ParametricFilter parametricFilter,
                                              final boolean ignoreIfInactive ) {
        if ( ignoreIfInactive && !parametricFilter.isActiveEqMode() ) {
            return;
        }

        setParametricFilter( filterIndex, parametricFilter );
    }

    public final void setParametricFilterBypassed( final int filterIndex,
                                                   final boolean parametricFilterBypassed ) {
        _parametricFilters[ filterIndex ].setBypassed( parametricFilterBypassed );
    }

    // Pseudo-copy constructor.
    protected final void setParametricFilters( final ParametricFilters pParametricFilters ) {
        _parametricFiltersBypassed = pParametricFilters.isParametricFiltersBypassed();
        _numberOfFilters = pParametricFilters.getNumberOfFilters();

        // Set the array to be copies of the source array.
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            setParametricFilter( filterIndex,
                                 pParametricFilters.getParametricFilter( filterIndex ) );
        }
    }

    // Pseudo-copy constructor.
    protected final void setParametricFilters( final ParametricFilters pParametricFilters,
                                               final boolean pIgnoreIfInactive ) {
        // NOTE: If we are ignoring inactive filters, we are trying to isolate
        //  changes to an existing filter and therefore also avoid changing the
        //  status of the overall Bypassed flag for the Parametric Filters.
        _numberOfFilters = pParametricFilters.getNumberOfFilters();

        // Set the array to be copies of the source array.
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            setParametricFilter( filterIndex,
                                 pParametricFilters.getParametricFilter( filterIndex ),
                                 pIgnoreIfInactive );
        }
    }

    public final void setParametricFiltersBypassed( final boolean parametricFiltersBypassed ) {
        _parametricFiltersBypassed = parametricFiltersBypassed;
    }

    // TODO: Enforce this method on all filters via an interface.
    public final void calculateEqCoefficients( final int filterIndex ) {
        _parametricFilters[ filterIndex ].calculateEqCoefficients();
    }
}
