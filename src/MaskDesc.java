/**
 * maskdesc.java
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

import java.awt.Color;

public class MaskDesc extends Color
{
	public static final String rcsid =
	"$Id: MaskDesc.java,v 1.1.1.1 1998/02/10 19:03:53 pcm Exp $";
	public static final int SerNoFlag = 0x01;
	public static final int ResNoFlag = 0x02;

        public short radius;
        public String mask;
        public byte  flags;

	public static int MAXMASK(){ return 40; }

	public MaskDesc(int r,int g, int b)
	{
	  super(r,g,b);
	}
	private static MaskDesc UserMask[] = new MaskDesc[MAXMASK()];
	private static int MaskCount = 0;

	public static void addUserMask(MaskDesc ptr)
	{
	  UserMask[MaskCount] = ptr;
	  MaskCount++;
	}
}
