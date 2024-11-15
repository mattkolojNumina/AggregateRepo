package pack;

public class 
Placement
    {
    public Box box ;
    public Point location ;
    public enum Rotation { XYZ, YZX, ZXY, XZY, ZYX, YXZ } ;
    public Rotation rotation ;
    
    public 
    Placement(Box box)
        {
        this.box      = box ;
        this.location = new Point() ;
        this.rotation = Rotation.XYZ ;
        }

    public void
    setBox(Box box)
        {
        this.box = box ;
        }
    
    public void
    setLocation(Point l)
        {
        this.location = l ;
        }
  
    public void
    setRotation(Rotation rotation)
        {
        this.rotation = rotation ;
        }
               
    public double
    minX()
        {
        return location.x ;
        }
    
    public double
    minY()
        {
        return location.y ;
        }
    
    public double
    minZ()
        {
        return location.z ;
        }
    
    public double 
    spanX()
        {
        double span = 0.0 ;
        
        switch(rotation)
           {
           case XYZ: 
           case XZY:
              span = box.size.x ; break ;
           case YXZ:
           case YZX:
              span = box.size.y ; break ;
           case ZXY:
           case ZYX:
              span = box.size.z ; break ;
           }
        return span ;
        }
    
    public double 
    spanY()
        {
        double span = 0.0 ;
        
        switch(rotation)
           {
           case XYZ: 
           case ZYX:
              span = box.size.y ; break ;
           case YXZ:
           case ZXY:
              span = box.size.x ; break ;
           case XZY:
           case YZX:
              span = box.size.z ; break ;
           }
        return span ;
        }
    
    public double 
    spanZ()
        {
        double span = 0.0 ;
        
        switch(rotation)
           {
           case XYZ: 
           case YXZ:
              span = box.size.z ; break ;
           case YZX:
           case ZYX:
              span = box.size.x ; break ;
           case XZY:
           case ZXY:
              span = box.size.y ; break ;
           }
        return span ;
        }
    
    public double
    maxX()
        {
        return minX() + spanX() ;
        }
    
    public double
    maxY()
        {
        return minY() + spanY() ;
        }
    
    public double
    maxZ()
        {
        return minZ() + spanZ() ;
        }
        
    public Point[]
    getCorners()
        {
        Point[] corners = new Point[8] ;
        
        corners[0] = new Point( location.x,           location.y,           location.z           ) ;
        corners[1] = new Point( location.x + spanX(), location.y,           location.z           ) ;
        corners[2] = new Point( location.x,           location.y + spanY(), location.z           ) ;
        corners[3] = new Point( location.x,           location.y,           location.z + spanZ() ) ;
        corners[4] = new Point( location.x,           location.y + spanY(), location.z + spanZ() ) ;
        corners[5] = new Point( location.x + spanX(), location.y,           location.z + spanZ() ) ;
        corners[6] = new Point( location.x + spanX(), location.y + spanY(), location.z           ) ;
        corners[7] = new Point( location.x + spanX(), location.y + spanY(), location.z + spanZ() ) ;
        return corners ;
        }
    
    public boolean
    contains(Point p)
        {
        return (minX() <= p.x) && (p.x < maxX())
            && (minY() <= p.y) && (p.y < maxY())
            && (minZ() <= p.z) && (p.z < maxZ());
        }
    
    public boolean
    contains(Placement p)
        {
        Point[] corners = p.getCorners() ;
        for(int i=0 ; i<8 ; i++)
            if(!contains(corners[i]))
                return false ;
        return true ;
        }
    
    public boolean
    fitsInside(Point size)
        {
 //System.out.println("fitsInside span "+spanX()+" "+spanY()+" "+spanZ()) ;
 //System.out.println("fitsInside size "+size.x+" "+size.y+" "+size.z); 
        return ( (spanX() <= size.x) && (spanY() <= size.y) && (spanZ() <= size.z)) ;
        }
    
    public boolean
    overlap(Placement p)
        {
        Point[] corners ;
        boolean disjoint ;
        
        corners = p.getCorners() ;
        
        // > x
        disjoint = true ;
        for(int i=0 ; i<8 ; i++)
            if(corners[i].x < maxX())
                disjoint = false ;
        if(disjoint)
            return false ;
        
        // < x
        disjoint = true ;
        for(int i=0 ; i<8 ; i++)
            if(corners[i].x > minX())
                disjoint = false ;
        if(disjoint)
            return false ;
        
        // > y 
        disjoint = true ;
        for(int i=0 ; i<8 ; i++)
            if(corners[i].y < maxY())
                disjoint = false ;
        if(disjoint)
            return false;
        
        // < y
        disjoint = true ;
        for(int i=0 ; i<8 ; i++)
            if(corners[i].y > minY())
                disjoint = false ;
        if(disjoint)
            return false ;    
        
        // > z
        disjoint = true ;
        for(int i=0 ; i<8 ; i++)
            if(corners[i].z < maxZ())
                disjoint = false ;
        if(disjoint)
            return false ;
        
        // < z
        disjoint = true ;
        for(int i=0 ; i<8 ; i++)
            if(corners[i].z > minZ())
                disjoint = false ;
        if(disjoint)
            return false ;    
              
        return true ;
        }

    }
