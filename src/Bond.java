
/**
 * Bond.java
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

import java.util.Vector;
import PDBAtom;

public class Bond
{
/* Bond Flags */
  public static final int WireFlag      = 0x02; /* Depth-cued wireframe         */
  public static final int DashFlag      = 0x04; /* Dashed Depth-cued wireframe  */
  public static final int CylinderFlag  = 0x08; /* Line/Cylinder representation */

  public static final int HydrBondFlag  = 0x00; /* Hydrogen bond [place keeper] */
  public static final int NormBondFlag  = 0x10;
  public static final int DoubBondFlag  = 0x20;
  public static final int TripBondFlag  = 0x40;
  public static final int AromBondFlag  = 0x80;

  private static Bond FreeBond;

  private atom srcatom;             /* Source Atom Ptr       */
  private atom dstatom;             /* Destination Atom Ptr  */
  private short radius;                    /* World Radius          */
  private short irad;                      /* Image Radius          */
  private short col;                       /* Bond Colour           */
  private int  flag;                      /* Database flags        */

  public final atom sourceAtom() { return srcatom; }
  public final atom destAtom() { return dstatom; }
  public final boolean contains(atom a) { return a.equals(srcatom) || a.equals(dstatom); }
  public final boolean checkFlag(int f) { return (flag & f) != 0; }
  public Bond(atom src, atom dst, int flags )
  {
    int i;
    /*
    if( (flag & (DoubBondFlag|TripBondFlag)) != 0)
        DrawDoubleBonds = true;
    */
    /*
    if( (ptr = FreeBond) == null)
    {
	ptr = new Bond[BondPool];
	for( i=1; i<BondPool; i++ )
	{   ptr.bnext = FreeBond;
	    FreeBond = ptr++;
	} 
    } else FreeBond
    */

    flag = flags | PDBAtom.SelectFlag;
    srcatom = src;
    dstatom = dst;
    radius = 0;
    col = 0;
  }
  /*
  public void CreateBond(int src,int dst, int flag )
  {
    PDBAtom sptr = null;
    PDBAtom dptr = null;
    Bond bptr;
    boolean done = false;
    int i,j,k;

    if( src == dst )
	return;

    for( i = 0; i < Database.clist.size() && !done; ++i)
    {
        Chain chain = Database.clist[i];
	for(j = 0; j < chain.glist.size() && !done; ++j)
	{
	    Group group = chain.elementAt(j);
	    for(k = 0; k < group.alist.size() && group.alist[k] != null; ++k)
	    {
	        PDBAtom aptr=group.alist[k];
	        if( aptr.serno == src )
		{   sptr = aptr;
		    if( dptr != null)
		    {   done = true;
			break;
		    }
		} else if( aptr.serno == dst )
		{   dptr = aptr;
		    if( sptr != null)
		    {   done = true;
			break;
		    }
		}
	    }
	}
    }

    // Both found!
    if( done ) 
    {   if( flag != 0)
        {   // Reset Non-bonded flags!
	    sptr.flag &= ~PDBAtom.NonBondFlag;
	    dptr.flag &= ~PDBAtom.NonBondFlag;

	    CurMolecule.blist.addElement(ProcessBond( sptr, dptr, flag ));
	    InfoBondCount++;

        } else // Hydrogen Bond!
        {   if( InfoHBondCount<0 ) 
            {   CurHBond = CurMolecule.hlist[0];
                InfoHBondCount = 0;
            }
            CreateHydrogenBond( null, null, sptr, dptr, 0, 0 );
        }
    }
  }

  public void CreateBondOrder(int src, int dst)
  {
    int bs,bd;
    int i;

    for(i = 0; i < Database.blist.length(); ++i)
    {
	Bond bptr = Database.blist[i];
	bs = bptr.srcatom.serno;
	bd = bptr.dstatom.serno;

	if( ((bs==src)&&(bd==dst)) || ((bs==dst)&&(bd==src)) )
        {   DrawDoubleBonds = true;
	    if((bptr.flag & NormBondFlag) != 0)
	    {  // Convert Single to Double
	       bptr.flag &= ~(NormBondFlag);
	       bptr.flag |= DoubBondFlag;
	    } else if( (bptr.flag & DoubBondFlag) != 0)
	    {  // Convert Double to Triple
	       bptr.flag &= ~(DoubBondFlag);
	       bptr.flag |= TripBondFlag;
	    }
	    return;
	}
    }
    CreateBond( src, dst, NormBondFlag );
  }
  */
}
