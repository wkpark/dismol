
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

public class XYZGroup extends GenericGroup
{

  public int SimpleAtomType(String type)
  {
    StringBuffer name = new StringBuffer();

    if( type.length() > 1 && (type.charAt(1) != ' ') )
    {
      name.append(Character.toUpperCase(type.charAt(0)));
      name.append(Character.toUpperCase(type.charAt(1)));
    }
    else
    {
      name.append(' ');
      name.append(Character.toUpperCase(type.charAt(0)));
    }
    name.append("  ");
    return( NewAtomType(name.toString()) );
  }
}
