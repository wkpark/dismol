/**
 * GenericGroup.java
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from 
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
import java.io.InputStream;
import java.io.IOException;
import PDBAtom;
import group;
import MaskDesc;
import Element;

public class GenericGroup extends group
{
  public static final String rcsid =
  "$Id: GenericGroup.java,v 1.10 1998/07/21 22:45:00 pcm Exp $";

  public int refno;                      /* Residue index number, pdb  */
  protected static long MinX=0, MinY=0, MinZ=0;
  protected static long MaxX=0, MaxY=0, MaxZ=0;
  protected static long OrigCX=0, OrigCY=0, OrigCZ=0;
  protected static long WorldRadius, WorldSize;
  protected static boolean HMinMaxFlag, MMinMaxFlag;
  protected static int MinMainTemp, MaxMainTemp;
  protected static int MinHetaTemp, MaxHetaTemp;
  protected int HetaAtomCount = 0;
  protected int MainAtomCount = 0;
  protected boolean HasHydrogen = false;
  protected static final long AbsMaxBondDist = 600;
  protected static final int VOXORDER = 21;
  protected static final int VOXORDER2 = (VOXORDER*VOXORDER);
  protected static final int VOXSIZE = (VOXORDER2*VOXORDER);
  protected static final long MaxHBondDist = 300*300;
  protected static final long MaxBondDist = 475*475;
  protected static final long MinBondDist = 100*100;

  protected static Vector ElemDesc;
  protected static Vector HashTable[] = new Vector[VOXSIZE];
  protected boolean IsSolvent(int x){ return (((x)>=46) && ((x)<=49)); }

  static
  {
      ElemDesc = new Vector();

      ElemDesc.addElement(" N  ");  /* 0*/
      ElemDesc.addElement(" CA ");  /* 1*/
      ElemDesc.addElement(" C  ");  /* 2*/
      ElemDesc.addElement(" O  ");  /* 3*/   /* 0-3   Amino Acid Backbone    */
      ElemDesc.addElement(" C\' "); /* 4*/
      ElemDesc.addElement(" OT ");  /* 5*/
      ElemDesc.addElement(" S  ");  /* 6*/
      ElemDesc.addElement(" P  ");  /* 7*/   /* 4-7   Shapely Amino Backbone */
      ElemDesc.addElement(" O1P");  /* 8*/
      ElemDesc.addElement(" O2P");  /* 9*/
      ElemDesc.addElement(" O5*");  /*10*/
      ElemDesc.addElement(" C5*");  /*11*/
      ElemDesc.addElement(" C4*");  /*12*/
      ElemDesc.addElement(" O4*");  /*13*/
      ElemDesc.addElement(" C3*");  /*14*/
      ElemDesc.addElement(" O3*");  /*15*/
      ElemDesc.addElement(" C2*");  /*16*/
      ElemDesc.addElement(" O2*");  /*17*/
      ElemDesc.addElement(" C1*");  /*18*/   /* 7-18  Nucleic Acid Backbone  */
      ElemDesc.addElement(" CA2");  /*19*/   /* 19    Shapely Special        */
      ElemDesc.addElement(" SG ");  /*20*/   /* 20    Cysteine Sulphur       */
      ElemDesc.addElement(" N1 ");  /*21*/
      ElemDesc.addElement(" N2 ");  /*22*/
      ElemDesc.addElement(" N3 ");  /*23*/
      ElemDesc.addElement(" N4 ");  /*24*/
      ElemDesc.addElement(" N6 ");  /*25*/
      ElemDesc.addElement(" O2 ");  /*26*/
      ElemDesc.addElement(" O4 ");  /*27*/
      ElemDesc.addElement(" O6 ");  /*28*/   /* 21-28 Nucleic Acid H-Bonding */
  }

  protected int NewAtomType(String ptr)
  {
    int refno;
    for( refno=0; refno < ElemDesc.size(); refno++ )
	if( ((String)ElemDesc.elementAt(refno)).startsWith(ptr) )
	  {
	    return(refno);
	  }

    ElemDesc.addElement(ptr);
    return( refno );
  }

  public void setDefaultZoomFactor()
  {
    int i;
    double minx = 1.e9;
    double maxx = -1.e9;
    double miny = 1.e9;
    double maxy = -1.e9;
    double minz = 1.e9;
    double maxz = -1.e9;
    for(i = 0; i < atomList.size(); ++i)
    {
        PDBAtom ptr = (PDBAtom)atomList.elementAt(i);
	if(ptr.x[0] > maxx) maxx = ptr.x[0];
	if(ptr.x[0] < minx) minx = ptr.x[0];
	if(ptr.x[1] > maxy) maxy = ptr.x[1];
	if(ptr.x[1] < miny) miny = ptr.x[1];
	if(ptr.x[2] > maxz) maxz = ptr.x[2];
	if(ptr.x[2] < minz) minz = ptr.x[2];
    }
    double z = Math.max(maxx-minx+2,maxy-miny+2);
    if(z > 0)
    {
      int maxdim = 550;
      if(mypanel_size != null)
      {
	int m = Math.min(mypanel_size.width, mypanel_size.height);
	if(m > 0) maxdim = m;
      }
      v.zoomFactor = 0.75*maxdim/z;
    }
    else super.setDefaultZoomFactor();
		// center display on molecule:
    v.setDefaultCenter(-(minx + maxx)/2, -(miny + maxy)/2);
    if(false) System.err.println("zfactor " + z + " => " + v.zoomFactor
				+ " min " + minx + "," + miny
				+ " max " + maxx + "," + maxy);
  }

  public void setColors()
  {
    //ShadeRef.CPKColourAttrib(atomList);
  }

  protected void ProcessAtom( PDBAtom ptr )
  {
    ptr.elemno = ptr.GetElemNumber(refno,(String)ElemDesc.elementAt(ptr.refno));
    if( ptr.elemno == 1 )
    {   ptr.flag |= PDBAtom.HydrogenFlag;
	HasHydrogen = true;
    }

    ptr.setParms(ptr.elemno);

    if( !IsSolvent(refno) )
    {   if( (ptr.flag&(PDBAtom.HydrogenFlag|PDBAtom.HeteroFlag)) == 0)
	    ptr.flag |= PDBAtom.NormAtomFlag;
    } else ptr.flag |= PDBAtom.HeteroFlag;

    /*#ifdef INVERT
    ptr.yorg = -ptr.yorg;
    #endif*/

    if( HMinMaxFlag || MMinMaxFlag )
    {   if( ptr.xorg < MinX ) 
	{   MinX = ptr.xorg;
	} else if( ptr.xorg > MaxX ) 
	    MaxX = ptr.xorg;

	if( ptr.yorg < MinY ) 
	{   MinY = ptr.yorg;
	} else if( ptr.yorg > MaxY ) 
	    MaxY = ptr.yorg;

	if( ptr.zorg < MinZ ) 
	{   MinZ = ptr.zorg;
	} else if( ptr.zorg > MaxZ ) 
	    MaxZ = ptr.zorg;
    } else 
    {   MinX = MaxX = ptr.xorg;
	MinY = MaxY = ptr.yorg;
	MinZ = MaxZ = ptr.zorg;
    }
	    
    if( (ptr.flag & PDBAtom.HeteroFlag) == 0)
    {   if( HMinMaxFlag )
	{   if( ptr.temp < MinHetaTemp ) 
	    {   MinHetaTemp = ptr.temp;
	    } else if( ptr.temp > MaxHetaTemp ) 
		MaxHetaTemp = ptr.temp;
	} else MinHetaTemp = MaxHetaTemp = ptr.temp;
	HMinMaxFlag = true;
	HetaAtomCount++;
    }
    else
    {
        if( MMinMaxFlag )
	{   if( ptr.temp < MinMainTemp ) 
	    {   MinMainTemp = ptr.temp;
	    } else if( ptr.temp > MaxMainTemp ) 
		MaxMainTemp = ptr.temp;
	} else MinMainTemp = MaxMainTemp = ptr.temp;
	MMinMaxFlag = true;
	MainAtomCount++;
    }
  }

  protected static String FetchRecord(InputStream fp)
  {
    StringBuffer buf = new StringBuffer();
    int i = 0;
    try
    {
      do
      {
	char ch;
	int c = fp.read();
	if(c == -1) break;
	ch = (char)c;
	if(ch == '\n' || ch == '\r')
	{
	  if(i == 0) continue;
	  else break;
	}
	buf.append(ch);
	++i;
      }while(i < 200);
    } catch (IOException e) {
      System.err.println("IOException " + e.getMessage());
      return buf.toString();	/* ?? */
    }
    return buf.toString();
  }

  protected void TestBonded(PDBAtom sptr, PDBAtom dptr, boolean flag )
  {
    Bond bptr;
    long dx, dy, dz;
    long max, dist;

    if( flag )
    {    /* Sum of covalent radii with 0.56A tolerance */
         dist = Element.getElement(sptr.elemno).covalrad + 
                Element.getElement(dptr.elemno).covalrad + 140;
         max = dist*dist;  
    } else 
    {    /* Fast Bio-Macromolecule Bonding Calculation */
         if( ((sptr.flag|dptr.flag) & PDBAtom.HydrogenFlag) != 0 )
	 {      max = MaxHBondDist;
         } else max = MaxBondDist;
    }

    dx = sptr.xorg-dptr.xorg;   if( (dist=dx*dx)>max ) return;
    dy = sptr.yorg-dptr.yorg;   if( (dist+=dy*dy)>max ) return;
    dz = sptr.zorg-dptr.zorg;   if( (dist+=dz*dz)>max ) return;

    if( dist > MinBondDist )
    {   /* Reset Non-bonded flags! */
	sptr.flag &= ~PDBAtom.NonBondFlag;
	dptr.flag &= ~PDBAtom.NonBondFlag;
	addBond(sptr,dptr);
	//bondList.addElement(new Bond(sptr,dptr,Bond.NormBondFlag));
	 
	//InfoBondCount++;
    }
  }

  protected Vector ExtractBonds(Vector bondlist)
  {
    Vector result = new Vector();
    int i;
    for(i = 0; i < bondlist.size(); ++i)
    {
        Bond temp = (Bond)bondlist.elementAt(i);
        if(!temp.checkFlag(Bond.NormBondFlag)) /* Double or Triple! */
            result.addElement(temp);
    }
    return( result );
  }

  protected void CreateMoleculeBonds(boolean info, boolean flag )
  {
    int i, j, x, y, z;
    long dx, dy, dz;
    //Group *group;
    //Bond  list;

    dx = (MaxX-MinX)+1;
    dy = (MaxY-MinY)+1;
    dz = (MaxZ-MinZ)+1;

    /* Save Explicit Double and Triple Bonds! */
    //list = ExtractBonds( CurMolecule->blist );
    //InfoBondCount = 0;
    for (i=0;i<VOXSIZE;i++)
       HashTable[i] = new Vector();

    for(j = 0; j < atomList.size(); ++j)
    {
        PDBAtom aptr = (PDBAtom)atomList.elementAt(j);
	//   ResetVoxelData();
	       /* Initially non-bonded! */
	aptr.flag |= PDBAtom.NonBondFlag;

	long mx = aptr.xorg-MinX;
	long my = aptr.yorg-MinY;
	long mz = aptr.zorg-MinZ;

	long tx = mx-AbsMaxBondDist;  
	long ty = my-AbsMaxBondDist;  
	long tz = mz-AbsMaxBondDist;  

	int lx = (tx>0)? (int)((VOXORDER*tx)/dx) : 0;
	int ly = (ty>0)? (int)((VOXORDER*ty)/dy) : 0;
	int lz = (tz>0)? (int)((VOXORDER*tz)/dz) : 0;

	tx = mx+AbsMaxBondDist;  
	ty = my+AbsMaxBondDist;  
	tz = mz+AbsMaxBondDist;

	int hx = (tx<dx)? (int)((VOXORDER*tx)/dx) : VOXORDER-1;
	int hy = (ty<dy)? (int)((VOXORDER*ty)/dy) : VOXORDER-1;
	int hz = (tz<dz)? (int)((VOXORDER*tz)/dz) : VOXORDER-1;
	for( x=lx; x<=hx; x++ )
	{
	  i = VOXORDER2*x + VOXORDER*ly;
	  for( y=ly; y<=hy; y++ )
	  {
	    for( z=lz; z<=hz; z++ )
	    {
	      Vector dptr = HashTable[i+z];
	      if(dptr != null)
	      {
		int k;
		for(k = 0; k < dptr.size(); ++k)
		  TestBonded(aptr,(PDBAtom)dptr.elementAt(k),flag);
	      }
	    }
	    i += VOXORDER;
	  }
	}
		
	x = (int)((VOXORDER*mx)/dx);
	y = (int)((VOXORDER*my)/dy);
	z = (int)((VOXORDER*mz)/dz);

	i = VOXORDER2*x + VOXORDER*y + z;
	if(HashTable[i] == null)
	  HashTable[i] = new Vector();
	HashTable[i].addElement(aptr);
	//VoxelsClean = false;
    }

    /* Replace Double & Triple Bonds! */
    //InsertBonds(&CurMolecule->blist,list);

    /*
    if( info )
    {   if( CommandActive )
	    WriteChar('\n');
	CommandActive=False;
	char buffer[40];
	sprintf(buffer,"Number of Bonds ..... %ld\n\n",(long)InfoBondCount);
	WriteString(buffer);
    }
    */
  }
}
