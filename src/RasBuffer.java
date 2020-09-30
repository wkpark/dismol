/**
 * RasBuffer.java
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from transfor.c, pixutils.c and render.c in
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
import java.awt.Color;
import java.awt.image.MemoryImageSource;
import java.awt.image.DirectColorModel;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.util.Vector;

class RefCountColor
{
  int refcount;
  Color color;
  RefCountColor(int r,int g,int b)
  {
    color = new Color(r,g,b);
    refcount = 0;
  }
  RefCountColor()
  {
    color = null;
    refcount = 0;
  }
  public final int getRed(){ return color.getRed(); }
  public final int getGreen(){ return color.getGreen(); }
  public final int getBlue(){ return color.getBlue(); }
}

class ViewStruct extends Object
{
  public int fbuf[];
  public short dbuf[];
  public int xmax, ymax;
  public int yskip;
  public int size;

  ViewStruct(int high, int wide)
  {
    ymax = high;
    xmax = wide+4;
    int dx=xmax % 4;
    if( dx != 0)
       xmax += 4-dx;
    yskip = xmax;
    
    size = xmax*ymax;
    fbuf = new int[size + 32];
    dbuf = new short[size + 32];
    int i;
    for(i = 0; i < size; ++i)
      dbuf[i] = -32000;
  }

  public void Initialise()
  {
    int i;
    if (size > 0) {
    for(i = 0; i < size; ++i)
      dbuf[i] = -32000;
    }
  }
}

class ArcEntry extends Object
{
  static final int ARCSIZE = 2048;
  short dx,dy,dz;
  double x;
  short inten;
  int offset;

  ArcEntry()
  {
    dx = dy = dz = 0;
    inten = 0;
    offset = 0;
    x=0;
  }
}

class ArcTableEntry extends Object
{
  static final int ARCSIZE = 2048;
  double x;

  ArcTableEntry()
  {
    x=0;
  }
}

class Edge extends Object
{
  long dx,dz,di;
  long x,z,i;

  Edge()
  {
    dx = dz = di = 0;
    x = z = i = 0;
  }
}

/*
class Vert
{
  int x,y,z;
  int inten;

  public Vert()
  {
    x=y=z=0;
    inten=0;
  } 
}

*/

class ShadeRef
{
  int col;
  int shade;
  Color color;

  ShadeRef(int cola,int shadea,int r,int g,int b)
  {
    color = new Color(r,g,b);
    col = cola;
    shade = shadea;
  }
}

public class RasBuffer
{
  public static ViewStruct view;
  static short LookUp[][];

  private static final double Ambient = 0.4; // Rasmol default
  private static final boolean EIGHTBIT = false;
  private static final int DefaultColDepth = (EIGHTBIT ? 16 : 32);
  private static int ColourDepth = DefaultColDepth;
  private static int ColourMask = ColourDepth-1;
  private static final int LutSize = (EIGHTBIT ? 256 : 1024); // 32 * 32
  private static int Lut[] = new int[LutSize];
  private static ColorModel colorModel;
  private static boolean ULut[] = new boolean[LutSize];
  private static int RLut[] = new int[LutSize];
  private static int GLut[] = new int[LutSize];
  private static int BLut[] = new int[LutSize];
  private static final int BackCol = 0;
  private static final int BoxCol = 1;
  private static final int LabelCol = 2;
  private static final int FirstCol = 3;
  private static int BackR,BackG,BackB;
  private static int LabR,LabG,LabB;
  private static int BoxR,BoxG,BoxB;
  private static boolean UseBackFade = false;
  private static boolean FakeSpecular = true;
  private static int SpecPower = 8;

  public static long t1 = System.currentTimeMillis();

  static ArcEntry ArcAc[] = new ArcEntry[ArcEntry.ARCSIZE];
  static int ArcAcPtr = 0;
  static ArcEntry ArcDn[] = new ArcEntry[ArcEntry.ARCSIZE];
  static int ArcDnPtr = 0;
  static ArcTableEntry ArcTable[] = new ArcTableEntry[ArcTableEntry.ARCSIZE];
  static int ArcTablePtr = 0;
  static int CurrentRad = 0;

  private static MemoryImageSource RasmolImg=null;
  private static boolean RedrawFlag=true;
 
  private static double worldSize=0;
  private static double worldRadius=0;

/* These values set the sizes of the sphere rendering
 * tables. The first value, maxrad, is the maximum
 * sphere radius and the second value is the table
 * size = (maxrad*(maxrad+1))/2 + 1
 */
  //#define MAXTABLE  32641
  private static final int MAXRAD = 255;
  private static int ColConst[] = new int[MAXRAD];

  private static final int MAXSHADE = 32;
  //private static final int MAXSHADE = 64;
  /*  private static ShadeRef ScaleRef[] = new ShadeRef[MAXSHADE]; */
  private static RefCountColor Shade[] = null;
  /*
  private static int MaskColour[MAXMASK];
  private static int MaskShade[MAXMASK];
  */
  private static int ScaleCount;
  private static int LastShade;

  private static double LastRX,LastRY,LastRZ;
  private static double Zoom;

  public static int Colour2Shade(int x){ return ((int)((x)-FirstCol)/ColourDepth); }
  public static int Shade2Colour(int x){ return ((x)*ColourDepth+FirstCol); }

  private static boolean XValid(int x){ return (x>=0 && x<view.xmax);}
  private static boolean YValid(int y){ return (y>=0 && y<view.ymax);}
  private static boolean ZValid(int z){ return (true);}


  private static boolean MatchChar(char a,char b){ return (((a)=='#')||((a)==(b))); }
  private static final double RootSix = Math.sqrt(6);
  private static final int ColBits = 24;


  static void setWorldSize(double size)
  {
    double DepthQueFactor=5.0;
    worldSize=size*DepthQueFactor;
    worldRadius=worldSize*0.5;
  }

  static void InitialiseTransform()
  {
    Shade = new RefCountColor[MAXSHADE];
    final boolean APPLEMAC = false;
    if(APPLEMAC)
      LastShade = Colour2Shade(LutSize-1);
    else
      LastShade = Colour2Shade(LutSize);
    int i;
    for( i=0; i<LastShade; i++ )
      Shade[i] = new RefCountColor();

    int rad;
    for( rad=0; rad<MAXRAD; rad++ )
    {
      int maxval = (int)(RootSix*rad)+4;
      ColConst[rad] = (ColourDepth<<ColBits)/maxval;
    }

    InitialiseTables();
    ResetColourMap();
  }

  private static int CPKMAX(){ return CPKShade.length; }

    private static final ShadeRef CPKShade[] = {
     new ShadeRef( 0, 0, 200, 200, 200 ),       /*  0 Light Grey   */
     new ShadeRef( 0, 0, 143, 143, 255 ),       /*  1 Sky Blue     */
     new ShadeRef( 0, 0, 240,   0,   0 ),       /*  2 Red          */
     new ShadeRef( 0, 0, 255, 200,  50 ),       /*  3 Yellow       */
     new ShadeRef( 0, 0, 255, 255, 255 ),       /*  4 White        */
     new ShadeRef( 0, 0, 255, 192, 203 ),       /*  5 Pink         */
     new ShadeRef( 0, 0, 218, 165,  32 ),       /*  6 Golden Rod   */
     new ShadeRef( 0, 0,   0,   0, 255 ),       /*  7 Blue         */
     new ShadeRef( 0, 0, 255, 165,   0 ),       /*  8 Orange       */
     new ShadeRef( 0, 0, 128, 128, 144 ),       /*  9 Dark Grey    */
     new ShadeRef( 0, 0, 165,  42,  42 ),       /* 10 Brown        */
     new ShadeRef( 0, 0, 160,  32, 240 ),       /* 11 Purple       */
     new ShadeRef( 0, 0, 255,  20, 147 ),       /* 12 Deep Pink    */
     new ShadeRef( 0, 0,   0, 255,   0 ),       /* 13 Green        */
     new ShadeRef( 0, 0, 178,  34,  34 ),       /* 14 Fire Brick   */
     new ShadeRef( 0, 0,  34, 139,  34 ) };     /* 15 Forest Green */

  private static final ShadeRef Shapely[] = {
     new ShadeRef(0, 0, 140, 255, 140 ),    /* ALA */
     new ShadeRef(0, 0, 255, 255, 255 ),    /* GLY */
     new ShadeRef(0, 0,  69,  94,  69 ),    /* LEU */
     new ShadeRef(0, 0, 255, 112,  66 ),    /* SER */
     new ShadeRef(0, 0, 255, 140, 255 ),    /* VAL */
     new ShadeRef(0, 0, 184,  76,   0 ),    /* THR */
     new ShadeRef(0, 0,  71,  71, 184 ),    /* LYS */
     new ShadeRef(0, 0, 160,   0,  66 ),    /* ASP */
     new ShadeRef(0, 0,   0,  76,   0 ),    /* ILE */
     new ShadeRef(0, 0, 255, 124, 112 ),    /* ASN */
     new ShadeRef(0, 0, 102,   0,   0 ),    /* GLU */
     new ShadeRef(0, 0,  82,  82,  82 ),    /* PRO */
     new ShadeRef(0, 0,   0,   0, 124 ),    /* ARG */
     new ShadeRef(0, 0,  83,  76,  66 ),    /* PHE */
     new ShadeRef(0, 0, 255,  76,  76 ),    /* GLN */
     new ShadeRef(0, 0, 140, 112,  76 ),    /* TYR */
     new ShadeRef(0, 0, 112, 112, 255 ),    /* HIS */
     new ShadeRef(0, 0, 255, 255, 112 ),    /* CYS */
     new ShadeRef(0, 0, 184, 160,  66 ),    /* MET */
     new ShadeRef(0, 0,  79,  70,   0 ),    /* TRP */

     new ShadeRef(0, 0, 255,   0, 255 ),    /* ASX */
     new ShadeRef(0, 0, 255,   0, 255 ),    /* GLX */
     new ShadeRef(0, 0, 255,   0, 255 ),    /* PCA */
     new ShadeRef(0, 0, 255,   0, 255 ),    /* HYP */

     new ShadeRef(0, 0, 160, 160, 255 ),    /*   A */
     new ShadeRef(0, 0, 255, 140,  75 ),    /*   C */
     new ShadeRef(0, 0, 255, 112, 112 ),    /*   G */
     new ShadeRef(0, 0, 160, 255, 160 ),    /*   T */

     new ShadeRef(0, 0, 184, 184, 184 ),    /* 28 -> BackBone */
     new ShadeRef(0, 0,  94,   0,  94 ),    /* 29 -> Special  */
     new ShadeRef(0, 0, 255,   0, 255 ) };  /* 30 -> Default  */

     
  private static final ShadeRef AminoShade[] = {
     new ShadeRef(0, 0, 230,  10,  10 ),    /*  0  ASP, GLU      */
     new ShadeRef(0, 0,  20,  90, 255 ),    /*  1  LYS, ARG      */
     new ShadeRef(0, 0, 130, 130, 210 ),    /*  2  HIS           */
     new ShadeRef(0, 0, 250, 150,   0 ),    /*  3  SER, THR      */
     new ShadeRef(0, 0,   0, 220, 220 ),    /*  4  ASN, GLN      */
     new ShadeRef(0, 0, 230, 230,   0 ),    /*  5  CYS, MET      */
     new ShadeRef(0, 0, 200, 200, 200 ),    /*  6  ALA           */
     new ShadeRef(0, 0, 235, 235, 235 ),    /*  7  GLY           */
     new ShadeRef(0, 0,  15, 130,  15 ),    /*  8  LEU, VAL, ILE */
     new ShadeRef(0, 0,  50,  50, 170 ),    /*  9  PHE, TYR      */
     new ShadeRef(0, 0, 180,  90, 180 ),    /* 10  TRP           */
     new ShadeRef(0, 0, 220, 150, 130 ),    /* 11  PRO, PCA, HYP */
     new ShadeRef(0, 0, 190, 160, 110 ) };  /* 12  Others        */

