/**
 * xyzreader.java - reads .xyz files
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from infile.c
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
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.InputStream;
import java.io.IOException;
import atom;
import PDBAtom;
import term;
import XYZGroup;

public class xyzreader
{
  public static final String rcsid =
  "$Id: xyzreader.java,v 1.4 1998/04/01 19:47:03 pcm Exp $";
  private static final int MAXRES = 100;
  private static String Residue[] = new String[MAXRES];
  private static int ResNo = 0;

  private static String InfoMoleculeName;
  private static String InfoSpaceGroup;
  private static String InfoClassification;
  private static String InfoIdentCode;
  private static String InfoFileName;
  private static String DataFileName;

  public static group read(InputStream inp)
  {
    XYZGroup grp = LoadXYZMolecule(inp);
    if(grp != null)
    {
      grp.CreateMoleculeBonds(false,false);
      grp.setColors();
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

  private static XYZGroup LoadXYZMolecule(InputStream fp)
  {
    String Record;
    XYZGroup grp = new XYZGroup();

    Record = GenericGroup.FetchRecord(fp);
    int atoms = Integer.parseInt(Record.trim());

    /* Molecule (step) Description */
    Record = GenericGroup.FetchRecord(fp);
    int i;
    for(i = 0; i < atoms; i++)
    {
	Record = GenericGroup.FetchRecord(fp);
	StringTokenizer st = new StringTokenizer(Record);
	PDBAtom ptr = new PDBAtom();
	ptr.serno = i;
 
	int count = st.countTokens();
	if(count < 4)
	{
	  System.err.println("xyzreader.java::LoadXYZMolecule bad record " + Record);
	  return null;	// ??
	}
	String type = st.nextToken();
	double xpos = Double.valueOf(st.nextToken()).doubleValue();
	double ypos = Double.valueOf(st.nextToken()).doubleValue();
	double zpos = Double.valueOf(st.nextToken()).doubleValue();
 
	ptr.refno = grp.SimpleAtomType(type);
	ptr.xorg =  (int)(250.0*xpos);
	ptr.yorg =  (int)(250.0*ypos);
	ptr.zorg = -(int)(250.0*zpos);
	ptr.x[0] = xpos/4.0;
	ptr.x[1] = ypos/4.0;
	ptr.x[2] = zpos/4.0;
 
	if( (count==5) || (count==8) )
        {
	  double charge = Double.valueOf(st.nextToken()).doubleValue();
	  ptr.temp = (short)(100.0*charge);
	}
	else ptr.temp = 0;
	grp.ProcessAtom( ptr );
	grp.addAtom(ptr);
    }
    return grp;
  }
}
