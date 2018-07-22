import java.util.List;
import java.util.ArrayList;

/**
 * An immutable representation of a tetris piece in a particular rotation. Each
 * piece is defined by the blocks that make up its body.
 * 
 * Typical client code looks like...
 * 
 * <pre>
 * Piece pyra = new Piece(PYRAMID_STR); // Create piece from string
 * int width = pyra.getWidth(); // 3
 * Piece pyra2 = pyramid.computeNextRotation(); // get rotation
 * 
 * Piece[] pieces = Piece.getPieces(); // the array of all root pieces
 * </pre>
 */
public class Piece {

	// String constants for the standard 7 Tetris pieces
	public static final String STICK_STR = "0 0 0 1 0 2 0 3";
	public static final String L1_STR = "0 0 0 1 0 2 1 0";
	public static final String L2_STR = "0 0 1 0 1 1 1 2";
	public static final String S1_STR = "0 0 1 0 1 1 2 1";
	public static final String S2_STR = "0 1 1 1 1 0 2 0";
	public static final String SQUARE_STR = "0 0 0 1 1 0 1 1";
	public static final String PYRAMID_STR = "0 0 1 0 1 1 2 0";

	// Attributes
	private List<TPoint> body;
	private List<Integer> skirt;
	private int width;
	private int height;
	private Piece next;
	static private Piece[] pieces; // singleton static array of first rotations

	/**
	 * Defines a new piece given a TPoint[] array of its body. Makes its own
	 * copy of the array and the TPoints inside it.
	 */
	public Piece(List<TPoint> points) {
	    // YOUR CODE HERE
		this.body = new ArrayList<TPoint>(points);
		this.width = this.computeWidth();
		this.height = this.computeHeight();
		this.skirt = this.computeSkirt();
	} 	

	/**
	 * Alternate constructor, takes a String with the x,y body points all
	 * separated by spaces, such as "0 0 1 0 2 0 1 1". (provided)
	 */
	public Piece(String points) {
		this(parsePoints(points));
	}
	
	// calcule de la longueurn
	
	private int computeWidth() {
		int X = 0;
		for(TPoint point_x : this.body){
			if(point_x.x > X){
				X = point_x.x;
			}
		}
		return X + 1;
	}
	
	//calcule de la hauteur
	
	private int computeHeight() {
		int Y = 0;
		for(TPoint point_y : this.body){
			if(point_y.y > Y){
				Y = point_y.y;
			}
		}
		return Y + 1;
	}
	
	// calcule de la jupe
	
	private List<Integer> computeSkirt() {
		List<Integer> tab = new ArrayList<Integer>(this.width);
		int min_y = this.body.get(0).y;
		for(TPoint pt : this.body){
			if(pt.y < min_y){
				min_y = pt.y;
			}
		}
		
		for(TPoint pt : this.body){
			if(pt.y == min_y){
				tab.add(pt.y);
			}else{
				if(!body.contains(new TPoint(pt.x, pt.y - 1))){
					tab.add(pt.y);
				}
			}
		}
		return tab;
	}
	
	
	public Piece(Piece piece) {
		this.height = piece.computeHeight();
		this.width  = piece.computeWidth();
		this.body = new ArrayList<TPoint>();
		this.skirt = piece.computeSkirt();
	}


	/**
	 * Given a string of x,y pairs ("0 0 0 1 0 2 1 0"), parses the points into a
	 * TPoint[] array. (Provided code)
	 */
	private static List<TPoint> parsePoints(String rep) {
	    // YOUR CODE HERE
	    List<TPoint> res = new ArrayList<TPoint>(0);
	    String[] tab = rep.split(" ");
	    int x  = 0;
	    int y = 0;
	    for(int i = 0; i < tab.length; i = i + 2){
	    	x  = Integer.parseInt(tab[i]);
	    	if(i < tab.length){
	    		y = Integer.parseInt(tab[i+1]);
	    	}
	    	res.add(new TPoint(x,y));
	    }
	    
		return res;
	    
	}
	
	/**
	 * Returns the width of the piece measured in blocks.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height of the piece measured in blocks.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns a reference to the piece's body. The caller should not modify this
	 * list.
	 */
	public List<TPoint> getBody() {
		return this.body;
	}

