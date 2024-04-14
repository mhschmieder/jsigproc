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

public class AllPassFilters implements AcousticalFilter {

    // All Pass Filters are bypassed by default as they are never flat.
    protected static final boolean ALL_PASS_FILTERS_BYPASSED_DEFAULT = true;

    private boolean              _allPassFiltersBypassed;
    private int                  _numberOfFilters;
    private AllPassFilter[]      _allPassFilters;

    // This is the default constructor; it sets all instance variables to
    // default values based on the supplied center frequencies per filter.
    public AllPassFilters( final int numberOfFilters, final String[] centerFrequencies ) {
        this( ALL_PASS_FILTERS_BYPASSED_DEFAULT, numberOfFilters, null, centerFrequencies );
    }

    // This is the fully qualified constructor.
    public AllPassFilters( final boolean allPassFiltersBypassed,
                           final int numberOfFilters,
                           final AllPassFilter[] allPassFilters ) {
        this( allPassFiltersBypassed, numberOfFilters, allPassFilters, null );
    }

    // This is the superset constructor, to allow a common initialization path.
    public AllPassFilters( final boolean allPassFiltersBypassed,
                           final int numberOfFilters,
                           final AllPassFilter[] allPassFilters,
                           final String[] centerFrequencies ) {
        _allPassFiltersBypassed = allPassFiltersBypassed;
        _numberOfFilters = numberOfFilters;

        _allPassFilters = new AllPassFilter[ _numberOfFilters ];

        // Set the array to be copies of the source array, if present.
        if ( allPassFilters != null ) {
            final int numberOfFiltersToCopy = FastMath.min( allPassFilters.length, _numberOfFilters );
            for ( int filterIndex = 0; filterIndex < numberOfFiltersToCopy; filterIndex++ ) {
                _allPassFilters[ filterIndex ] = new AllPassFilter( allPassFilters[ filterIndex ] );
            }
        }
        else if ( centerFrequencies != null ) {
            final int numberOfFiltersToSet = FastMath.min( centerFrequencies.length, _numberOfFilters );
            for ( int filterIndex = 0; filterIndex < numberOfFiltersToSet; filterIndex++ ) {
                _allPassFilters[ filterIndex ] = new AllPassFilter( NumberUtilities
                        .parseDouble( centerFrequencies[ filterIndex ] ) );
            }
        }
    }

    // NOTE: This is the copy constructor, and is offered in place of clone()
    //  to guarantee that the source object is never modified by the new target
    //  object created here.
    public AllPassFilters( final AllPassFilters allPassFilters ) {
        this( allPassFilters.isAllPassFiltersBypassed(),
              allPassFilters.getNumberOfFilters(),
              allPassFilters.getAllPassFilters(),
              null );
    }

    // NOTE: Cloning is disabled as it is dangerous; use the copy constructor
    //  instead.
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final AllPassFilter getAllPassFilter( final int filterIndex ) {
        return _allPassFilters[ filterIndex ];
    }

    protected final AllPassFilter[] getAllPassFilters() {
        return _allPassFilters;
    }

    public final double getF( final int filterIndex ) {
        return _allPassFilters[ filterIndex ].getF();
    }

    // Return the All Pass Filter value at a given frequency (in Hertz).
    @Override
    public Complex getH( final double f ) {
        // Multiply all of the enabled all pass filter values together at a
        // given frequency, to compute the composite filter value.
        Complex h = Complex.ONE;

        if ( !_allPassFiltersBypassed ) {
            for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
                h = h.multiply( _allPassFilters[ filterIndex ].getH( f ) );
            }
        }

