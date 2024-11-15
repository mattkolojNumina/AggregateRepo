package pack;

public class 
Evaluate
    {

    static public Point
    openSpaceAt(Point extreme, Box container, PlacementList placements)
       {
       return openSpaceAtOverlap(extreme,container,placements) ;
       // return openSpaceAtProject(extreme,container,placements) ;
       } 

    static public Point
    openSpaceAtOverlap(Point extreme, Box container, PlacementList placements) 
       {
       Point space = new Point(container.size.x-extreme.x,
                               container.size.y-extreme.y,
                               container.size.z-extreme.z) ;
       Box spaceBox = new Box("space",space,0.0) ;
       Placement openSpace = new Placement(spaceBox) ;
       openSpace.setLocation(extreme) ;

       for(Placement placement : placements)
          {
          if(placement.contains(openSpace.location))
             {
             openSpace.box.size.x = 0.0 ;
             openSpace.box.size.y = 0.0 ;
             openSpace.box.size.z = 0.0 ;
             break ;
             }
         
          if(openSpace.overlap(placement))
             {
             double bestVolume = 0.0 ;
             Point bestSpace = new Point(0.0,0.0,0.0) ;

             // check x
             if((openSpace.location.x<=placement.location.x) &&
                (placement.location.x<(openSpace.location.x+openSpace.spanX())))
                {
                Point trialSpace = new Point(openSpace.box.size) ;
                trialSpace.x = placement.location.x - openSpace.location.x ;
                double trialVolume = trialSpace.x*trialSpace.y*trialSpace.z ;
                if(bestVolume < trialVolume)
                   {
                   bestVolume = trialVolume ;
                   bestSpace = trialSpace ;
                   }                   
                }

             // check y
             if((openSpace.location.y<=placement.location.y) &&
                (placement.location.y<(openSpace.location.y+openSpace.spanY())))
                {
                Point trialSpace = new Point(openSpace.box.size) ;
                trialSpace.y = placement.location.y - openSpace.location.y ;
                double trialVolume = trialSpace.x*trialSpace.y*trialSpace.z ;
                if(bestVolume < trialVolume)
                   {
                   bestVolume = trialVolume ;
                   bestSpace = trialSpace ;
                   }                   
                }

             // check z
             if((openSpace.location.z<=placement.location.z) &&
                (placement.location.z<(openSpace.location.z+openSpace.spanZ())))
                {
                Point trialSpace = new Point(openSpace.box.size) ;
                trialSpace.z = placement.location.z - openSpace.location.z ;
                double trialVolume = trialSpace.x*trialSpace.y*trialSpace.z ;
                if(bestVolume < trialVolume)
                   {
                   bestVolume = trialVolume ;
                   bestSpace = trialSpace ;
                   }                   
                }

             openSpace.box.size = bestSpace ;
             if(bestVolume==0.0)
                break ;
             }
          }
        
       return openSpace.box.size ;
       }

    static public Point
    openSpaceAtProject(Point extreme, Box container, PlacementList placements)
        {
        Point space = new Point(container.size.x-extreme.x,
                                container.size.y-extreme.y,
                                container.size.z-extreme.z) ;
        
        for(Placement placement : placements)
            {
            if( (placement.minY() <= extreme.y) && (extreme.y < placement.maxY()) )
                if( (placement.minZ() <= extreme.z) && (extreme.z < placement.maxZ()) )
                   if( (placement.minX() - extreme.x) >= 0.0 )
                       if( (placement.minX() - extreme.x) < space.x )
                           space.x = (placement.minX() - extreme.x) ;
            
            if( (placement.minZ() <= extreme.z) && (extreme.z < placement.maxZ()) )
                if( (placement.minX() <= extreme.x) && (extreme.x < placement.maxX()) )
                   if( (placement.minY() - extreme.y) >= 0.0 )
                       if( (placement.minY() - extreme.y) < space.y )
                           space.y = (placement.minY() - extreme.y) ;
            
            if( (placement.minX() <= extreme.x) && (extreme.x < placement.maxX()) )
                if( (placement.minY() <= extreme.y) && (extreme.y < placement.maxY()) )
                   if( (placement.minZ() - extreme.z) >= 0.0 )
                       if( (placement.minZ() - extreme.z) < space.z )
                           space.z = (placement.minZ() - extreme.z) ;
            
            }
        
        return space ;
        }
    
    static public double
    evaluateMaxDiagonal(Box container, PlacementList placements, ExtremeList extremes)
        {
        double best = 0.0 ;
        
        for(Point extreme : extremes)
            {
            Point size = openSpaceAt(extreme,container,placements) ;
            double diagonal = size.norm();
            if(best<diagonal)
                best=diagonal ;
            }
        
        return best ;
        }
    
    static public double
    evaluateRMSDiagonal(Box container, PlacementList placements, ExtremeList extremes)
        {
        double sum = 0.0 ;
        double count = 0.0 ;
        double rms = 0.0 ;
        for(Point extreme : extremes)
            {
            Point size = openSpaceAt(extreme,container,placements) ;
            double diagonal = size.norm();
            sum += diagonal * diagonal ;
            count += 1.0 ;
            }
        
        if(count>0)
            rms = Math.sqrt(sum/count) ;
        
        return rms ;
        }
    
    static public double
    evaluateRMSVolume(Box container, PlacementList placements, ExtremeList extremes)
        {
        double sum = 0.0 ;
        double count = 0.0 ;
        double rms = 0.0 ;
        for(Point extreme : extremes)
            {
            Point size = openSpaceAt(extreme,container,placements) ;
            double volume = size.x * size.y * size.z;
            sum += volume * volume ;
            count += 1.0 ;
            }
        
        if(count>0)
            rms = Math.sqrt(sum/count) ;
        
        return rms ;
        }
    
    static public double
    evaluateGridVolume(Box container, PlacementList placements, ExtremeList extremes)
        {
        double best = 0.0 ;
        
        for(Point extreme : extremes)
            {
            Point size = openSpaceAt(extreme,container,placements) ;
            double volume = size.x * size.y * size.z ;

            if(best<volume)
                best=volume ;
            }
        
        if(placements.size()>0)
            {
            Placement current = placements.get(placements.size()-1) ;
            Point corner = current.location ;
            PlacementList allBut = new PlacementList() ;
            for(int i=0 ; i<(placements.size()-1) ; i++)
               allBut.add(placements.get(i)) ;
            Point inner = openSpaceAt(corner,container,allBut) ;
            int gridX = (int)(Math.floor(inner.x / current.spanX())) ;
            int gridY = (int)(Math.floor(inner.y / current.spanY())) ;
            int gridZ = (int)(Math.floor(inner.z / current.spanZ())) ;
            best += gridX * gridY * gridZ * current.box.getVolume() ;
            }
        
        return best ;
        }  
    
    static public double
    evaluateMaxVolume(Box container, PlacementList placements, ExtremeList extremes)
        {
        double best = 0.0 ;
        
        for(Point extreme : extremes)
            {
            Point size = openSpaceAt(extreme,container,placements) ;
            double volume = size.x * size.y * size.z ;

            if(best<volume)
                best=volume ;
            }
        
        return best ;
        }   
    
    static public double 
    evaluate(Box container, PlacementList placements, ExtremeList extremes)
        {
        //return evaluateRMSVolume(container,placements,extremes) ;
        //return evaluateRMSDiagonal(container,placements,extremes) ;
        //return evaluateMaxDiagonal(container,placements,extremes) ;
        //return evaluateMaxVolume(container,placements,extremes) ;
        return evaluateGridVolume(container,placements,extremes) ;
        }
    }