	/**
	 * Returns a reference to the piece's skirt. For each x value across the
	 * piece, the skirt gives the lowest y value in the body. This is useful for
	 * computing where the piece will land. The caller should not modify this
	 * list.
	 */
	public List<Integer> getSkirt() {
		return this.skirt;
	}

	/**
	 * Returns a new piece that is 90 degrees counter-clockwise rotated from the
	 * receiver.
	 */
	public Piece computeNextRotation() {
	    // calcule de la next rotation 
		List<TPoint> rotationBody = new ArrayList<TPoint>();
		int newMaxX = height - 1;
		for(int i = 0 ; i < this.body.size() ; i++){
			TPoint p_rotation = new TPoint(newMaxX- body.get(i).y, body.get(i).x);
			rotationBody.add(p_rotation);
		}
		//Sort(rotationBody);
		Piece computedNext = new Piece(rotationBody);
	    return computedNext;
	}

	/**
	 * Returns true if two pieces are the same -- their bodies contain the same
	 * points. Interestingly, this is not the same as having exactly the same
	 * body arrays, since the points may not be in the same order in the bodies.
	 * Used internally to detect if two rotations are effectively the same.
	 */
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		
		if( !(obj instanceof Piece)){
			return false;
		}
		
		Piece p = (Piece) obj;
		return(this.width == p.width && this.height == p.height &&  this.skirt.containsAll(p.skirt) && this.body.containsAll(p.body));
	    
	}

	public String toString() {
	    // YOUR CODE HERE
		String s = "";
		for(TPoint pt : body){
			s += pt.toString();
		}
		return s;
	}

	/**
	 * Returns an array containing the first rotation of each of the 7 standard
	 * tetris pieces in the order STICK, L1, L2, S1, S2, SQUARE, PYRAMID. The
	 * next (counterclockwise) rotation can be obtained from each piece with the
	 * {@link #fastRotation()} message. In this way, the client can iterate
	 * through all the rotations until eventually getting back to the first
	 * rotation. (provided code)
	 */
	public static Piece[] getPieces() {
		// lazy evaluation -- create static array if needed
		if (Piece.pieces == null) {
			Piece.pieces = new Piece[] { 
					new Piece(STICK_STR), 
					new Piece(L1_STR),
					new Piece(L2_STR), 
					new Piece(S1_STR),
					new Piece(S2_STR),
					new Piece(SQUARE_STR),
					new Piece(PYRAMID_STR)};
		}

		return Piece.pieces;
	}
	
	/*public static void main (String [] args){
			TPoint t1 = new TPoint(1,0);
			TPoint t2 = new TPoint(0,1);
			TPoint t3 = new TPoint(1,1);
			TPoint t4 = new TPoint(2,8);
			List<TPoint> listA = new ArrayList<TPoint>();
	
			
			listA.add(t1); listA.add(t2); listA.add(t3); listA.add(t4);
			// listA.addAll(t1,t2,t3,t4);
	
	
			Piece pyr1 = new Piece(listA);
			Piece pyr2 = new Piece("0 0 1 0 1 1 2 0");
			
		
			System.out.println(pyr2.toString());
			System.out.println(pyr2.computeNextRotation().toString());
		}*/
	public static void main(String[] s){
		Piece pyr1 = new Piece(Piece.PYRAMID_STR);
		Piece pyr2 = pyr1.computeNextRotation();
		Piece pyr3 = pyr2.computeNextRotation();
		System.out.println(pyr2);
		System.out.println(pyr3);
		//Piece pyr1 = new Piece(Piece.PYRAMID_STR);

		//assertEquals(new ArrayList<Integer>(Arrays.asList(new Integer[] {0, 0, 0})), pyr1.getSkirt());
		System.out.println(pyr1.getSkirt());
		Piece pyr4 = new Piece(Piece.S1_STR);	
		System.out.println(pyr4.getSkirt());
		//assertEquals(new ArrayList<Integer>(Arrays.asList(new Integer[] {0, 0, 1})), s.getSkirt());
	}

	public Piece fastRotation() {
		
		return next;
	}

}
