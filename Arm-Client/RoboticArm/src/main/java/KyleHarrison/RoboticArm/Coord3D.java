package KyleHarrison.RoboticArm;
// Coord3D.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, June 2011


public class Coord3D
{
  private int x, y, z;

  public Coord3D()
  {  x = 0; y = 0; z = 0;  }

  public Coord3D(int x, int y, int z)
  { this.x = x;
    this.y = y;
    this.z = z;
  }

  public void setX(int x)
  {  this.x = x; }

  public void setY(int y)
  {  this.y = y; }

  public void setZ(int z)
  {  this.z = z; }


  public int getX()
  {  return x;  }

  public int getY()
  {  return y;  }

  public int getZ()
  {  return z;  }

  public String toString()
  {  return "(" + x + ", " + y + ", " + z + ")";  }

}  // end of Coord3D class

