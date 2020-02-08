
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.stream.Stream;
import uk.ac.starlink.table.AbstractStarTable;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.ValueInfo;

public class AttractorStarTable extends AbstractStarTable {

    private final Attractor att_;
    private final long nrow_;
    private final int ndim_;
    private final ColumnInfo[] colInfos_;
    public static final ValueInfo ATTRACTOR_INFO =
        new DefaultValueInfo( "Attractor", String.class );

    public AttractorStarTable( Attractor att, int nrow ) {
        att_ = att;
        nrow_ = nrow;
        ndim_ = att.ndim_;
        colInfos_ = new ColumnInfo[ ndim_ ];
        for ( int id = 0; id < ndim_; id++ ) {
            char nchr = ndim_ <= 3 ? "xyz".charAt( id )
                                   : (char) ( 'a' + id );
            colInfos_[ id ] = new ColumnInfo( Character.toString( nchr ),
                                              Double.class,
                                              "Value of dimension #" + id );
        }
        setParameter( new DescribedValue( ATTRACTOR_INFO, att.toString() ) );
    }

    public int getColumnCount() {
        return ndim_;
    }

    public long getRowCount() {
        return nrow_;
    }

    public ColumnInfo getColumnInfo( int icol ) {
        return colInfos_[ icol ];
    }

    public RowSequence getRowSequence() {
        Stream<double[]> stream = att_.get().skip( 100 );
        if ( nrow_ >= 0 ) {
            stream = stream.limit( nrow_ );
        }
        Iterator<double[]> it = stream.iterator();
        return new RowSequence() {
            double[] point_;
            public boolean next() {
                if ( it.hasNext() ) {
                    point_ = it.next();
                    return true;
                }
                else {
                    return false;
                }
            }
            public Double getCell( int ic ) {
                return new Double( point_[ ic ] );
            }
            public Object[] getRow() {
                Object[] row = new Object[ ndim_ ];
                for ( int id = 0; id < ndim_; id++ ) {
                    row[ id ] = new Double( point_[ id ] );
                }
                return row;
            }
            public void close() {
            }
        };
    }
}
