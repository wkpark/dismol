/**
 * PDBAtom.java - an Atom with pdb-related fields added
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from abstree.c
 * RasMol2 Molecular Graphics by Roger Sayle, August 1995, Version 2.6
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

import java.awt.*;
import java.lang.Math;
import java.util.Vector;
import atom;
import Element;

public class PDBAtom extends atom
{
  public static final String rcsid =
  "$Id: PDBAtom.java,v 1.3 1998/04/01 01:00:18 pcm Exp $";

/*=================*/
/*  Database Flags */
/*=================*/

  public static final int SelectFlag    = 0x01;
  public static final int DrawBondFlag  = 0x0e;
  public static final int AllAtomFlag   = 0x1c;
  public static final int HelixFlag     = 0x03;
  public static final int DrawKnotFlag  = 0x3e;
  public static final int WideKnotFlag  = 0x0e;

/* Atom Flags */
  public static final byte SphereFlag   = 0x02;     /* Sphere representation */
  public static final byte HeteroFlag   = 0x04;     /* HETATM record         */
  public static final byte HydrogenFlag = 0x08;     /* Hydrogen atom         */
  public static final byte NormAtomFlag = 0x10;
  public static final byte NonBondFlag  = 0x20;
  public static final byte BreakFlag    = 0x40;     /* Break in backbone     */

  public double radius;
  public long   xorg, yorg, zorg;          /* World Co-ordinates    */
  public int    serno;                     /* Atom Serial Number    */
  public int    temp;                      /* Temperature Factor    */
  public int    refno;                     /* ElemDesc index number */
  public byte   flag;                      /* Database flags        */
  public char   altl;                      /* Alternate Location    */
  private Object label;
  private int  irad;                      /* Image Radius          */
  private int  mbox;                      /* Shadow Casting NOnce  */
  public int elemno;
  private String aname = "?";
  private String asymbol = "?";
  public PDBAtom CurAtom = null;
  public static double atomsize_parm = 400;
  public static double bondsize_parm = 100;

  public PDBAtom()
  {
    xorg = yorg = zorg = 0;
    serno = temp = 0;
    /*
    if( CurAtom != null)
    {   ptr->anext = CurAtom->anext;
        CurAtom->anext = ptr;
    } else 
    {   ptr->anext = CurGroup->alist;
        CurGroup->alist = ptr;
    }
    */
    CurAtom = this;

    /*    SelectCount++; */
    flag = SelectFlag | NonBondFlag;
    label = null;
    radius = 375;
    altl = ' ';
    mbox = 0;
    setColor(Color.black,0);
  }

  public String name () { return aname; }
  public String symbol (){ return asymbol; }
  public int atomicNumber (){ return elemno; }
  public double mass (){ return 12; }
  public double covalentRadius () { return radius/1000; }
  public double vdwEnergy (){ return 0.357; }
  public double vdwRadius (){ return irad/100.0; }
  public int maxNumBonds (){ return 4; }
    static int cnt = 0;
  public final void setParms(){ setParms(elemno); }

  public void setParms(int enumber)
  {
    elemno = enumber;
    Element e = Element.getElement(enumber);
    irad = e.vdwrad;
    radius = atomsize_parm*vdwRadius();
    aname = e.name;
    asymbol = e.symbol;

    if(cnt++ < 0)
      System.err.println(aname + " radii " + vdwRadius());
  }

  public int GetElemNumber( int group_refno, String ptr)
  {
    char ch1,ch2;
    if(false /* IsNADGroup(group_refno) || IsCOAGroup(group_refno) */)
    {   /* Exceptions to Brookhaven Atom Naming! */
        ch1 = ' ';
    } else 
    {   ch1 = ptr.charAt(0);
        /* Handle HG, HD etc.. in Amino Acids! */
    /*
        if( (ch1=='H') && IsProtein(group_refno) )
            return( 1 );
    */
    }
    ch2 = ptr.charAt(1);

    if(false) System.err.println("GetElemNumber " + ptr);

    switch( ch1 )
    {   case(' '):  switch( ch2 )
                    {   case('B'):  return(  5 );
                        case('C'):  return(  6 );
                        case('D'):  return(  1 );
                        case('F'):  return(  9 );
                        case('H'):  return(  1 );
                        case('I'):  return( 53 );
                        case('K'):  return( 19 );
                        case('L'):  return(  1 );
                        case('N'):  return(  7 );
                        case('O'):  return(  8 );
                        case('P'):  return( 15 );
                        case('S'):  return( 16 );
                        case('U'):  return( 92 );
                        case('V'):  return( 23 );
                        case('W'):  return( 74 );
                        case('Y'):  return( 39 );
                    }
                    break;

        case('A'):  switch( ch2 )
                    {   case('C'):  return( 89 );
                        case('G'):  return( 47 );
                        case('L'):  return( 13 );
                        case('M'):  return( 95 );
                        case('R'):  return( 18 );
                        case('S'):  return( 33 );
                        case('T'):  return( 85 );
                        case('U'):  return( 79 );
                    }
                    break;

        case('B'):  switch( ch2 )
                    {   case('A'):  return( 56 );
                        case('E'):  return(  4 );
                        case('I'):  return( 83 );
                        case('K'):  return( 97 );
                        case('R'):  return( 35 );
                    }
                    break;

        case('C'):  switch( ch2 )
                    {   case('A'):  return( 20 );
                        case('D'):  return( 48 );
                        case('E'):  return( 58 );
                        case('F'):  return( 98 );
                        case('L'):  return( 17 );
                        case('M'):  return( 96 );
                        case('O'):  return( 27 );
                        case('R'):  return( 24 );
                        case('S'):  return( 55 );
                        case('U'):  return( 29 );
                    }
                    break;

        case('D'):  if( ch2=='Y' )
                        return( 66 );
                    break;

        case('E'):  if( ch2=='R' )
                    {   return( 68 );
                    } else if( ch2=='S' )
                    {   return( 99 );
                    } else if( ch2=='U' )
                        return( 63 );
                    break;

        case('F'):  if( ch2=='E' )
                    {   return(  26 );
                    } else if( ch2=='M' )
                    {   return( 100 );
                    } else if( ch2=='R' )
                        return(  87 );
                    break;

        case('G'):  if( ch2=='A' )
                    {   return( 31 );
                    } else if( ch2=='D' )
                    {   return( 64 );
                    } else if( ch2=='E' )
                        return( 32 );
                    break;

        case('H'):  if( ch2=='E' )
                    {   return(  2 );
                    } else if( ch2=='F' )
                    {   return( 72 );
                    } else if( ch2=='G' )
                    {   return( 80 );
                    } else if( ch2=='O' )
                        return( 67 );
                    break;

        case('I'):  if( ch2=='N' )
                    {   return( 49 );
                    } else if( ch2=='R' )
                        return( 77 );
                    break;

        case('K'):  if( ch2=='R' )
                        return( 36 );
                    break;

        case('L'):  if( ch2=='A' )
                    {   return(  57 );
                    } else if( ch2=='I' )
                    {   return(   3 );
                    } else if( (ch2=='R') || (ch2=='W') )
                    {   return( 103 );
                    } else if( ch2=='U' )
                        return(  71 );
                    break;

        case('M'):  if( ch2=='D' )
                    {   return( 101 );
                    } else if( ch2=='G' )
                    {   return(  12 );
                    } else if( ch2=='N' )
                    {   return(  25 );
                    } else if( ch2=='O' )
                        return(  42 );
                    break;

        case('N'):  switch( ch2 )
                    {   case('A'):  return(  11 );
                        case('B'):  return(  41 );
                        case('D'):  return(  60 );
                        case('E'):  return(  10 );
                        case('I'):  return(  28 );
                        case('O'):  return( 102 );
                        case('P'):  return(  93 );
                    }
                    break;

        case('O'):  if( ch2=='S' )
                        return( 76 );
                    break;

        case('P'):  switch( ch2 )
                    {   case('A'):  return( 91 );
                        case('B'):  return( 82 );
                        case('D'):  return( 46 );
                        case('M'):  return( 61 );
                        case('O'):  return( 84 );
                        case('R'):  return( 59 );
                        case('T'):  return( 78 );
                        case('U'):  return( 94 );
                    }
                    break;

        case('R'):  switch( ch2 )
                    {   case('A'):  return( 88 );
                        case('B'):  return( 37 );
                        case('E'):  return( 75 );
                        case('H'):  return( 45 );
                        case('N'):  return( 86 );
                        case('U'):  return( 44 );
                    }
                    break;

        case('S'):  switch( ch2 )
                    {   case('B'):  return( 51 );
                        case('C'):  return( 21 );
                        case('E'):  return( 34 );
                        case('I'):  return( 14 );
                        case('M'):  return( 62 );
                        case('N'):  return( 50 );
                        case('R'):  return( 38 );
                    }
                    break;

        case('T'):  switch( ch2 )
                    {   case('A'):  return( 73 );
                        case('B'):  return( 65 );
                        case('C'):  return( 43 );
                        case('E'):  return( 52 );
                        case('H'):  return( 90 );
                        case('I'):  return( 22 );
                        case('L'):  return( 81 );
                        case('M'):  return( 69 );
                    }
                    break;

        case('X'):  if( ch2=='E' )
                        return( 54 );
                    break;

        case('Y'):  if( ch2=='B' )
                        return( 70 );
                    break;

        case('Z'):  if( ch2=='N' )
                    {   return( 30 );
                    } else if( ch2=='R' )
                        return( 40 );
                    break;
    }

    if( (ch1>='0') && (ch1<='9') )
        if( (ch2=='H') || (ch2=='D') )
            return( 1 ); /* Hydrogen */

    return( 0 );
  }
}
