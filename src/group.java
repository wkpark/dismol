/**
 * group.java - group of atoms and terms
 * Copyright (c) 1997,1998,1999 Will Ware, all rights reserved.
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
import java.awt.image.ImageProducer;
import java.lang.Math;
import java.util.Vector;
import java.io.*;
import java.awt.Font;

import atom;
import PDBAtom;
import RasBuffer;
import Bond;
import term;
import view;
import dlentry;
import dl_atom;
import dl_bond;
import dlforce;
import RasFont;

import PngEncoder;

import Vert;

public class group
{
  public static final String rcsid =
  "$Id: group.java,v 1.8 2000/04/15 23:32:12 pcm Exp $";
  public Vector atomList;
  public Vector bondList;
//  public Vector chainList;
  public Vector termList;
  private boolean needToEnumerateTerms;
  private boolean showForces = false;
  private boolean calcForces = false;
  private Vector drawingList;
  public Dimension mypanel_size;
  public view v;
  public double forceMultiplier = 10.0;

  private Image backBuffer = null;
  private Graphics backGC;
  private Dimension backSize;

  private int xcol = -1;

  private double cenX=0,cenY=0,cenZ=0;
  private double worldSize,worldRadius;

  //
  public Font font;
  public int fontSize=14;
  public FontMetrics fontMet;

  public group ()
  {
    v = new view ();
    empty ();
  }
  public group (Panel p)
  {
    mypanel_size = p.getSize();
    v = new view ();
    empty ();
  }
  public final void setPanelSize(Dimension d)
  {
    mypanel_size = d;
  }
  public void updateViewSize ()
  {
    v.updateSize (mypanel_size.width, mypanel_size.height);
  }

  public void setDefaultZoomFactor()
  {
    v.zoomFactor = 25.;
  }

  public void empty ()
  {
    needToEnumerateTerms = true;
    atomList = new Vector ();
    bondList = new Vector ();
    termList = new Vector ();
  }
  public void setShowForces (boolean sf,boolean calc)
  {
    showForces = sf;
    if (calc) computeForces ();
  }

  public void setShowForces (boolean sf)
  {
    showForces = sf;
  }

  public int selectedAtomId (double[] scrPos)
  {
    int i;
    atom a;
    int aminid;
    double sqDist, minSqDist = 0;
    aminid = -1;
    double zpos = -1.e9;
    for (i = 0; i < atomList.size (); i++)
      {
        a = (atom) atomList.elementAt (i);
	dl_atom dla = new dl_atom(a,v);
	sqDist = dla.pixelSquaredDistance(scrPos);
	if (sqDist < 0)
	  {
	    if(dla.zvalue() > zpos)
	      {
		minSqDist = 0;
		aminid = i;
		zpos = dla.zvalue();
	      }
	  }
        else if (sqDist < minSqDist || i == 0)
          {
            minSqDist = sqDist;
            aminid = i;
          }
      }
    if (minSqDist < 3)
      return aminid;
    return -1;
  }

  public atom selectedAtom (double[] scrPos, boolean picky)
  {
    int i;
    atom a, amin;
    double sqDist, minSqDist = 0;
    amin = null;
    double zpos = -1.e9;
    for (i = 0; i < atomList.size (); i++)
      {
        a = (atom) atomList.elementAt (i);
	dl_atom dla = new dl_atom(a,v);
	sqDist = dla.pixelSquaredDistance(scrPos);
	if (sqDist < 0)
	  {
	    if(dla.zvalue() > zpos)
	      {
		minSqDist = 0;
		amin = a;
		zpos = dla.zvalue();
	      }
	  }
        else if (sqDist < minSqDist || i == 0)
          {
            minSqDist = sqDist;
            amin = a;
          }
      }
    // if we're picky, we need to be right on top of the atom
    if (!picky || minSqDist < 3)
      return amin;
    else
      return null;
  }
  public void addAtom (atom a)
  {
    needToEnumerateTerms = true;
    atomList.addElement (a);
  }
  public void addAtom (atom a, double[] scrPos)
  {
    needToEnumerateTerms = true;
    a.x = v.screenToXyz (scrPos);
    atomList.addElement (a);
  }
  public void addAtom (atom a, double x0, double x1, double x2)
  {
    needToEnumerateTerms = true;
    a.x[0] = x0;
    a.x[1] = x1;
    a.x[2] = x2;
    atomList.addElement (a);
  }
  public void deleteAtom (atom a)
  {
    int i;
    if (atomList.size () == 0)
      return;
    needToEnumerateTerms = true;
    // remove all bonds connected to the atom
    for (i = 0; i < atomList.size (); i++)
      {
	atom a2 = (atom) atomList.elementAt (i);
	if (a2.bonds.contains (a))
	  a2.bonds.removeElement (a);
      }
    // remove the atom
    atomList.removeElement (a);
    deleteBonds(a,null);
  }
  protected void deleteBonds(atom a1,atom a2)
  {
    int i;
    for (i = 0; i < bondList.size(); ++i)
    {
      Bond b = (Bond)bondList.elementAt(i);
      if(b.contains(a1) && (a2 == null || b.contains(a2)))
      {
	bondList.removeElement (b);
	if(a2 != null)
	  break;		// assume no duplicate bonds
      }
    }
  }
  public void addBond (atom a1, atom a2)
  {
    if (a1 == null || a2 == null)
      return;
    if (a1.bonds.contains (a2))
      return;
    needToEnumerateTerms = true;
    a1.bonds.addElement (a2);
    a2.bonds.addElement (a1);
    a1.rehybridize ();
    a2.rehybridize ();
    bondList.addElement(new Bond(a1,a2,Bond.NormBondFlag));
  }
  public void addBond (int a1, int a2)
  {
//    System.out.println("addBond: "+a1+","+a2);
    atom at1 = (atom) atomList.elementAt (a1);
    atom at2 = (atom) atomList.elementAt (a2);
    addBond (at1, at2);
  }
  public void deleteBond (atom a1, atom a2)
  {
    if (!a1.bonds.contains (a2))
      return;
    needToEnumerateTerms = true;
    a1.bonds.removeElement (a2);
    a2.bonds.removeElement (a1);
    a1.rehybridize ();
    a2.rehybridize ();
    deleteBonds(a1,a2);
  }

  public void resizeAtoms(double parm)
  {
    int i;
    v.atomsize_parm = parm;
    for (i = 0; i < atomList.size (); i++)
      ((PDBAtom)atomList.elementAt(i)).setParms();
  }

  public void setBond(double parm)
  {
    int i;
    v.bondsize_parm = parm;
  }

  public void centerAtoms (int mode)
  {
    int i, j;
    atom a;
    double[] x = { 0, 0, 0 };
    for (i = 0; i < atomList.size (); i++)
      {
        a = (atom) atomList.elementAt (i);
        for (j = 0; j < 3; j++)
          x[j] += a.x[j];
      }
    for (j = 0; j < 3; j++)
      x[j] /= atomList.size ();
    /*
    if (mode > 0)
      for (i = 0; i < atomList.size (); i++)
        {
          a = (atom) atomList.elementAt (i);
          for (j = 0; j < 3; j++)
            a.x[j] -= x[j];
        }
    */
    cenX=x[0];
    cenY=x[1];
    cenZ=x[2];
    if (mode > 0) {
      v.setDefaultCenter(cenX,cenY); // XXX
    }
  }

  public void InitialTransform ()
  {
    int j;
    double max=0.0;
    atom a;
    centerAtoms (0);

    for(j = 0; j < atomList.size(); ++j)
    {
        double dist;
        double dx,dy,dz;
        a = (atom) atomList.elementAt (j);

        dx=a.x[0];
        dy=a.x[1];
        dz=a.x[2];

        dist=dx*dx+dy*dy+dz*dz;
        if( dist > max )
           max = dist;
    }
    worldRadius = Math.sqrt(max);
//    LocalRadius = ((long)sqrt(max))+750.;
//    if (LocalRadius > WorldRadius) {
//      WorldRadius = LocalRadius;
//    }
    worldSize = worldRadius*2.0;
    RasBuffer.setWorldSize(worldSize);
//    System.out.println("worldSize="+worldSize);
  }

  private void drawAxisLine(Graphics g, boolean is_wireframe,
			    double[] start, double[] stop, int index,
			    double angstroms_between_ticks,
			    double angstroms_per_tick_side)
  {
    double[] scr1 = v.xyzToScreen (start);
    double[] scr2 = v.xyzToScreen (stop);
    if(is_wireframe)
      g.drawLine((int)scr1[0],(int)scr1[1],(int)scr2[0],(int)scr2[1]);
    else
    {
      RasBuffer.DrawLine((int)scr1[0],(int)scr1[1],(int)scr1[2],
			(int)scr2[0],(int)scr2[1],(int)scr2[2], 1, 1);
    }
    if(index >= 0)
    {
      double d[] = {stop[0] - start[0], stop[1] - start[1], stop[2] - start[2]};
      double length = Math.sqrt(d[0]*d[0] + d[1]*d[1] + d[2]*d[2]);
      double num_ticks = length/angstroms_between_ticks;
      d[0] /= num_ticks;
      d[1] /= num_ticks;
      d[2] /= num_ticks;
      double tick1start[] = { start[0], start[1], start[2] };
      double tick1stop[] = { start[0], start[1], start[2] };
      double tick2start[] = { start[0], start[1], start[2] };
      double tick2stop[] = { start[0], start[1], start[2] };
      int i = (index == 0 ? 1 : 0);
      tick1start[i] -= angstroms_per_tick_side;
      tick1stop[i] += angstroms_per_tick_side;
      i = (index == 2 ? 1 : 2);
      tick2start[i] -= angstroms_per_tick_side;
      tick2stop[i] += angstroms_per_tick_side;
      double j;
      for(j = 0; j < num_ticks; ++j)
      {
	drawAxisLine(g, is_wireframe, tick1start, tick1stop, -1, 0.0, 0.0);
	drawAxisLine(g, is_wireframe, tick2start, tick2stop, -1, 0.0, 0.0);
	for(i = 0; i < 3; ++i)
	{
	  tick1start[i] += d[i];
	  tick1stop[i] += d[i];
	  tick2start[i] += d[i];
	  tick2stop[i] += d[i];
	}
      }
    }

  }

  public double drawScaledAxes(Graphics g, boolean is_wireframe)
  {
    double minxy[] = { 0, 0, 0 };
    double maxxy[] = {(double)mypanel_size.width,(double)mypanel_size.height,0};
    double minscreen[] = v.screenToXyz(minxy);
    double maxscreen[] = v.screenToXyz(maxxy);
    double max_dimension = Math.max(mypanel_size.height,mypanel_size.width)/v.zoomFactor;
    double log10d = Math.log(max_dimension)/Math.log(10);
    double angstroms_between_ticks = Math.pow(10,Math.floor(log10d) - 1);
    double angstroms_per_tick_side = 2/v.zoomFactor;
    Color save_color = g.getColor();
    g.setColor(Color.blue);
    int i;
    for(i = 0; i < 3; ++i)
    {
      minxy[0] = maxxy[0] = 0;
      minxy[1] = maxxy[1] = 0;
      minxy[2] = maxxy[2] = 0;
      if(minscreen[i] < maxscreen[i])
      {
	minxy[i] = minscreen[i];
	maxxy[i] = maxscreen[i];
      }
      else
      {
	minxy[i] = maxscreen[i];
	maxxy[i] = minscreen[i];
      }
      drawAxisLine(g, is_wireframe, minxy, maxxy, i, angstroms_between_ticks,
		   angstroms_per_tick_side);
    }
    g.setColor(save_color);
    return angstroms_between_ticks;
  }

  public void drawAxes(Graphics g, boolean is_wireframe)
  {
    double size= 20;
    double ro[] = {0,0,0};
    double o[] = v.xyzToScreen(ro);
    double re[] = {o[0]+size,o[1]+size,o[2]+size};
    double e[] = v.screenToXyz(re);
    double u = Math.sqrt(e[0]*e[0] + e[1]*e[1] + e[2]*e[2]);
//    double u = v.worldRadius;
    double x[] = {u,0,0};
    double y[] = {0,u,0};
    double z[] = {0,0,u};

    double s[] = v.xyzToScreen (x);
    xcol = 42;//RasBuffer.MatchColour(100,100,100);

    Vert[] pp=new Vert[10];

    pp[0]=new Vert((int)s[0],(int)s[1],(int)s[2]);

    RasBuffer.DrawLine((int)o[0],(int)o[1],(int)o[2],
                       (int)s[0],(int)s[1],(int)s[2], xcol, 1);
    s = v.xyzToScreen (y);
    pp[1]=new Vert((int)s[0],(int)s[1],(int)s[2]);

    RasBuffer.DrawLine((int)o[0],(int)o[1],(int)o[2],
                       (int)s[0],(int)s[1],(int)s[2], xcol, 1);
    s = v.xyzToScreen (z);
    pp[2]=new Vert((int)s[0],(int)s[1],(int)s[2]);

    RasBuffer.DrawLine((int)o[0],(int)o[1],(int)o[2],
                       (int)s[0],(int)s[1],(int)s[2], xcol, 1);

    //pp[0].inten=xcol+10;
    //pp[1].inten=xcol+10;
    //pp[2].inten=xcol+10;
    //RasBuffer.ClipPolygon(pp,3);
    
  }

  public void drawLineToAtom (Graphics g, atom a, double x, double y)
  {
    dl_atom dummy = new dl_atom (a, v);
    dummy.drawLineToAtom (a, x, y, g);
  }
  public void bubblePaint (Panel mypanel)
  {
    int i;
    Vector dlist = new Vector ();
    //setBackgroundBuffer(mypanel);
    dl_atom dla = null;
    for (i = 0; i < atomList.size (); i++)
      {
	dla = new dl_atom ((atom) atomList.elementAt (i), v);
	dlist.addElement (dla);
      }
    if (dla != null)
      dla.quickpaint (dlist, backGC);
  }

  public void boundingboxPaint(Panel mypanel)
  {
    
  }

  public void wireframePaint (Panel mypanel)
  {
    int i, j;
    Vector dlist = new Vector ();
    setBackgroundBuffer(mypanel,true);
    //setBackgroundBuffer(mypanel,false);
    //if (calcForces)
    //  computeForces ();

    dl_bond dlb = null;
    dl_atom dla = null;
    for (i = 0; i < bondList.size (); i ++)
    {
      Bond b = (Bond)bondList.elementAt (i);
      atom a1 = b.sourceAtom();
      atom a2 = b.destAtom();
      dlb = new dl_bond (a1, a2, v);
      dlist.addElement (dlb);
    }
    for (i = 0; i < atomList.size (); i++)
    {
      atom a=(atom)atomList.elementAt(i);
      if (a.bonds.size () == 0) {
	dla = new dl_atom (a, v);
	dlist.addElement (dla);
      }
      if (showForces)
      {
        dlforce dlf = new dlforce (a.x, a.f, v);
        dlf.setForceMultiplier (forceMultiplier);
        dlist.addElement (dlf);
      }
    }
    if (dlb != null)
      dlb.quickpaint (dlist, backGC);
    else if (dla != null)
      dla.quickpaint (dlist, backGC);
  }

  private void setBackgroundBuffer(Panel mypanel,boolean use_ras)
  {
//    if (backSize == null || backSize.height != mypanel.getSize().height
//	|| backSize.width != mypanel.getSize().width)
    {
      RasBuffer.CPKColourAttrib(atomList);
      if(use_ras) {
	ImageProducer prod=
           RasBuffer.imageProducer(mypanel_size.width, mypanel_size.height);
	backBuffer=mypanel.createImage(prod);
      } else {
        backBuffer=mypanel.createImage(mypanel_size.width, mypanel_size.height);
        backGC = backBuffer.getGraphics();
      }
      backSize = mypanel_size;
    }
//    if(!use_ras) {
//      backGC.setColor(mypanel.getBackground());
//      backGC.fillRect(0,0,mypanel_size.width,mypanel_size.height);
//    }
  }

  public void stickPaint (Panel mypanel)
  {
    int i, j;
    dl_atom dla = null;
    dl_bond dlb = null;
    Vector dlist = new Vector ();
    setBackgroundBuffer(mypanel,true);
    //if (calcForces)
    //  computeForces ();

    for (i = 0; i < bondList.size (); i++)
      {
	Bond b = (Bond)bondList.elementAt (i);
	atom a1 = b.sourceAtom();
	atom a2 = b.destAtom();
	dlb = new dl_bond (a1, a2, v);
	dlist.addElement (dlb);
      }
//    long t1 = System.currentTimeMillis();
//    if (dla != null)
//	dla.paint (dlist, backGC);
    //long t2 = System.currentTimeMillis();
    for (i = 0; i < atomList.size (); i++)
    {
      atom a=(atom)atomList.elementAt(i);
      if (a.bonds.size () == 0) {
	dla = new dl_atom (a, v);
	dlist.addElement (dla);
      }
      if (showForces) {
        dlforce dlf = new dlforce (a.x, a.f, v);
        dlf.setForceMultiplier (forceMultiplier);
        dlist.addElement (dlf);
      }
    }
    if (dlb != null)
	dlb.paint (dlist, backGC);
    else if (dla != null)
	dla.paint (dlist, backGC);
    //long tnow = System.currentTimeMillis();
    //System.err.println("group paint time " + (tnow - t1) + " " + (tnow - t2));
  }
  public void fullPaint (Panel mypanel)
  {
    int i, j;
    dl_atom dla = null;
    dl_bond dlb = null;
    Vector dlist = new Vector ();
    setBackgroundBuffer(mypanel,true);
    //if (calcForces)
    // computeForces ();
    for (i = 0; i < atomList.size (); i++)
      {
        atom a = (atom) atomList.elementAt(i);
	dla = new dl_atom (a, v);
	dlist.addElement (dla);
	if (showForces)
	  {
	    dlforce dlf = new dlforce (a.x, a.f, v);
	    dlf.setForceMultiplier (forceMultiplier);
	    dlist.addElement (dlf);
	  }
      }
    if(v.atomsize_parm < 360)
      for (i = 0; i < bondList.size (); i++)
      {
	Bond b = (Bond)bondList.elementAt (i);
	atom a1 = b.sourceAtom();
	atom a2 = b.destAtom();
	dlb = new dl_bond (a1, a2, v);
	dlist.addElement (dlb);
      }
    //long t1 = System.currentTimeMillis();
    if (dla != null)
	dla.paint (dlist, backGC);
    //long t2 = System.currentTimeMillis();
    if (dlb != null)
	dlb.paint (dlist, backGC);
    //long tnow = System.currentTimeMillis();
    //System.err.println("group paint time " + (tnow - t1) + " " + (tnow - t2));
  }

  public void DisplayLabels (Panel mypanel)
  {
    int i, j;
    Vector dlist = new Vector ();
    for (i = 0; i < atomList.size (); i++)
      {
	PDBAtom a = (PDBAtom) atomList.elementAt(i);
        if (a.symbol() != "H " && a.symbol() != "C ") {
          double[] scr = v.xyzToScreen (a.x);
          RasFont.DisplayRasString((int)scr[0] + 4,(int)scr[1],(int)300,a.symbol(),a.getColorIndex()+20);
        }
      }
  }

  public void paint (Graphics g, Panel mypanel)
  {
    if(backBuffer != null) {
      g.drawImage(backBuffer, 0, 0, mypanel);
      backBuffer=null;
    }
  }

  public void banner (String str, Color co, Panel mypanel)
  {
    if (backBuffer==null) return;
    if (font == null) {
      font = new Font("Times", Font.BOLD, fontSize);
      fontMet = mypanel.getFontMetrics(font);
    }
    if (str == "") str="DisMol";

    int sw = fontMet.stringWidth(str);
    //int xx = (int)Math.round((double)(mypanel_size.width - sw) / 2D);
    //int yy = mypanel_size.height - fontSize - 15;
    int yy = fontSize;

    Image image = mypanel.createImage(mypanel_size.width,mypanel_size.height);
    Graphics2D g = (Graphics2D) image.getGraphics();
    g.drawImage(backBuffer, 0, 0, mypanel);

//    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                       RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(co);
    g.setFont(font);
    g.drawString(str, 2 , yy);
    backBuffer=image;
  }

  public byte[] getPng()
  {
    byte[] pngbytes;

    PngEncoder encode = new PngEncoder(backBuffer);
    pngbytes=encode.pngEncode();
    return pngbytes;
  }

  private void enumerateTerms ()
  {
    int i, j, k;
    if (!needToEnumerateTerms)
      return;
    needToEnumerateTerms = false;

    for (i = 0; i < atomList.size (); i++)
      ((atom) atomList.elementAt (i)).rehybridize ();

    termList = new Vector ();
 
    atom a = new carbon ();
    term t;
    t = new lterm (a, a);
    t.enumerate (atomList, termList);
    t = new aterm (a, a, a);
    t.enumerate (atomList, termList);
    t = new tterm (a, a, a, a);
    t.enumerate (atomList, termList);
    t = new lrterm ();
    t.enumerate (atomList, termList);
  }
  public void computeForces ()
  {
    int i;
    enumerateTerms ();
    for (i = 0; i < atomList.size (); i++)
      ((atom) atomList.elementAt (i)).zeroForce ();
    for (i = 0; i < termList.size (); i++)
      ((term) termList.elementAt (i)).computeForces ();
  }
  public void energyMinimizeStep (double stepsize)
  {
    int i;
    computeForces ();
    for (i = 0; i < atomList.size (); i++)
      {
	int j;
	double flensq, m;
	atom a = (atom) atomList.elementAt (i);
	for (j = 0, flensq = 0.0; j < 3; j++)
	  flensq += a.f[j] * a.f[j];
	if (flensq > 0.0)
	  {
	    m = stepsize / Math.sqrt (flensq);
	    for (j = 0; j < 3; j++)
	      a.x[j] += m * a.f[j];
	  }
      }
//    centerAtoms ();
  }
}

// vim:et:sts=2
