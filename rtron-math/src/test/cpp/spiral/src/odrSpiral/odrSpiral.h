/* ===================================================
 *  file:       odrSpiral.c
 * ---------------------------------------------------
 *  purpose:	free sample for computing spirals
 *              in OpenDRIVE applications 
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
 
 
/**
* compute the actual "standard" spiral, starting with curvature 0
* @param s      run-length along spiral
* @param cDot   first derivative of curvature [1/m2]
* @param x      resulting x-coordinate in spirals local co-ordinate system [m]
* @param y      resulting y-coordinate in spirals local co-ordinate system [m]
* @param t      tangent direction at s [rad]
*/

extern void odrSpiral( double s, double cDot, double *x, double *y, double *t );

/**
 * added to make this function accessible
 */
extern void fresnel( double xxa, double *ssa, double *cca );
