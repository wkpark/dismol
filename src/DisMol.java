/**
 * DisMol.java
 * Copyright (c) 1998 Peter McCluskey, all rights reserved.
 * Based on code from Will Ware's NanoCAD and Roger Sayle's RasMol.
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
 * any author be liable for any direct, indirect, incidental, special,
 * exemplary, or consequential damages (including, but not limited to,
 * procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of
 * liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage.
 */

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.AWTEvent;
import java.lang.Math;
import java.util.StringTokenizer;
import java.io.*;
import java.net.Socket;
import java.util.Locale;
import java.util.Vector;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.awt.datatransfer.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.ImageProducer;

//import netscape.security.PrivilegeManager;

import atom;
import view;
import group;
import pdbreader;
import xyzreader;
import RasBuffer;
import RasFont; // added by wkpark@kldp.org 2003
import RasCalc; // added by wkpark@kldp.org 2003

public class DisMol extends Applet
  implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
  public static final String rcsid =
  "$Id: DisMol.java,v 1.14 2000/04/15 23:32:12 pcm Exp $";
  private String version="1.0";
  private String banner=null;
  private Button getMoleculeFile;
  private Button resizeAtoms;
  private Choice whichElement;
  private group grp;
  //private Panel drawingArea;
  private Choice fileList;
  private TextArea inputWindow2;
  private int xxx, yyy;
  private atom atom1;
  private double atom1z;
  private boolean inDrawingArea, needToRepaint = true;
  private Label atomInfoBlab;
  private Panel controls;
//  private TextArea instrucs;
  private boolean resized_axes = false;
