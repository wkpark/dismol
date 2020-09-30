/**
 * pdbreader.java - reads .pdb files
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in large part on the code from infile.c
 * in RasMol2 Molecular Graphics by Roger Sayle, August 1995, Version 2.6
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

public class pdbreader
{
  public static final String rcsid =
  "$Id: pdbreader.java,v 1.5 1998/04/01 23:59:51 pcm Exp $";
  //Chain CurChain;
  PDBGroup CurGroup;
  private static final int MAXRES = 100;
  private static String Residue[] = new String[MAXRES];
  private static int ResNo = 0;

  private static String InfoMoleculeName;
  private static String InfoSpaceGroup;
  private static String InfoClassification;
  private static String InfoIdentCode;
  private static String InfoFileName;
  private static String DataFileName;
  private static double InfoCellAlpha, InfoCellBeta, InfoCellGamma;
  private static double InfoCellA, InfoCellB, InfoCellC;
  private static final double Deg2Rad = Math.PI/180.0;

  public static group read(InputStream inp)
  {
    PDBGroup grp = LoadPDBMolecule(inp,false);
    if(grp != null)
    {
      if(grp.bondList.size() < 0.5*grp.atomList.size())
///	grp.CreateMoleculeBonds(false,false); // fast
	grp.CreateMoleculeBonds(false,true); // exact
//      grp.setColors();
    }

    return grp;
  }


  private static String ExtractString(String Record, int pos, int len)
  {
    if(false)System.err.println("Record(" + Integer.toString(pos) + ","
		       + Integer.toString(len) + ": '" + Record + "'");
    if(Record.length() <= pos) return "";
    if(Record.length() < pos+len) len = Record.length() - pos;
    return Record.substring(pos,pos+len).trim();
  }


  private static PDBGroup LoadPDBMolecule(InputStream fp, boolean flag)
  {
    /*    FeatEntry *ptr = 0; */
    int srcatm, dstatm;
    String src;
    String Record;
    boolean ignore = false;
    PDBGroup grp = new PDBGroup();
    int rec_cnt = 0;
    /* NMRModel = 0; */
 
    while( (Record = GenericGroup.FetchRecord(fp)).length() > 0)
    {
        if( Record.charAt(0) == 'A' )
        {   if( !ignore && Record.startsWith("ATOM") )
                grp.ProcessPDBAtom(Record, false );

        } else switch(Record.charAt(0))
        {   case('C'):    if( Record.startsWith("CONE") )
                          {
			      if( ignore || flag) continue;
 
                              srcatm = Integer.parseInt(Record.substring(6,11).trim());
			      int i;
                              if( srcatm == 0) return (grp);
                                  for( i=11; i<=26 && i+5 <= Record.length(); i+=5 )
				    {
				      String s = Record.substring(i,i+5).trim();
				      if(s.equals(""))
					continue;
				      dstatm = Integer.parseInt(s);
				      if(false) System.err.println("dstatm " + dstatm + " srcatm " + srcatm);
                                      if( dstatm > 0 ) //&& (dstatm>srcatm) )
					grp.addBond(srcatm-1,dstatm-1);
					/* CreateBondOrder(srcatm,dstatm); */
                                  }

                                  for( i=31; i<=56 && i+5 <= Record.length(); i+=5 )
				    {
				      String s = Record.substring(i,i+5).trim();
				      if(s.equals(""))
					continue;
				      dstatm = Integer.parseInt(s);
				      if(false)System.err.println("dstatm " + dstatm + " srcatm " + srcatm);
//                                      if( i < 41 || (i > 45 && i < 56)) {
//                                        if( dstatm > 0&& srcatm < dstatm )
//                                          grp.addHydroBond(srcatm-1,dstatm-1,HydroBond);
//                                      } else
                                      if( dstatm > 0 && (dstatm>srcatm) )
					grp.addBond(srcatm-1,dstatm-1);
					/* CreateBondOrder(srcatm,dstatm); */
                                  }
                               
                          } else if( Record.startsWith("COMP") )
                          {   /* First or MOLECULE: COMPND record */
			    if(Record.length() > 11)
			    {
                              if( (Record.charAt(9)==' ') && 
                                  !Record.startsWith("MOL_ID:",10) )  
                              {   InfoMoleculeName = ExtractString(Record,10,60);
                              } else if( InfoMoleculeName == null &&
                                         Record.startsWith("MOLECULE: ",11) )
                                  InfoMoleculeName = ExtractString(Record,21,49);
			    }
                          } else if( Record.startsWith("CRYS") )
                          {
			      StringBuffer dst = new StringBuffer();
			      int i;
                              for(i = 55; i < Record.length() && i < 66; ++i)
                                  if( Record.charAt(i) != ' ' ) 
                                  {
				    dst.append(Record.charAt(i));
                                  }
			      InfoSpaceGroup = dst.toString();

                              InfoCellA = new Double(Record.substring( 6,15)).doubleValue()/1000.0;
                              InfoCellB = new Double(Record.substring(15,24)).doubleValue()/1000.0;
                              InfoCellC = new Double(Record.substring(24,33)).doubleValue()/1000.0;
 
                              InfoCellAlpha = Deg2Rad*(new Double(Record.substring(33,40)).doubleValue()/100.0);
                              InfoCellBeta =  Deg2Rad*(new Double(Record.substring(40,47)).doubleValue()/100.0);
                              InfoCellGamma = Deg2Rad*(new Double(Record.substring(47,54)).doubleValue()/100.0);
 
                          }
			  else if( Record.startsWith("COLO") )
                              grp.ProcessPDBColourMask(Record);
                          break;

            case('E'):    if( Record.startsWith("ENDM") )
                          {   /* break after single model??? */
                              if( flag)
                              {
				grp.have_connect_atom = false;
                              } else ignore = true;
 
                          } else if( Record.startsWith("END") )
                              if( Record.length() < 4 || (Record.charAt(4)==' ') )
                              {   /* Treat END same as TER! */
				grp.have_connect_atom = false;
                              }
                          break;

            case('H'):    if( Record.startsWith("HETA") )
                          {   if( !ignore ) grp.ProcessPDBAtom(Record,true);
                          } else if( Record.startsWith("HELI") )
                          {   if( ignore ) continue;
 
                              /* Remaining HELIX record fields   */
                              /* 38-39 .... Helix Classification */
                              /* 31 ....... Same Chain as 19?    */
			  /*
                              ptr = AllocFeature();
                              ptr->type = FeatHelix;
                              ptr->chain = Record[19];
                              ptr->init = (int)ReadValue(21,4);
                              ptr->term = (int)ReadValue(33,4);
			  */
                          } else if( Record.startsWith("HEAD") )
                          {   InfoClassification = ExtractString(Record,10,40);
                              InfoIdentCode = ExtractString(Record,62, 4);
                          }
                          break;

            case('M'):    if( Record.startsWith("MODE") )
                              if( flag) /* NMRModel++ */;
                          break;
 
            case('S'):    if( Record.startsWith("SHEE") )
                          {   if( ignore ) break;
                              /* Remaining SHEET record fields   */
                              /* 38-39 .... Strand Parallelism   */
                              /* 32 ....... Same Chain as 21?    */
			  /*
                              ptr = AllocFeature();
                              ptr->type = FeatSheet;
                              ptr->chain = Record[21];
                              ptr->init = (int)ReadValue(22,4);
                              ptr->term = (int)ReadValue(33,4);
			  */
                          }
                          break;

            case('T'):    if( Record.startsWith("TURN") )
                          {   if( ignore ) continue;
			  /*
                              ptr = AllocFeature();
                              ptr->type = FeatTurn;
                              ptr->chain = Record[19];
                              ptr->init = (int)ReadValue(20,4);
                              ptr->term = (int)ReadValue(31,4);
			  */
                          } else if( Record.startsWith("TER") )
                          {   if( Record.length() < 3 || (Record.charAt(3)==' ') )
                              {
				grp.have_connect_atom = false;
                              }
                          }
                          break;
        }
        if((++rec_cnt % 1000) == 0)
	{
	  System.gc();
	}
    }
 
    if( grp != null)
        InfoFileName = DataFileName;
    /*
    if( FeatList ) ProcessFeatures();
    */

//    grp.InitialTransform();
    return( grp );
  }

/*==============================*/
/* Molecule File Format Parsing */
/*==============================*/
  private static int FindResNo( String ptr )
  {
    int hi,lo;
    int refno;
    int flag;
    int mid;

    for( refno=0; refno<ResNo; refno++ )
	if( Residue[refno].startsWith(ptr) )
	    return( refno );

    lo = 0;
    hi = ResSynonym.RessynMax();
    while( lo < hi )
    {   mid = (hi+lo)>>1;
        flag = ResSynonym.names[mid].compareTo(ptr);
        if(flag == 0) return( ResSynonym.codes[mid] );

        /* Binary Search */
        if( flag<0 )
        {   lo = mid+1;
        } else hi = mid;
    }

    if( ResNo++ == MAXRES )
      return -1;
    /*	FatalDataError("Too many new residues"); */
    Residue[refno] = ptr;
    return( refno );
  }
}
