/**
 * RasFont.java
 * Copyright (c) 2003 Won-Kyu Park, all rights reserved.
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from font.h, pixutils.c in RasMol v2.7.x
 * RasMol Molecular Graphics by Roger Sayle, August 1995, Version 2.6
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

public class RasFont
{
  private static String VectFont[] = {
    /*  32 ' ' */ "",
    /*  33 '!' */ "ChdhdicichCkdkewbwck",
    /*  34 '"' */ "CvbvbwcwcubtFvevewfwfuet",
    /*  35 '#' */ "AlmlAqmqDidtJijt",
    /*  36 '$' */ "AjcihijjkljnhodobparbtduiuktFgfw",
    /*  37 '%' */ "AgmwIhlhmimllmimhlhiihCqfqgrgufvcvbubrcq",
    /*  38 '&' */ "Kgihbsbtcvewfwgugsfqboamakbichegfghhkk",
    /*  39 ''' */ "Evdvdweweucs",
    /*  40 '(' */ "Egchbiakasbucvew",
    /*  41 ')' */ "Agchdiekesducvaw",
    /*  42 '*' */ "AplpCkjuCujk",
    /*  43 '+' */ "CokoGkgs",
    /*  44 ',' */ "Dgcgchdhdgdfce",
    /*  45 '-' */ "Coko",
    /*  46 '.' */ "Ggfgfhghgg",
    /*  47 '/' */ "Agmw",
    /*  48 '0' */ "EwcvbtbjchegigkhljltkviwewChkv",
    /*  49 '1' */ "DtgwggDgjg",
    /*  50 '2' */ "Kgagaibkhojpkrktjvhwdwbvat",
    /*  51 '3' */ "AtbvdwhwjvktkrjphoeoHojnklkjjhhgdgbhaj",
    /*  52 '4' */ "Klalhwhg",
    /*  53 '5' */ "Kwawaocphpjokmkjjhhgcgah",
    /*  54 '6' */ "Jvhwdwbvatajbhdghgjhkjkmjohpdpboan",
    /*  55 '7' */ "Awkwdg",
    /*  56 '8' */ "DwbvatarbpdohojpkrktjvhwdwDobnalajbhdghgjhkjkljnho",
    /*  57 '9' */ "Bhdghgjhkjktjvhwdwbvataqbodnhnjokq",
    /*  58 ':' */ "GififjgjgiGpfpfqgqgp",
    /*  59 ';' */ "DicicjdjdidhcgCpdpdqcqcp",
    /*  60 '<' */ "Lgbolw",
    /*  61 '=' */ "AsisAnjn",
    /*  62 '>' */ "Bglobw",
    /*  63 '?' */ "ArasbucvewgwivjuktkrjphognflfjFhfgggghfh",
    /*  64 '@' */ "Knjlhkflenepfrhsjrkqkkljmjnkomosnumvkwewcvbuasakbichegkg",
    /*  65 'A' */ "AgcmhwmmogCmmm",
    /*  66 'B' */ "AgigkhlimklmkniokplqmslukviwawBwbgboio",
    /*  67 'C' */ "Milhjgegchbiakasbucvewjwlvmu",
    /*  68 'D' */ "AgigkhlimkmslukviwawBwbg",
    /*  69 'E' */ "KwawagkgAoho",
    /*  70 'F' */ "KwawagAoho",
    /*  71 'G' */ "MilhjgegchbiakasbucvewjwlvmuMgmmHmom",
    /*  72 'H' */ "AgawKgkwAoko",
    /*  73 'I' */ "AgggAwgwDgdw",
    /*  74 'J' */ "AmakbichegghhiikiwDwnw",
    /*  75 'K' */ "AgawAmkwCokg",
    /*  76 'L' */ "Kgagaw",
    /*  77 'M' */ "Agawhmowog",
    /*  78 'N' */ "Agawmgmw",
    /*  79 'O' */ "Akbichegigkhlimkmslukviwewcvbuasak",
    /*  80 'P' */ "Agawiwkvlumsmrlpkoinan",
    /*  81 'Q' */ "AkbichegigkhlimkmslukviwewcvbuasakHlmg",
    /*  82 'R' */ "AgawiwkvlumsmrlpkoinanInmg",
    /*  83 'S' */ "Ajchegigkhlimklmkncpbqasbucvewiwkvmt",
    /*  84 'T' */ "HghwAwow",
    /*  85 'U' */ "Awakbichegigkhlimkmw",
    /*  86 'V' */ "Awggmw",
    /*  87 'W' */ "Aweghokgow",
    /*  88 'X' */ "AwkgAgkw",
    /*  89 'Y' */ "AwfokwFgfo",
    /*  90 'Z' */ "Awkwagkg",
    /*  91 '[' */ "Igdgdwiw",
    /*  92 '\' */ "Awmg",
    /*  93 ']' */ "Dgigiwdw",
    /*  94 '^' */ "Cqgwkq",
    /*  95 '_' */ "Bglg",
    /*  96 '`' */ "Cvdvdwcwcues",
    /*  97 'a' */ "BqdrirjqkokhlgKhigdgbhajbldmimkk",
    /*  98 'b' */ "BichegggihjikkkmjoipgqeqcpboAwbwbgag",
    /*  99 'c' */ "Jqhrercqbpanakbicheghgjh",
    /* 100 'd' */ "KiggegchbiakanbpcqerhrjqkoLgkgkwlw",
    /* 101 'e' */ "Kiihggegchbiakanbpcqerhrjqkokmam",
    /* 102 'f' */ "BgdgCgcrdtfuhtirAnfn",
    /* 103 'g' */ "KiihggegchbiakambodphpjokmLpkpkdjbhaeacbbc",
    /* 104 'h' */ "AwbwbgagBncpeqhqjpknkg",
    /* 105 'i' */ "AgegCgcpbpCrcsbsbrcr",
    /* 106 'j' */ "GshshrgrgsGphphegcebdbbcae",
    /* 107 'k' */ "AgbgbwawBjhpIgem",
    /* 108 'l' */ "AgegCgcubu",
    /* 109 'm' */ "AqbqbgBncpeqfqhpiniginjplqmqoppnpg",
    /* 110 'n' */ "AqbqbgagBncpeqhqjpknkg",
    /* 111 'o' */ "Akbichegggihjikkkmjoipgqeqcpboamak",
    /* 112 'p' */ "BichegggihjikkkmjoipgqeqcpboAqbqbaaa",
    /* 113 'q' */ "KiggegchbiakanbpcqerhrjqkoLpkpkdlbna",
    /* 114 'r' */ "AqbqbgBncpeqhqjo",
    /* 115 's' */ "Ahcgggihjjikbmanbpdqhqjp",
    /* 116 't' */ "AqiqEuehfghgih",
    /* 117 'u' */ "AqakbichegggihjikkKqkglg",
    /* 118 'v' */ "Arfgkr",
    /* 119 'w' */ "Areghmkgor",
    /* 120 'x' */ "ArkgAgkr",
    /* 121 'y' */ "AqakbichegggihjikkKqkdjbhaeacbbc",
    /* 122 'z' */ "Arkragkg",
    /* 123 '{' */ "Egchbjblcnaocpbrbtcvew",
    /* 124 '|' */ "Gggw",
    /* 125 '}' */ "Agchdjdlcneocpdrdtcvaw",
    /* 126 '~' */ "Arbtcueuftfsgrirjsku",
    /* 127 'Angstroms' */ "AgdkhqlkogDklkHwjuhrfuhw",
    /* 128 'Degrees' */ "Aqbocnemgminjokqksjuivgwewcvbuasaq"
  };

  private static int SplineCount=5;
  private static int FontSize=8;
  private static int FontStroke=0;
  private static boolean FontPS=false;
  private static short FontDimen[] = new short[23];
  private static int FontWid[] = new int[97];

  private static int InvertY(short y)
  {
    return (int)-y;
  }

  public static void InitialiseFont ()
  {
    SplineCount = 5;
    SetFontSize (8);
    SetFontStroke (0);
  }

  public static void SetFontSize (int size)
  {
    int count;
    int i,j;

//    if (LabelList || (MonitList && DrawMonitDistance))
//      ReDrawFlag |= RFRefresh;

    FontSize = size > 0 ? size:-size;
    FontPS = false;
    if (size < 0)
      FontPS = true;
    count = 0;
    for (i = 0; i < 23; i++)
      {
	FontDimen[i] = (short)(count >> 4);
	count += FontSize;
      }

    for (i = 0; i < 97; i++)
      {
	if (FontPS)
	  {
            String font = VectFont[i];
            int last = font.length();
	    FontWid[i] = 0;
            j=0;
	    while (j < last)
	      {
                int f=font.charAt(j);
		if (f < 'a')
		  {
		    if (FontDimen[f - 'A'] > FontWid[i])
		      FontWid[i] = FontDimen[f - 'A'];
		  }
		else
		  {
		    if (FontDimen[f - 'a'] > FontWid[i])
		      FontWid[i] = FontDimen[f - 'a'];
		  }
		j += 2;
	      }
	    FontWid[i] += FontSize / 4 + 1;
	  }
	else
	  {
	    FontWid[i] = FontSize;
	  }
      }
  }

  public static void SetFontStroke (int width)
  {
    FontStroke = width;
  }

  public static void ClipCharacter (int x, int y, int z, int glyph, int col)
  {
    int sx, sy;
    int ex=0, ey=0;
    int j;

    if (true) {
       //System.out.println("glyph :" + (char)(glyph+32));
       if (glyph < 0 && glyph >=97) return;
       return;
    }

    //char font = VectFont[glyph].toCharArray();
    String font = VectFont[glyph];
    int last = font.length();
    j=0;

    while (j < last)
      {				/* Uppercase test */
        char f=font.charAt(j);
        char f1=font.charAt(j+1);
	if (f < 'a')
	  {
	    sx = x + FontDimen[f - 'A'];
	    sy = y + InvertY (FontDimen[f1 - 'a']);
	    j += 2;
	  }
	else
	  {
	    sx = ex;
	    sy = ey;
	  }

	ex = x + FontDimen[f - 'a'];
	ey = y + InvertY (FontDimen[f1 - 'a']);
	if (FontStroke < 1)
	  {
	    if ((ex != sx) || (ey != sy))
	      {
		//RasBuffer.ClipLine (sx, sy, z, ex, ey, z, col, ' ');
		RasBuffer.ClipTwinLine (sx, sy, z, ex, ey, z, col, col);
	      }
	    else
	      RasBuffer.ClipPoint (ex, ey, z, col);
	  }
	else
	  {
	    if ((ex != sx) || (ey != sy))
	      {
//		RasBuffer.ClipCylinder (sx, sy, z, ex, ey, z, col, col, FontStroke, ' ',
//			      FontStroke);
		RasBuffer.DrawCylinder (sx, sy, z, ex, ey, z, col, col, FontStroke);
	      }			/* else ClipSphere(ex,ey,z,FontStroke,col); */
	  }
	j += 2;
      }
  }


  public static void DisplayRasString (int x, int y, int z, String label, int col)
  {
    boolean clip;
    int high, max;
    int sx, sy;
    int ex=0, ey=0;

    high = (FontSize * 3) >> 1;
//#ifdef INVERT
    if (((y + high) < 0) || (y >= RasBuffer.view.ymax))
      return;
    clip = (y < 0) || (y + high >= RasBuffer.view.ymax);
//#else
//    if( (y<0) || ((y-high)>=view.ymax) ) return;
//    clip = (y-high<0) || (y>=view.ymax);
//#endif

    int j=0;
    int last=label.length();
    if (x < 0)
      {
	while (j < last && (x <= -FontSize))
	  {
	    x += FontWid[(label.charAt(j) - 32)] + FontStroke;
	    j++;
	  }

	if (j < last)
	  {
	    ClipCharacter (x, y, z, (label.charAt(j) - 32), col);
	    x += FontWid[(label.charAt(j) - 32)] + FontStroke;
	    j++;
	  }
	else
	  return;
      }

    if (!clip)
      {
	max = RasBuffer.view.xmax - FontSize;
	while (j < last && (x < max))
	  {
	    String font = VectFont[label.charAt(j) - 32];
            int flast = font.length();
            int ii=0;
	    while (ii < flast)
	      {			/* Uppercase test */
		if (font.charAt(ii) < 'a')
		  {
		    sx = x + FontDimen[font.charAt(ii) - 'A'];
		    sy = y + InvertY (FontDimen[font.charAt(ii+1) - 'a']);
		    ii += 2;
		  }
		else
		  {
		    sx = ex;
		    sy = ey;
		  }

		ex = x + FontDimen[font.charAt(ii) - 'a'];
		ey = y + InvertY (FontDimen[font.charAt(ii+1) - 'a']);
		if (FontStroke < 1)
		  {
		    if ((ex != sx) || (ey != sy))
		      {
			//RasBuffer.DrawTwinLine (sx, sy, z, ex, ey, z, col, col, ' ');
			//RasBuffer.DrawTwinLine (sx, sy, z, ex, ey, z, col, col, 1);
			RasBuffer.ClipTwinLine (sx, sy, z, ex, ey, z, col, col);
		      }
		    else
		      RasBuffer.ClipPoint (ex, ey, z, col);
		  }
		else
		  {
		    if ((ex != sx) || (ey != sy))
		      {
//			RasBuffer.DrawCylinder (sx, sy, z, ex, ey, z, col, col,
//				      FontStroke, ' ', FontStroke);
			RasBuffer.DrawCylinder (sx, sy, z, ex, ey, z, col, col, FontStroke);
		      }		/* else DrawSphere(ex,ey,z,FontStroke,col); */
		  }
		ii += 2;
	      }

	    x += FontWid[(label.charAt(j) - 32)] + FontStroke;
	    j++;
	  }

	if (j < last)
	  ClipCharacter (x, y, z, (label.charAt(j) - 32), col);
      }
    else			/* Always Clip! */
      while (j < last && (x < RasBuffer.view.xmax))
	{
	  ClipCharacter (x, y, z, (label.charAt(j) - 32), col);
	  x += FontWid[(label.charAt(j) - 32)] + FontStroke;
	  j++;
	}
  }
}
