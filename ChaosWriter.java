
import java.io.IOException;
import java.util.Random;
import uk.ac.starlink.table.StarTableOutput;

public class ChaosWriter {

    public static void main( String[] args ) throws IOException {
        int nrow = 10_000_000;
        Random rnd = new Random( 456623 );
        StarTableOutput sto = new StarTableOutput();
        for ( int ig = 0; ig < 10; ) {
            Attractor attractor =
                Attractor.clifford( Attractor.randoms( rnd, 4, 2 ) );
            double frac = Attractor.getSpaceFraction( attractor, 100_000, 100 );
            if ( frac > 0.4 ) {
                String loc = "a" + ig++ + ".fits";
                System.out.println( "\n" + loc + "\t" + frac );
                sto.writeStarTable( new AttractorStarTable( attractor, nrow ),
                                    loc, "fits" );
            }
            else {
                System.out.print( "." );
            }
        }
    }
}
