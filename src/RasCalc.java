/**
 * RasCalc.java
 * Copyright (c) 2003 Won-Kyu Park, all rights reserved.
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from abstree.c in RasMol v2.7.x
 * RasMol Molecular Graphics by Roger Sayle, August 1995, Version 2.6
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and other materials provided with the distribution.
 *
 * This software is provided "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability
 * or fitness for any particular purpose are disclaimed. In no event shall
 * Peter McCluskey be liable for any direct, indirect, incidental, special,
 * exemplary, or consequential damages (including, but not limited to,
 * procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of
 * liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage.
 */

import atom;

class RasCalc
{

  private static double Rad2Deg = 180.0/3.14159265358979;

  public static double CalcDistance (atom atm1, atom atm2) 
  {
    double dx, dy, dz;
    double dist2;
    dx = atm1.x[0] - atm2.x[0];
    dy = atm1.x[1] - atm2.x[1];
    dz = atm1.x[2] - atm2.x[2];
    if (dx!=0 || dy!=0 || dz!=0)
      {
	dist2 = dx * dx + dy * dy + dz * dz;
	return (Math.sqrt (dist2));
      }
    else
      return 0.0;
  }

  public static double CalcAngle (atom atm1, atom atm2, atom atm3) 
  {
    double ulen2, vlen2;
    double ux, uy, uz;
    double vx, vy, vz;
    double temp;
    ux = atm1.x[0] - atm2.x[0];
    
//#ifdef INVERT
    uy = atm2.x[1] - atm1.x[1]; // + atm2.fyorg - atm1.fyorg;
//#else	/*  */
//    uy = atm1.yorg - atm2.yorg + atm1.fyorg - atm2.fyorg;
//#endif	/*  */
    uz = atm1.x[2] - atm2.x[2]; // + atm1.fzorg - atm2.fzorg;
    if (0==ux && 0==uy && 0==uz)
      return 0.0;
    ulen2 = ux * ux + uy * uy + uz * uz;
    vx = atm3.x[0] - atm2.x[0];
//#ifdef INVERT
    vy = atm2.x[1] - atm3.x[1];
//#else	/*  */
//  vy = atm3.yorg - atm2.yorg + atm3.fyorg - atm2.fyorg;
//#endif	/*  */
    vz = atm3.x[2] - atm2.x[2];
    if (0==vx && 0==vy && 0==vz)
      return 0.0;
    vlen2 = vx * vx + vy * vy + vz * vz;
    temp = (ux * vx + uy * vy + uz * vz) / Math.sqrt (ulen2 * vlen2);
    return Rad2Deg * Math.acos (temp);
  }

  public static double CalcTorsion (atom atm1, atom atm2, atom atm3, atom atm4) 
  {
    double ax, ay, az;
    double bx, by, bz;
    double cx, cy, cz;
    double px, py, pz;
    double qx, qy, qz;
    double cosom, om;
    double rx, ry, rz;
    double plen, qlen;

    ax = atm2.x[0]- atm1.x[0];
    ay = atm2.x[1]- atm1.x[1];
    az = atm2.x[2]- atm1.x[2];
    if (0==ax && 0==ay && 0==az)
      return 0.0;
    bx = atm3.x[0]- atm2.x[0];
    by = atm3.x[1]- atm2.x[1];
    bz = atm3.x[2]- atm2.x[2];
    if (0==bx && 0==by && 0==bz)
      return 0.0;
    cx = atm4.x[0]- atm3.x[0];
    cy = atm4.x[1]- atm3.x[1];
    cz = atm4.x[2]- atm3.x[2];
    if (0==cx && 0==cy && 0==cz)
      return 0.0;
/*    
//#ifdef INVERT
    ay = -ay;
    by = -by;
    cy = -cy;
    
//#endif
*/
    az = -az;
    bz = -bz;
    cz = -cz;
    px = ay * bz - az * by;
    py = az * bx - ax * bz;
    pz = ax * by - ay * bx;
    qx = by * cz - bz * cy;
    qy = bz * cx - bx * cz;
    qz = bx * cy - by * cx;
    plen = px * px + py * py + pz * pz;
    qlen = qx * qx + qy * qy + qz * qz;
    cosom = (px * qx + py * qy + pz * qz) / Math.sqrt (plen * qlen);
    if (cosom > 1.0)
      {
	return 0.0;
      }
    else if (cosom < -1.0)
      return 180.0;
    om = -Rad2Deg * Math.acos (cosom);
    if (om < -180.)
      om += 360.;
    if (om > 180.)
      om -= 360.;
    rx = py * qz - pz * qy;
    ry = pz * qx - px * qz;
    rz = px * qy - py * qx;
    if (ax * rx + ay * ry + az * rz > 0.)
      return -om;
    return om;
  }

  /*
  double CalcDihedral (atom atm1, atom atm2, atom atm3, atom atm4) 
  {
    return (180.0 - CalcTorsion (atm1, atm2, atm3, atm4));
  }
  */
  
  /*
// Note: curr == prev.gnext! 
  double CalcPhiAngle (Group * prev, Group * curr) 
  {
    atom * prevc;
    atom * currca;
    atom * currc;
    atom * currn;
    if (!(prevc = FindGroupAtom (prev, 2)))
      return 360.0;
    if (!(currca = FindGroupAtom (curr, 1)))
      return 360.0;
    if (!(currc = FindGroupAtom (curr, 2)))
      return 360.0;
    if (!(currn = FindGroupAtom (curr, 0)))
      return 360.0;
    return CalcTorsion (prevc, currn, currca, currc);
  }
  
// Note: next == curr.gnext! 
  double CalcPsiAngle (Group * curr, Group * next) 
  {
    atom * nextn;
    atom * currca;
    atom * currc;
    atom * currn;
    if (!(nextn = FindGroupAtom (next, 0)))
      return 360.0;
    if (!(currca = FindGroupAtom (curr, 1)))
      return 360.0;
    if (!(currc = FindGroupAtom (curr, 2)))
      return 360.0;
    if (!(currn = FindGroupAtom (curr, 0)))
      return 360.0;
    return CalcTorsion (currn, currca, currc, nextn);
  }
  
// Note: prev == prev.gnext! 
  double CalcOmegaAngle (Group * prev, Group * curr) 
  {
    atom * prevc;
    atom * prevca;
    atom * currn;
    atom * currca;
    if (!(prevca = FindGroupAtom (prev, 1)))
      return 360.0;
    if (!(prevc = FindGroupAtom (prev, 2)))
      return 360.0;
    if (!(currn = FindGroupAtom (curr, 0)))
      return 360.0;
    if (!(currca = FindGroupAtom (curr, 1)))
      return 360.0;
    return (CalcTorsion (prevca, prevc, currn, currca));
  }

  */
}