//  private Checkbox showAxes;
  private boolean axes_flag = false;
  private String fileUrl="";
  private String postUrl="";
  private String sizeOfAtoms;

  private String molString="";
  private int mol_type=0; // 0=pdb,1=xyz
  private int mol_number=-1; //
  private int mol_index=0;
  private int info_precision=2;

  private Color bgcolor=Color.black;
  private Color fontColor=Color.lightGray;

  static boolean standalone=false;

  static String[] drawModeNames = {
    "WIREFRAME", "STICKS", "BALLNSTICK", "SPACEFILL"
  };

  private String helpMessage =
    "Rotation: <Shift> + MouseDrag on air\n" +
    "Zoom: <Ctrl> + Vertical MouseDrag on air\n" +
    "Perspective: <Ctrl> + Horizontal MouseDrag on air\n" +
    "----\n\n" +
    "Move a atom: Drag a atom\n" +
    "add a bond: <Shift> + Drag one atom to another atom\n" +
    "del a bond: <Ctrl> + Drag on the atom to it's bond\n" +
    "----\n\n" +
    "<A> select atom to add\n" +
    "<D> change style  <S> Labels on/off\n" +
    "<D> change style <X> Axes on/off\n" +
    "<I> Initialize view positions\n" +
    "<f> Increase pick info precisions\n" +
    "<P> Post or save a png image\n" +
    "<Q> Query on/off <R> Rotation on/off\n";

  public static final int WIREFRAME = 0;
  public static final int STICKS = 1;
  public static final int BALLSTK = 2;
  public static final int SPACEFILL = 3;

  private int     display_mode=WIREFRAME;
  private boolean display_label=false;
  private boolean spin_mode=false;
  private boolean movie_mode=false;
  private boolean force_mode=false;
  private boolean PickMode=false;

  private int     atom_selected=0;
  static String[] atomNames = {
    " C", " H", " N", " O", " S", " F", " P"
  };

  private static final int PickCoord = 0;
  private static final int PickDist  = 1;
  private static final int PickAngle = 2;
  private static final int PickTorsn = 3;

  private int PickCount = 0;
  private int PickAtom[] = {0,0,0,0};
  private boolean PickTest = false;

  private long    t1=0;
  private long    tMouseDown=0;
  private long    tDoubleClick=300;
  private int     lastx=0,lasty=0;
  private int     diffX=0,diffY=0,diffZ=0;

  public DisMol()
  {
    RasBuffer.InitialiseTransform();
    RasFont.InitialiseFont();
  }

  public void paint (Graphics g)
  {
    if(grp == null) return;
    grp.setPanelSize(getSize());
    grp.updateViewSize ();

/*    Graphics2D g2d = (Graphics2D)g;

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    if(spin_mode) {
      long tnow = System.currentTimeMillis();
//      System.err.println("paint time " + (tnow - t1));
      if ((tnow - t1) > 10) {
         grp.v.rotate (0.06, 0.0);
         t1= System.currentTimeMillis();
         needToRepaint=true;
      }
    }
*/
    if (display_mode == STICKS)
      grp.stickPaint(this);
    else if (display_mode == WIREFRAME)
      grp.wireframePaint(this);
    else if (display_mode == SPACEFILL)
      grp.fullPaint(this);
    else if (display_mode == BALLSTK)
      grp.fullPaint(this);

    if (display_label)
      grp.DisplayLabels(this);

    if (PickCount > 1 || PickTest)
      PickAtoms();

    if(axes_flag) grp.drawAxes(g, dragFlag);
    if (banner != null)
      grp.banner(banner, fontColor, this);

    grp.paint(g,null);
    //repaint();
    // if(this.getCursor() != Cursor.getDefaultCursor())
    // this.setCursor(Cursor.getDefaultCursor());
    //System.err.println("paint");
    if (!spin_mode && !movie_mode) setPainted();
  }

  public void update(Graphics g) {
    int degX=(int)(0.7*diffX);
    int degY=(int)(0.7*diffY);
    int degZ=(int)(0.7*diffZ);
    //System.err.println("-- update");
    if (degX != 0 || degY != 0) {
      double factor=0.025;
      //double factor=0.0174532925;
      grp.v.rotate (degX*factor,degY*factor);
      needToRepaint=true;
      if (!spin_mode) {
        diffX=0;diffY=0;diffZ=0;
        // needToRepaint=false;
      }

      double rot[] = grp.v.getRotateInfo();
      atomInfoBlab.setText ("Rotate: " + (int)(rot[0]*180.)
        + "," + (int)(rot[1]*180.) + "," + (int)(rot[2]*180.));
    } else if (degZ != 0) {
      double zfactor=0.015;
      grp.v.rotatez(degZ*zfactor);
      needToRepaint=true;
      if (!spin_mode) {
        diffZ=0;
        // needToRepaint=false;
      }
    }
    if (movie_mode && mol_type==1) {
      long tnow = System.currentTimeMillis();
      //System.out.println("movie time : " + (tnow - t1));
      if ((tnow - t1) > 100) {
        t1= tnow;
        mol_index++;
        if (mol_index >= mol_number) mol_index=0;
        showNthMolecule(mol_index);
        needToRepaint=true;
      }
    }
    if (needToRepaint)
      paint(g);
    if (!movie_mode && degX == 0 && degY == 0) setPainted();
  }

  private synchronized void setPainted() {
    needToRepaint = false;
    //notifyAll();
  }

  private int x0, y0;  // xxx,yyy tracks the mouse, x0,y0 stands still
  private boolean dragFlag = false;
  private int mouseModifiers;
  public void atomInfo ()
  {
    atomInfoBlab.setBackground (this.getBackground ());
    atomInfoBlab.setText ("");
  }

  public void atomInfo (String s)
  {
    atomInfoBlab.setBackground (this.getBackground ());
    atomInfoBlab.setText (s);
  }

  public void atomInfo (atom a)
  {
    if (a == null)
      {
	atomInfoBlab.setBackground (this.getBackground ());
	atomInfoBlab.setText ("");
	return;
      }
    String hinfo = "", atom_idno = "";
    PDBAtom p = (PDBAtom)a;
    if(p != null)
    {
      atom_idno = "Atom: #" + p.serno + " ";
    }
    switch (a.hybridization)
      {
      case atom.SP3: hinfo = "sp3"; break;
      case atom.SP2: hinfo = "sp2"; break;
      case atom.SP:  hinfo = "sp"; break;
      }
    atomInfoBlab.setBackground (this.getBackground ());
    atomInfoBlab.setText (atom_idno + " " +
		  a.symbol() + " " + hinfo + " " + toString(a.x[0],4)
		  + "," + toString(a.x[1],4) + "," + toString(a.x[2],4));
  }

  /* imported from http://w3.one.net/~monkey/java/rounding/ */
  private static String toString (double d, int place)
  {
    if (place <= 0)
      return ""+(int)(d+((d > 0)? 0.5 : -0.5));
    String s = "";
    if (d < 0) {
      s += "-";
      d = -d;
    }
    d += 0.5*Math.pow(10,-place);
    if (d > 1) {
      int i = (int)d;
      s += i;
      d -= i;
    } else
      s += "0";
    if (d > 0) {
      d += 1.0;
      String f = ""+(int)(d*Math.pow(10,place));
      s += "."+f.substring(1);
    }
    return s;
  }

  private void PickAtoms()
  { // new method to pick a information
    double temp=0.0;
    char unit[]={128}; /* Degree symbol */
    atom s = (atom) grp.atomList.elementAt(PickAtom[0]);
    atom e = null;
    int pCount = PickCount;
    
    if (PickTest) pCount++;
    if (pCount < 2) return;

    if (pCount == 2) {
      temp = RasCalc.CalcDistance((atom)grp.atomList.elementAt(PickAtom[0]),
                                  (atom)grp.atomList.elementAt(PickAtom[1]));
      e = (atom) grp.atomList.elementAt(PickAtom[1]);
      unit[0] = 127; /* Angstrom symbol */
    } else if (pCount == 3) {
      temp = RasCalc.CalcAngle((atom)grp.atomList.elementAt(PickAtom[0]),
                               (atom)grp.atomList.elementAt(PickAtom[1]),
                               (atom)grp.atomList.elementAt(PickAtom[2]));
      e = (atom) grp.atomList.elementAt(PickAtom[2]);
    } else if (pCount == 4) {
      temp = RasCalc.CalcTorsion((atom)grp.atomList.elementAt(PickAtom[0]),
                                 (atom)grp.atomList.elementAt(PickAtom[1]),
                                 (atom)grp.atomList.elementAt(PickAtom[2]),
                                 (atom)grp.atomList.elementAt(PickAtom[3]));
      e = (atom) grp.atomList.elementAt(PickAtom[3]);
    }

    String u = new String(unit);
    double ss[]=grp.v.xyzToScreen(s.x);
    double se[]=grp.v.xyzToScreen(e.x);
    double mx=(ss[0] + se[0])*0.5,my=(ss[1] + se[1])*0.5;
    RasFont.DisplayRasString((int)mx + 4,
                 (int)my,
                 (int)1000,
                 toString(temp,info_precision)+u,s.getColorIndex()+20);

    RasBuffer.ClipDashLine((int)ss[0],(int)ss[1],(int)ss[2],
                 (int)se[0],(int)se[1],(int)se[2],
                 s.getColorIndex(),e.getColorIndex());

    needToRepaint=true;
  }

  public String getPickInfo()
  { // new method to pick a information
    double temp=0.0;
    double dist=0.0,ang=0.0,tang=0.0;
    String info = "";
    
    if (PickCount < 1) return "";

    PDBAtom a = (PDBAtom) grp.atomList.elementAt(PickAtom[0]);
    String name = a.symbol();

    info = name;
    
    if (PickCount >= 2) {
      dist = RasCalc.CalcDistance((atom)grp.atomList.elementAt(PickAtom[0]),
                                  (atom)grp.atomList.elementAt(PickAtom[1]));
      info = name + " " + (PickAtom[1]+1) + " " + toString(dist,4);
    }
    if (PickCount >= 3) {
      ang = RasCalc.CalcAngle((atom)grp.atomList.elementAt(PickAtom[0]),
                              (atom)grp.atomList.elementAt(PickAtom[1]),
                              (atom)grp.atomList.elementAt(PickAtom[2]));
      info += " " + (PickAtom[2]+1) + " " + toString(ang,4);
    }
    if (PickCount >= 4) {
      tang = RasCalc.CalcTorsion((atom)grp.atomList.elementAt(PickAtom[0]),
                                 (atom)grp.atomList.elementAt(PickAtom[1]),
                                 (atom)grp.atomList.elementAt(PickAtom[2]),
                                 (atom)grp.atomList.elementAt(PickAtom[3]));
      info += " " + (PickAtom[3]+1)+ " " + toString(tang,4);
    }
    return info;
  }

  public void mouseClicked(MouseEvent e) { }
  public void mousePressed (MouseEvent e)
  {
    int x = e.getX();
    int y = e.getY();
    Rectangle r = this.getBounds();
    inDrawingArea = y < r.height;
    needToRepaint = false;
    double[] scrPos = { x, y, 0 };
    boolean dclick;

    if (lastx == x && lasty == y &&
      ((e.getWhen()-tMouseDown) < tDoubleClick)) {
      dclick=true;
    } else {
      dclick=false;
      tMouseDown=e.getWhen();
      lastx=x;
      lasty=y;
    }

    dragFlag = false;

    atom1 = grp.selectedAtom (scrPos, true);

    // We only care about the SHIFT and CTRL modifiers, mask out all others
    mouseModifiers = e.getModifiers() & (Event.SHIFT_MASK | Event.CTRL_MASK);
    if (atom1 != null)
      {
	double[] atomScrPos = grp.v.xyzToScreen (atom1.x);
	atom1z = atomScrPos[2];
      }
    else
      {
	atom1z = 0;
      }
    atomInfo (atom1);

    if (PickMode) {
      int tmp;
      PickTest=false;
      tmp = grp.selectedAtomId (scrPos);
      if (tmp == -1 && dclick) {
        PickCount=0;
        needToRepaint=true;
        repaint();
      } else {
        if (tmp != -1) {
          if (PickCount < 4) PickCount++;
          else PickCount=1;
          if (PickCount >0) PickAtom[PickCount-1]=tmp;
//        System.out.println("PickAtom : " + tmp);
        }
      }
      needToRepaint=true;
    }

    xxx = x; yyy = y;
    x0 = x; y0 = y;
    e.consume();
  }

  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
    if (PickMode && ! dragFlag) {
      PickCount=0;
      PickTest=false;
      needToRepaint=true;
      repaint();
    }
  }
  public void mouseMoved(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    double[] scrPos = { x, y, 0 };
    if (PickMode) {
      int tmp;
      tmp = grp.selectedAtomId (scrPos);
      if (tmp != -1 && PickCount < 4) {
        if (PickCount >0 && PickAtom[PickCount-1] != tmp) {
          PickAtom[PickCount]=tmp;
          PickTest=true;
          // System.out.println("PickAtom : " + tmp);
          needToRepaint=true;
          repaint();
        }
      } else if (PickTest) {
        PickTest=false;
        needToRepaint=true;
        repaint();
      }
    }
  }

  public void mouseDragged (MouseEvent e)
  {
    boolean movingAtom = false;  // if moving atom, no need for extra line
    int x = e.getX();
    int y = e.getY();
    if (!dragFlag)
      if (x < x0 - 2 || x > x0 + 2 || y < y0 - 2 || y > y0 + 2)
	dragFlag = true;
    if (dragFlag)
      {
	needToRepaint = true;
	if (atom1 == null)
	  {
	    switch (mouseModifiers)
	      {
	      default:
		//grp.v.rotate (0.01 * (x - xxx), 0.01 * (y - yyy));
                diffX=x - xxx; diffY=y - yyy;
                diffZ=0;
		break;
	      case Event.CTRL_MASK:
		// grp.forceMultiplier *= Math.exp (0.01 * (x - xxx));
                diffX=0; diffY=0; diffZ=0;//

		grp.v.pan (x - xxx, y - yyy);
		break;
	      case Event.SHIFT_MASK:
                diffX=0; diffY=0;

		grp.v.zoomFactor *= Math.exp (0.01 * (y - yyy));
		//grp.v.perspDist *= Math.exp (0.01 * (x - xxx));
                diffZ=x - xxx;
                if (y > getSize().height/2) diffZ*=-1;
                // System.err.println("zoomFactor: " + grp.v.zoomFactor);
                atomInfo ("zoom: " + (int)(grp.v.zoomFactor));

		if(grp.v.zoomFactor > 80)
		  grp.v.zoomFactor = 80;
		else if(grp.v.zoomFactor < 3.0)
		  grp.v.zoomFactor = 3.0;
		//if(grp.v.perspDist < 400)
		//  grp.v.perspDist = 400;
		resized_axes = true;
		break;
	      }
	    repaint();
	    /* grp.wireframePaint (this); */
	  }
	else
	  {
	    needToRepaint = true;
            // drag selected atom.
	    switch (mouseModifiers)
	      {
	      default:
		double[] scrPos = { x, y, atom1z };
		atom1.x = grp.v.screenToXyz (scrPos);
		movingAtom = true;
	        atomInfo (atom1);
                repaint();
		break;
	      case Event.SHIFT_MASK:
		//grp.bubblePaint (this);
                repaint();
		break;
	      case Event.CTRL_MASK:
		//grp.bubblePaint (this);
                repaint();
		break;
	      }
	  }
	if (atom1 != null && !movingAtom)
	  grp.drawLineToAtom (this.getGraphics(), atom1, x, y);
      }
      xxx = x; yyy = y;
      e.consume();
  }

  public void mouseReleased (MouseEvent e)
  {
    int x=e.getX();
    int y=e.getY();
    if (atom1 != null)
      {
	double[] scrPos = { x, y, atom1z };
	atom atom2 = grp.selectedAtom (scrPos, false);
	if (dragFlag)
	  { // we dragged on an atom
            needToRepaint=true;
	    switch (mouseModifiers)
	      {
	      default:
		atom1.x = grp.v.screenToXyz (scrPos);
		atom2 = atom1;
	        atomInfo (atom2);
		break;
	      case Event.SHIFT_MASK:
		if (atom1 != atom2 && atom1 != null && atom2 != null)
		  {
		    // create a new bond if none exists, or increment the
		    // order if it does exist.
		    grp.addBond (atom1, atom2);
		  }
		else if (atom2 == atom1)
		  {
	    	    PDBAtom a = new PDBAtom ();
	            a.elemno=a.GetElemNumber(1,atomNames[atom_selected]);
	            a.setParms(a.elemno);
	            a.x= grp.v.screenToXyz (scrPos);
	            //grp.addAtom((atom)a);
		    //a.x[2]=atom1.x[2];
	            grp.addAtom(a);
		    grp.addBond (atom1, a);
	            //atomInfo (a);
		  }
		break;
	      case Event.CTRL_MASK:
		if (atom1 != atom2 && atom1 != null && atom2 != null)
		  grp.deleteBond (atom1, atom2);
		break;
	      }
	    // give information about the last atom we visited
	    atomInfo (atom2);
	  }
	else
	  {
	    // clicked on a atom	
            switch (mouseModifiers)
	      {
	      default:
	        break;
              case Event.CTRL_MASK:
                if (atom1 != null) {
	          needToRepaint = true;
	          grp.deleteAtom (atom1);
                  atomInfo ();
		}
                break;
              }
          }	    
      }
    else if (!dragFlag)
      { // we clicked on air
	double[] scrPos = { x, y, atom1z };
	switch (mouseModifiers)
	  {
	  default:
	    break;
/*
	  case Event.SHIFT_MASK:
  	    PDBAtom a = new PDBAtom ();
	    needToRepaint = true;

            a.elemno=a.GetElemNumber(1," C");
            a.setParms(a.elemno);
            a.x= grp.v.screenToXyz (scrPos);

	    if (ename.compareTo ("Carbon") == 0)
	      a = new carbon ();
	    else if (ename.compareTo ("Nitrogen") == 0)
	      a = new nitrogen ();
	    else if (ename.compareTo ("Oxygen") == 0)
	      a = new oxygen ();
	    else
	      a = new hydrogen ();
	    grp.addAtom (a, scrPos);
	    atomInfo (a);
	    break;
*/
	  case Event.CTRL_MASK:
	    needToRepaint = true;
	    atomInfo ();
	    //grp.updateViewSize (this,this.size().height);
	    //grp.centerAtoms ();
	    break;
	  }
        if (xxx == x && yyy == y) { // One click stops rotation
           diffX=0;diffY=0;diffZ=0;needToRepaint=false;
        }
      }
    else
    {
      needToRepaint = true;
      dragFlag = false;
    }
    if (needToRepaint)
    {
      //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      repaint ();
    }
    //return true;
    e.consume();
  }

  private void energyMinimize ()
  {
    double scale;
    for (scale = 0.1; scale > 0.0001; scale *= 0.9) {
      grp.energyMinimizeStep (scale);
      repaint();
    }
  }

  private void constrain (Container container, Component component,
			  int gridX, int gridY, int gridW, int gridH)
  {
    constrain (container, component, gridX, gridY, gridW, gridH,
	       GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST,
	       0.5, 0.5, 0, 0, 0, 0);
  }
  private void constrain (Container container, Component component,
			  int gridX, int gridY, int gridW, int gridH,
			  int fill, int anchor, double weightX,
			  double weightY, int top, int left, int bottom,
			  int right)
  {
    GridBagConstraints c = new GridBagConstraints ();
    c.gridx = gridX; c.gridy = gridY;
    c.gridwidth = gridW; c.gridheight = gridH;
    c.fill = fill; c.anchor = anchor;
    c.weightx = weightX; c.weighty = weightY;
    if (top + bottom + left + right > 0)
      c.insets = new Insets (top, left, bottom, right);
    ((GridBagLayout) container.getLayout()).setConstraints(component, c);
  }

  /* imported from the oekaki */
  private void post(URL url, byte abyte0[], Label label, boolean flag)
  throws IOException, InterruptedException
  {
        char c = '2';
        Socket socket = null;
        BufferedReader bufferedreader = null;

        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("POST " + url.getFile() + " HTTP/1.0\r\nAccept-Language: ");
        stringbuffer.append(Locale.getDefault().getLanguage() + "\r\n");
        stringbuffer.append("Content-type");
        stringbuffer.append(": ");
        stringbuffer.append("application/octet-stream");
        stringbuffer.append("\r\nReferer: ");
        stringbuffer.append(getDocumentBase().toExternalForm());
        stringbuffer.append("\r\nUser-Agent: DisMol (" + System.getProperty("os.name") + ';' + System.getProperty("os.version") + ")\r\nHost: ");
        stringbuffer.append(url.getHost());
        stringbuffer.append("\r\nConnection: close\r\nContent-Length: ");
        stringbuffer.append(abyte0.length);
        stringbuffer.append("\r\n\r\n");
        int i = url.getPort();
        socket = new Socket(url.getHost(), i <= 0 ? 80 : i);
        OutputStream outputstream = socket.getOutputStream();
        outputstream.write(stringbuffer.toString().getBytes("UTF8"));
        stringbuffer = null;
        outputstream.flush();
        int k = abyte0.length;
        for(int i1 = 0; i1 < k;) {
            int j1 = Math.min(k - i1, 5000);
            outputstream.write(abyte0, i1, j1);
            i1 += j1;
            label.setText(String.valueOf(i1 / 1024) + "KB/ All data size=" + k / 1024 + "KB");
        }

        outputstream.flush();
        try {
            bufferedreader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
            String s = bufferedreader.readLine();
            if(s != null)
            {
                s = s.substring(Math.max(s.indexOf(32), 0)).trim() + '2';
                c = s.charAt(0);
            }
            while((s = bufferedreader.readLine()) != null)
                if(s.length() <= 0)
                    break;
        }
        catch(IOException f) { }
     }

  public String xyzFile()
  {
    int i;
    atom a;
    String mol="";

    mol=grp.atomList.size () + "\ngenerated by dismol " + version + "\n";
    for (i = 0; i < grp.atomList.size (); i++)
      {
        a = (atom) grp.atomList.elementAt (i);
        mol += a.symbol() + "  " + toString(a.x[0],5) + "  " + toString(a.x[1],5) + "  " + toString(-a.x[2],5) + "\n";
      }
    return mol;
  }

  public void readMolFile(String mol)
  {
    group my;
    my=new group();
    InputStream in = new ByteArrayInputStream(mol.toString().getBytes());
    my = pdbreader.read(in);
    mol_type=0;
     
    if(my == null || my.atomList.size() == 0) {
      my = xyzreader.read(in);
      if(my == null || my.atomList.size() == 0) {
        atomInfo ("Unable to read molecular file");
      } else {
        mol_type=1;
      }
    }
    if (my != null) {
      if (grp.atomList.size() == 0) {
        my.centerAtoms (1);
        my.InitialTransform();
        //my.mypanel_size = grp.mypanel_size;
        my.setPanelSize(getSize());
        my.setDefaultZoomFactor();
        grp= my;
        //grp.v.reset();
      } else {
        view v;
        v=grp.v;
        //my.mypanel_size = grp.mypanel_size;
        my.setPanelSize(getSize());
        grp= my;
        grp.v=v;
      }

      molString=mol;
      needToRepaint = true;
      repaint();
      atomInfo ("MolFile readed");
    }
  }

  public void readXYZFile(String mol)
  {
    group my;
    my=new group();
    String smol="";
    mol_type=1;
    mol_number=-1;
    mol_index=0;
    movie_mode=false;
    molString=mol;

    smol=getNthMolecule(0);
    //System.err.println("readXYZ:" + smol);

    InputStream in = new ByteArrayInputStream(smol.toString().getBytes());
    my = xyzreader.read(in);
     
    if(my == null) {
      atomInfo ("Unable to read xyz file");
    } else {
      if (grp.atomList.size() == 0) {
        my.centerAtoms (1);
        my.InitialTransform();
        //my.mypanel_size = grp.mypanel_size;
        my.setPanelSize(getSize());
        my.setDefaultZoomFactor();
        grp= my;
        //grp.v.reset();
      } else {
        view v;

        v=grp.v;
        //my.mypanel_size = grp.mypanel_size;
        my.setPanelSize(getSize());
        grp= my;
        grp.v=v;
      }
      grp.setShowForces(force_mode,false);

      needToRepaint = true;
      repaint();
      atomInfo ("XYZ MolFile readed");
    }
  }

  public String molFile()
  {
     return xyzFile();
  }

  public void cleanMolecule()
  {
    grp=new group();
    needToRepaint = true;
    repaint();
  }

  private group readMoleculeFile(String model)
  {
    URL url;
    String file_type = "chemical/x-pdb";

    try {
      url = new URL(getDocumentBase(),model);
    } catch (MalformedURLException f) {
      System.err.println("couldn't open this file");
      return null;
    }
    InputStream instream;
    if(model.endsWith(".pdb") || model.endsWith(".ent"))
      file_type = "chemical/x-pdb";
    else if(fileUrl.endsWith(".xyz"))
      file_type = "chemical/x-xyz";
    else {
      System.err.println("I don't know what to do with encoding type " + file_type);
      return null;
    }
    try {
      instream = url.openStream();
    } catch (IOException f) {
      System.err.println("couldn't open this file");
      return null;
    }
    atomInfoBlab.setText ("reading " + model + " type " + file_type);
    atomInfoBlab.setBackground (this.getBackground ());

    group r;

    if(file_type.equals("chemical/x-xyz"))
      r = xyzreader.read(instream);
    else r = pdbreader.read(instream);
    if(r == null)
      atomInfoBlab.setText ("Unable to read file");
    else atomInfoBlab.setText ("Transfer complete");
    return r;
  }

  private String streamToString(InputStream instream)
  {
    String s = new String("");
    try
    {
       while (instream.available() > 0)
         {
            s = s + (char)instream.read();
         }
    } catch (IOException e) {
      System.err.println("IOException " + e.getMessage());
      return "";	/* ?? */
    }
    return s;
  }

  private String getNthMolecule(int nth)
  {
    int i,j,state=0;
    String line;
    String[] lines=null;
    int nmol=0,imol=0,an;
    
    String smol="";

    if (mol_type == 1 ) {
      String t;
      boolean delim=false;
      String mol=molString;
      if (molString == null || mol.trim().equals(""))
         return "";
      int delimeterPos = mol.indexOf("\n");
      Vector vect=new Vector();

      if (delimeterPos < 0) {
          vect.add(mol);
      }

      int tokenStartPos = 0;
      while (delimeterPos >= 0) {
        String token = mol.substring(tokenStartPos, delimeterPos);
        vect.add(token.length() > 0 ? token : null);
        mol = mol.substring(delimeterPos + 1);
        delimeterPos = mol.indexOf("\n");
      }
      if (mol != "") vect.add(mol);
      lines = (String[]) vect.toArray(new String[0]);

      //lines = molString.split("\\n");
      if (mol_number == -1) {
        // check
        for (i=0;i<lines.length;i++) {
          line=lines[i];
          try {
            StringTokenizer st = new StringTokenizer(line);
            int count = st.countTokens();
            if (count != 1) break;
            an = Integer.parseInt(st.nextToken());
            if (an < 1) break;
            i++; // comment
            i+=an; // skip atom number
            nmol++;
          } catch (NullPointerException e) {
            break;
          }
        }
        // set mol_number
        if (nmol > 0) mol_number = nmol;
        else return "";
      }
      // System.err.println("getNthMol:" + mol_number);
      nmol=mol_number;
      i=0;
      for (imol=0;imol<nth;imol++) {
	if (i > lines.length) break;
        line=lines[i];

        StringTokenizer st = new StringTokenizer(line);
        int count = st.countTokens();
        if (count > 1) return "";
        an = Integer.valueOf(st.nextToken()).intValue();
        i++; // comment
        i+=an; // skip atom number
	i++;
      }
      // System.err.println("getNthMol: i=" + i + lines[i]);

      for (;i<lines.length;i++) {
        line=lines[i];
        //System.err.println("mol: " + i +" : " + line);

        StringTokenizer st = new StringTokenizer(line);
        int count = st.countTokens();
        if (count > 1) return "";
        smol+=line+"\n";
        an = Integer.valueOf(st.nextToken()).intValue();
        i++; // comment
        line=lines[i];
        smol+=line+"\n";
        for (j=1;j<=an;j++) {
          line=lines[i+j];
          smol+=line+"\n";
        //System.err.println("mol: " + j +" : " + line);
        }
	break;
      }
      // System.err.println("getNthMol: smol=" + smol);
    }
    return smol;
  }

  private void showNthMolecule(int nth)
  {
    String smol="";
    smol=getNthMolecule(nth);

    group my;
    my=new group();
    InputStream in = new ByteArrayInputStream(smol.toString().getBytes());
    my = xyzreader.read(in);
   
    if(my == null) {
      atomInfoBlab.setText ("Unable to show "+nth+" molecular");
    } else {
      view v;

      v=grp.v;
      grp= my;
      grp.v=v;
      grp.setShowForces(force_mode,false);

      needToRepaint = true;
      repaint();
      atomInfoBlab.setText ("Molecular:" + (nth+1));
    }
    return;
  }

  private group readMoleculeURL()
  {
    URL url;
    String file_type = "chemical/x-pdb";

    try {
      url = new URL(fileUrl);
    } catch (MalformedURLException e) {
      System.err.println("couldn't connect to url");
      return null;
    }
    //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    InputStream instream;
    URLConnection connect;

    try {
      connect = url.openConnection();
      instream = connect.getInputStream();
      file_type = connect.getContentType();
      if(!file_type.equals("chemical/x-pdb") && !file_type.equals("chemical/x-xyz"))
      {
        if(fileUrl.endsWith(".pdb") || fileUrl.endsWith(".ent"))
	  file_type = "chemical/x-pdb";
        else if(fileUrl.endsWith(".xyz"))
	  file_type = "chemical/x-xyz";
        else
        {
	  System.err.println("I don't know what to do with encoding type " + file_type);
	  return null;
        }
      }
      atomInfoBlab.setText ("reading " + connect.getContentLength()
			  + " bytes, type " + file_type);

    } catch (IOException e) {
      System.err.println("couldn't connect to url");
      return null;
    }
    atomInfoBlab.setBackground (this.getBackground ());

    group r;

    if(file_type.equals("chemical/x-xyz"))
      r = xyzreader.read(instream);
    else r = pdbreader.read(instream);
    if(r == null)
      atomInfoBlab.setText ("Unable to read file");
    else atomInfoBlab.setText ("Transfer complete");
    return r;
  }

  public void keyPressed(KeyEvent e) { }
  public void keyReleased(KeyEvent e) { }
  //public boolean keyDown(Event e, int keycode)
  public void keyTyped(KeyEvent e)
  {
    //char keyChar = (char) keycode;
    char keyChar = e.getKeyChar();
    if (keyChar == 'h' || keyChar == 'H') {
       System.err.println("oops");
       helpWindow();
       return;
    }
    if (standalone == true && (keyChar == 'z' || keyChar == 'Z')) {
       System.err.println("oops");
       destroy();
       return;
    }
    if (keyChar == KeyEvent.CHAR_UNDEFINED)
       return;
    if (keyChar == 'a' || keyChar == 'A') {
       atom_selected++;
       atom_selected %= atomNames.length;
       atomInfoBlab.setText("'" + atomNames[atom_selected] + "' selected.");
       repaint();
    } else if (keyChar == 'v' || keyChar == 'V') {
      if (force_mode) {
        force_mode=false;
        showStatus("DisMol: hide forces");
      } else {
        force_mode=true;
        showStatus("DisMol: show forces");
      }
      grp.setShowForces(force_mode,false);
      needToRepaint = true;
      repaint();
    } else if (keyChar == 'w' || keyChar == 'W') {
      if (bgcolor == Color.white) {
        bgcolor=Color.black;
        fontColor=Color.lightGray;
      } else {
        bgcolor=Color.white;
        fontColor=Color.black;
      }
      RasBuffer.setBackColor(bgcolor);

      needToRepaint = true;
      repaint();
    } else if (keyChar == 'c' || keyChar == 'C') {
       grp.centerAtoms (1);
       showStatus("DisMol: positioning to the center of atoms");
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'd' || keyChar == 'D') {
       display_mode++;
       display_mode %= drawModeNames.length;
       showStatus("DisMol: " + drawModeNames[display_mode] + " model");
       setRenderingStyle();
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'x' || keyChar == 'X') {
       if(axes_flag) {
          axes_flag=false;
          showStatus("DisMol: Axes Off");
       } else {
          axes_flag=true;
          showStatus("DisMol: Axes On");
       }
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'y' || keyChar == 'Y') {
       setClipboard();
    } else if (keyChar == 'e' || keyChar == 'E') {
       showStatus("DisMol: Energy Minimize");
       energyMinimize();
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'f' || keyChar == 'F') {
       showStatus("DisMol: Increase the pick info precision");
       info_precision++;
       if (info_precision > 5) info_precision=2;

       needToRepaint = true;
       repaint();
    } else if (keyChar == 'i' || keyChar == 'I') {
       showStatus("DisMol: Reset to initial positions");
       if (mol_type==1) showNthMolecule(mol_index);
       else readMolFile(molString);

       grp.v.reset();
       grp.InitialTransform();
       grp.setPanelSize(getSize());
       grp.setDefaultZoomFactor();

       needToRepaint = true;
       repaint();
    } else if (keyChar == 'm' || keyChar == 'M') {
       if (mol_number==1 || mol_type==0) return;
       if (movie_mode) {
          showStatus("DisMol: Movie off");
          movie_mode = false;
       } else {
          showStatus("DisMol: Movie on");
          movie_mode = true;
       }
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'n' || keyChar == 'N') {
       showStatus("DisMol: next molecular");
       if (mol_type == 0) return;
       mol_index++;
       if (mol_index >= mol_number) mol_index=0;
       showNthMolecule(mol_index);
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'b' || keyChar == 'B') {
       showStatus("DisMol: prev molecular");
       if (mol_type == 0) return;
       mol_index--;
       if (mol_index < 0) mol_index=mol_number-1;
       showNthMolecule(mol_index);
       needToRepaint = true;
       repaint();
    } else if (keyChar == 'p' || keyChar == 'P') {
       showStatus("DisMol: post or save a png image");
       byte[] pngbytes;
       URL post_url;
       if (standalone) {
          pngbytes=grp.getPng();
          try {
             FileOutputStream out = new FileOutputStream("dismol.png");
             out.write(pngbytes);
             out.close();
          } catch (IOException f) {
             showStatus("I/O error");
             return;
          }
          return;
       }
       try {
         post_url = new URL(getDocumentBase(),postUrl);
         pngbytes=grp.getPng();
       } catch (MalformedURLException f) {
         showStatus("DisMol: invalid post_url");
         e.consume();
         return;
       }
       try {
         post(post_url,pngbytes,atomInfoBlab,true);
       } catch (IOException f) {
         showStatus("DisMol: invalid post_url");
         e.consume();
         return;
       } catch (InterruptedException f) {
         showStatus("DisMol: invalid post_url");
         e.consume();
         return;
       }
       showStatus("DisMol: png posted completely");
    }
    else if (keyChar == 'q' || keyChar == 'Q') {
       if (PickMode) {
          showStatus("DisMol: Pick off");
          //PickCount = 0;
          PickMode = false;
          PickTest = false;
       } else {
          showStatus("DisMol: Pick on");
          PickCount = 0;
          PickTest = false;
          PickMode = true;
       }
       needToRepaint = true;
       repaint();
    }
    else if (keyChar == 'r' || keyChar == 'R') {
       if (spin_mode) {
          showStatus("DisMol: Spin off");
          spin_mode = false;
       } else {
          showStatus("DisMol: Spin on");
          spin_mode = true;
       }
       needToRepaint = true;
       repaint();
    }
    else if (keyChar == 's' || keyChar == 'S') {
       if (display_label) {
          showStatus("DisMol: Labels off");
          display_label = false;
       } else {
          showStatus("DisMol: Labels on");
          display_label = true;
       }
       needToRepaint = true;
       repaint();
    }
    e.consume();
  }

  public void setRenderingStyle()
  {
    if (grp != null) {
      if (display_mode == WIREFRAME)
        grp.resizeAtoms(100.0);
      else if (display_mode == STICKS)
        grp.resizeAtoms(100.0);
      else if (display_mode == BALLSTK)
        grp.resizeAtoms(200.0);
      else if (display_mode == SPACEFILL)
        grp.resizeAtoms(550.0);
    }
  }

  private Color convertToColor(String s) // Convert hexadecimal RGB parameter to color
  { int i,j;
    int hex[];
    String h="0123456789abcdef";
    Color c;
    hex=new int[6];
    if ((s!=null)&&(s.length()==7))
    {
      for (i=0;i<6;i++)
        for (j=0;j<16;j++)
          if (Character.toLowerCase(s.charAt(i+1))==h.charAt(j))
            hex[i]=j;
      c=new Color(hex[0]*16+hex[1],hex[2]*16+hex[3],hex[4]*16+hex[5]);
    }
    else
      c=Color.black; // Default
    return c;
  }

  public void helpWindow ()
  {
    closableWindow help = new closableWindow("DisMol Help");
    help.setSize(350, 320);
    TextArea helpMsg=new TextArea(10,40);
    helpMsg.setText (helpMessage);
    helpMsg.setEditable(false);

    help.add("Center", helpMsg);
    help.show(); 
  }

  public void init ()
  {
    controls = new Panel ();

    setLayout(new BorderLayout());
    controls.setLayout (new GridBagLayout ());

    String spaces = "                              "; /* 30 spaces */
    atomInfoBlab = new Label (spaces + spaces + spaces + spaces + spaces);
    constrain (controls, atomInfoBlab, 0, 1, 5, 1);
    controls.add(atomInfoBlab);
    fileList = new Choice ();

    if (!standalone) {
      bgcolor=convertToColor(getParameter("bgcolor"));
      banner=getParameter("banner");
      postUrl = getParameter("post_url");
    }
    RasBuffer.setBackColor(bgcolor);
    if (postUrl == null)
      postUrl="get.cgi";

/*
    getMoleculeFile = new Button ("Get new file");
    constrain (controls, getMoleculeFile, 1, 0, 1, 1);
    controls.add (getMoleculeFile);
*/
    fileList.addItemListener(new ItemListener()
    {
      public void itemStateChanged (ItemEvent e) {
	fileUrl = fileList.getSelectedItem();
	System.err.println(fileUrl);
	if((grp = readMoleculeURL()) != null)
	{
	  grp.setDefaultZoomFactor();
	  needToRepaint = true;
	  repaint();
	}
      }
    });

    this.add ("South",controls);
    this.requestFocus();
    //this.repaint();
  }

  public void start() {
    String urls=null;
    String model=null;
    String mol=null;
    String mode=null;
    String astr=null;

    RasBuffer.InitialiseTransform();

    if (!standalone) {
      urls = getParameter("url");
      model = getParameter("model");
      mode = getParameter("mode");
      astr = getParameter("atomsize");
//  String spin = getParameter("spinfp");

    if (model != null) {
      grp = readMoleculeFile(model);
    } else if(urls != null) {
      StringTokenizer toker = new StringTokenizer(urls," ;\n\r");
      while(toker.hasMoreTokens())
      {
        String u = toker.nextToken();
        if(fileUrl == null)
  	  fileUrl = u;
        fileList.addItem(u);
      }
      grp = readMoleculeURL();
    } else {
      mol = getParameter("molString");
      //String nmol = "";

      // JME style input. pdb style only
      if (mol != null) {
        StringTokenizer toker = new StringTokenizer(mol,"|\n\r");
        while(toker.hasMoreTokens()) {
          String u = toker.nextToken();
          molString += u+"\n";
        }
      }
    }

    } // ! standalone

    if (molString != "") {
      InputStream in= new ByteArrayInputStream(molString.toString().getBytes());
      grp = pdbreader.read(in);
      if(grp.atomList.size() == 0) {
        try {
          in.reset();
          grp = xyzreader.read(in);
        } catch (IOException f) { }
        if(grp.atomList.size() == 0) {
          atomInfoBlab.setText ("Unable to read file");
        } else {
          mol_type=1;
        }
      } else {
        mol_type=0;
      }
    }

    if(grp != null) {
      atomInfoBlab.setText ("molString readed");
      grp.centerAtoms(1);
      grp.InitialTransform(); // XXX
      if(astr != null)
      {
	double atom_size = Double.valueOf(astr).doubleValue();
	if(atom_size > 0.01)
	  grp.resizeAtoms(atom_size);
      }
      String rotate = getParameter("rotate");
      if (rotate != null) {
      StringTokenizer st = new StringTokenizer(rotate);
      int count = st.countTokens();
      if (count == 3) {
        try {
          int ix,iy,iz;
          ix = Integer.parseInt(st.nextToken());
          iy = Integer.parseInt(st.nextToken());
          iz = Integer.parseInt(st.nextToken());
          grp.v.rotate(ix,iy,iz);
        } catch (NullPointerException e) { }
      }
      }
    }

    if (mode != null) {
      if (mode.equals("sticks") || mode.equals("stick")) {
        if (grp !=null) grp.resizeAtoms(100.0);
        display_mode=STICKS;
      }
      else if (mode.equals("spacefill")) {
        if (grp !=null) grp.resizeAtoms(550.0);
        display_mode=SPACEFILL;
      }
      else if (mode.equals("wireframe")) {
        if (grp !=null) grp.resizeAtoms(100.0);
        display_mode=WIREFRAME;
      }
      else if (mode.equals("ball&stick")) {
        if (grp !=null) grp.resizeAtoms(200.0);
        display_mode=BALLSTK;
      }
    }

    if (urls != null) {
      constrain (controls, fileList, 0, 0, 1, 1);
      controls.add (fileList);
    } 

    if (grp != null) {
      int ht = getSize().height;
      int wd = getSize().width;
      grp.setPanelSize(new Dimension(wd,ht));
      grp.setDefaultZoomFactor(); // XXX
    } else {
      grp=new group();
      grp.setPanelSize(getSize());
      atomInfoBlab.setText ("Welcome to DisMol");
    }

    //this.addActionListener(this);
    this.addKeyListener(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
  }

  public void stop() {
    this.getInputContext().removeNotify(this);
    this.removeNotify();

    this.removeMouseMotionListener(this);
    this.removeMouseListener(this);
    this.removeKeyListener(this);

/*
    Component cmp[]= this.getComponents();
    for(int i=0;i<cmp.length;i++)
    {
      if(cmp != null)
      {
        cmp.removeNotify();
      }
    }
*/
  }

  public void destroy() {
    stop();
  }

  public static void main(String args[])
  {
    standalone = true;
    Frame frame = new Frame("DisMol");
    DisMol dismol = new DisMol();
    dismol.init();
    //if(args.length > 0)
    //    dismol.options(args[0]);
    dismol.start();
    /*
    dismol.addWindowListener(
          new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
          System.exit(0);
          }
    });
    */
    frame.add("Center", dismol);
    frame.setSize(432, 384);
    frame.show();
    dismol.grp = new group();

    int ht = dismol.getSize().height;
    int wd = dismol.getSize().width;
    //dismol.dimension = dismol.size();
    dismol.grp.setPanelSize(new Dimension(wd,ht));
    dismol.grp = dismol.readMoleculeFile(args[0]);
    if(args.length > 0)
    {
      dismol.grp.InitialTransform();
      dismol.grp.setDefaultZoomFactor();
    }
  }

 
  public void actionPerformed(ActionEvent event) {
    Image image = createImage(getSize().width,getSize().height);
    ImageSelection imgSel = new ImageSelection(image);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
  }

  // If an image is on the system clipboard, this method returns it;
  // otherwise it returns null.
  public Image getClipboard() {
    DataFlavor imageFlavor =
	        new DataFlavor("image/x-java-image; class=java.awt.Image",
		   "Image");
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
   
    try {
      if (t != null && t.isDataFlavorSupported(imageFlavor)) {
        Image text = (Image)t.getTransferData(imageFlavor);
        return text;
      }
    } catch (UnsupportedFlavorException e) {
    } catch (IOException e) {
    }
    return null;
  }

  public void setClipboard() {
/*    try
    {
    PrivilegeManager.enablePrivilege("UniversalSystemClipboardAccess");
    showStatus("Privilege granted to access system clipboard");
    }
	catch (Throwable t) // either NoSuchMethodException or NoSuchMethodError
    {
    // Probably not Netscape; assume we do not need capabilities...
    showStatus("Error requesting capability; proceeding anyway");
    t.printStackTrace();
    }
    */

    Image image = this.createImage(getSize().width,getSize().height);
    Graphics g = image.getGraphics();
    if (banner != null)
      grp.banner(banner, fontColor, this);
    grp.paint(g,this);
    ImageSelection imgSel = new ImageSelection(image);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
  }
    
  // This class is used to hold an image while on the clipboard.
  public class ImageSelection implements Transferable {
    private Image image;

    public DataFlavor imageFlavor;

    public ImageSelection(Image image) {
      this.image = image;
      // hack in order to be able to compile in java1.3
      imageFlavor =
	        new DataFlavor("image/x-java-image; class=java.awt.Image",
		   "Image");
    }

    // Returns supported flavors
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[]{imageFlavor};
    }
    
    // Returns true if flavor is supported
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavor.equals(imageFlavor);
    }
    
    // Returns image
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      if (!flavor.equals(imageFlavor)) {
        throw new UnsupportedFlavorException(flavor);
      }
      return image;
    }
  }
}

/*
class closableWindow extends Frame {
  public closableWindow(String s) {
    super(s);
  }

  public void processEvent(AWTEvent e) {
    if (e.getID() == Event.WINDOW_DESTROY) {
      hide();
    }
    super.processEvent(e);
  }
}
*/
class closableWindow extends Frame {
  public closableWindow(String s) {
    super(s);
  }

  public boolean handleEvent(Event e) {
    if (e.id == Event.WINDOW_DESTROY) {
      hide();
      return true;
    }
    return super.handleEvent(e);
  }
}



// vim:et:sts=2:sw=2:
