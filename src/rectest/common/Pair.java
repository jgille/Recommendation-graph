package rectest.common;

/**
 * This class represents a pair.
 *
 * @author Jon Ivmark
 */
public class Pair<CAR, CDR> {

    private final CAR car;
    private final CDR cdr;

    public Pair (CAR car, CDR cdr) {
	this.car = car;
	this.cdr = cdr;
    }

    public static <CAR, CDR> Pair<CAR, CDR> create (CAR car, CDR cdr) {
	return new Pair<CAR, CDR> (car, cdr);
    }

    public CAR car () {
	return car;
    }

    public CDR cdr () {
	return cdr;
    }
}