  private static final int AminoIndex[] = {
      6, /*ALA*/   7, /*GLY*/   8, /*LEU*/   3,  /*SER*/
      8, /*VAL*/   3, /*THR*/   1, /*LYS*/   0,  /*ASP*/
      8, /*ILE*/   4, /*ASN*/   0, /*GLU*/  11,  /*PRO*/
      1, /*ARG*/   9, /*PHE*/   4, /*GLN*/   9,  /*TYR*/
      2, /*HIS*/   5, /*CYS*/   5, /*MET*/  10,  /*TRP*/
      4, /*ASX*/   4, /*GLX*/  11, /*PCA*/  11   /*HYP*/
			  };

  private static final ShadeRef HBondShade[] = {
     new ShadeRef(0, 0, 255, 255, 255 ),    /* 0  Offset =  2   */
     new ShadeRef(0, 0, 255,   0, 255 ),    /* 1  Offset =  3   */
     new ShadeRef(0, 0, 255,   0,   0 ),    /* 2  Offset =  4   */
     new ShadeRef(0, 0, 255, 165,   0 ),    /* 3  Offset =  5   */
     new ShadeRef(0, 0,   0, 255, 255 ),    /* 4  Offset = -3   */
     new ShadeRef(0, 0,   0, 255,   0 ),    /* 5  Offset = -4   */
     new ShadeRef(0, 0, 255, 255,   0 ) };  /* 6  Others        */


  private static final ShadeRef StructShade[] = {
     new ShadeRef(0, 0, 255, 255, 255 ),    /* 0  Default     */
     new ShadeRef(0, 0, 255,   0, 128 ),    /* 1  Alpha Helix */
     new ShadeRef(0, 0, 255, 200,   0 ),    /* 2  Beta Sheet  */
     new ShadeRef(0, 0,  96, 128, 255 ) };  /* 3  Turn        */

  private static final ShadeRef PotentialShade[] = {
     new ShadeRef(0, 0, 255,   0,   0 ),    /* 0  Red     25 < V       */
     new ShadeRef(0, 0, 255, 165,   0 ),    /* 1  Orange  10 < V <  25 */
     new ShadeRef(0, 0, 255, 255,   0 ),    /* 2  Yellow   3 < V <  10 */
     new ShadeRef(0, 0,   0, 255,   0 ),    /* 3  Green    0 < V <   3 */
     new ShadeRef(0, 0,   0, 255, 255 ),    /* 4  Cyan    -3 < V <   0 */
     new ShadeRef(0, 0,   0,   0, 255 ),    /* 5  Blue   -10 < V <  -3 */
     new ShadeRef(0, 0, 160,  32, 240 ),    /* 6  Purple -25 < V < -10 */
     new ShadeRef(0, 0, 255, 255, 255 ) };  /* 7  White        V < -25 */

  public static int CPKColor(int cpkcol)
  {
    ShadeRef ref = CPKShade[cpkcol];

    if(ref.col == 0)
    {   ref.shade = DefineShade(ref.color.getRed(),ref.color.getGreen(),ref.color.getBlue() );
	ref.col = Shade2Colour(ref.shade);
    }
    return ref.col;
  }
  public static void CPKColourAttrib(Vector atomList)
  {
    int i;

    for( i=0; i<CPKMAX(); i++ )
	CPKShade[i].col = 0;
    if(Shade == null) InitialiseTransform();
    else ResetColourAttrib(atomList);

    {
	ShadeRef ref = CPKShade[13]; /* Green */
	ref.shade = DefineShade(ref.color.getRed(),ref.color.getGreen(),ref.color.getBlue() );
	ref.col = Shade2Colour(ref.shade);
        Shade[ref.shade].refcount++;
    }

    for(i = 0; i < atomList.size(); ++i)
    {
        PDBAtom ptr = (PDBAtom)atomList.elementAt(i);
	if( (ptr.flag & PDBAtom.SelectFlag) != 0)
	{
	    ShadeRef ref = CPKShade[Element.getElement(ptr.elemno).cpkcol];

	    if(ref.col == 0)
	    {   ref.shade = DefineShade(ref.color.getRed(),ref.color.getGreen(),ref.color.getBlue() );
		ref.col = Shade2Colour(ref.shade);
	    }
	    Shade[ref.shade].refcount++;
	    ptr.setColor(ref.color,ref.col);
	    if(false)
	      System.err.println("atom " + i + " color " + ref.shade
				 + " " + ref.col + " " + ref.color.getRed()
			       + "," + ref.color.getGreen()
			       + "," + ref.color.getBlue()
			       + " elemno " + ptr.elemno + " refcount "
				 + Shade[ref.shade].refcount);
	}
	else System.err.println("atom " + i + " not selected\n");
    }
    DefineColourMap();
  }

  private static int DefineShade(int r, int g, int b )
  {
    int d,dr,dg,db;
    int dist,best;
    int i;

    /* Already defined! */
    for( i=0; i<LastShade; i++ )
        if( Shade[i].refcount != 0)
            if( (Shade[i].color.getRed()==r)
		&&(Shade[i].color.getGreen()==g)
		&&(Shade[i].color.getBlue()==b) )
                return(i);

    /* Allocate request */
    for( i=0; i<LastShade; i++ )
         if( Shade[i].refcount == 0)
         {
             Shade[i] = new RefCountColor(r,g,b);
             return(i);
         }

    System.err.println("Warning: Unable to allocate shade!");

    best = dist = 0;

    /* Nearest match */
    for( i=0; i<LastShade; i++ )
    {   dr = Shade[i].color.getRed() - r;
        dg = Shade[i].color.getGreen() - g;
        db = Shade[i].color.getBlue() - b;
        d = dr*dr + dg*dg + db*db;
        if( i == 0 || (d<dist) )
        {   dist = d;
            best = i;
        }
    }
    return( best );
  }

  private static void ResetColourMap()
  {
    int i;

    /*
    if(EIGHTBIT)
    {
    for( i=0; i<256; i++ )
        ULut[i] = false;
    }

    SpecPower = 8;
    FakeSpecular = false;
    Ambient = DefaultAmbient;
    */
    UseBackFade = false;
    FakeSpecular = true;

    BackR = BackG = BackB = 0;
    BoxR = BoxG = BoxB = 255;
    LabR = LabG = LabB = 255;
    for( i=0; i<LastShade; i++ )
        Shade[i].refcount = 0;
    /*
    ScaleCount = 0;
    */
  }

  public static void setBackColor(Color col)
  {
    BackR = col.getRed();
    BackG = col.getGreen();
    BackB = col.getBlue();
  }

  private static void ResetColourAttrib(Vector atomList)
  {
    int i;
    for(i = 0; i < atomList.size(); ++i)
    {
        PDBAtom ptr = (PDBAtom)atomList.elementAt(i);
        if( (ptr.flag & PDBAtom.SelectFlag) != 0 && ptr.getColorIndex() != 0)
	{
	  int c = Colour2Shade(ptr.getColorIndex());
	  if(Shade[c].refcount > 0)
	    Shade[c].refcount--;
	}
    }
  }

  private static void InitialiseTables()
  {
    int i,rad;
    short ptr[];

    LookUp = new short[MAXRAD][MAXRAD];
    LookUp[0][0] = 0;
    LookUp[1][0] = 1;
    LookUp[1][1] = 0;
    
    for( rad=2; rad<MAXRAD; rad++ )
    { 
      LookUp[rad][0] = (short)rad;

      int root = rad-1;
      int root2 = root*root;

      int arg = rad*rad;
      for( i=1; i<rad; i++ )
      {   /* arg = rad*rad - i*i */
	arg -= (i<<1)-1;

            /* root = isqrt(arg)   */
	while( arg < root2 )
	{
	  root2 -= (root<<1)-1;
	  root--;
	}
            /* Thanks to James Crook */
	LookUp[rad][i] = (short)(((arg-root2)<i)? root : root+1);
      }
      
      LookUp[rad][rad] = 0;    
    }
  }

  private static void SetLutEntry(int i, int r, int g, int b)
  {
    ULut[i] = true;
    RLut[i] = r;
    GLut[i] = g;
    BLut[i] = b;

    Lut[i] = (((r<<8)|g)<<8 ) | b;
  }

  private static double Power(double x, int y)
  {
    double result = x;
    while( y>1 )
    {   if((y&1) != 0) { result *= x; y--; }
        else { result *= result; y>>=1; }
    }
    return( result );
  }

