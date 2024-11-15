package pack;

import java.util.*;
import static rds.RDSLog.*;

public class 
Manifest
    {
    public String id ;
    public Box container = null ;
    public BoxList items = null ;
    public PlacementList placements = null ;
    public ExtremeList extremes = null ;
    public double weight ;
    public double volume ;
    public int count ;
    public boolean debug;
    public HashSet<String> skus;
    
    public
    Manifest(String id)
        {
        this.id = id ;
        items = new BoxList() ;
        placements = new PlacementList() ;
        extremes = new ExtremeList() ;
        weight = 0.0 ;
        count = 0 ;
        volume = 0.0 ;
        skus = new HashSet<>();
        
        extremes.add(new Point(0,0,0)) ;
        debug = false;
        }
    
    public void
    setContainer(Box container)
        {
        this.container = container ;
        }
    
    public void
    addContent(Box box)
        {
        items.add(box) ;
        }
   
    public void
    removeContent(Box box)
        {
        items.remove(box) ;
        }

    public Box
    getContainer()
      {
      return this.container ;
      }     

    public void
    debug(String message)
      {
        if (debug) inform("%s",message) ;
      }

    public void
    sort()
        {
        Collections.sort(items) ;
        }
    
        public double
    getVolume()
      {
      double volume = 0.0 ;
      for(Placement placement : placements)
        volume += placement.box.getVolumeValue() ;
      return volume ;
      }
    
    public double
    getFraction()
        {
        double fraction = 0.0 ;
        if(container.getVolume()>0.0)
            {
            for(Placement placement : placements)
                fraction += placement.box.getVolumeValue() ;
            fraction /= container.getVolume() ;
            }
        return fraction ;
        }
   
    public double
    getNetWeight()
      {
      double weight = 0.0 ;
      for(Placement placement : placements)
    	  weight += placement.box.getWeight() ;
      return weight ;
      }
    
    public double
    getWeight()
      {
      return container.getTare() + getNetWeight() ;	
      }
    
    
    public boolean
    checkOverlap()
       {
       boolean overlap = false ;
       for(int i=0 ; i<placements.size() ; i++)
          for(int j=i+1 ; j<placements.size() ; j++)
             if(placements.get(i).overlap(placements.get(j)))
                overlap = true ;
       return overlap ;
       }  
    
    public int
    getSkuCount() {
   	 return skus.size();
    }

    public void
    list()
        {
        debug("container "+container.id) ;
        debug("count "+count+"/"+container.count) ;
        debug("weight "+weight+"/"+container.weight) ;
        debug("fraction "+getFraction()) ;
        debug("container: "
             + container.size.x + " "
             + container.size.y  + " "
             + container.size.z ) ;
        for(Box i : items)
            {
            debug("unpacked: "+i.getId()) ;
            }
        for(Placement placement : placements)
            {
            debug("packed: "+placement.box.getId()) ;
            debug("    at: "+placement.location.x+" "+placement.location.y+" "+placement.location.z) ;
            debug("   rot: "+placement.rotation) ;
            debug("  span: "+placement.spanX()+" "+placement.spanY()+" "+placement.spanZ());
            
            }
        for(Point extreme : extremes)
            {
            debug("extreme: "+extreme.x+" "+extreme.y+" "+extreme.z) ;
            }
        debug("") ;
        }
    
    public boolean
    unpack()
        {
        for(Placement p : placements)
            items.add(p.box) ;
        placements = new PlacementList() ;
        extremes = new ExtremeList() ;
        weight = 0.0 ;
        count = 0 ;     
        extremes.add(new Point(0,0,0)) ;
        
        return true ;
        }
    
    public boolean
    pack()
        {
        BoxList remains = new BoxList() ;
       
        Collections.sort(items) ;
        for(Box item : items)
            {
            double best = 0.0 ;
            ExtremeList bestExtremes = null ;
            PlacementList bestPlacements = null ;
            
            for(Point extreme : extremes)
                {
                for(Placement.Rotation rotation : Placement.Rotation.values())
                    {
                    Placement place = new Placement(item) ;
                    place.setRotation(rotation) ;
                    place.setLocation(extreme) ;

                    if(place.fitsInside(Evaluate.openSpaceAt(extreme, container, placements)))
                        {
                        ExtremeList trialExtremes = extremes.addPlacement(place, container, placements) ;
                        PlacementList trialPlacements = placements.addPlacement(place,placements) ;
                        double quality = Evaluate.evaluate(container, trialPlacements, trialExtremes) ;

                        if(best <= quality)
                            {
                            best = quality ;
                            bestExtremes = trialExtremes ;
                            bestPlacements = trialPlacements ;
                            }
                        }
                    }
                }
          
 
            if( ( bestExtremes   != null ) 
             && ( bestPlacements != null ) 
             && ( ( container.count  ==   0 ) || ( (count+1                ) <= (container.count                        ) ) )
             && ( ( container.weight == 0.0 ) || ( (weight+item.weight     ) <= (container.weight-container.tare        ) ) ) 
             && ( ( container.fill   == 0.0 ) || ( (volume+item.getVolume()) <= (container.fill * container.getVolume() ) ) ) )
                {
                extremes   = bestExtremes ;
                placements = bestPlacements ;
                count     += 1 ;
                weight    += item.weight ;
                volume    += item.getVolume();
                }
            else
                remains.add(item) ;
            }
        
        items = remains ;
        
        return items.isEmpty() ;
        }
       
    public boolean
    isEmpty()
        {
        return placements.isEmpty() ;
        }

    public void setDebug(boolean debug) 
        {
        this.debug = debug;      
        }
       
    }
