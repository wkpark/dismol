
/**
 * pdbgroup.java
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
import PDBAtom;
import GenericGroup;
import MaskDesc;
import Element;

public class PDBGroup extends GenericGroup
{
  public static final String rcsid =
  "$Id: PDBGroup.java,v 1.5 1998/04/02 23:03:15 pcm Exp $";

  public int serno;                      /* Group serial number, pdb   */
  private PDBAtom ConnectAtom;
  private static char PDBInsert;
  public boolean have_connect_atom = false;

  public PDBGroup()
  {
  }

  private boolean IsSugarPhosphate(int c){ return c == 7; }
  private boolean IsNucleo(int c){ return ((c>=24) && (c<=42)); }
  private boolean IsProtein(int c){ return ((c<=23) || ((c>=43) && (c<=45))); }
  private boolean IsAlphaCarbon(int c){ return c == 1; }

  private long ReadPDBCoord( String Record, int offset)
  {
    if(Record.length() <= offset)
      return 0;
    int len = Math.min(8,Record.length() - offset);
    long result = (long)(Double.valueOf(Record.substring(offset,offset + len).trim()).doubleValue() * 1000);
  
    if(false)
    {
      System.err.println("Record: " + Record.substring(offset,offset+len)
			 + " result " + result);
      System.err.println(Record.length() + " offset " + offset + " len " + len);
    }
    return result;
  }

  public void ProcessPDBAtom(String Record, boolean heta)
  {
     PDBAtom ptr;
     long dx,dy,dz;
     int temp,serno;
 
     dx = ReadPDBCoord(Record, 30);
     dy = ReadPDBCoord(Record, 38);
     dz = ReadPDBCoord(Record, 46);
 
    /* Process Pseudo Atoms Limits!! */
     if( (Record.charAt(13)=='Q') && (Record.charAt(12)==' ') )
     {
        temp = Integer.parseInt(Record.substring(60,66));
        if( MMinMaxFlag)
        {   if( temp < MinMainTemp )
            {   MinMainTemp = temp;
            } else if( temp > MaxMainTemp )
                MaxMainTemp = temp;
        }
 
        /* Dummy co-ordinates! */
        if( (dx==dy) && (dx==dz) )
        {   if( dx == 0 || (dx == 9999000L) )
                return;
        }
 
        if( HMinMaxFlag || MMinMaxFlag )
        {   if( dx < MinX )
            {   MinX = dx;
            } else if( dx > MaxX )
                MaxX = dx;
 
            if( dy < MinY )
            {   MinY = dy;
            } else if( dy > MaxY )
                MaxY = dy;
 
            if( dz < MinZ )
            {   MinZ = dz;
            } else if( dz > MaxZ )
                MaxZ = dz;
        }
        return;
     }
 
 
    /* Ignore XPLOR Pseudo Atoms!! */
     if( (dx==9999000L) && (dy==9999000L) && (dz==9999000L) )
       return;
 
     serno = Integer.parseInt(Record.substring(22,26).trim());
     /*
     if( CurGroup == null || (CurGroup.serno!=serno)
        || (CurChain.ident!=Record.charAt(21))
        || (PDBInsert!=Record.charAt(26)) )
        ProcessPDBGroup(Record, heta, serno );
     */
 
    ptr = new PDBAtom();
    ptr.refno = ComplexAtomType(Record,12);
    ptr.serno = Integer.parseInt(Record.substring(6,11).trim());
    if(Record.length() > 60)
      ptr.temp = (short)(Double.valueOf(Record.substring(60,Math.min(66,Record.length())).trim()).doubleValue()*100);
    else ptr.temp = 0;
    ptr.altl = Record.charAt(16);
 
    ptr.xorg =  dx/4;
    ptr.yorg =  dy/4;
    ptr.zorg = -dz/4;
    ptr.x[0] = dx/1000.0;
    ptr.x[1] = dy/1000.0;
    ptr.x[2] = -dz/1000.0;
 
    if( heta ) ptr.flag |= PDBAtom.HeteroFlag;
    ProcessAtom(ptr);
    addAtom(ptr);
 
    /* Create biopolymer Backbone */
    if( IsAlphaCarbon(ptr.refno) && IsProtein(refno) )
    {   if( have_connect_atom )
        {   dx = ConnectAtom.xorg - ptr.xorg;
            dy = ConnectAtom.yorg - ptr.yorg;
            dz = ConnectAtom.zorg - ptr.zorg;
 
            /* Break backbone if CA-CA > 7.00A */
            if( dx*dx+dy*dy+dz*dz < (long)1750*1750 )
            {
/*
                CurChain.blist.addElement(Bond.ProcessBond(ptr,ConnectAtom,Bond.NormBondFlag));
*/
            } else ptr.flag |= PDBAtom.BreakFlag;
        }
        ConnectAtom = ptr;
    } else if( IsSugarPhosphate(ptr.refno) && IsNucleo(refno) )
    {   if( ConnectAtom != null)
        {
/*
            CurChain.blist.addElement(Bond.ProcessBond(ConnectAtom,ptr,Bond.NormBondFlag));
*/
        }
        ConnectAtom = ptr;
    }
  }


  int ComplexAtomType(String ptr,int offset)
  {
     StringBuffer name = new StringBuffer();
     int i;

     if( Character.isDigit(ptr.charAt(offset + 1)) )
     {
       name.append(" ");
       name.append(Character.toUpperCase(ptr.charAt(offset)));
       name.append("  ");
     }
     else name = new StringBuffer(ptr.substring(offset,offset+4).toUpperCase());

    /* Handle Unconventional Naming */
    /*
    if( IsProtein(CurGroup->refno) )
    {   if( name[0]=='H' )
        {   name[0]=' ';
            name[1]='H';
        }
    } else if( IsNucleo(CurGroup->refno) )
    {   if( name[3]=='\'' )
            name[3] = '*';

        if( (name[1]=='O') && (name[2]=='P') )
        {   if( !strncmp(name," OP1",4) ||
                !strncmp(name,"1OP ",4) )
                return( 8 );
            if( !strncmp(name," OP2",4) ||
                !strncmp(name,"2OP ",4) )
                return( 9 );
        }
    }
    */
    return( NewAtomType(name.toString()) );
  }

  public void ProcessPDBColourMask(String Record)
  {
    int r = (int)((ReadPDBCoord(Record,30)>>2) + 5);
    int g = (int)((ReadPDBCoord(Record,38)>>2) + 5);
    int b = (int)((ReadPDBCoord(Record,46)>>2) + 5);
    MaskDesc ptr = new MaskDesc(r,g,b);
    int i;
 
    ptr.flags = 0;
    ptr.mask = Record.substring(6,27);
    for( i=6; i<11; i++ )
        if(Record.charAt(i) != '#' )
            ptr.flags |= MaskDesc.SerNoFlag;
 
    for( i=22; i<26; i++ )
        if(Record.charAt(i) != '#' )
            ptr.flags |= MaskDesc.ResNoFlag;
 
    ptr.radius = (short)((5*Integer.parseInt(Record.substring(54,60)))>>1);
    MaskDesc.addUserMask(ptr);
  }

  private void ProcessPDBGroup(String Record, boolean heta, int serno )
  {
    PDBInsert = Record.charAt(26);
    /*
    if( CurChain == null || (CurChain.ident!=Record.charAt(21)) )
        CreateChain( Record.charAt(21) );
    CreateGroup( GroupPool );
 
    CurGroup.refno = FindResNo( Record+17 );
    CurGroup.serno = serno;
    ProcessGroup( heta );
    */
  }

}
