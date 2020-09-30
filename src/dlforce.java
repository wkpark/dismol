/**
 * dlforce.java - entry in a drawing list
 * Copyright (c) 1997,1998 Will Ware, all rights reserved.
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

public class dlforce extends dlentry
{
  public static final String rcsid =
  "$Id: dlforce.java,v 1.2 1998/04/02 23:03:15 pcm Exp $";
  private static int force_color = 3; // CPK green
  //private static Color force_color = Color.green;
  private double begin[], end[], orig[], f[], r1, zval;
  private double forceMultiplier;
  private double arrowHeadSize = 0.4;
  private double zoomFactor = 1;
  private final static double sqrtHalf = Math.sqrt (0.5);

  public dlforce (double origin[], double f0[], view v)
  {
    int i;
    double rvec[] = new double[3];
    v.xyzToScreen (origin, rvec);

    orig = new double[3];
    f = new double[3];
    vw = v;

    begin = v.xyzToScreen (origin);
    for (i = 0; i < 3; i++)
      {
	f[i] = f0[i];
	orig[i] = origin[i];
      }
    setForceMultiplier (10.0);
    r1 = radiusRatio * 0.2 * v.zoomFactor;
    r1 *= v.perspectiveFactor (rvec);
    zoomFactor= v.zoomFactor;
  }

  public void setForceMultiplier (double fm)
  {
    int i;
    end = new double[3];
    double ff[] = {0,0,0};

    forceMultiplier = fm;
    double norm = Math.sqrt(f[0]*f[0]+f[1]*f[1]+f[2]*f[2]);
    if (norm > 5.0) {
      ff[0]=f[0]/norm*5;
      ff[1]=f[1]/norm*5;
      ff[2]=f[2]/norm*5;
    } else {
      ff[0]=f[0]; ff[1]=f[1]; ff[2]=f[2];
    }

    for (i = 0; i < 3; i++) {
      end[i] = orig[i] + ff[i] * forceMultiplier;
      //System.err.println("Force:" + factor);
    }
    end = vw.xyzToScreen (end);
    zval = (begin[2] + end[2]) / 2;
  }
  public double zvalue ()
  {
    return zval;
  }
  public void quickpaint (Graphics g)
  {
    /*
    drawBondLine (g, Color.green, Color.green, begin, end);
    */
    RasBuffer.ClipTwinLine((int)begin[0], (int)begin[1], (int)begin[2],
                (int)end[0], (int)end[1], (int)end[2],
                force_color, force_color);
    double[] u = new double[3];
    double[] v = new double[3];
    u[0] = end[1] - begin[1];
    u[1] = begin[0] - end[0];
    u[2] = begin[2] - end[2];
    v[0] = end[0] - begin[0];
    v[1] = end[1] - begin[1];
    v[2] = u[2];

    double m = Math.sqrt (u[0] * u[0] + u[1] * u[1]);
    if (m > arrowHeadSize)
      m = sqrtHalf * arrowHeadSize / m;
    else
      m = sqrtHalf;
    m*=zoomFactor;
    u[0] *= m;
    u[1] *= m;
    u[2] *= m;
    v[0] *= m;
    v[1] *= m;
    v[2] *= m;
    double m1 = Math.sqrt (u[0] * u[0] + u[1] * u[1]);

    /*
    g.setColor (Color.green);
    g.drawLine ((int)end[0], (int)end[1], (int)(end[0] + u[0] - v[0]),
		(int) (end[1] + u[1] - v[1]));
    g.drawLine ((int)end[0], (int)end[1], (int)(end[0] - u[0] - v[0]),
		(int) (end[1] - u[1] - v[1]));
		*/
    RasBuffer.ClipTwinLine((int)end[0], (int)end[1], (int)end[2],
                (int)(end[0] + u[0] - v[0]) , (int)(end[1] + u[1] - v[1]),
		(int)(end[2] + u[2] - v[2]), force_color, force_color);
    RasBuffer.ClipTwinLine((int)end[0], (int)end[1], (int)end[2],
                (int)(end[0] - u[0] - v[0]) , (int)(end[1] - u[1] - v[1]),
		(int)(end[2] - u[2] - v[2]), force_color, force_color);
  }
  public void paint (Graphics g)
  {
	  /*
    int i;
    double xd = (x1 - x2);
    double yd = (y1 - y2);
    double zd = (z1 - z2);
    double lensq = xd * xd + yd * yd + zd * zd;
    double len = 0.01 + Math.sqrt(lensq);
    */
    {
	//force_color=RasBuffer.CPKColor( 2 /* CPK Green */);
	//force_color=3; //RasBuffer.CPKColor( 2 /* CPK Green */);
        //System.err.println("Force color #2: " + force_color);
    }

    RasBuffer.DrawCylinder((int)begin[0],
			  (int)begin[1],
			  (int)begin[2],
			  (int)end[0],
			  (int)end[1],
			  (int)end[2],
			  force_color, force_color,
			  (int)r1);
    double[] u = new double[3];
    double[] v = new double[3];
    u[0] = end[1] - begin[1];
    u[1] = begin[0] - end[0];
    u[2] = begin[2] - end[2];
    v[0] = end[0] - begin[0];
    v[1] = end[1] - begin[1];
    v[2] = u[2];
    double m = Math.sqrt (u[0] * u[0] + u[1] * u[1]);
    if (m > arrowHeadSize)
      m = sqrtHalf * arrowHeadSize / m;
    else
      m = sqrtHalf;
    m*=zoomFactor;
    u[0] *= m;
    u[1] *= m;
    u[2] *= m;
    v[0] *= m;
    v[1] *= m;
    v[2] *= m;
    double m1 = Math.sqrt (u[0] * u[0] + u[1] * u[1]);

    RasBuffer.DrawCylinder((int)end[0], (int)end[1], (int)end[2],
                (int)(end[0] + u[0] - v[0]) , (int)(end[1] + u[1] - v[1]),
		(int)(end[2] + u[2] - v[2]),
		force_color, force_color,(int)r1);
    RasBuffer.DrawCylinder((int)end[0], (int)end[1], (int)end[2],
                (int)(end[0] - u[0] - v[0]) , (int)(end[1] - u[1] - v[1]),
		(int)(end[2] - u[2] - v[2]),
		force_color, force_color,(int)r1);
			  /*
    System.err.println("" + begin[0] + "  " + begin[1] + "  " +  begin[2]);
    System.err.println("" + end[0] + "  " + end[1] + "  " +  end[2]);
    */

  }
}