  private static void DefineColourMap()
  {
    double fade;
    double temp,inten;
    int col,r,g,b;
    int i,j,k=0;
    boolean DisplayMode = false;

    for( i=0; i<LutSize; i++ )
        ULut[i] = false;

    colorModel = new DirectColorModel(24,0xff0000,0xff00,0xff);

    if( !DisplayMode )
    {
      SetLutEntry(BackCol,BackR,BackG,BackB);
      SetLutEntry(LabelCol,LabR,LabG,LabB);
      SetLutEntry(BoxCol,BoxR,BoxG,BoxB);
    } else SetLutEntry(BackCol,80,80,80);


    double diffuse = 1.0 - Ambient;
    if( DisplayMode )
    {   for( i=0; i<ColourDepth; i++ )
        {   temp = (double)i/ColourMask;
            inten = diffuse*temp + Ambient;

            /* Unselected [40,40,255] */
            /* Selected   [255,160,0]  */
            r = (int)(255*inten);
            g = (int)(160*inten);
            b = (int)(40*inten);

            SetLutEntry( FirstCol+i, b, b, r );
            SetLutEntry( Shade2Colour(1)+i, r, g, 0 );
        }
    } else
        for( i=0; i<ColourDepth; i++ )
        {   temp = (double)i/ColourMask;
            inten = diffuse*temp + Ambient;
            fade = 1.0-inten;

	    /* */
            if( FakeSpecular )
            {   temp = Power(temp,SpecPower);
                k = (int)(255*temp);
                temp = 1.0 - temp;
                inten *= temp;
                fade *= temp;
            }
	    /* */

            for( j=0; j<LastShade; j++ )
                if( Shade[j].refcount != 0)
                {   col = Shade2Colour(j);
                    if( UseBackFade )
                    {   temp = 1.0-inten;
                        r = (int)(Shade[j].getRed()*inten + fade*BackR); 
                        g = (int)(Shade[j].getGreen()*inten + fade*BackG);
                        b = (int)(Shade[j].getBlue()*inten + fade*BackB);
                    } else
                    {   r = (int)(Shade[j].getRed()*inten); 
                        g = (int)(Shade[j].getGreen()*inten);
                        b = (int)(Shade[j].getBlue()*inten);
                    }

		    /* */
                    if( FakeSpecular )
                    {   r += k;
                        g += k;
                        b += k;
                    }
		    /* */

                    SetLutEntry( col+i, r, g, b );
                }
        }
  }

/* Drawing procedures */

//  public static int colourDepth(int z, double worldRadius)
//  {
//    return (2.0*ColourDepth*(z+worldRadius)/worldRadius);
//    //return ((ColourDepth*(z+v.imageRadius-ZOffset))/v.imageSize);
//  }


  public static void PlotPoint( int x, int y, int z, int col )
  {
    int offset;


    offset = y * view.yskip+x;
    if( z > view.dbuf[offset] )
    {   view.fbuf[offset] = Lut[col];
        view.dbuf[offset] = (short)z;
    }
  }

  public static void ClipPoint( int x, int y, int z, int col )
  {
    int offset;

    if ( x >= 0 && x < view.xmax && y >= 0 && y < view.ymax)  //&& ZValid(z) && ZBack(z) )
    { /* PlotPoint(x,y,z,col); */
      offset = y*view.yskip+x;
      if( z > view.dbuf[offset] )
      {
        view.fbuf[offset]= Lut[col];
        view.dbuf[offset]= (short)z;
      }
    }
  }

  private static void UpdateLine(int wide, int dy, int col, int z, int rad,
				 int offset, int dxmin, int dxmax)
  {
    int   dx = -wide;
    short tptr[] = LookUp[wide];
    int   tindex = wide;
    int   stdcol = Lut[col];
    int   cc = ColConst[rad];
    int   r = 0;
    if (wide == 0) return; /* */
    while(wide == 1 && (long)(512+wide-dy)*(long)cc > (long)Integer.MAX_VALUE)
    { // prevent overflow in low radius inten calc.
      cc = ColConst[rad + ++r];
    }
    while(dx < 0 && dx < dxmin) { --tindex; ++dx; }
    offset += dx-1; // adjusted by wkpark
    while(dx < 0 && dx < dxmax)
    {
      int dz = tptr[tindex--];
      short depth = (short)(dz + z);
      if(offset >=0 && offset < view.size && depth > view.dbuf[offset])
      {
 	view.dbuf[offset] = depth;
	int inten = dz+dz+dx-dy;
	if( inten>0 )
	{
	  //inten = (inten*cc) >> ColBits;
	  inten = (int)((long)inten*cc >> ColBits);
	  try
	  {
	    view.fbuf[offset] = Lut[col+inten];
	  }catch(ArrayIndexOutOfBoundsException e)
	  {
	    System.err.println("#1ArrayIndexOutOfBoundsException " + inten + " " + (dz+dz+dx-dy) + "*" + cc + " r " + r);
	  }
	}
	else {
          view.fbuf[offset] = stdcol;
        }
      }
      ++dx;
      ++offset;
    }

    if(dx < dxmax)
    {
      while(dx <= wide && dx < dxmin) { ++tindex; ++dx; ++offset;}
      //while(dx < dxmin) { ++tindex; ++dx; ++offset;}
      do
      {
        int dz=0;
        short depth = 0;
        if (dx < dxmin) { tindex++; dx++; offset++; continue; }
	dz= tptr[tindex++];
	depth = (short)(dz + z);
	if(offset >=0 && offset < view.size && depth > view.dbuf[offset])
	{
	  view.dbuf[offset] = depth;
	  int inten = dz+dz+dx-dy;
	  if( inten>0 )
	  {
	    inten = (int)((long)inten*cc >> ColBits);
	    //inten = (inten*cc) >> ColBits;
//	    try
//	    {
	      view.fbuf[offset] = Lut[col+inten];
//	    }catch(ArrayIndexOutOfBoundsException e)
//	    {
//	      System.err.println("#2ArrayIndexOutOfBoundsException " + inten + " " + (dz+dz+dx-dy) + "*" + cc + " r " + r);
//	    }
	  }
	  else view.fbuf[offset] = stdcol;
	}
	++dx;
	++offset;
      } while(dx < wide && dx < dxmax);
    }
  }

  public static void DrawSphere(int x, int y, int z, int rad, int col)
  {
    int offset = (y-rad)*view.yskip + x;
    int fold = offset;
    int dy = -rad;
    int dxmax = view.xmax - x;
    int dxmin = -x;
    if(rad >= MAXRAD)
    {
      System.err.println("radius too big " + rad);
      return;
    }

    //System.err.println("offset " + offset + " y " + y + " x " + x + " rad " + rad + " t " + (((System.currentTimeMillis() - t1)/10)/100.0));
    while(dy < 0 && y + dy < view.ymax)
    {
      if(y + dy >= 0)
      {
	int wide = LookUp[rad][-dy];
	UpdateLine(wide, dy, col, z, rad, fold, dxmin, dxmax);
      }
      fold += view.yskip;
      dy++;
    }

 
    if(y + dy < view.ymax)
    do { 
      if(y + dy >= 0)
      {
	int wide = LookUp[rad][dy];
	UpdateLine(wide, dy, col, z, rad, fold, dxmin, dxmax);
      }
      fold += view.yskip;
      dy++;
    } while(dy <= rad && y + dy < view.ymax);
  }

  private static void DrawArcAc(int dbase,int fbase,int z,int c)
  {
    int i;
    for(i = 0; i < ArcAcPtr; ++i)
    {
      ArcEntry ptr = ArcAc[i];
      short depth = (short)(ptr.dz+z);
      int ix = dbase + ptr.offset;
      if(ix >= view.size) break;
      if(ix >= 0 && depth > view.dbuf[ix])
      {
	view.dbuf[dbase + ptr.offset] = depth;
	view.fbuf[fbase + ptr.offset] = Lut[ptr.inten+c];
      }
    }
  }

  private static void ClipArcAc(int dbase,int fbase,int x, int y, int z,int c)
  {
    int i;
    int temp;
    for(i = 0; i < ArcAcPtr; ++i)
    {
      ArcEntry ptr = ArcAc[i];
      short depth = (short)(ptr.dz+z);
      int ix = dbase + ptr.offset;
      temp = (int)(ptr.dx+x);
      if(ix >= view.size) break;
      if (temp >= 0 && temp < view.xmax)
      if(ix >= 0 && depth > view.dbuf[ix])
      {
	view.dbuf[dbase + ptr.offset] = depth;
	view.fbuf[fbase + ptr.offset] = Lut[ptr.inten+c];
      }
    }
  }

  private static void DrawArcDn(int dbase,int fbase,int z, int c)
  {
    int i;
    for(i = 0; i < ArcDnPtr; ++i)
    {
      ArcEntry ptr = ArcDn[i];
      short depth = (short)(ptr.dz+z);
      int ix = dbase + ptr.offset;
      if(ix >= view.size) break;
      if(ix >= 0 && depth > view.dbuf[ix])
      {
	view.dbuf[dbase + ptr.offset] = depth;
	view.fbuf[fbase + ptr.offset] = Lut[ptr.inten+c];
      }
    }
  }

  private static void ClipArcDn(int dbase,int fbase,int x,int y,int z, int c)
  {
    int i;
    int temp;
    for(i = 0; i < ArcDnPtr; ++i)
    {
      ArcEntry ptr = ArcDn[i];
      short depth = (short)(ptr.dz+z);
      int ix = dbase + ptr.offset;
      temp = (int)(ptr.dx+x);
      if(ix >= view.size) break;
      if (temp >= 0 && temp < view.xmax)
      if(ix >= 0 && depth > view.dbuf[ix])
      {
	view.dbuf[dbase + ptr.offset] = depth;
	view.fbuf[fbase + ptr.offset] = Lut[ptr.inten+c];
      }
    }
  }

