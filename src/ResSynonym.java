/**
 * ResSynonym.java
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * based in large part on the code from molecule.c
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

public class ResSynonym
{
  public static final String names[] = {
    "ADE",  /*   A : Adenosine   */
    "CPR",  /* PRO : Cis-proline */
    "CSH",  /* CYS : Cystine     */
    "CSM",  /* CYS : Cystine     */
    "CYH",  /* CYS : Cystine     */
    "CYT",  /*   C : Cytosine    */
    "D2O",  /* DOD : Heavy Water */
    "GUA",  /*   G : Guanosine   */
    "H2O",  /* HOH : Solvent     */
    "SOL",  /* HOH : Solvent     */
    "SUL",  /* SO4 : Sulphate    */
    "THY",  /*   T : Thymidine   */
    "TIP",  /* HOH : Water       */
    "TRY",  /* TRP : Tryptophan  */
    "URI",  /*   U : Uridine     */
    "WAT"   /* HOH : Water       */
        };
  public static final int codes[] = {
    24,  /*   A : Adenosine   */
    11,  /* PRO : Cis-proline */
    17,  /* CYS : Cystine     */
    17,  /* CYS : Cystine     */
    17,  /* CYS : Cystine     */
    25,  /*   C : Cytosine    */
    47,  /* DOD : Heavy Water */
    26,  /*   G : Guanosine   */
    46,  /* HOH : Solvent     */
    46,  /* HOH : Solvent     */
    48,  /* SO4 : Sulphate    */
    27,  /*   T : Thymidine   */
    46,  /* HOH : Water       */
    20,  /* TRP : Tryptophan  */
    28,  /*   U : Uridine     */
    46   /* HOH : Water       */
  };
  public static int RessynMax(){ return names.length; }
}
