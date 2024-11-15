package pack;

import java.util.ArrayList;

public class 
Box
implements Comparable<Box>
    {
    public String id ;
    public String sku ;
    public String buyingDepartment = "";
    public String orderId;
    public int orderLineSeq = -1;
    public Point size ;
    public Point delta ;
    public double tare ;
    public double weight ;
    public double volume ;
    public double lineTotalVolume = 0;
    public double lineTotalWeight = 0;
    public int lineTotalQty = 0;
    public int minSplitLineQty = 0;
    public double buyingDepartmentTotalVolume = 0;
    public double buyingDepartmentTotalWeight = 0;
    public int count ;
    public double fill ;
    public boolean fullcaseShippable;
    public boolean isFluid ;
    public boolean isNest;
    public boolean isStack;
    public boolean selected;
    public ArrayList<Box> boxes;
 
    public 
    Box()
        {
        this.id       = "" ;
        this.sku      = "" ;
        this.size     = new Point() ;
        this.delta    = new Point() ;
        this.tare     = 0.0 ;
        this.weight   = 0.0 ;
        this.volume   = 0.0 ;
        this.count    = 0 ;
        this.fill     = 0.0 ;
        this.fullcaseShippable = false ;
        this.isFluid  = false ;
        this.isNest = false ;
        this.isStack = false;
        }
    
    public
    Box(String id, Point size)
       {
       this.id        = id ;
       this.sku       = "" ;
       this.size      = size ;
       this.delta    = new Point() ;
       this.tare      = 0.0 ;
       this.weight    = 0.0 ;
       this.count     = 0 ;
       this.fill      = 0.0 ;
       this.fullcaseShippable = false ;
       this.isFluid  = false ;
       this.isNest = false ;
       this.isStack = false;
       }
    
    public
    Box(String id, double weight, double volume, String orderId, String buyingDepartment, int orderLineSeq )
       {
       this.id        = id ;
       this.sku       = "" ;
       this.delta    = new Point() ;
       this.tare      = 0.0 ;
       this.weight    = weight ;
       this.volume    = volume ;
       this.lineTotalVolume = 0.0 ;
       this.buyingDepartmentTotalVolume = 0.0;
       this.lineTotalQty = 0;
       this.minSplitLineQty = 0;
       this.count     = 0 ;
       this.fill      = 0.0 ;
       this.fullcaseShippable = false ;
       this.isFluid  = false ;
       this.isNest = false ;
       this.isStack = false;
       this.orderId = orderId;
       this.buyingDepartment = buyingDepartment;
       this.orderLineSeq = orderLineSeq;
       }
    
    public 
    Box(String id, Point size, double weight)
        {
        this.id       = id ;
        this.sku      = "" ;
        this.size     = size ;
        this.delta    = new Point() ;
        this.weight   = weight ;
        this.count    = 0 ;
        this.tare     = 0.0 ;
        this.fill     = 0.0 ;
        this.fullcaseShippable = false ;
        this.isFluid  = false ;
        this.isNest = false ;
        this.isStack = false;
        }

    public 
    Box(String id, Point size, double weight, int count)
        {
        this.id       = id ;
        this.sku      = "" ;
        this.size     = size ;
        this.delta    = new Point() ;
        this.weight   = weight ;
        this.count    = count ;
        this.tare     = 0.0 ;
        this.fill     = 0.0 ;
        this.fullcaseShippable = false ;
        this.isFluid  = false ;
        this.isNest = false ;
        this.isStack = false;
        }     

    public 
    Box(String id, Point size, Point delta, double weight)
        {
        this.id       = id ;
        this.sku      = "" ;
        this.size     = size ;
        this.delta    = delta ;
        this.delta    = new Point() ;
        this.weight   = weight ;
        this.count    = 0 ;
        this.tare     = 0.0 ;
        this.fill     = 0.0 ;
        this.fullcaseShippable = false ;
        this.isFluid  = false ;
        this.isNest = false ;
        this.isStack = false;
        }
    
    public void
    setId(String id)
      {
      this.id = id ;
      }
  
    public void
    setSku(String sku)
      {
      this.sku = sku ;
      }
 
    public void
    setSize(Point s)
      {
      this.size = s ;
      }
    
    public void
    setWeight(double weight)
      {
      this.weight = weight ;
      }

    public void
    setTare(double tare)
     {
     this.tare = tare ;	
     }
    
    public void
    setFill(double fill)
     {
     this.fill = fill ;
     }
    
    public void
    setCount(int count)
      {
      this.count = count ;
      }
    
    public void
    setLineTotalVolume( double volume ) {
   	 this.lineTotalVolume = volume;
    }
    
    public void
    setBuyingDepartmentTotalVolume( double volume ) {
   	 this.buyingDepartmentTotalVolume = volume;
    }

    public void
    setFullcaseShippable(boolean fullcaseShippable)
      {
      this.fullcaseShippable = fullcaseShippable ;
      }

    public void
    setFluid(boolean isFluid)
      {
      this.isFluid = isFluid ;
      }
    
    public void
    setNestting(boolean isNestting) {
   	 this.isNest = isNestting;
    }

    public void
    checkFluid(double maxFluid)
      {
      if(getVolume()<maxFluid)
        isFluid = true ;
      }
    
    public void
    addBox(Box box) {
   	 this.boxes.add(box);
    }
      
    public String
    getId()
        {
        return this.id ;
        }
    
    public Point
    getSize()
        {
        return this.size ;
        }
    
    public double
    getVolume()
        {
   	  if( this.size==null )
   		  return this.volume;
        return size.x * size.y * size.z ;
        }
    
    public double
    getVolumeValue() {
   	 return this.volume;
    }
    
    public double
    getLineTotalVolume() {
   	 return this.lineTotalVolume;
    }
    
    public double
    getBuyingDepartmentTotalVolume() {
   	 return this.buyingDepartmentTotalVolume;
    }    
    
    public int
    getOrderLineSeq() {
   	 return this.orderLineSeq;
    }
    
    public String
    getBuyingDepartment() {
   	 return this.buyingDepartment;
    }
    
    public double
    getWeight()
        {
        return this.weight ;
        }

    public double
    getTare()
       {
       return this.tare ;
       }
    
    public int
    getCount()
        {
        return this.count ;
        }
    
    public double
    getFill()
       {
       return this.fill ;
       }

    public boolean
    getFullcaseShippable()
       {
       return this.fullcaseShippable ;
       }

    
    @Override
    public int compareTo(Box b)
        {
   	      if( buyingDepartmentTotalWeight <= 48 && b.buyingDepartmentTotalWeight > 48 ) return 1;
   	      if( buyingDepartmentTotalWeight > 48 && b.buyingDepartmentTotalWeight <=48 ) return -1;
   	      if( lineTotalWeight <=48 && b.lineTotalWeight > 48 ) return 1;
   	      if( lineTotalWeight >48 && b.lineTotalWeight <= 48 ) return -1;
   	 		if(buyingDepartmentTotalVolume < b.buyingDepartmentTotalVolume ) return 1;
   	 		if(buyingDepartmentTotalVolume > b.buyingDepartmentTotalVolume ) return -1;
   	 		int result = buyingDepartment.compareTo(b.buyingDepartment);
   	 		if( result !=0 ) return result;
   	 		if(lineTotalVolume < b.lineTotalVolume ) return 1;
   	 		if(lineTotalVolume > b.lineTotalVolume ) return -1;
   	 		if( orderLineSeq < b.orderLineSeq ) return -1;
   	 		if( orderLineSeq > b.orderLineSeq ) return 1;
   	 		if( volume < b.volume ) return 1;
   	 		if( volume > b.volume ) return -1;
   	 		if( weight < b.weight ) return 1;
   	 		if( weight > b.weight ) return -1;
   	 		return 0;
        }
    }
