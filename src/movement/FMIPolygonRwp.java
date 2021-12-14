package movement;

import core.Coord;
import core.Settings;
import input.WKTReader;
import movement.map.SimMap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FMIPolygonRwp extends MapBasedMovement {

  public static final String POLYGON_FILE = "polygonFile";

//  final List<Coord> polygon = Arrays.asList(
//      new Coord( 500, 250 ),
//      new Coord( 250, 500 ),
//      new Coord( 500, 750 ),
//      new Coord( 750, 500 ),
//      new Coord( 500, 250 )
//  );

  private Coord lastWaypoint;
  private List<Coord> polygon;

  @Override
  public Path getPath() {
    final Path p;
    p = new Path( super.generateSpeed() );
    p.addWaypoint( this.lastWaypoint.clone() );

    Coord c;
    do {
      c = this.randomCoord();
    } while ( pathIntersects( this.polygon, this.lastWaypoint, c ) );
    p.addWaypoint( c );

    this.lastWaypoint = c;
    return p;
  }

  @Override
  public Coord getInitialLocation() {
    do {
      this.lastWaypoint = this.randomCoord();
    } while (isOutside(polygon, this.lastWaypoint));
    return this.lastWaypoint;
  }

  @Override
  public MapBasedMovement replicate() {
    return new FMIPolygonRwp( this );
  }

  private Coord randomCoord() {
    return new Coord(rng.nextDouble() * super.getMaxX(), rng.nextDouble() * super.getMaxY() );
  }

  public FMIPolygonRwp(final Settings settings ) {
    super( settings );

    String polygonFile = null;
    try {
      polygonFile = settings.getSetting(POLYGON_FILE);
    } catch (Throwable ignored) {
    }

    try {
      polygon = new LinkedList<>();
      List<Coord> allPointsRead = (new WKTReader()).readPoints(new File(polygonFile));
      for (Coord coord : allPointsRead) {
        SimMap map = getMap();
        Coord offset = map.getOffset();

        if (map.isMirrored()) {
          coord.setLocation(coord.getX(), -coord.getY());
        }
        coord.translate(offset.getX(), offset.getY());
        polygon.add(coord);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public FMIPolygonRwp(final FMIPolygonRwp other ) {
    super( other );
    this.polygon = other.polygon;
  }

  private static boolean pathIntersects(final List <Coord> polygon, final Coord start, final Coord end ) {
    final int count = countIntersectedEdges( polygon, start, end );
    return ( count > 0 );
  }

  private static boolean isInside(final List <Coord> polygon, final Coord point ) {
    final int count = countIntersectedEdges( polygon, point, new Coord( -10,0 ) );
    return ( ( count % 2 ) != 0 );
  }

  private static boolean isOutside(final List <Coord> polygon, final Coord point ) {
    return !isInside( polygon, point );
  }

  private static int countIntersectedEdges(final List <Coord> polygon, final Coord start, final Coord end ) {
    int count = 0;
    for ( int i = 0; i < polygon.size() - 1; i++ ) {
      final Coord polyP1 = polygon.get( i );
      final Coord polyP2 = polygon.get( i + 1 );

      final Coord intersection = intersection( start, end, polyP1, polyP2 );
      if ( intersection == null ) continue;

      if ( isOnSegment( polyP1, polyP2, intersection )
            && isOnSegment( start, end, intersection ) ) {
        count++;
      }
    }
    return count;
  }

  private static boolean isOnSegment(final Coord L0, final Coord L1, final Coord point ) {
    final double crossProduct = ( point.getY() - L0.getY() ) * ( L1.getX() - L0.getX() ) - ( point.getX() - L0.getX() ) * ( L1.getY() - L0.getY() );
    if ( Math.abs( crossProduct ) > 0.0000001 )
      return false;

    final double dotProduct = ( point.getX() - L0.getX() ) * ( L1.getX() - L0.getX() ) + ( point.getY() - L0.getY() ) * ( L1.getY() - L0.getY() );
    if ( dotProduct < 0 )
      return false;

    final double squaredLength = ( L1.getX() - L0.getX() ) * ( L1.getX() - L0.getX() ) + (L1.getY() - L0.getY() ) * (L1.getY() - L0.getY() );
    if ( dotProduct > squaredLength )
      return false;
    return true;
  }

  private static Coord intersection(final Coord L0_p0, final Coord L0_p1, final Coord L1_p0, final Coord L1_p1 ) {
    final double[] p0 = getParams( L0_p0, L0_p1 );
    final double[] p1 = getParams( L1_p0, L1_p1 );
    final double D = p0[ 1 ] * p1[ 0 ] - p0[ 0 ] * p1[ 1 ];
    if ( D == 0.0 ) return null;

    final double x = ( p0[ 2 ] * p1[ 1 ] - p0[ 1 ] * p1[ 2 ] ) / D;
    final double y = ( p0[ 2 ] * p1[ 0 ] - p0[ 0 ] * p1[ 2 ] ) / D;

    return new Coord( x, y );
  }

  private static double[] getParams(final Coord c0, final Coord c1 ) {
    final double A = c0.getY() - c1.getY();
    final double B = c0.getX() - c1.getX();
    final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
    return new double[] { A, B, C };
  }
}