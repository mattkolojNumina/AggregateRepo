package pack;

public class 
Point
    {
    static final double TOLERANCE = 1e-9 ;
    
    public double x = 0.0 ;
    public double y = 0.0 ;
    public double z = 0.0 ;
    
    public
    Point()
        {
        this.x = 0.0 ;
        this.y = 0.0 ;
        this.z = 0.0 ;
        }
    
    public 
    Point(double x, double y, double z)
        {
        this.x = x ;
        this.y = y ;
        this.z = z ;
        }
    
    public
    Point(Point p)
        {
        this.x = p.x ;
        this.y = p.y ;
        this.z = p.z ;
        }
    
    public void
    setPoint(double x, double y, double z)
        {
        this.x = x ;
        this.y = y ;
        this.z = z ;
        }
    
    public void
    setPoint(Point p)
        {
        this.x = p.x ;
        this.y = p.y ;
        this.z = p.z ;
        }
    
    public void
    setX(double x)
        {
        this.x = x ;
        }
    
    public double
    getX()
        {
        return this.x ;
        }
    
    public void
    setY(double y)
        {
        this.y = y ;
        }

    public double
    getY()
        {
        return this.y ;
        }

    public void
    setZ(double z)
        {
        this.z = z ;
        }
        
    public double
    getZ()
        {
        return this.z ;
        }
    
    public boolean
    equals(double x, double y, double z)
        {
        return (Math.abs(this.x-x)<TOLERANCE) 
            && (Math.abs(this.y-y)<TOLERANCE) 
            && (Math.abs(this.z-z)<TOLERANCE);
        }
    
    public boolean
    equals(Point p)
        {
        return (Math.abs(this.x-p.x)<TOLERANCE) 
            && (Math.abs(this.y-p.y)<TOLERANCE) 
            && (Math.abs(this.z-p.z)<TOLERANCE);
        }    
    
    public void
    translate(double x, double y, double z)
        {
        this.x += x ;
        this.y += y ;
        this.z += z ;
        }
    
    public double
    dot(double x, double y, double z)
        {
        return this.x*x + this.y*y + this.z*z ;
        }
    
    public double
    dot(Point p)
        {
        return this.x*p.x + this.y*p.y * this.z*p.z ;
        }
    
    public double
    norm()
        {
        return Math.sqrt(x*x + y*y + z*z) ;    
        }
    
    public Point
    cross(double x, double y, double z)
        {
        Point c = new Point(this.y*z - this.z*y,
                            this.x*z - this.z*x,
                            this.x*y - this.y*x) ;
        return c ;
        }
    
    public Point
    cross(Point p)
        {
        Point c = new Point(this.y*p.z - this.z*p.y,
                            this.x*p.z - this.z*p.x,
                            this.x*p.y - this.y*p.x) ;
        return c ;
        }

   public void order() {
      if (x < y) {
         double swap = x;
         x = y;
         y = swap;
      }
      if (x < z) {
         double swap = x;
         x = z;
         z = swap;
      }
      if (y < z) {
         double swap = y;
         y = z;
         z = swap;
      }        
   }
    }
