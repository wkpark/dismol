/**
 * Element.java
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in part on the code from abstree.c
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

import java.lang.String;

public class Element
{
  public String symbol;
  public int covalrad;
  public int vdwrad;
  public int cpkcol;
  public String name;

  private Element(String s, int cr, int vr, int cc, String n)
  {
    symbol = s;
    covalrad = cr;
    vdwrad = vr;
    cpkcol = cc;
    name = n;
  }
  public static final Element getElement(int i) { return elist[i]; }

  private static final Element elist[] =  {
    new Element("  ", 170, 360, 12, ""             ),  /*   0 */
    new Element("H ",  80, 275,  4, "HYDROGEN"     ),  /*   1 */
    new Element("He", 400, 550,  5, "HELIUM"       ),  /*   2 */
    new Element("Li", 170, 305, 14, "LITHIUM"      ),  /*   3 */
    new Element("Be",  88, 157, 12, "BERYLLIUM"    ),  /*   4 */
    new Element("B ", 208, 387, 13, "BORON"        ),  /*   5 */
    new Element("C ", 180, 387,  0, "CARBON"       ),  /*   6 */
    new Element("N ", 170, 350,  1, "NITROGEN"     ),  /*   7 */
    new Element("O ", 170, 337,  2, "OXYGEN"       ),  /*   8 */
    new Element("F ", 160, 325,  6, "FLUORINE"     ),  /*   9 */
    new Element("Ne", 280, 505, 12, "NEON"         ),  /*  10 */
    new Element("Na", 243, 550,  7, "SODIUM"       ),  /*  11 */
    new Element("Mg", 275, 375, 15, "MAGNESIUM"    ),  /*  12 */
    new Element("Al", 338, 375,  9, "ALUMINIUM"    ),  /*  13 */
    new Element("Si", 300, 550,  6, "SILICON"      ),  /*  14 */
    new Element("P ", 259, 470,  8, "PHOSPHORUS"   ),  /*  15 */  /* 262? */
    new Element("S ", 255, 452,  3, "SULPHUR"      ),  /*  16 */
    new Element("Cl", 250, 437, 13, "CHLORINE"     ),  /*  17 */
    new Element("Ar", 392, 692, 12, "ARGON"        ),  /*  18 */
    new Element("K ", 332, 597, 12, "POTASSIUM"    ),  /*  19 */
    new Element("Ca", 248, 487,  9, "CALCIUM"      ),  /*  20 */
    new Element("Sc", 360, 330, 12, "SCANDIUM"     ),  /*  21 */
    new Element("Ti", 368, 487,  9, "TITANIUM"     ),  /*  22 */
    new Element("V ", 332, 265, 12, "VANADIUM"     ),  /*  23 */
    new Element("Cr", 338, 282,  9, "CHROMIUM"     ),  /*  24 */
    new Element("Mn", 338, 297,  9, "MANGANESE"    ),  /*  25 */
    new Element("Fe", 335, 487,  8, "IRON"         ),  /*  26 */
    new Element("Co", 332, 282, 12, "COBALT"       ),  /*  27 */
    new Element("Ni", 405, 310, 10, "NICKEL"       ),  /*  28 */  /* >375! */
    new Element("Cu", 380, 287, 10, "COPPER"       ),  /*  29 */
    new Element("Zn", 362, 287, 10, "ZINC"         ),  /*  30 */
    new Element("Ga", 305, 387, 12, "GALLIUM"      ),  /*  31 */
    new Element("Ge", 292, 999, 12, "GERMANIUM"    ),  /*  32 */  /* 1225? */
    new Element("As", 302, 207, 12, "ARSENIC"      ),  /*  33 */
    new Element("Se", 305, 225, 12, "SELENIUM"     ),  /*  34 */
    new Element("Br", 302, 437, 10, "BROMINE"      ),  /*  35 */
    new Element("Kr", 400, 475, 12, "KRYPTON"      ),  /*  36 */
    new Element("Rb", 368, 662, 12, "RUBIDIUM"     ),  /*  37 */
    new Element("Sr", 280, 505, 12, "STRONTIUM"    ),  /*  38 */
    new Element("Y ", 445, 402, 12, "YTTRIUM"      ),  /*  39 */
    new Element("Zr", 390, 355, 12, "ZIRCONIUM"    ),  /*  40 */
    new Element("Nb", 370, 332, 12, "NIOBIUM"      ),  /*  41 */
    new Element("Mo", 368, 437, 12, "MOLYBDENUM"   ),  /*  42 */
    new Element("Tc", 338, 450, 12, "TECHNETIUM"   ),  /*  43 */
    new Element("Ru", 350, 300, 12, "RUTHENIUM"    ),  /*  44 */
    new Element("Rh", 362, 305, 12, "RHODIUM"      ),  /*  45 */
    new Element("Pd", 375, 360, 12, "PALLADIUM"    ),  /*  46 */
    new Element("Ag", 398, 387,  9, "SILVER"       ),  /*  47 */
    new Element("Cd", 422, 437, 12, "CADMIUM"      ),  /*  48 */
    new Element("In", 408, 362, 12, "INDIUM"       ),  /*  49 */
    new Element("Sn", 365, 417, 12, "TIN"          ),  /*  50 */
    new Element("Sb", 365, 280, 12, "ANTIMONY"     ),  /*  51 */
    new Element("Te", 368, 315, 12, "TELLURIUM"    ),  /*  52 */
    new Element("I ", 350, 437, 11, "IODINE"       ),  /*  53 */
    new Element("Xe", 425, 525, 12, "XENON"        ),  /*  54 */
    new Element("Cs", 418, 752, 12, "CAESIUM"      ),  /*  55 */
    new Element("Ba", 335, 602,  8, "BARIUM"       ),  /*  56 */
    new Element("La", 468, 457, 12, "LANTHANUM"    ),  /*  57 */
    new Element("Ce", 458, 465, 12, "CERIUM"       ),  /*  58 */
    new Element("Pr", 455, 405, 12, "PRASEODYMIUM" ),  /*  59 */
    new Element("Nd", 452, 447, 12, "NEODYMIUM"    ),  /*  60 */
    new Element("Pm", 450, 440, 12, "PROMETHIUM"   ),  /*  61 */
    new Element("Sm", 450, 435, 12, "SAMARIUM"     ),  /*  62 */
    new Element("Eu", 498, 490, 12, "EUROPIUM"     ),  /*  63 */
    new Element("Gd", 448, 422, 12, "GADOLINIUM"   ),  /*  64 */
    new Element("Tb", 440, 415, 12, "TERBIUM"      ),  /*  65 */
    new Element("Dy", 438, 407, 12, "DYSPROSIUM"   ),  /*  66 */
    new Element("Ho", 435, 402, 12, "HOLMIUM"      ),  /*  67 */
    new Element("Er", 432, 397, 12, "ERBIUM"       ),  /*  68 */
    new Element("Tm", 430, 392, 12, "THULIUM"      ),  /*  69 */
    new Element("Yb", 485, 385, 12, "YTTERBIUM"    ),  /*  70 */
    new Element("Lu", 430, 382, 12, "LUTETIUM"     ),  /*  71 */
    new Element("Hf", 392, 350, 12, "HAFNIUM"      ),  /*  72 */
    new Element("Ta", 358, 305, 12, "TANTALUM"     ),  /*  73 */
    new Element("W ", 342, 315, 12, "TUNGSTEN"     ),  /*  74 */
    new Element("Re", 338, 325, 12, "RHENIUM"      ),  /*  75 */
    new Element("Os", 342, 395, 12, "OSMIUM"       ),  /*  76 */
    new Element("Ir", 330, 305, 12, "IRIDIUM"      ),  /*  77 */
    new Element("Pt", 375, 387, 12, "PLATINUM"     ),  /*  78 */
    new Element("Au", 375, 362,  6, "GOLD"         ),  /*  79 */
    new Element("Hg", 425, 495, 12, "MERCURY"      ),  /*  80 */
    new Element("Tl", 388, 427, 12, "THALLIUM"     ),  /*  81 */
    new Element("Pb", 385, 540, 12, "LEAD"         ),  /*  82 */
    new Element("Bi", 385, 432, 12, "BISMUTH"      ),  /*  83 */
    new Element("Po", 420, 302, 12, "POLONIUM"     ),  /*  84 */
    new Element("At", 302, 280, 12, "ASTATINE"     ),  /*  85 */
    new Element("Rn", 475, 575, 12, "RADON"        ),  /*  86 */
    new Element("Fr", 450, 810, 12, "FRANCIUM"     ),  /*  87 */
    new Element("Ra", 358, 642, 12, "RADIUM"       ),  /*  88 */
    new Element("Ac", 295, 530, 12, "ACTINIUM"     ),  /*  89 */
    new Element("Th", 255, 460, 12, "THORIUM"      ),  /*  90 */
    new Element("Pa", 222, 400, 12, "PROTACTINIUM" ),  /*  91 */
    new Element("U ", 242, 437, 12, "URANIUM"      ),  /*  92 */
    new Element("Np", 238, 427, 12, "NEPTUNIUM"    ),  /*  93 */
    new Element("Pu", 232, 417, 12, "PLUTONIUM"    ),  /*  94 */
    new Element("Am", 230, 415, 12, "AMERICIUM"    ),  /*  95 */
    new Element("Cm", 228, 412, 12, "CURIUM"       ),  /*  96 */
    new Element("Bk", 225, 410, 12, "BERKELIUM"    ),  /*  97 */
    new Element("Cf", 222, 407, 12, "CALIFORNIUM"  ),  /*  98 */
    new Element("Es", 220, 405, 12, "EINSTEINIUM"  ),  /*  99 */
    new Element("Fm", 218, 402, 12, "FERMIUM"      ),  /* 100 */
    new Element("Md", 215, 400, 12, "MENDELEVIUM"  ),  /* 101 */
    new Element("No", 212, 397, 12, "NOBELIUM"     ),  /* 102 */
    new Element("Lr", 210, 395, 12, "LAWRENCIUM"   )   /* 103 */ /* Lw? */
        };
}


