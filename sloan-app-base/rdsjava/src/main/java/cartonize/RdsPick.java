package cartonize;

import java.util.Map;

import pack.Point;
import rds.RDSUtil;
import static rds.RDSLog.*;

public class RdsPick implements Comparable<RdsPick> {
   private int pickSeq;
   private int orderLineSeq;
   private String sku;
   private String uom;
   private Point size;
   private double weight;
   private double volume;
   private String buyingDepartment;
   private String orderId;
   private String exception;
   private String solution;
   private int qty;

   public RdsPick(Map<String, String> p) {
      this.pickSeq = getMapInt(p, "pickSeq");
      this.sku = getMapStr(p, "sku");
      this.uom = getMapStr(p, "pickUom");
      this.orderLineSeq = getMapInt(p,"orderLineSeq");
      this.exception = "";
      this.solution = "";
      double maxV = getMapDbl(p,"maxV")-1;
      double maxW = getMapDbl(p,"maxW");
      double length = getMapDbl(p,"length");
      double defaultLength = getMapDbl(p,"defaultLength");
      int picked = getMapInt(p,"picked");
      if( length == 0 ) {
      	length = defaultLength;
      	alert("Use default length %.2f for sku %s", length, this.sku);
      	this.exception += "Invalid length 0. ";
      	this.solution += String.format("Use default length [%.2f]. ", length);
      }
      double width = getMapDbl(p,"width");
      double defaultWidth = getMapDbl(p,"defaultWidth");
      if( width == 0 ) {
      	width = defaultWidth;
      	alert("Use default width %.2f for sku %s", width, this.sku);
      	this.exception += "Invalid Width 0. ";
      	this.solution += String.format("Use default width [%.2f]. ", width);
      }      
      double height = getMapDbl(p,"height");
      double defaultHeight = getMapDbl(p,"defaultHeight");
      if( height == 0 ) {
      	height = defaultHeight;
      	alert("Use default height %.2f for sku %s", height, this.sku);
      	this.exception += "Invalid Height 0. ";
      	this.solution += String.format("Use default height [%.2f]. ", height);
      }        
      int cubicDivisorint = getMapInt(p,"cubicDivisorint");
      int qtyShelfPack = getMapInt(p,"qtyShelfPack");
      int qty = (int) getMapDbl(p,"qty");
      this.qty = qty;
      //this.shipAlone= getMapInt(p, "shipAlone");
      //this.shippable= getMapInt(p, "shippable");
      Point size = new Point(
            length, 
            width, 
            height);
      double unitWeight = getMapDbl(p,"weight");
      if( unitWeight == 0 ) {
      	unitWeight = getMapDbl(p,"defaultWeight");
      	alert("Use default weight %.2f for sku %s", unitWeight, this.sku); 
      	this.exception += "Invalid Weight 0. ";
      	this.solution += String.format("Use default weight [%.2f]. ", unitWeight);
      }
      this.weight = unitWeight * qty;
      this.volume = length*width*height*qty/cubicDivisorint;
      if( picked == 1 ) {
      	inform("Use minimum dim and weight for marked out picks");
      	this.volume = 0.1;
      	this.weight = 0.1;
      }
      if( this.weight > maxW ) {
      	this.weight = maxW;
      	if( qty > 1 ) {
      		this.exception += String.format("ShelfPack exceeds max allowed weight [%.2f].", maxW);
      		this.solution += String.format("Use max allowed weight [%.2f].", maxW);
      	} else {
      		this.exception += String.format("%s exceeds max allowed weight [%.2f].", this.uom, maxW);
      		this.solution += String.format("Use max allowed weight [%.2f].", maxW);
      	}
      	
      }
      if( this.volume > maxV ) {
      	if( qty > 1 ) {
      		if( cubicDivisorint == 1 && this.volume/qty <= maxV ) {
      			this.exception += String.format("ShelfPack exceeds max allowed volume [%.2f].", maxV);
      			this.solution += String.format("Overwrite cubicDivisorint from 1 to %d.", qty);
      			this.volume /= qty;
      		} else {
      			this.exception += String.format("ShelfPack exceeds max allowed volume [%.2f].", maxV);
      			double defaultV = defaultLength*defaultWidth*defaultHeight*qty/cubicDivisorint;
      			if( defaultV <= maxV ) {
      				this.solution += String.format("Use default dim [%.2fx%.2fx%.2f].", defaultLength, defaultWidth, defaultHeight);
      				this.volume = defaultV;
      			} else {
      				this.solution += String.format("Use max allowed volume [%.2f].", maxV);
      				this.volume = maxV;
      			}
      		}
      	} else {
      		if( cubicDivisorint == 1 && qtyShelfPack > 1 && this.volume/qtyShelfPack <= maxV ) {
      			this.exception += String.format("%s exceeds max allowed volume [%.2f].", this.uom, maxV);
      			this.solution += String.format("Overwrite cubicDivisorint from 1 to %d.", qtyShelfPack);
      			this.volume /= qtyShelfPack;
      		} else {
      			this.exception += String.format("%s exceeds max allowed volume [%.2f].", this.uom, maxV);
      			double defaultV = defaultLength*defaultWidth*defaultHeight/cubicDivisorint;
      			if( defaultV <= maxV ) {
      				this.solution += String.format("Use default dim [%.2fx%.2fx%.2f].", defaultLength, defaultWidth, defaultHeight);
      				this.volume = defaultV;
      			} else {
      				this.solution += String.format("Use max allowed volume [%.2f].", maxV);
      				this.volume = maxV;
      			}
      		}
      	}
      }
      size.order();
      this.size = size;
      this.buyingDepartment = getMapStr(p,"buyingDepartment");
      this.orderId = getMapStr(p,"orderId");
   }

