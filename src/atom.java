/**
 * atom.java - definition of an atom, elements are subclasses of atom
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

import java.awt.Color;
import java.util.Vector;

public abstract class atom
{
  public static final String rcsid =
  "$Id: atom.java,v 1.3 1998/04/01 01:00:18 pcm Exp $";
  // hybridizations are a virtual enum
  public static final int SP3 = 0;
  public static final int SP2 = 1;
  public static final int SP  = 2;
  public static final int NONE = 3;

  // these should be defined within elements, as class variables
  public abstract String name ();
  public abstract String symbol ();
  public abstract int atomicNumber ();
  public abstract double mass ();
  public abstract double covalentRadius ();
  public abstract double vdwEnergy ();
  public abstract double vdwRadius ();
  public abstract int maxNumBonds ();
  public void setParms(Element e){} // probably should be abstract

  private Color col;
  private int color_index = -1;
  public Color color () { return col; }
  public void setColor(Color c,int i){ col = c; color_index = i; }
  public final int getColorIndex(){ return color_index; }

  // these should be instance variables
  public int Charge;
  public double fractionalCharge;
  public int hybridization;
  public double[] x;
  public double[] v;
  public double[] f;
  public Vector bonds;
  
  public atom ()
  {
    hybridization = NONE;
    bonds = new Vector();
    Charge = 0;
    fractionalCharge = 0.0;
    double zvec[] = { 0.0, 0.0, 0.0 };
    x = v = f = zvec;
  }
  public void zeroForce ()
  {
    double newf[] = { 0.0, 0.0, 0.0 };
    f = newf;
  }
  public int sigmaBonds ()
  {
    return bonds.size ();
  }
  // overload me, unless I'm hydrogen
  public void rehybridize ()
  {
    hybridization = NONE;
  }
}
