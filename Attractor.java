
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class Attractor implements Supplier<Stream<double[]>> {

    final int ndim_;
    final UnaryOperator<double[]> func_;
    final double[] seed_;

    public Attractor( int ndim, UnaryOperator<double[]> func ) {
        this( ndim, func, new double[ ndim ] );
    }

    public Attractor( int ndim, UnaryOperator<double[]> func, double[] seed ) {
        ndim_ = ndim;
        func_ = func;
        seed_ = seed;
    }

    public Stream<double[]> get() {
        return Stream.iterate( seed_, func_ );
    }

    public static double[] randoms( Random rnd, int n, double absmax ) {
        double[] rnds = new double[ n ];
        for ( int i = 0; i < n; i++ ) {
            rnds[ i ] = 2 * absmax * rnd.nextDouble() - absmax;
        }
        return rnds;
    }

    public static Attractor clifford( double[] p4 ) {
        final double a = p4[ 0 ];
        final double b = p4[ 1 ];
        final double c = p4[ 2 ];
        final double d = p4[ 3 ];
        return new Attractor( 2, xy -> {
            double x = xy[ 0 ];
            double y = xy[ 1 ];
            return new double[] {
                Math.sin( a * y ) + c * Math.cos( a * x ),
                Math.sin( b * x ) + d * Math.cos( b * y ),
            };
        } ) {
            @Override
            public String toString() {
                return "clifford" + Arrays.toString( p4 );
            }
        };
    }

    public static Attractor rampe3( double[] p6 ) {
        return new Attractor( 3, xyz -> {
            double x = xyz[ 0 ];
            double y = xyz[ 1 ];
            double z = xyz[ 2 ];
           return new double[] {
               x * z * Math.sin( p6[0] * x ) - Math.cos( p6[1] * y ),
               y * x * Math.sin( p6[2] * y ) - Math.cos( p6[3] * z ),
               z * y * Math.sin( p6[4] * z ) - Math.cos( p6[5] * x ),
           };
        } ) {
            @Override
            public String toString() {
                return "rampe" + Arrays.toString( p6 );
            }
        };
    }

    public static double getSpaceFraction( Attractor att, int np, int nbin ) {
        int ndim = att.ndim_;
        Bounds bounds =
            att.get().skip( 100 ).limit( np ) 
           .collect( () -> new Bounds( att.ndim_),
                     Bounds::update, Bounds::combine );
        Grid grid =
            att.get().skip( 100 ).limit( np )
           .collect( () -> new Grid( bounds, nbin ),
                     Grid::accept, Grid::combine );
        return grid.getFillFraction();
    }

    private static class Grid {
        private final Bounds bounds_;
        private final int nbin_;
        private final int ndim_;
        private final double[] factors_;
        private int[] grid_;
        Grid( Bounds bounds, int nbin ) {
            bounds_ = bounds;
            nbin_ = nbin;
            ndim_ = bounds.ndim_;
            factors_ = new double[ ndim_ ];
            int npix = 1;
            for ( int i = 0; i < ndim_; i++ ) {
                factors_[ i ] = 0.99999999 * nbin
                              / ( bounds.maxs_[ i ] - bounds.mins_[ i ] );
                npix *= nbin;
            }
            grid_ = new int[ npix ];
        }
        public void accept( double[] point ) {
            grid_[ toBinIndex( point ) ]++;
        }
        public double getFillFraction() {
            int nf = 0;
            for ( int i = 0; i < grid_.length; i++ ) {
                if ( grid_[ i ] > 0 ) {
                    nf++;
                }
            }
            return 1.0 * nf / grid_.length;
        }
        public Grid combine( Grid other ) {
            for ( int i = 0; i < grid_.length; i++ ) {
                grid_[ i ] += other.grid_[ i ];
            }
            return this;
        }
        private int toBinIndex( double[] point ) {
            int ibin = 0;
            int step = 1;
            for ( int i = 0; i < ndim_; i++ ) {
                ibin += step * toBinCoord( i, point[ i ] );
                step *= nbin_;
            }
            return ibin;
        }
        private int toBinCoord( int idim, double value ) {
            int ibin =
                (int) ( factors_[ idim ] * ( value - bounds_.mins_[ idim ] ) );
            return Math.min( nbin_ - 1, Math.max( 0, ibin ) );
        }
    }

    private static class Bounds {
        final int ndim_;
        double[] mins_;
        double[] maxs_;
        Bounds( int ndim ) {
            ndim_ = ndim;
            mins_ = new double[ ndim ];
            maxs_ = new double[ ndim ];
            for ( int i = 0; i < ndim; i++ ) {
                mins_[ i ] = + Double.MAX_VALUE;
                maxs_[ i ] = - Double.MAX_VALUE;
            }
        }
        void update( double[] point ) {
            for ( int i = 0; i < ndim_; i++ ) {
                mins_[ i ] = Math.min( mins_[ i ], point[ i ] );
                maxs_[ i ] = Math.max( maxs_[ i ], point[ i ] );
            }
        }
        Bounds combine( Bounds other ) {
            for ( int i = 0; i < ndim_; i++ ) {
                mins_[ i ] = Math.min( this.mins_[ i ], other.mins_[ i ] );
                maxs_[ i ] = Math.max( this.maxs_[ i ], other.maxs_[ i ] );
            }
            return this;
        }
        @Override
        public String toString() {
            StringBuffer sbuf = new StringBuffer();
            for ( int i = 0; i < ndim_; i++ ) {
                sbuf.append( (float) mins_[ i ] + " - " + (float) maxs_[ i ] )
                    .append( " ; " );
            }
            return sbuf.toString();
        }
    }
}