   public void setPickSeq(int pickSeq) {
      this.pickSeq = pickSeq;
   }

   public void setSku(String sku) {
      this.sku = sku;
   }

   public void setSize(Point s) {
      this.size = s;
   }

   public void setWeight(double weight) {
      this.weight = weight;
   }

   public int getPickSeq() {
      return this.pickSeq;
   }
   
   public int getOrderLineSeq() {
   	return this.orderLineSeq;
   }
   
   public String getSku() {
      return this.sku;
   }
   
   public String getUom() {
      return this.uom;
   }
   
   public Point getSize() {
      return this.size;
   }

   public double getVolume() {
      //return size.x * size.y * size.z;
   	return this.volume;
   }
   
   public String getBuyingDepartment() {
   	return this.buyingDepartment;
   }
   
   public String getOrderId() {
   	return this.orderId;
   }
   
   public double getLength() {
      return size.x;
   }
   
   public double getWidth() {
      return size.y;
   }
   
   public double getHeight() {
      return size.z;
   }

   public double getWeight() {
      return this.weight;
   }
   
   public String getException() {
   	return this.exception;
   }
   
   public String getSolution() {
   	return this.solution;
   }   
   
   public int getQty() {
   	return this.qty;
   }
   
   /*
   public boolean isCase() {
      return CartonizeApp.UOM_CASE.equals(this.uom);
   }

   public boolean isEach() {
      return CartonizeApp.UOM_EACH.equals(this.uom);
   }*/

   /*
   public boolean isShipAlone() {
      return this.shipAlone==1;
   }*/
   
   /*
   public boolean isShippable() {
      return this.shippable==1;
   }*/  
   
   @Override
   public int compareTo(RdsPick b) {
      if (getVolume() < b.getVolume())
         return 1;
      else if (getVolume() > b.getVolume())
         return -1;
      return 0;
   }
   

   /** Gets a value from a {@code Map} or an empty string. */
   protected static String getMapStr( Map<String,String> m, String name ) {
      if (m == null)
         return "";
      String v = m.get( name );
      return (v == null) ? "" : v;
   }

   /** Gets a value from a {@code Map} and converts it to an int. */
   protected static int getMapInt( Map<String,String> m, String name ) {
      if (m == null)
         return -1;
      return RDSUtil.stringToInt( m.get( name ), -1 );
   }

   /** Gets a value from a {@code Map} and converts it to a double. */
   protected static double getMapDbl( Map<String,String> m, String name ) {
      if (m == null)
         return 0.0;
      return RDSUtil.stringToDouble( m.get( name ), 0.0 );
   }

}
