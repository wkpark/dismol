/**
 * dl_atom.java - entry in a drawing list, for drawing an atom
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * Copyright (c) 1997 Will Ware, all rights reserved.
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
 * Will Ware be liable for any direct, indirect, incidental, special,
 * exemplary, or consequential damages (including, but not limited to,
 * procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of
 * liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage.
 */

import java.awt.*;
import java.util.Vector;
import atom;

public class dl_atom extends dlentry
{
  public static final String rcsid =
  "$Id: dl_atom.java,v 1.4 1998/05/20 17:30:50 pcm Exp $";
  private double x,y,z, r1;  // screen coordinates for first atom
  private static double rvec[] = new double[3];
  private atom atm1;
  static int cnt = 0;
  private double radius = 0.3;
  private view vw;
  public dl_atom (atom a, view v)
  {
    atm1 = a;
    vw = v;
    v.xyzToScreen (a.x, rvec);
    x = rvec[0];
    y = rvec[1];
    z = rvec[2];
    r1 = radiusRatio * a.covalentRadius () * v.atomsize_parm * v.zoomFactor;
    r1 *= v.perspectiveFactor (rvec);
  }
  public double zvalue ()
  {
    return z;
  }
  public double xvalue ()
  {
    return x + r1;
  }
  public double yvalue ()
  {
    return y + r1;
  }
  // square of distance from displayed edge of atom, negative if inside atom
  public double pixelSquaredDistance(double[] scrPos)
  {
    double dx = x - scrPos[0];
    double dy = y - scrPos[1];
    return (dx * dx + dy * dy) - r1*r1;
  }
  public void quickpaint (Graphics g)
  {
    /*g.drawOval ((int)x, (int)y, (int)(2 * r1), (int)(2 * r1)); */
    atom a=atm1;
    int col = ((PDBAtom)atm1).getColorIndex();
    double sx[] = {a.x[0]+radius,a.x[1],a.x[2]};
    double sy[] = {a.x[0],a.x[1]+radius,a.x[2]};
    double sz[] = {a.x[0],a.x[1],a.x[2]+radius};

    sx = vw.xyzToScreen (sx);
    sy = vw.xyzToScreen (sy);
    sz = vw.xyzToScreen (sz);

    double ex[] = {a.x[0]-radius,a.x[1],a.x[2]};
    double ey[] = {a.x[0],a.x[1]-radius,a.x[2]};
    double ez[] = {a.x[0],a.x[1],a.x[2]-radius};

    ex = vw.xyzToScreen (ex);
    ey = vw.xyzToScreen (ey);
    ez = vw.xyzToScreen (ez);

    RasBuffer.ClipTwinLine((int)(ex[0]),(int)(ex[1]),(int)(ex[2]),
       (int)(sx[0]),(int)(sx[1]),(int)(sx[2]),col,col);
    RasBuffer.ClipTwinLine((int)(ey[0]),(int)(ey[1]),(int)(ey[2]),
       (int)(sy[0]),(int)(sy[1]),(int)(sy[2]),col,col);
    RasBuffer.ClipTwinLine((int)(ez[0]),(int)(ez[1]),(int)(ez[2]),
       (int)(sz[0]),(int)(sz[1]),(int)(sz[2]),col,col);

  }

  public void paint (Graphics g)
  {
    int iradius = (int)r1;
    int c = ((PDBAtom)atm1).getColorIndex();

    RasBuffer.DrawSphere((int)x, (int)y, (int)z, iradius, c);
  }
}
