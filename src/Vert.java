import java.io.*;

public class Vert extends Object{ //implements Serializable {
  public int x,y,z;
  public int inten;

  public Vert()
  {
    x=y=z=0;
    inten=0;
  }

  public Vert(int x,int y,int z) {
    this.x=x;
    this.y=y;
    this.z=z;
    this.inten=0;
  }

  public Vert(int x,int y,int z, int inten) {
    this.x=x;
    this.y=y;
    this.z=z;
    this.inten=inten;
  }

}