        // Return the All Pass Filter value at a given frequency.
        return h;
    }

    public final int getNumberOfFilters() {
        return _numberOfFilters;
    }

    public final double getO( final int filterIndex ) {
        return _allPassFilters[ filterIndex ].getO();
    }

    public final boolean isActiveEqMode() {
        // Iterate through the individual all pass filters, and if the overall
        // All Pass Filters is not bypassed and any of the individual All Pass
        // Filters are not bypassed, activate the EQ Mode.
        boolean activeEqMode = false;

        if ( !_allPassFiltersBypassed ) {
            for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
                if ( _allPassFilters[ filterIndex ].isActiveEqMode() ) {
                    activeEqMode = true;
                    break;
                }
            }
        }

        return activeEqMode;
    }

    public final boolean isAllAllPassFiltersBypassed() {
        boolean allBypassed = true;
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            if ( !isAllPassFilterBypassed( filterIndex ) ) {
                allBypassed = false;
                break;
            }
        }
        return allBypassed;
    }

    public final boolean isAllAllPassFiltersEnabled() {
        boolean allEnabled = true;
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            if ( isAllPassFilterBypassed( filterIndex ) ) {
                allEnabled = false;
                break;
            }
        }
        return allEnabled;
    }

    public final boolean isAllPassFilterBypassed( final int filterIndex ) {
        return _allPassFilters[ filterIndex ].isBypassed();
    }

    public final boolean isAllPassFiltersBypassed() {
        return _allPassFiltersBypassed;
    }

    public final boolean isEqBoostMode() {
        // Iterate through the individual all pass filters, and if any of them
        // have positive non-unity gain, activate the EQ Boost Mode.
        boolean eqBoostMode = false;

        if ( !_allPassFiltersBypassed ) {
            for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
                if ( _allPassFilters[ filterIndex ].isEqBoostMode() ) {
                    eqBoostMode = true;
                    break;
                }
            }
        }

        return eqBoostMode;
    }

    public final boolean isNonDefaultEqMode() {
        // Iterate through the individual All Pass filters, and if any of them
        // have non-default values, activate the Non-Default EQ Mode.
        boolean nonDefaultEqMode = false;

        if ( isAllPassFiltersBypassed() != ALL_PASS_FILTERS_BYPASSED_DEFAULT ) {
            return true;
        }

        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            if ( _allPassFilters[ filterIndex ].isNonDefaultEqMode() ) {
                nonDefaultEqMode = true;
                break;
            }
        }

        return nonDefaultEqMode;
    }

    public final void setAllAllPassFiltersBypassed( final boolean allAllPassFiltersBypassed ) {
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            setAllPassFilterBypassed( filterIndex, allAllPassFiltersBypassed );
        }
    }

    public final void setAllPassFilter( final int filterIndex, final AllPassFilter allPassFilter ) {
        _allPassFilters[ filterIndex ] = new AllPassFilter( allPassFilter );
    }

    public final void setAllPassFilter( final int filterIndex,
                                        final boolean allPassBypassed,
                                        final double f,
                                        final double o ) {
        _allPassFilters[ filterIndex ] = new AllPassFilter( allPassBypassed, f, o );
    }

    public final void setAllPassFilterBypassed( final int filterIndex,
                                                final boolean allPassFilterBypassed ) {
        _allPassFilters[ filterIndex ].setBypassed( allPassFilterBypassed );
    }

    // Pseudo-copy constructor.
    protected final void setAllPassFilters( final AllPassFilters allPassFilters ) {
        _allPassFiltersBypassed = allPassFilters.isAllPassFiltersBypassed();
        _numberOfFilters = allPassFilters.getNumberOfFilters();

        // Set the array to be copies of the source array.
        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            setAllPassFilter( filterIndex, allPassFilters.getAllPassFilter( filterIndex ) );
        }
    }

    public void setAllPassFiltersBypassed( final boolean allPassFiltersBypassed ) {
        _allPassFiltersBypassed = allPassFiltersBypassed;
    }

    // Fully qualified pseudo-constructor.
    public final void setDefaults( final int numberOfFilters, final String[] centerFrequency ) {
        _allPassFiltersBypassed = ALL_PASS_FILTERS_BYPASSED_DEFAULT;
        _numberOfFilters = numberOfFilters;

        for ( int filterIndex = 0; filterIndex < _numberOfFilters; filterIndex++ ) {
            _allPassFilters[ filterIndex ] = new AllPassFilter( NumberUtilities
                    .parseDouble( centerFrequency[ filterIndex ] ) );
        }
    }

    public final void setF( final int filterIndex, final double f ) {
        _allPassFilters[ filterIndex ].setF( f );
    }

    public final void setNumberOfFilters( final int numberOfFilters ) {
        _numberOfFilters = numberOfFilters;
    }

    public final void setO( final int filterIndex, final double o ) {
        _allPassFilters[ filterIndex ].setO( o );
    }
}
