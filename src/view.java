/**
 * view.java - camera angle, XYZ-to-screen coord translation, perspective
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

import java.lang.Math;
import java.lang.Double;

public class view
{
  public static final String rcsid =
  "$Id: view.java,v 1.5 1998/05/20 17:30:51 pcm Exp $";
  // rotation matrix
  private double[] m = { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
  private double[] r = { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
  private double[] Ir = { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
  public double zoomFactor = 25.0; // pixels per angstrom
  public double perspDist = 1000.0; // z distance for perspective, in pixels

//  public double origCX=0, origCY=0, origCZ=0; // from rasmol
//  public double worldRadius, worldSize;

  private double xCenterDefault = 0.0; // offset from 0,0
  private double yCenterDefault = 0.0;
  private double xCenter = 200.0;
  private double yCenter = 200.0;
//  private double zCenter = 0.0;
  private double zCenter = 10000.0;

  private double CenX = 0.0; // from rasmol
  private double CenY = 0.0;
  private double CenZ = 0.0;

  private boolean panned = false;
  private int renorm_counter = 0;

  public void updateSize (int x, int y)
  {
    if(!panned)
    {
      xCenter = xCenterDefault + x / 2;
      yCenter = yCenterDefault + y / 2;
    }
  }
  public void pan (int dx, int dy)
  {
    xCenter += dx;
    yCenter += dy;
    panned = true;
  }
  public void setDefaultCenter(double x, double y)
  {
    double dxy[] = new double[3];
    dxy[0] = x;
    dxy[1] = y;
    dxy[2] = 0;
    double dxdy[] = xyzToScreen(dxy);
    xCenterDefault = dxdy[0] - xCenter;
    yCenterDefault = dxdy[1] - yCenter;
  }

  public void CentreTransform( int xo, int yo, int zo, int xlatecen )
  {
/*
    double x, y, z;
        
        x = xo - CenX;
        y = yo - CenY; 
        z = zo - CenZ;
        if( xlatecen )
        {       DialValue[DialTX] += (x*MatX[0]+y*MatX[1]+z*MatX[2])/XRange;
                DialValue[DialTY] += (x*MatY[0]+y*MatY[1]+z*MatY[2])/YRange;
                DialValue[DialTZ] += (x*MatZ[0]+y*MatZ[1]+z*MatZ[2])/ZRange;
        }

        if( UseSlabPlane )
        {       DialValue[DialSlab] -= (x*MatZ[0]+y*MatZ[1]+z*MatZ[2])/ImageRadius;
                if( DialValue[DialSlab]<-1 )
                {       DialValue[DialSlab] = -1;
                        UseSlabPlane = False;
                        UseShadow = True;
                }
                if( DialValue[DialSlab]>1 )
                        DialValue[DialSlab] = 1;
        }

        if( UseDepthPlane )
        {       DialValue[DialBClip] -= (x*MatZ[0]+y*MatZ[1]+z*MatZ[2])/ImageRadius;
                if( DialValue[DialBClip]<-1 )
                        DialValue[DialBClip] = -1;
                if( DialValue[DialBClip]>1 )
                {       DialValue[DialBClip] = 1;
                        UseDepthPlane = False;
                        UseShadow = True;
                }
        }
*/

    CenX = xo;
    CenY = yo;
    CenZ = zo;

