
import java.io.IOException;
import java.util.Random;
import java.util.function.Supplier;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableOutput;
import uk.ac.starlink.table.ValueInfo;

public class ChaosWriter {

    public static final ValueInfo FILL_INFO =
        new DefaultValueInfo( "FillFactor", Double.class,
                              "Proportion of space filled" );

    public static void writeFiles( Supplier<Attractor> supplier, int nrow,
                                   double minFill, int nfile, String baseName )
            throws IOException {
        StarTableOutput sto = new StarTableOutput();
        for ( int ig = 0; ig < nfile; ) {
            Attractor attractor = supplier.get();
            double frac = Attractor.getSpaceFraction( attractor, 100 );
            if ( frac > minFill ) {
                String loc = baseName + ++ig + ".fits";
                System.out.println( "\n" + loc + "\t" + frac +
                                    "\t" + attractor );
                StarTable table = new AttractorStarTable( attractor, nrow );
                table.setParameter( new DescribedValue( FILL_INFO,
                                                        new Double( frac ) ) );
                sto.writeStarTable( table, loc, "fits" );
            }
            else {
                System.out.print( "." );
            }
        }
    }

    public static void main( String[] args ) throws IOException {
        Random rnd = new Random( 456623 );
        writeFiles( () -> Attractor.clifford( Attractor.randoms( rnd, 4, 2. ) ),
                    10_000_000, 0.3, 8, "c" );
        writeFiles( () -> Attractor.rampe( Attractor.randoms( rnd, 6, 2. ) ),
                    10_000_000, 0.01, 8, "r" );
    }
}
