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
import java.lang.Math;
import java.util.StringTokenizer;
import java.io.*;
import java.net.Socket;
import java.util.Locale;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import atom;
import view;
import group;
import pdbreader;
import xyzreader;
import RasBuffer;
import RasFont; // added by wkpark@kldp.org 2003
import RasCalc; // added by wkpark@kldp.org 2003

public class DisMol extends Applet
  implements KeyListener, MouseListener, MouseMotionListener {
  public static final String rcsid =
  "$Id: DisMol.java,v 1.14 2000/04/15 23:32:12 pcm Exp $";
  private Button getMoleculeFile;
  private Button resizeAtoms;
  private Choice whichElement;
  private group grp;
  private Panel drawingArea;
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
    "<D> change style  <L> Labels on/off\n" +
    "<D> change style <A> Axes on/off\n" +
    "<Q> Query on/off <R> Rotation on/off\n";

  public static final int WIREFRAME = 0;
  public static final int STICKS = 1;
  public static final int BALLSTK = 2;
  public static final int SPACEFILL = 3;

  private int     display_mode=WIREFRAME;
  private boolean display_label=false;
  private boolean spin_mode=false;
  private boolean PickMode=false;

  private static final int PickCoord = 0;
  private static final int PickDist  = 1;
  private static final int PickAngle = 2;
  private static final int PickTorsn = 3;

  private int PickCount = 0;
  private int PickAtom[] = {0,0,0,0};

  private long    t1=0;
  private long    tMouseDown=0;
  private long    tDoubleClick=300;
  private int     lastx=0,lasty=0;
  private int     diffX=0,diffY=0;

  public DisMol()
  {
    RasBuffer.InitialiseTransform();
    RasFont.InitialiseFont();
  }

  public void paint (Graphics g)
  {
    if(grp == null) return;
    int ht = drawingArea.getSize().height;
    int wd = drawingArea.getSize().width;
    if(ht == 0)
    {
      ht = getSize().height;
      wd = getSize().width;
    }
    grp.setPanelSize(new Dimension(wd,ht));
    grp.updateViewSize ();

//    if(spin_mode) {
//      long tnow = System.currentTimeMillis();
////      System.err.println("paint time " + (tnow - t1));
//      if ((tnow - t1) > 10) {
//         grp.v.rotate (0.06, 0.0);
//         t1= System.currentTimeMillis();
//         needToRepaint=true;
//      }
//    }
    if (display_mode == STICKS)
      grp.stickPaint(drawingArea);
    else if (display_mode == WIREFRAME)
      grp.wireframePaint(drawingArea);
    else if (display_mode == SPACEFILL)
      grp.fullPaint(drawingArea);
    else if (display_mode == BALLSTK)
      grp.fullPaint(drawingArea);

    if (display_label)
      grp.DisplayLabels(drawingArea);

    if (PickMode) {
      PickAtoms();
    }

    if(axes_flag)
      grp.drawAxes(g, dragFlag);
    grp.paint(g,drawingArea);
    // if(this.getCursor() != Cursor.getDefaultCursor())
    // this.setCursor(Cursor.getDefaultCursor());
    repaint();
    if (!spin_mode) setPainted();
    Graphics2D g2d = (Graphics2D)g;

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
  }

  public void update(Graphics g) {
    int degX=(int)(0.7*diffX);
    int degY=(int)(0.7*diffY);
    if (degX != 0 || degY != 0) {
//       grp.v.rotate (0.01 * diffX, 0.01 * diffY);
       double factor=0.0174532925;
       grp.v.rotate (degX*factor,degY*factor);
       needToRepaint=true;
       if (!spin_mode) {
          diffX=0;diffY=0;
         // needToRepaint=false;
       }
    } //else
      // needToRepaint=false;
    if (needToRepaint)
      paint(g);
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
      if (d < 0)
        {
  	s += "-";
  	d = -d;
        }
      d += 0.5*Math.pow(10,-place);
      if (d > 1)
        {
  	int i = (int)d;
  	s += i;
  	d -= i;
        }
      else
        s += "0";
      if (d > 0)
        {
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
    
    if (PickCount < 2) return;

    if (PickCount == 2) {
        temp = RasCalc.CalcDistance((atom)grp.atomList.elementAt(PickAtom[0]),
                                    (atom)grp.atomList.elementAt(PickAtom[1]));
        e = (atom) grp.atomList.elementAt(PickAtom[1]);
        unit[0] = 127; /* Angstrom symbol */
    } else if (PickCount == 3) {
        temp = RasCalc.CalcAngle((atom)grp.atomList.elementAt(PickAtom[0]),
                                 (atom)grp.atomList.elementAt(PickAtom[1]),
                                 (atom)grp.atomList.elementAt(PickAtom[2]));
        e = (atom) grp.atomList.elementAt(PickAtom[2]);
    } else if (PickCount == 4) {
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
                             (int)300,
                             toString(temp,2)+u,s.getColorIndex()+20);

    RasBuffer.ClipDashLine((int)ss[0],(int)ss[1],(int)ss[2],
                 (int)se[0],(int)se[1],(int)se[2],
                 s.getColorIndex(),e.getColorIndex());

    needToRepaint=true;
  }

  public void mouseClicked(MouseEvent e) {
  }
  public void mousePressed (MouseEvent e)
  {
    int x = e.getX();
    int y = e.getY();
    Rectangle r = drawingArea.getBounds();
    inDrawingArea = y < r.height;
    needToRepaint = false;
    double[] scrPos = { x, y, 0 };
    boolean dclick;

    if ((lastx==x) && (lasty==y) &&
      //((e.when-tMouseDown) < tDoubleClick)) {
      ((e.getWhen()-tMouseDown) < tDoubleClick)) {
      dclick=true;
    } else {
      dclick=false;
      //tMouseDown=e.when;
      tMouseDown=e.getWhen();
      lastx=x;
      lasty=y;
    }

    dragFlag = false;

    atom1 = grp.selectedAtom (scrPos, true);

    // We only care about the SHIFT and CTRL modifiers, mask out all others
    //mouseModifiers = e.getModifiers() & (e.isShiftDown() | e.isControlDown());
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
      tmp = grp.selectedAtomId (scrPos);
      if (tmp == -1 && dclick) PickCount=0;
      else {
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
    //return true;
    e.consume();
  }

  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
  }
  public void mouseMoved(MouseEvent e) {
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
		break;
	      case Event.CTRL_MASK:
		// grp.forceMultiplier *= Math.exp (0.01 * (x - xxx));
                diffX=0; diffY=0; //

		grp.v.pan (x - xxx, y - yyy);
		break;
	      case Event.SHIFT_MASK:
                diffX=0; diffY=0;

		grp.v.zoomFactor *= Math.exp (0.01 * (y - yyy));
		grp.v.perspDist *= Math.exp (0.01 * (x - xxx));
                // System.err.println("zoomFactor: " + grp.v.zoomFactor);

		if(grp.v.zoomFactor > 80)
		  grp.v.zoomFactor = 80;
		else if(grp.v.zoomFactor < 1.0)
		  grp.v.zoomFactor = 1.0;
		if(grp.v.perspDist < 400)
		  grp.v.perspDist = 400;
		resized_axes = true;
		break;
	      }
	    repaint();
	    /* grp.wireframePaint (drawingArea); */
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
		// grp.wireframePaint (drawingArea); 
	        atomInfo (atom1);
                repaint();
		break;
	      case Event.SHIFT_MASK:
		//grp.bubblePaint (drawingArea);
                repaint();
		break;
	      case Event.CTRL_MASK:
		//grp.bubblePaint (drawingArea);
                repaint();
		break;
	      }
	  }
	if (atom1 != null && !movingAtom)
	  grp.drawLineToAtom (drawingArea.getGraphics(), atom1, x, y);
      }
      xxx = x; yyy = y;
    //return true;
      e.consume();
  }

  //public boolean mouseUp (Event e, int x, int y)
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
	            a.elemno=a.GetElemNumber(1," C");
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
	  case Event.CTRL_MASK:
	    needToRepaint = true;
	    atomInfo ();
	    //grp.updateViewSize (drawingArea,drawingArea.size().height);
	    //grp.centerAtoms ();
	    break;
	  }
        if (xxx == x && yyy == y) { // One click stops rotation
           diffX=0;diffY=0;needToRepaint=false;
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

  private group readMoleculeURL(Panel not_used)
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
    if (keyChar == KeyEvent.CHAR_UNDEFINED)
       return;
    if (keyChar == 'd' || keyChar == 'D') {
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
    }
    /*
    else if (keyChar == 'e' || keyChar == 'E') {
       showStatus("DisMol: Energy Minimize");
       energyMinimize();
       needToRepaint = true;
       repaint();
    }
    */

    else if (keyChar == 'i' || keyChar == 'I') {
       showStatus("DisMol: Initial position");
       needToRepaint = true;
       repaint();
    }
    else if (keyChar == 'p' || keyChar == 'P') {
       showStatus("DisMol: save image");
       byte[] pngbytes;
       URL post_url;
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
          PickCount = 0;
          PickMode = false;
       } else {
          showStatus("DisMol: Pick on");
          PickCount = 0;
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
/*
    else if (keyChar == 't') { //testing
       grp = new aspirin (drawingArea);
       needToRepaint = true;
       repaint();
    }
*/
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
    help.resize(300, 250);
    TextArea helpMsg=new TextArea(10,40);
    helpMsg.setText (helpMessage);

    help.add("Center", helpMsg);
    help.show(); 
  }

  public void init ()
  {
    controls = new Panel ();
    drawingArea = new Panel ();

    setLayout(new BorderLayout());
    drawingArea.setLayout(new BorderLayout());
//    drawingArea.setBackground(convertToColor(getParameter("bgcolor")));
    drawingArea.setBackground(Color.black);

    GridBagLayout gridbag = new GridBagLayout ();
    controls.setLayout (gridbag);

    String spaces = "                              "; /* 30 spaces */
    atomInfoBlab = new Label (spaces + spaces + spaces + spaces + spaces);
    constrain (controls, atomInfoBlab, 0, 1, 5, 1);
    controls.add(atomInfoBlab);
    this.add ("North",drawingArea);
    fileList = new Choice ();
    postUrl = getParameter("post_url");

    if (postUrl == null)
       postUrl="get.cgi";

/* */
//    showAxes = new Checkbox ("Show axes");
//    showAxes.setState (false);
//      axes_flag = false;
//    constrain (controls, showAxes, 2, 0, 1, 1);
//    controls.add (showAxes);
/* */

    sizeOfAtoms = PDBAtom.atomsize_parm + "";
/*
    inputWindow2 = new TextArea (sizeOfAtoms, 1, 4);
    inputWindow2.setEditable (true);
    constrain (controls, inputWindow2, 3, 0, 1, 1);
    controls.add (inputWindow2);

    resizeAtoms = new Button ("atom size");
    constrain (controls, resizeAtoms, 4, 0, 1, 1);
    controls.add (resizeAtoms);

*/

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
	if((grp = readMoleculeURL(drawingArea)) != null)
	{
	  grp.setDefaultZoomFactor();
	  needToRepaint = true;
	  repaint();
	}
      }
    });

