/* ===================================================
 *  file:       main.c
 * ---------------------------------------------------
 *  purpose:	sample using the free method for
 *              spiral computation
 * ---------------------------------------------------
 *  using methods of CEPHES library
 * ---------------------------------------------------
 *  first edit:	09.03.2010 by M. Dupuis @ VIRES GmbH
 *  last mod.:  10.03.2020 by N. Dillmann @ ASAM e.V.
 * =================================================== 
 * ASAM OpenDRIVE 1.6 Spiral example implementation
 * 
 * 
 * (C) by ASAM e.V., 2020
 * Any use is limited to the scope described in the license terms. 
 * The license terms can be viewed at www.asam.net/license
 */
 
 
#include <stdio.h>
#include "odrSpiral.h"

int main( int argc, char** argv )
{
    double s;
    double t;
    double x;
    double y;
    
    fprintf( stderr, "#  OpenDRIVE spiral example\n" );
    fprintf( stderr, "#  ------------------------\n#\n" );
    fprintf( stderr, "#  Computing a spiral with\n" );
    fprintf( stderr, "#    initial curvature =   0.000 1/m\n" );
    fprintf( stderr, "#    length            = 300.000 m\n" );
    fprintf( stderr, "#    curvDot           =   0.001 1/m2\n#\n" );
    
    for ( s = 0.0; s < 300.0; s += 1.0 )
    {
        odrSpiral( s, 0.001, &x, &y, &t );
        fprintf( stderr, "%10.4f %10.4f \n", x, y );
    }
    fprintf( stderr, "#  done.\n" );
    
}