  private static void DrawCylinderCapsX(int x1,int y1,int z1,
				       int x2,int y2,int z2,
				       int c1,int c2,int rad)
  {
    int offset;
    int dx,dy,dz;

    int lx = x2-x1;
    int ly = y2-y1;

    int end = ly*view.yskip+lx;
    int temp = y1*view.yskip+x1;
    int fold = temp;
    int dold = temp;

    ArcAcPtr = 0;
    ArcDnPtr = 0;
    if(ArcAc[0] == null)
    {
      int i;
      for(i = 0; i < ArcEntry.ARCSIZE; ++i)
      {
	ArcAc[i] = new ArcEntry();
	ArcDn[i] = new ArcEntry();
      }
    }

    temp = -(rad*view.yskip);
    short wptr[] = LookUp[rad];
    for( dy= -rad; dy<=rad; dy++ )
    {
      int wide = wptr[Math.abs(dy)];

      short lptr[] = LookUp[wide];
      for( dx= -wide; dx<=wide; dx++ )
      {
	  dz = lptr[Math.abs(dx)];
	  int inten = dz + dz + dx + dy;
	  if( inten>0 )
          {
	    inten = (int)((inten*ColConst[rad])>>ColBits);
	    if( inten>ColourMask ) inten = ColourMask;
	  }else inten = 0;
	  offset = temp+dx;

	  if((x1+dx) >= 0 && (x1+dx) < view.xmax && (y1+dy) >= 0 && (y1+dy) < view.ymax)
          {
	    short depth = (short)(dz+z1);
	    if(depth > view.dbuf[dold + offset])
	    {
	      view.dbuf[dold + offset] = depth;
	      view.fbuf[fold+offset] = Lut[c1+inten];
	    }
	  }

	  if((x2+dx) >= 0 && (x2+dx) < view.xmax && (y2+dy) >= 0 && (y2+dy) < view.ymax)
          {
	    short depth = (short)(dz+z2);
            if( depth > view.dbuf[dold+(offset+end)]) 
            {   view.dbuf[dold+(offset+end)] = (short)(depth);
	       view.fbuf[fold+(offset+end)] = Lut[c2+inten];
            }
	  }

		// an out of bounds exception here means an excessive radius

	  ArcAc[ArcAcPtr].offset = offset;
	  ArcAc[ArcAcPtr].inten = (short)inten;
	  ArcAc[ArcAcPtr].dx=(short)dx;
	  ArcAc[ArcAcPtr].dy=(short)dy;
	  ArcAc[ArcAcPtr].dz=(short)dz;
	  ArcAcPtr++;

	  ArcDn[ArcDnPtr].offset = offset;
	  ArcDn[ArcDnPtr].inten = (short)inten;
	  ArcDn[ArcDnPtr].dx=(short)dx;
	  ArcDn[ArcDnPtr].dy=(short)dy;
	  ArcDn[ArcDnPtr].dz=(short)dz;
	  ArcDnPtr++;
      }
      temp += view.yskip;
    }
  }

private static void DrawCylinderCaps( int x1, int y1, int z1,
                              int x2, int y2, int z2,
                              int c1, int c2, int rad )
{
    int offset;
    int inten1,inten2,s1,s2,absx;
    int wide;
    int dx,dy,dz;
    int lx = x2-x1;
    int ly = y2-y1;
    boolean p;
    //int alts, altc;

    int end = ly*view.yskip+lx;
    int temp = y1*view.yskip+x1;
    int fold = temp;
    int dold = temp;

    s1 = s2 = 0;

    temp = -(rad*view.yskip);
    for( dy= -rad; dy<=rad; dy++ )
    {   wide = LookUp[rad][Math.abs(dy)];
        //alts = 0;

        for( dx= -wide; dx<=wide; dx++ )
        {
	    short depth;
	    absx = Math.abs(dx);
            dz = LookUp[wide][absx];
	    int inten = dz + dz + dx + dy;
            if( inten>0 )
            {   inten = (int)((inten*ColConst[rad])>>ColBits);
                // inten = (int)(ColourMask*inten + 0.1);
                if( inten>ColourMask ) inten = ColourMask;
            } else inten = 0;

            //if( UseDepthCue )
            //{   inten1 = inten - DepthTable[inten][s1];
            //    if( inten1<0 ) inten1 = 0;
            //    inten2 = inten - DepthTable[inten][s2];
            //    if( inten2<0 ) inten2 = 0;
            //} else
            inten2 = inten1 = inten;

            offset = temp+dx;
	    if (wide == 0) continue;

            //dptr = dold+offset;
	    depth = (short) (z1+dz);
	    // XXX
            p = (4*dx*dx*rad*rad + 4*dy*dy*wide*wide < rad*rad*wide*wide );
            //SETPIXELP(dptr,fold+offset,depth,Lut[c1+inten1], \
            //Lut[inten1],p);
	    if(XValid(x1+dx) && YValid(y1+dy))
            if( depth > view.dbuf[dold+offset]) 
            {  view.dbuf[dold+offset] = (short)(depth);
               view.fbuf[fold+offset] = Lut[c1+inten1];
            }

            //dptr = dold+(offset+end);
	    depth = (short) (z2+dz);
            p = (4*dx*dx*rad*rad + 4*dy*dy*wide*wide < rad*rad*wide*wide);
            //SETPIXELP(dptr,fold+(offset+end),depth,Lut[c2+inten2], \
            //Lut[inten2],p);
	    if(XValid(x2+dx) && YValid(y2+dy))
            if( depth > view.dbuf[dold+(offset+end)]) 
            {   view.dbuf[dold+(offset+end)] = (short)(depth);
               view.fbuf[fold+(offset+end)] = Lut[c2+inten2];
            }
        }
        temp += view.yskip;
    }
  }

public static void DrawCylinder( int x1, int y1, int z1,
                   int x2, int y2, int z2,
                   int c1, int c2, int rad)
{
    int wide,rads,tmp;
    int lx,ly,lz,dx,dy,dz;
    double rx,ry,rz;
    int rx2,ry2,rz2;
    double ynor,cosy,siny,znor,cosz,sinz,temp;

    int ax,ay,az,bx,by,bz;
    double inten,lightfactor;
    /*
    double CapInten;
    boolean cap=true;
    */

    int offset;
    int zerr,ystep,err;
    int ix,iy;
    int col,mid;
    int altc,cola,s;
    boolean p;

    //if( cap )
    //{   ClipCylinder(x1,y1,z1,x2,y2,z2,c1,c2,rad,altl,arad,cap);
    //    return;
    //}

    /* Avoid ArcTable Overflow! */
    if( rad > MAXRAD ) rad = MAXRAD;

    /* Trivial Case */
    if( (x1==x2) && (y1==y2) )
    {   if( z1>z2 )
        {      DrawSphere(x1,y1,z1,rad,c1);
        } else DrawSphere(x2,y2,z2,rad,c2);
        return;
    }

    if( z1<z2 )
    {   tmp=x1; x1=x2; x2=tmp;
        tmp=y1; y1=y2; y2=tmp;
        tmp=z1; z1=z2; z2=tmp;
        tmp=c1; c1=c2; c2=tmp;
    }

    lx = x2-x1;
    ly = y2-y1;
    lz = z2-z1;

    DrawCylinderCaps(x1,y1,z1,x2,y2,z2,c1,c2,rad);

    //altc = 0;
    //if ( altl != '\0' && altl != ' ')
    //  altc = AltlColours[((int)altl)&(AltlDepth-1)];
    //cola = altc;
    lightfactor = (double)(rad)*RootSix;
    //if( UseDepthCue )
    //{   s = (ColourDepth*(ImageRadius-z1+ZOffset-ShiftS))/ImageSize;
    //    if( s<0 ) s = 0;
    //    if( s>ColourMask ) s = ColourMask;
    //}

    ix = (lx<0)? -1:1;
    iy = (ly<0)? -1:1;

    dx = ix*lx;
    dy = iy*ly;
    ystep = iy*view.yskip;
    dz = lz;

    rads = rad*rad;
    tmp = lx*lx+ly*ly;
    znor = Math.sqrt(tmp);
    cosz = (double)lx/znor;
    sinz = (double)ly/znor;
    ynor = Math.sqrt(tmp+lz*lz);
    cosy = (double)lz/ynor;
    siny = (double)znor/ynor;

/*    if( cap )
    {   CapInten = -(lx+ly+lz+lz);
        CapInten /= (ynor*RootSix);
        if( CapInten > 0.0 )
        {   CapInten = (char)(ColourMask*CapInten);
            if( CapInten>ColourMask ) CapInten = ColourMask;
        } else CapInten = 0;
    }
    */

    if( rad != CurrentRad )
    {   ArcTablePtr=0;
        if(ArcTable[0] == null)
        {
          int i;
          for(i = 0; i < ArcTableEntry.ARCSIZE; ++i)
          {
		ArcTable[i] = new ArcTableEntry();
          }
        }
        for( wide=-2*rad; wide<2*rad; wide++ )
        {    ArcTable[ArcTablePtr].x = Math.max(1,Math.sqrt(rads-((double)wide*wide)/4));
            ArcTablePtr++;
        }
        CurrentRad = rad;
    }

    ArcTablePtr=0;
    for( wide=0;wide<(rad*4);wide++ )
    {    /*spatial rotation along y*/
        rx = - ArcTable[ArcTablePtr].x*cosy;
        ry = (double)ix*((double)wide/2-rad);
        rz = ArcTable[ArcTablePtr].x*siny;
        ArcTablePtr++;
        /*spatial rotation along z*/
        temp = rx*cosz - ry*sinz;
        ry = rx*sinz + ry*cosz;
        rx = temp;
        /*rz = rz*/

        /*round up*/
        //RoundEdges
        if( rx>=0.5 ) rx2 = (int)((rx+0.5));
        else rx2 = (int)((rx-0.5));
        if( ry>=0.5 ) ry2 = (int)((ry+0.5));
        else ry2 = (int)((ry-0.5));
        if( rz>=0.5 ) rz2 = (int)((rz+0.5));
        else rz2 = (int)((rz-0.5));

        //LineInten;
        inten = (rx)+(ry)+(rz)+(rz);
        //inten /= lightfactor;
        if( inten > 0.0 )
        {
	    inten = (int)((int)(inten*ColConst[rad])>>ColBits);
      	    //inten = (int)(ColourMask*inten + 0.1);
            if( inten>ColourMask ) inten = ColourMask;
            //if( UseDepthCue )
            //{   inten -= DepthTable[(char)inten][s];
            //    if( inten<0 ) inten = 0;
            //}
        } else inten = 0;
        col = c1;

        ax = x1 + rx2; bx = ax + lx;
        ay = y1 + ry2; by = ay + ly;
        az = z1 + rz2; bz = az + lz;

        //ClipLine(x1+rx2,y1+ry2,z1+rz2,x1-rx2,y1-ry2,z1-rz2,col);//,altl);

        mid = (ax+bx)>>1;

        offset = (int)ay*view.yskip + ax;
        //fptr = View.fbuf+offset;
        //dptr = View.dbuf+offset;

        //SETPIXEL(dptr,fptr,az,Lut[c1+(char)inten]);
        //SETPIXEL(dptr,fptr,az,Lut[c1+inten]);
        if(XValid(ax) && YValid(ay))
        if( (short)(az) > view.dbuf[offset] )
        {   view.dbuf[offset] = (short)az;
            view.fbuf[offset] = Lut[c1+(int)inten];
        }
        if( dx>dy )
        {   err = zerr = -(dx>>1);
	    int yy=ay;
            if( c1 != c2 )
            {   mid = (ax+bx)>>1;
                while( ax!=mid ) //XLineStep;
                  { if((err+=dy)>0) {
		    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    yy+=iy;
		    err-=dx; }
                    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    ax+=ix;
                    //p = altc && (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    p = (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    if(XValid(ax) && YValid(yy))
                    //CommonLineStep(dx);
                    if( (short)(az) > view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if(XValid(ax-iy) && YValid(yy))
                    if( (short)(az) > view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }
 		    if( (zerr-=dz)>0 ) while (zerr>0) { zerr -= dx; az -= 1; }
		  }
                col = c2;
            }
            while( ax!=bx ) //XLineStep;
                  { if((err+=dy)>0) {
		    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    yy+=iy;
		    err-=dx; }
                    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    ax+=ix;
                    //CommonLineStep(dx);
                    if(XValid(ax) && YValid(yy))
                    if( (short)(az) > view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if(XValid(ax-iy) && YValid(yy))
                    if( (short)(az) > view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }
 		    if( (zerr-=dz)>0 ) while (zerr>0) { zerr -= dx; az -= 1; }
		  }
        } else
        {   err = zerr = -(dy>>1);
	    int xx=ax;
            if( c1 != c2 )
            {   mid = (ay+by)>>1;
                while( ay!=mid ) //YLineStep;
                   { if((err+=dx)>0) {
		    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    xx+=ix;
		    err-=dy; }
                    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    ay+=iy;
                    //CommonLineStep(dy);
                    if(XValid(xx) && YValid(ay))
                    if( (short)(az) > view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if(XValid(xx-iy) && YValid(ay))
                    if( (short)(az) > view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }

 		    if( (zerr-=dz)>0 ) while (zerr>0) { zerr -= dy; az -= 1; }
		   }
                col = c2;
            }
            while( ay!=by ) //YLineStep;
                   { if((err+=dx)>0) {
		    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    xx+=ix;
		    err-=dy; }
                    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    ay+=iy;
                    //p = altc && (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    //p = (ay-mid<(dy/4)) && (mid-ay<(dy/4));
                    //CommonLineStep(dy);
                    if(XValid(xx) && YValid(ay))
                    if( (short)(az) > view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if(XValid(xx-iy) && YValid(ay))
                    if( (short)(az) > view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }
 		    if( (zerr-=dz)>0 ) while (zerr>0) { zerr -= dy; az -= 1; }
		   }
        }
    }
}


public static void DrawCylinderOnAxe( int x1, int y1, int z1,
                   int x2, int y2, int z2,
                   int c1, int c2, int rad )
{
    int rads;
    int lx,ly,lz,dx,dy,dz;
    int ix,iy;
    double rx,ry,rz;
    int rx2,ry2,rz2;
    double nor,cos,sin,temp;
    int ax,ay,az,bx,by,bz;
    double inten, lightfactor;

    int offset,off;
    int zerr,ystep,err;
    int col, mid,altc,cola,s;
    boolean p;

    lx = x2-x1;
    ly = y2-y1;
    lz = z2-z1;

    ix = (lx<0)? -1:1;
    iy = (ly<0)? -1:1;

    DrawCylinderCaps(x1,y1,z1,x2,y2,z2,c1,c2,rad);

    //altc = 0;
    //if ( altl != '\0' && altl != ' ')
    //  altc = AltlColours[((int)altl)&(AltlDepth-1)];
    //cola = altc;
    lightfactor = (double)(rad)*RootSix;
    //if( UseDepthCue )
    //{   s = (ColourDepth*(ImageRadius-z1+ZOffset-ShiftS))/ImageSize;
    //    if( s<0 ) s = 0;
    //    if( s>ColourMask ) s = ColourMask;
    //}

    rads = rad*rad;
    if( rad != CurrentRad )
    {   ArcTablePtr=0;
        if(ArcTable[0] == null)
        {
          int i;
          for(i = 0; i < ArcTableEntry.ARCSIZE; ++i)
          {
		ArcTable[i] = new ArcTableEntry();
          }
        }

        for( rx2=-2*rad; rx2<2*rad; rx2++ )
        {   ArcTable[ArcTablePtr].x = Math.sqrt(rads-((double)rx2*rx2)/4);
            ArcTablePtr++;
        }
        CurrentRad = rad;
    }

    if( lx==0 )
    {    nor = Math.sqrt(lz*lz+ly*ly);
        cos = (double)lz/nor;
        sin = (double)ly/nor;

        ArcTablePtr=0;
        for( rx2=-rad;rx2<rad;rx2++ )
        {    //spatial rotation along y
            rx = rx2;
            temp = iy*ArcTable[ArcTablePtr].x;
            ry = -temp*cos;
            rz =  temp*sin;
            ArcTablePtr++;
            ArcTablePtr++;

            //RoundEdges
            if( rx>=0.5 ) rx2 = (int)((rx+0.5));
            else rx2 = (int)((rx-0.5));
            if( ry>=0.5 ) ry2 = (int)((ry+0.5));
            else ry2 = (int)((ry-0.5));
            if( rz>=0.5 ) rz2 = (int)((rz+0.5));
            else rz2 = (int)((rz-0.5));

            ax = x1 + rx2; bx = ax + lx;

            ay = y1 + ry2; by = ay + ly;
            az = z1 + rz2; bz = az + lz;

            //LineInten;
            inten = (rx)+(ry)+(rz)+(rz);
            inten /= lightfactor;
            if( inten > 0.0 )
            //{   inten = (int)((inten*ColConst[rad])>>ColBits);
            //                    if( inten>ColourMask ) inten = ColourMask;
            {   inten = (int)(ColourMask*inten + 0.1);
                if( inten>ColourMask ) inten = ColourMask;
                //if( UseDepthCue )
                //{   inten -= DepthTable[(char)inten][s];
                //    if( inten<0 ) inten = 0;
                //}
            } else inten = 0;

            offset = (int)ay*view.yskip + ax;
            //fptr = view.fbuf+offset;
            //dptr = view.dbuf+offset;
            //SETPIXEL(dptr,fptr,az,Lut[c1+inten]);
            if( az > view.dbuf[offset] )
            {   view.dbuf[offset] = (short)az;
                view.fbuf[offset] = Lut[c1+(int)inten];
            }

            dx = 0;
            dy = iy*ly;
            ystep = iy*view.yskip;
            dz = lz;
            col = c1;

            mid = (ay+by)>>1;
            err = zerr = -(dy>>1);
            if( c1 != c2 )
            {   while( ay!=mid ) //YLineStep;
                   { if((err+=dx)>0) {
		    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    err-=dy; }
                    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    ay+=iy;
                    //p = altc && (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    //p = (ay-mid<(dy/4)) && (mid-ay<(dy/4));
                    //CommonLineStep(dy);
                    if( (az) >= view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if( (az) >= view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }

 		    if( (zerr+=dz)>0 ) while (zerr>0) { zerr -= dy; az -= 1; }
		   }
      		    col = c2;
            }
            while( ay!=by ) //YLineStep;
                   { if((err+=dx)>0) {
		    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    err-=dy; }
                    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    ay+=iy;
                    //p = altc && (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    p = (ay-mid<(dy/4)) && (mid-ay<(dy/4));
                    //CommonLineStep(dy);
                    if( (az) >= view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if( (az) >= view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }
 		    if( (zerr+=dz)>0 ) while (zerr>0) { zerr -= dy; az -= 1; }
		   }
       	    }
    } else if( ly==0 )
    {
        nor = (int)Math.sqrt(lx*lx+lz*lz);
        cos = (double)lz/nor;
        sin = (double)lx/nor;

        ArcTablePtr=0;
        for( ry2=-rad;ry2<rad;ry2++ )
        {    temp = ix*ArcTable[ArcTablePtr].x;
            rx = -temp*cos;
            ry = ry2;
            rz =  temp*sin;
            ArcTablePtr++;
            ArcTablePtr++;

            //RoundEdges
            if( rx>=0.5 ) rx2 = (int)((rx+0.5));
            else rx2 = (int)((rx-0.5));
            if( ry>=0.5 ) ry2 = (int)((ry+0.5));
            else ry2 = (int)((ry-0.5));
            if( rz>=0.5 ) rz2 = (int)((rz+0.5));
            else rz2 = (int)((rz-0.5));

            ax = x1 + rx2; bx = ax + lx;
            ay = y1 + ry2; by = ay + ly;
            az = z1 + rz2; bz = az + lz;
	    
            //LineInten;
            inten = (rx)+(ry)+(rz)+(rz);
            inten /= lightfactor;
            if( inten > 0.0 )
            //{   inten = (int)((inten*ColConst[rad])>>ColBits);
            //                    if( inten>ColourMask ) inten = ColourMask;
            {   inten = (int)(ColourMask*inten + 0.1);
                if( inten>ColourMask ) inten = ColourMask;
                //if( UseDepthCue )
                //{   inten -= DepthTable[(char)inten][s];
                //    if( inten<0 ) inten = 0;
                //}
            } else inten = 0;


            offset = (int)ay*view.yskip + ax;
            //fptr = view.fbuf+offset;
            //dptr = view.dbuf+offset;

            //SETPIXEL(dptr,fptr,az,Lut[c1+inten]);
            if( az > view.dbuf[offset] )
            {   view.dbuf[offset] = (short)az;
                view.fbuf[offset] = Lut[c1+(int)inten];
            }

            dx = ix*lx;
            dy = 0;
            ystep = iy*view.yskip;
            dz = lz;
            col = c1;

            mid = (ax+bx)>>1;
            err = zerr = -(dx>>1);
            if( c1 != c2 )
            {   while( ax!=mid ) //XLineStep;
                  { if((err+=dy)>0) {
		    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    err-=dx; }
                    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    ax+=ix;
                    //p = altc && (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    p = (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    //CommonLineStep(dx);
                    if( (az) >= view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if( (az) >= view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }
 		    if( (zerr+=dz)>0 ) while (zerr>0) { zerr -= dx; az -= 1; }
		  }

                col = c2;
            }
            while( ax!=bx ) // XLineStep;
                  { if((err+=dy)>0) {
		    //fptr+=ystep; dptr+=ystep;
		    offset+=ystep;
		    err-=dx; }
                    //fptr+=ix; dptr+=ix;
		    offset+=ix;
		    ax+=ix;
                    //p = altc && (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    p = (ax-mid<(dx/4)) && (mid-ax<(dx/4));
                    //CommonLineStep(dx);
                    if( (az) >= view.dbuf[offset])
                                   {   view.dbuf[offset] = (short)(az);
                                       view.fbuf[offset]=Lut[col+(int)inten];
                                   }
                    if( (az) >= view.dbuf[offset-iy])
                                   {   view.dbuf[offset-iy] = (short)(az);
                                       view.fbuf[offset-iy]=Lut[col+(int)inten];
                                   }
 		    if( (zerr+=dz)>0 ) while (zerr>0) { zerr -= dx; az += 1; }
		  }
        }
    }
  }

  public static void DrawCylinderOld(int x1,int y1,int z1, int x2,int y2,int z2,
				  int c1,int c2,int rad)
  {
    int dbase;
    int fbase;

    int zrate,zerr,ystep,err;
    int ix,iy,ax,ay;
    int lx,ly,lz;
    int mid,tmp;
    int temp;

    if(rad > 25)
    {
      System.err.println("bond radius " + rad + " too large; set to 25");
      rad = 25;
    }
    if(false)
      System.err.println(x1 + "," + y1 + " - " + x2 + "," + y2 + " colors "
		       + c1 + " " + c2 + " radius " + rad);
    /* Trivial Case */
    if( (x1==x2) && (y1==y2) )
    {   if( z1>z2 )
        {      DrawSphere(x1,y1,z1,rad,c1);
        } else DrawSphere(x2,y2,z2,rad,c2);
        return;
    }

    if( z1<z2 )
    {   tmp=x1; x1=x2; x2=tmp;
        tmp=y1; y1=y2; y2=tmp;
        tmp=z1; z1=z2; z2=tmp;
        tmp=c1; c1=c2; c2=tmp;
    }

    DrawCylinderCaps(x1,y1,z1,x2,y2,z2,c1,c2,rad);

    lx = x2-x1;
    ly = y2-y1;
    lz = z2-z1;

    if( ly>0 ) { ystep = view.yskip; ay = ly; iy = 1; }
    else { ystep = -view.yskip; ay = -ly; iy = -1; }
    if( lx>0 ) { ax = lx; ix = 1; }
    else { ax = -lx; ix = -1; }
    zrate = lz/Math.max(ax,ay);

    temp = y1*view.yskip+x1;
    fbase = temp;
    dbase = temp;

    if( ax>ay )
    {   lz -= ax*zrate;
        zerr = err = -(ax>>1);

        if( c1 != c2 )
        {   mid = (x1+x2)>>1;
            while( x1!=mid )
            {   z1 += zrate;  if( (zerr-=lz)>0 ) while(zerr>0) { zerr-=ax; z1--; }
                fbase+=ix; dbase+=ix; x1+=ix;
                if( (err+=ay)>0 )
                {
		  fbase+=ystep; dbase+=ystep; err-=ax; y1+=iy;
		    ClipArcDn(dbase,fbase,x1,y1,z1,c1);
                }
                else
		  ClipArcAc(dbase,fbase,x1,y1,z1,c1);
            }
        }

        while( x1!=x2 )
        {   z1 += zrate;  if( (zerr-=lz)>0 ) while(zerr>0) { zerr-=ax; z1--; }
            fbase+=ix; dbase+=ix; x1+=ix;
            if( (err+=ay)>0 )
            {
	      fbase+=ystep; dbase+=ystep; err-=ax; y1+=iy;
	        ClipArcDn(dbase,fbase,x1,y1,z1,c2);
            }
            else
    	      ClipArcAc(dbase,fbase,x1,y1,z1,c2);
        }
    } else /*ay>=ax*/
    {   lz -= ay*zrate;
        zerr = err = -(ay>>1);

        if( c1 != c2 )
        {   mid = (y1+y2)>>1;
            while( y1!=mid )
            {   z1 += zrate;  if( (zerr-=lz)>0 )while(zerr>0)  { zerr-=ay; z1--; }
                fbase+=ystep; dbase+=ystep; y1+=iy;
                if( (err+=ax)>0 )
                {
		  fbase+=ix; dbase+=ix; err-=ay; x1+=ix;
    	          ClipArcAc(dbase,fbase,x1,y1,z1,c1);
                }
                else
		  ClipArcDn(dbase,fbase,x1,y1,z1,c1);
            }
        }

        while( y1!=y2)
        {   z1 += zrate;  if( (zerr-=lz)>0 ) while(zerr>0)  { zerr-=ay; z1--; }
            fbase+=ystep; dbase+=ystep; y1+=iy;
            if( (err+=ax)>0 )
            {
	      fbase+=ix; dbase+=ix; err-=ay;x1+=ix;
	      ClipArcAc(dbase,fbase,x1,y1,z1,c2);
            }
            else
	      ClipArcDn(dbase,fbase,x1,y1,z1,c2);
        }
    }
  }

  public static void DrawLine(int x1,int y1,int z1, int x2,int y2,int z2,
			      int color, int width)
  {
    int dbase;
    int fbase;

    int zrate,zerr,ystep,err;
    int ix,iy,ax,ay;
    int lx,ly,lz;
    int mid,tmp;
    int temp;

    if(width > 25)
    {
      System.err.println("bond radius " + width + " too large; set to 25");
      width = 25;
    }
    /* Trivial Case */
    if( (x1==x2) && (y1==y2) )
    {   if( z1>z2 )
          DrawSphere(x1,y1,z1,width,color);
        else
          DrawSphere(x2,y2,z2,width,color);
        return;
    }
    
    if( z1<z2 )
    {   tmp=x1; x1=x2; x2=tmp;
        tmp=y1; y1=y2; y2=tmp;
        tmp=z1; z1=z2; z2=tmp;
    }

    lx = x2-x1;
    ly = y2-y1;
    lz = z2-z1;

    if( ly>0 ) { ystep = view.yskip; ay = ly; iy = 1; }
    else { ystep = -view.yskip; ay = -ly; iy = -1; }
    if( lx>0 ) { ax = lx; ix = 1; }
    else { ax = -lx; ix = -1; }
    zrate = lz/Math.max(ax,ay);

    temp = y1*view.yskip+x1;
    fbase = temp;
    dbase = temp;

    if( ax>ay )
    {   lz -= ax*zrate;
        zerr = err = -(ax>>1);

        while( x1!=x2 )
        {   z1 += zrate;  if( (zerr-=lz)>0 ) while(zerr > 0) { zerr-=ax; z1--; }
            fbase+=ix; dbase+=ix; x1+=ix;
	    if( (err+=ay)>0 )
	    {
	      fbase+=ystep; dbase+=ystep; err-=ax;
	      if(x1 >= 0 && x1 < view.xmax && fbase > 0 && fbase < view.fbuf.length)
		UpdateLine(1, y1, color, z1, width, fbase, 0, view.xmax);
	    }
	    else if(x1 >= 0 && x1 < view.xmax && fbase > 0 && fbase < view.fbuf.length)
	      UpdateLine(1, y1, color, z1, width, fbase, 0, view.xmax);
        }
    } else /*ay>=ax*/
    {   lz -= ay*zrate;
        zerr = err = -(ay>>1);

        while( y1!=y2)
        {   z1 += zrate;  if( (zerr-=lz)>0 ) while(zerr > 0) { zerr-=ay; z1--; }
            fbase+=ystep; dbase+=ystep; y1+=iy;
	    if( (err+=ax)>0 )
	    {
	      fbase+=ix; dbase+=ix; err-=ay;
	      if(x1 >= 0 && x1 < view.xmax && fbase > 0 && fbase < view.fbuf.length)
		UpdateLine(1, y1, color, z1, width, fbase, 0, view.xmax);
	    }
	    else if(x1 >= 0 && x1 < view.xmax && fbase > 0 && fbase < view.fbuf.length)
	      UpdateLine(1, y1, color, z1, width, fbase, 0, view.xmax);
        }
    }
  }

/*
  public static void DrawTwinLine(int x1,int y1,int z1, int x2,int y2,int z2,
			      int c1, int c2, int width)
  {
    int xm=(x2+x1)>>1,ym=(y2+y1)>>1,zm=(z2+z1)>>1;

    DrawLine(x1,y1,z1, xm,ym,zm,c1,width);
    DrawLine(xm,ym,zm, x2,y2,z2,c2,width);
  }

*/

  public static void DrawTwinLine( int x1, int y1, int z1,
                   int x2, int y2, int z2,
                   int col1, int col2)//, int width) // char altl )
  {
    int offset;
    int zrate, zerr;
    int ystep,err;
    int ix,iy,iz;
    int dx,dy,dz;
    int mid;
    int c, ca;
    int p, altc;
    int inten=0;

    inten=(int)(ColourDepth*((z1+z2)*0.5+worldRadius-10000.0)/worldSize);
//    System.out.println("inten="+inten+" z="+z1);
    if (inten >24)
      inten=24;
    else if (inten <0)
      inten=0;
    col1+=inten;
    col2+=inten;
    c = Lut[col1];
    altc = 0;
//    ca = c;
//    if ( altl != '\0' && altl != ' ') {
//      altc = AltlColours[((int)altl)&(AltlDepth-1)];
//      ca = Lut[altc];
//    }

    offset = (int)y1*view.yskip + x1;
//    fptr = view.fbuf+offset;
//    dptr = view.dbuf+offset;

//    SETPIXEL(dptr,fptr,z1,c);
    if (view.dbuf[offset] < z1) {
      view.dbuf[offset]=(short)z1;
      view.fbuf[offset]=c;
    }

    dx = x2-x1;  dy = y2-y1; 
    if( 0==dx && 0==dy ) return;
    dz = z2-z1;

    if( dy<0 ) 
    {   ystep = -view.yskip;
        dy = -dy; 
        iy = -1;
    } else
    {   ystep = view.yskip;
        iy = 1;
    }

    if( dx<0 ) 
    {   dx = -dx;
        ix = -1;
    } else ix = 1;

    if( dz<0 ) 
    {   dz = -dz;
        iz = -1;
    } else iz = 1;

    if( dx>dy )
    {   if( dz >= dx )
        {   zrate = dz/dx;
            dz -= dx*zrate;
            if( iz < 0 )
                zrate = -zrate;
        } else zrate = 0;
        err = zerr = -(dx>>1);

        if( col1 != col2 )
        {   mid = (x1+x2)>>1;
            while( x1!=mid ) {
              if((err+=dy)>0) {
//                fptr+=ystep; dptr+=ystep;
                offset+=ystep;
                err-=dx;
              }
//              fptr+=ix; dptr+=ix;
              offset+=ix;
              x1+=ix;
//              p = altc && (x1-mid<(dx/4)) && (mid-x1<(dx/4));

              z1 += zrate;
//              SETPIXELP(dptr,fptr,z1,c,ca,p);
              if (view.dbuf[offset] < z1) {
                view.dbuf[offset]=(short)z1;
                view.fbuf[offset]=c;
              }
              if( (zerr+=dz)>0 ) while (zerr>0) { zerr-=(dx); z1+=iz; }
            }
            c = Lut[col2];
        }
        while( x1!=x2 ) {
          if((err+=dy)>0) {
//            fptr+=ystep; dptr+=ystep;
            offset+=ystep;
            err-=dx;
          } 
//          fptr+=ix; dptr+=ix;
          offset+=ix;
          x1+=ix;
//          p = altc && (x1-mid<(dx/4)) && (mid-x1<(dx/4));
          z1 += zrate;
//          SETPIXELP(dptr,fptr,z1,c,ca,p);
          if (view.dbuf[offset] < z1) {
             view.dbuf[offset]=(short)z1;
             view.fbuf[offset]=c;
          }
          if( (zerr+=dz)>0 ) while (zerr>0){ zerr-=(dx); z1+=iz; }
        }
    } else
    {   if( dz >= dy )
        {   zrate = dz/dy;
            dz -= dy*zrate;
            if( iz < 0 )
                zrate = -zrate;
        } else zrate = 0;
        err = zerr = -(dy>>1);

        if( col1 != col2 )
        {   mid = (y1+y2)>>1;
            while( y1!=mid )
            {
               if((err+=dx)>0) {
//                 fptr+=ix; dptr+=ix;
                 offset+=ix;
                 err-=dy;
               }
//               fptr+=ystep; dptr+=ystep;
               offset+=ystep;
               y1+=iy;
//               p = altc && (y1-mid<(dy/4)) && (mid-y1<(dy/4));

               z1 += zrate;
//               SETPIXELP(dptr,fptr,z1,c,ca,p);
               if (view.dbuf[offset] < z1) {
                 view.dbuf[offset]=(short)z1;
                 view.fbuf[offset]=c;
               }
               if( (zerr+=dz)>0 ) while (zerr>0) { zerr-=(dy); z1+=iz; }
            }
            c = Lut[col2];
        }
        while( y1!=y2 )
        {
           if((err+=dx)>0) {
//             fptr+=ix; dptr+=ix;
             offset+=ix;
             err-=dy;
           }
//           fptr+=ystep; dptr+=ystep;
           offset+=ystep;
           y1+=iy;
//           p = altc && (y1-mid<(dy/4)) && (mid-y1<(dy/4));
           z1 += zrate;
//           SETPIXELP(dptr,fptr,z1,c,ca,p);
           if (view.dbuf[offset] < z1) {
             view.dbuf[offset]=(short)z1;
             view.fbuf[offset]=c;
           }
           if( (zerr+=dz)>0 ) while (zerr>0) { zerr-=(dy); z1+=iz; }
        }
    }
  }

  private static final int BitAbove = 0x01;
  private static final int BitBelow = 0x02;
  private static final int BitRight = 0x04;
  private static final int BitLeft  = 0x08;
  private static final int BitFront = 0x10;
  private static final int BitBack  = 0x20;

  private static int OutCode(int x,int y,int z)
  {
    int code=0;
    {   if( (y)<0 )
        {   code = BitAbove;
        } else if( (y) >= view.ymax )
        {   code = BitBelow;
        } else code = 0;
                        
        if( (x) < 0 )
        {   code |= BitLeft;
        } else if( (x) >= view.xmax )
            code |= BitRight;
        if( !ZValid((z)) )
            code |= BitFront;
//        if( !ZBack((z))  )
//            code |= BitBack;
    }
    return code;
  }

  public static void ClipLine( int x1, int y1, int z1,
               int x2, int y2, int z2,
               int col) //,  char altl )
  {
    int code1=0,code2=0;
    int delta,rest;
    int temp;

    code1=OutCode(x1,y1,z1);
    code2=OutCode(x2,y2,z2);
    if( (code1 & code2) != 0) //Reject(code1,code2) )
        return;

//    System.out.println("OutCode: " + code1 + ',' + code2);
  
    while( 0 != (code1 | code2))//!Accept(code1,code2) )
    {  if( 0==code1 )
        {   temp=x1; x1=x2; x2=temp;
            temp=y1; y1=y2; y2=temp;
            temp=z1; z1=z2; z2=temp;
            code1 = code2;
            code2 = 0;
        }

        if( (code1 & BitAbove)!=0 )
        {   delta = y2-y1;
            x1 += (int)(((long)y1*(x1-x2))/delta);  
            z1 += (int)(((long)y1*(z1-z2))/delta);
            y1 = 0;
        } else if( (code1 & BitLeft)!=0 )
        {   delta = x2-x1;
            y1 += (int)(((long)x1*(y1-y2))/delta);
            z1 += (int)(((long)x1*(z1-z2))/delta);
            x1 = 0;
        } else if( (code1 & BitRight)!=0 )
        {   delta = x2-x1;
            temp=view.xmax-1; rest=temp-x1;
            y1 += (int)(((long)rest*(y2-y1))/delta);
            z1 += (int)(((long)rest*(z2-z1))/delta);
            x1 = temp;
        } else if( (code1 & BitBelow)!=0 )
        {   delta = y2-y1;
            temp=view.ymax-1; rest=temp-y1;
            x1 += (int)(((long)rest*(x2-x1))/delta);
            z1 += (int)(((long)rest*(z2-z1))/delta);
            y1 = temp;
        }
//          else if( code1 & BitFront ) /* SLAB */
//        {   delta = z2-z1;
//            rest = (SlabValue-1)-z1;
//            x1 += (int)(((long)rest*(x2-x1))/delta);
//            y1 += (int)(((long)rest*(y2-y1))/delta);
//            z1 = SlabValue-1;
//        } else /* DEPTH */
//        {   delta = z2-z1;
//            rest = (DepthValue+1)-z1;
//            x1 += (int)(((long)rest*(x2-x1))/delta);
//            y1 += (int)(((long)rest*(y2-y1))/delta);       
//            z1 = DepthValue+1;
//        }

        code1=OutCode(x1,y1,z1);
        if( (code1 & code2)!=0)//Reject(code1,code2) )
            return;

    }
    DrawTwinLine(x1,y1,z1,x2,y2,z2,col,col);//,1);//,altl);
  }

  public static void ClipTwinLine( int x1, int y1, int z1,
                   int x2, int y2, int z2,
                   int col1, int col2)//char altl )
 {
    int xmid,ymid,zmid;
    int code1=0,code2=0;

    if( col1!=col2 )
    {   code1=OutCode(x1,y1,z1);
        code2=OutCode(x2,y2,z2);
        if( (code1&code2)==0 ) // !Reject(code1,code2)
        {   if( (code1|code2)!=0)//!Accept(code1,code2) )
            {  xmid = (x1+x2)/2;
               ymid = (y1+y2)/2;
               zmid = (z1+z2)/2;
               ClipLine(x1,y1,z1,xmid,ymid,zmid,col1);//,altl);
               ClipLine(xmid,ymid,zmid,x2,y2,z2,col2);//,altl);
            } else
               DrawTwinLine(x1,y1,z1,x2,y2,z2,col1,col2);//,altl);
        }
    } else ClipLine(x1,y1,z1,x2,y2,z2,col1);//,altl);
}



  public static void ClipDashLine( int x1, int y1, int z1,
                                   int x2, int y2, int z2,
                                   int col1, int col2) // char altl )
  {
    int offset;
    int ix,iy,iz;
    int dx,dy,dz;
    int zrate, zerr;
    int ystep,err;
    int co;
    int c, mid;
    //int ca;
    int count;
    //int altc;

    if( (x1==x2) && (y1==y2) )
         return;

    /* Reject(OutCode(x1,y1,z1),OutCode(x2,y2,z2)) */
    if( (x1<0) && (x2<0) ) return;
    if( (y1<0) && (y2<0) ) return;
    if( (x1>=view.xmax) && (x2>=view.xmax) ) return;
    if( (y1>=view.ymax) && (y2>=view.ymax) ) return;

    c = Lut[col1];
// altc = 0;
//  ca = c;
//  if ( altl != '\0' && altl != ' ') {
//    altc = AltlColours[((int)altl)&(AltlDepth-1)];
//    ca = Lut[altc];
//  }

    dx = x2 - x1;  
    dy = y2 - y1;
    dz = z2 - z1;  

    offset = y1*view.yskip + x1;
//    fptr = view.fbuf+offset;
//    dptr = view.dbuf+offset;
    count = 0;

    ystep = view.yskip;
    ix = iy = iz = 1;
    if( dy<0 ) { dy = -dy; iy = -1; ystep = -ystep; }
    if( dx<0 ) { dx = -dx; ix = -1; }
    if( dz<0 ) { dz = -dz; iz = -1; }

    if( dx>dy )
    {   if( x2<x1 )
        {   mid = col1;
            col1 = col2;
            col2 = mid;
        }
        if( dz >= dx )
        {   zrate = dz/dx;
            dz -= dx*zrate;
            if( iz < 0 )
                zrate = -zrate;
        } else zrate = 0;

        err = zerr = -(dx>>1);
        mid = (x1+x2)/2;

        while( x1!=x2 )
        {   if( XValid(x1) && YValid(y1) ) //&& ZValid(z1) && ZBack(z1) )
            {   if( count<2 )
                {   co = (x1<mid)? col1 : col2;
                    c = Lut[co];
                    //SETPIXEL(dptr,fptr,z1,c);
                    view.dbuf[offset]=(short)z1;
                    view.fbuf[offset]=c;
                    count++;
                } else if( count==3 )
                {   count = 0;
                } else count++;
            }

            if( (err+=dy)>0 )
            {   err -= dx;
//               fptr+=ystep;
//               dptr+=ystep;
                offset+=ystep;
                y1+=iy;
            }

            if( (zerr+=dz)>0 ) while (zerr>0) {   zerr -= dx; z1 += iz; }

//            fptr+=ix; dptr+=ix;
            offset+=ix;
            x1 +=ix;
            z1 += zrate;
        }
    } else
    {   if( y1>y2 )
        {   mid = col1;
            col1 = col2;
            col2 = mid;
        }

        if( dz >= dy )
        {   zrate = dz/dy;
            dz -= dy*zrate;
            if( iz < 0 )
                zrate = -zrate;
        } else zrate = 0;

        err = zerr = -(dy>>1);
        mid = (y1+y2)/2;
        
        while( y1!=y2 )
        {   if( XValid(x1) && YValid(y1) ) //&& ZValid(z1) && ZBack(z1) )
            {   if( count<2 )
                {   co = (y1<mid)? col1 : col2;
		    c = Lut[co];
                    //SETPIXEL(dptr,fptr,z1,c);
                    view.dbuf[offset]=(short)z1;
                    view.fbuf[offset]=c;
                    count++;
                } else if( count==3 )
                {   count = 0;
                } else count++;
            }

            if( (err+=dx)>0 )
            {   err-=dy;
//                fptr+=ix;
//                dptr+=ix;
                offset+=ix;
                x1+=ix;
            }

            if( (zerr+=dz)>0 ) while(zerr>0) {   zerr -= dy; z1 += iz; }
//            fptr+=ystep; dptr+=ystep;
            offset+=ystep;
            y1+=iy;
            z1 += zrate; 
        }
    }
  }

  public static void ClipPolygon(Vert[] poly, int count )
  {
    Edge lft=new Edge(), rgt=new Edge();
//    Edge *pmin, *pmax;
    Edge pmin, pmax;

//    Pixel __huge *fbase;
//    short __huge *dbase;
//    short __huge *dptr;
    int dptr;
    long offset=0;

    long dz,di;
    long z,inten;
    int ri,li,ry,ly;
    int xmin,xmax;
    int dy,ymin;
    int top,rem;
    int x,y,i;

    /* Reject Clip Polygon */
/*
    if( UseSlabPlane )
        for( i=0; i<count; i++ )
            if( poly[i].z >= SlabValue )
                return;
    if( UseDepthPlane )
        for( i=0; i<count; i++ )
            if( poly[i].z <= DepthValue )
                return;
*/

    /* Find top vertex */
    top = 0;  
    ymin = poly[0].y;
    for( i=1; i<count; i++ ) {
       if( poly[i].y < ymin )
       {   ymin = poly[i].y;
           top = i;
       }
    }

    rem = count;
    ly = ry = y = ymin;
    li = ri = top;

    if( y<0 )
    {   rem--;

        while( ly<=0 && rem > 0 )
        {   i = li-1; if( i<0 ) i=count-1;
            if( poly[i].y > 0 )
            {   dy = poly[i].y - ly;
                lft.di = (((long)(poly[i].inten - poly[li].inten))<<16)/dy;
                lft.dx = (((long)(poly[i].x - poly[li].x))<<16)/dy;
                lft.dz = (((long)(poly[i].z - poly[li].z))<<16)/dy;

                lft.i = (((long)poly[li].inten)<<16) - (long)ly*lft.di;
                lft.x = (((long)poly[li].x)<<16) - (long)ly*lft.dx;
                lft.z = (((long)poly[li].z)<<16) - (long)ly*lft.dz;
            } else rem--;
            ly = poly[i].y;
            li = i;
        }

        while( ry<=0 && rem > 0 )
        {   i = ri+1; if( i>=count ) i = 0;
            if( poly[i].y > 0 )
            {   dy = poly[i].y - ry;
                rgt.di = (((long)(poly[i].inten - poly[ri].inten))<<16)/dy;
                rgt.dx = (((long)(poly[i].x - poly[ri].x))<<16)/dy;
                rgt.dz = (((long)(poly[i].z - poly[ri].z))<<16)/dy;

                rgt.i = (((long)poly[ri].inten)<<16) - (long)ry*rgt.di;
                rgt.x = (((long)poly[ri].x)<<16) - (long)ry*rgt.dx;
                rgt.z = (((long)poly[ri].z)<<16) - (long)ry*rgt.dz;
            } else rem--;
            ry = poly[i].y;
            ri = i;
        }

//        fbase = view.fbuf;
//        dbase = view.dbuf;
        y = 0;
    } else /* y >= 0 */
    {
          offset = (long)y*view.yskip;
//        fbase = view.fbuf+offset;
//        dbase = view.dbuf+offset;
    }

    while( rem > 0 )
    {   while( ly<=y && rem > 0 )
        {   i = li-1; if( i<0 ) i=count-1;
            if( poly[i].y > y )
            {   dy = poly[i].y - ly;
                lft.di = (((long)(poly[i].inten - poly[li].inten))<<16)/dy;
                lft.dx = (((long)(poly[i].x - poly[li].x))<<16)/dy;
                lft.dz = (((long)(poly[i].z - poly[li].z))<<16)/dy;

                lft.i = ((long)poly[li].inten)<<16;
                lft.x = ((long)poly[li].x)<<16;
                lft.z = ((long)poly[li].z)<<16;
            }
            ly = poly[i].y;
            rem--;  li = i;
        }

        while( ry<=y && rem > 0 )
        {   i = ri+1; if( i>=count ) i = 0;
            if( poly[i].y > y )
            {   dy = poly[i].y - ry;
                rgt.di = (((long)(poly[i].inten - poly[ri].inten))<<16)/dy;
                rgt.dx = (((long)(poly[i].x - poly[ri].x))<<16)/dy;
                rgt.dz = (((long)(poly[i].z - poly[ri].z))<<16)/dy;

                rgt.i = ((long)poly[ri].inten)<<16;
                rgt.x = ((long)poly[ri].x)<<16;
                rgt.z = ((long)poly[ri].z)<<16;
            }
            ry = poly[i].y;
            rem--; ri = i;
        }

        ymin = (ly < ry) ? ly:ry;
        if( ymin>view.ymax )
        {   ymin = view.ymax;
            rem = 0;
        }
        
        while( y<ymin )
        {   if( lft.x < rgt.x )
            {   pmin = lft;
                pmax = rgt;
            } else
            {   pmin = rgt;
                pmax = lft;
            }

            xmax = (int)(pmax.x>>16)+1;
            xmin = (int)(pmin.x>>16);

            if( (xmin<view.xmax) && (xmax>=0) )
            {   di = (long)((pmax.i-pmin.i)/(xmax-xmin));
                dz = (long)((pmax.z-pmin.z)/(xmax-xmin));
                if( xmin<0 )
                {   inten = pmin.i - xmin*di;
                    z = pmin.z - xmin*dz;
                    xmin = 0;
                } else /* xmin >= 0 */
                {   inten = pmin.i;  
                    z = pmin.z;
                }

                if( xmax>=view.xmax )
                    xmax = view.xmax;

//                dptr = dbase+xmin;
                dptr = (int)offset+xmin;
                for( x=xmin; x<xmax; x++ )
//                {   if( (int)(z>>16) > *dptr )
                {
                    if( (short)(z>>16) > view.dbuf[dptr] )
                    {
//                        fbase[x] = Lut[(int)(inten>>16)];
//                        *dptr = (int)(z>>16);
                        view.fbuf[(int)offset+x] = Lut[(int)(inten>>16)];
                        view.dbuf[dptr] = (short)(z>>16);
                    }
                    inten += di;
                    z += dz;
                    dptr++;
                }
            }

            lft.x += lft.dx;  rgt.x += rgt.dx;
            lft.z += lft.dz;  rgt.z += rgt.dz;
            lft.i += lft.di;  rgt.i += rgt.di;
//            dbase += view.yskip;
//            fbase += view.yskip;
            offset +=view.yskip;

            y++;
        }
    }
  }


  private static void Resize(int high, int wide)
  {
    if(view == null || view.xmax != wide || view.ymax != high) {
      view = new ViewStruct(high,wide);
      RedrawFlag = true;
    }
  }

  public static ImageProducer imageProducer(int xmax, int ymax)
  {
//  InitialiseTransform();
    Resize(ymax, xmax);
//    t1 = System.currentTimeMillis();
//  System.err.println("imageProducer " + view.xmax + "," + view.ymax + "," + view.yskip);
    if (RedrawFlag || RasmolImg == null) {
      int i;
      ColorModel colormodel = new DirectColorModel(24,0xff0000,0xff00,0xff);
      RasmolImg= new MemoryImageSource(view.xmax, view.ymax, colormodel,
				       view.fbuf, 0, view.yskip);
      RasmolImg.setAnimated(true);
      RasmolImg.setFullBufferUpdates(true);
      RedrawFlag = false;
      for (i=0;i<view.fbuf.length;i++) view.fbuf[i]= Lut[BackCol];
    } else {
      RasmolImg.newPixels(0,0,xmax,ymax);
    }
    return (RasmolImg);
  }

}

/*

void AminoColourAttrib()
{
    ShadeRef *ref;
    Chain __far *chain;
    Group __far *group;
    Atom __far *ptr;
    int i;

    if( !Database ) return;
    for( i=0; i<13; i++ )
	AminoShade[i].col = 0;
    ResetColourAttrib();

    ForEachAtom
	if( ptr->flag&SelectFlag )
	{   if( IsAmino(group->refno) )
	    {   ref = AminoShade + AminoIndex[group->refno];
	    } else ref = AminoShade+12;

	    if( !ref->col )
	    {   ref->shade = DefineShade( ref->r, ref->g, ref->b );
		ref->col = Shade2Colour(ref->shade);
	    }
	    Shade[ref->shade].refcount++;
	    ptr->col = ref->col;
	}
}


void ShapelyColourAttrib()
{
    ShadeRef *ref;
    Chain __far *chain;
    Group __far *group;
    Atom __far *ptr;
    int i;

    if( !Database ) return;
    for( i=0; i<30; i++ )
	Shapely[i].col = 0;
    ResetColourAttrib();

    ForEachAtom
	if( ptr->flag&SelectFlag )
	{   if( IsAminoNucleo(group->refno) )
	    {   ref = Shapely + group->refno;
	    } else ref = Shapely+30;

	    if( !ref->col )
	    {   ref->shade = DefineShade( ref->r, ref->g, ref->b );
		ref->col = Shade2Colour(ref->shade);
	    }
	    Shade[ref->shade].refcount++;
	    ptr->col = ref->col;
	}
}


void StructColourAttrib()
{
    ShadeRef *ref;
    Chain __far *chain;
    Group __far *group;
    Atom __far *ptr;
    int i;

    if( !Database )
	return;

    if( InfoHelixCount<0 )
	DetermineStructure(False);

    for( i=0; i<4; i++ )
	StructShade[i].col = 0;
    ResetColourAttrib();

    ForEachAtom
	if( ptr->flag&SelectFlag )
	{   if( group->struc & HelixFlag )
	    {   ref = StructShade+1;
	    } else if( group->struc & SheetFlag )
	    {   ref = StructShade+2;
	    } else if( group->struc & TurnFlag )
	    {   ref = StructShade+3;
	    } else ref = StructShade;

	    if( !ref->col )
	    {   ref->shade = DefineShade( ref->r, ref->g, ref->b );
		ref->col = Shade2Colour(ref->shade);
	    }
	    Shade[ref->shade].refcount++;
	    ptr->col = ref->col;
	}
}

int IsCPKColour(atom ptr )
{
    ShadeRef *cpk;
    ShadeDesc *col;

    cpk = CPKShade + Element[ptr->elemno].cpkcol;
    col = Shade + Colour2Shade(ptr->col);
    return( (col->r==cpk->r) && 
	    (col->g==cpk->g) && 
	    (col->b==cpk->b) );
}


int IsVDWRadius( ptr )
    Atom __far *ptr;
{
    int rad;

    if( ptr->flag & SphereFlag )
    {   rad = ElemVDWRadius( ptr->elemno );
        return( ptr->radius == rad );
    } else return( False );
}
*/