//    ReDrawFlag |= RFRotate;
  }

  private void renormalize (int i, int j, int k)
  {
    double r;
    r = 0.000001 + Math.sqrt (m[i] * m[i] + m[j] * m[j] + m[k] * m[k]);
    m[i] /= r;
    m[j] /= r;
    m[k] /= r;
  }
  public void rotate (double xAngle, double yAngle)//, double zAngle)
  {
    double sa, ca, m0, m1, m2, m3, m4, m5;
    sa = Math.sin (xAngle);
    ca = Math.cos (xAngle);

    m0 = ca * m[0] + sa * m[6];
    m1 = ca * m[1] + sa * m[7];
    m2 = ca * m[2] + sa * m[8];
    m[6] = -sa * m[0] + ca * m[6];
    m[7] = -sa * m[1] + ca * m[7];
    m[8] = -sa * m[2] + ca * m[8];
    m[0] = m0;
    m[1] = m1;
    m[2] = m2;


    sa = Math.sin (yAngle);
    ca = Math.cos (yAngle);

    m3 = ca * m[3] + sa * m[6];
    m4 = ca * m[4] + sa * m[7];
    m5 = ca * m[5] + sa * m[8];
    m[6] = -sa * m[3] + ca * m[6];
    m[7] = -sa * m[4] + ca * m[7];
    m[8] = -sa * m[5] + ca * m[8];
    m[3] = m3;
    m[4] = m4;
    m[5] = m5;

/*
    switch (renorm_counter++)
      {
      case 0:
        renormalize (0, 1, 2);
        break;
      case 1:
        renormalize (3, 4, 5);
        break;
      case 2:
        renormalize (6, 7, 8);
        break;
      case 3:
        renormalize (0, 3, 6);
        break;
      case 4:
        renormalize (1, 4, 7);
        break;
      default:
        renormalize (2, 5, 8);
        renorm_counter = 0;
        break;
      }
*/
  }

  public double perspectiveFactor (double z)
  {
    double denom = perspDist - z;
    if (denom < 5) denom = 5;
    return perspDist / denom;
  }

  public double perspectiveFactor (double[] scrPos)
  {
    return perspectiveFactor (scrPos[2] - zCenter);
  }

  public double[] xyzToScreen (double[] xyz)
  {
    double[] rvec = new double[3];
    return xyzToScreen(xyz, rvec);
  }

  public double[] xyzToScreen (double[] xyz, double[] rvec)
  {
    double x, y, z, denom;
    // rotate
    x = m[0] * xyz[0] + m[1] * xyz[1] + m[2] * xyz[2];
    y = m[3] * xyz[0] + m[4] * xyz[1] + m[5] * xyz[2];
    z = m[6] * xyz[0] + m[7] * xyz[1] + m[8] * xyz[2];
    // zoom
    x *= zoomFactor;
    y *= zoomFactor;
    z *= zoomFactor;
    // perspective
    double perspective = perspectiveFactor (z);
    x *= perspective;
    y *= perspective;
    // translation
    x += xCenter;
    y += yCenter;
    z += zCenter;

    rvec[0] = x;
    rvec[1] = y;
    rvec[2] = z;
    return rvec;
  }

  public double[] screenToXyz (double[] vec)
  {
    double x, y, z, denom;
    double[] rvec = new double[3];
    // undo translation
    x = vec[0] - xCenter;
    y = vec[1] - yCenter;
    z = vec[2] - zCenter;
    // undo persective
    double perspective = perspectiveFactor (z);
    x /= perspective;
    y /= perspective;
    // undo zoom
    x = x / zoomFactor;
    y = y / zoomFactor;
    z = z / zoomFactor;
    // undo rotation, multiply by the inverted matrix
    denom = 1.0 / (m[0] * (m[4] * m[8] - m[5] * m[7]) +
                   m[1] * (m[5] * m[6] - m[3] * m[8]) +
                   m[2] * (m[3] * m[7] - m[4] * m[6]));
    rvec[0] =
      ((m[4] * m[8] - m[5] * m[7]) * x +
       (m[2] * m[7] - m[1] * m[8]) * y +
       (m[1] * m[5] - m[2] * m[4]) * z) * denom;
    rvec[1] =
      ((m[5] * m[6] - m[3] * m[8]) * x +
       (m[0] * m[8] - m[2] * m[6]) * y +
       (m[2] * m[3] - m[0] * m[5]) * z) * denom;
    rvec[2] =
      ((m[3] * m[7] - m[4] * m[6]) * x +
       (m[1] * m[6] - m[0] * m[7]) * y +
       (m[0] * m[4] - m[1] * m[3]) * z) * denom;
    return rvec;
  }
}