/*  instrucs = new TextArea (5, 80);
    constrain (controls, instrucs, 0, 2, 5, 1);
    instrucs.setText (
"Mouse operations (S=shift, C=control) / pan: S-drag air\n"+
"rotate: drag air / zoom: C-drag air horiz / move atom: drag atom\n"+
"atom info: click atom / recenter: C-click air / perspective: C-drag air vert\n"+
"Use atom size of 0 to 750 to control the radii of each atom.\n"+
"Use \"Show axes\" button to control whether axes are displayed\n"+
"\n"+
"==========================================================================\n"+
"DisMol 0.13  April 2000\n"+
"Copyright (c) 2000 Peter McCluskey, all rights reserved.\n"+
"based on code from Will Ware's NanoCAD and Roger Sayle's RasMol.\n"+
"\n"+
"Redistribution and use in source and binary forms, with or without\n"+
"modification, are permitted provided that the following conditions\n"+
"are met:\n"+
"1. Redistributions of source code must retain the above copyright\n"+
"   notice, this list of conditions and the following disclaimer.\n"+
"2. Redistributions in binary form must reproduce the above copyright\n"+
"   notice, this list of conditions and the following disclaimer in the\n"+
"   documentation and other materials provided with the distribution.\n"+
"\n"+
"This software is provided \"as is\" and any express or implied warranties,\n"+
"including, but not limited to, the implied warranties of merchantability\n"+
"or fitness for any particular purpose are disclaimed. In no event shall\n"+
"the authors be liable for any direct, indirect, incidental, special,\n"+
"exemplary, or consequential damages (including, but not limited to,\n"+
"procurement of substitute goods or services; loss of use, data, or\n"+
"profits; or business interruption) however caused and on any theory of\n"+
"liability, whether in contract, strict liability, or tort (including\n"+
"negligence or otherwise) arising in any way out of the use of this\n"+
"software, even if advised of the possibility of such damage.\n"
);
    //instrucs.setEditable (false);
    //controls.add(instrucs);
*/

    this.add ("South",controls);


    //this.repaint();
  }

  public void start() {
    String urls = getParameter("url");
    String model = getParameter("model");
//  String spin = getParameter("spinfp");
    RasBuffer.InitialiseTransform();

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
      grp = readMoleculeURL(drawingArea);
    } else {
      String mol = getParameter("molString");
      String nmol = "";

      // JME style input. pdb style only
      StringTokenizer toker = new StringTokenizer(mol,"|\n\r");
      while(toker.hasMoreTokens())
      {
        String u = toker.nextToken();
        nmol += u+"\n";
      }

      if (mol != null) {
         InputStream in = new ByteArrayInputStream(nmol.toString().getBytes());
         grp = pdbreader.read(in);
         if(grp == null)
           atomInfoBlab.setText ("Unable to read file");
         else atomInfoBlab.setText ("Transfer complete");
      }
    }

    if(grp != null)
    {
      grp.InitialTransform();
//      grp.centerAtoms();
      String astr = getParameter("atomsize");
      if(astr != null)
      {
	double atom_size = Double.valueOf(astr).doubleValue();
	if(atom_size > 0.01)
	  grp.resizeAtoms(atom_size);
      }
    } else {
      atomInfoBlab.setText ("Unable to read file");
    }

    String mode = getParameter("mode");
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

    if(grp != null)
    {
      int ht = drawingArea.getSize().height;
      int wd = drawingArea.getSize().width;
      if(ht == 0)
      {
	ht = getSize().height - controls.getSize().height;
	wd = getSize().width;
      }
      grp.setPanelSize(new Dimension(wd,ht));
      grp.setDefaultZoomFactor();
    }

    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public void stop() {
    removeMouseMotionListener(this);
    removeMouseListener(this);
    removeKeyListener(this);
  }

  public void destroy() {
    stop();
  }
}

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
