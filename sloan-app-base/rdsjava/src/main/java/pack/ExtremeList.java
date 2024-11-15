package pack;

import java.util.ArrayList;

public class 
ExtremeList 
extends ArrayList<Point>
    {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean
    add(Point p)
        {
        for(Point x : this)
            if(x.equals(p))
                return true ;
        
        return super.add(p) ;
        }
    
    public ExtremeList
    addPlacement(Placement place, Box container, PlacementList placements)
        {
        ExtremeList list = new ExtremeList() ;
        for(Point old : this)
            if(!old.equals(place.location))
                list.add(old) ;
        
        Point corner ;
        double project ;
        
        // x point project y
        corner = new Point(place.maxX(),place.minY(),place.minZ()) ;
        project = place.minY() ;
        for(Placement placement : placements)
            if( (placement.minX() <= corner.x) && (corner.x < placement.maxX()))
                if( (placement.minZ() <= corner.z) && (corner.z < placement.maxZ()))
                   if( corner.y >= placement.maxY() )
                       if( project > (corner.y - placement.maxY()))
                           project = (corner.y - placement.maxY()) ;
        list.add(new Point(corner.x, corner.y-project, corner.z)) ;
        
        // x point project z
        corner = new Point(place.maxX(),place.minY(),place.minZ()) ;
        project = place.minZ() ;
        for(Placement placement : placements)
            if( (placement.minX() <= corner.x) && (corner.x < placement.maxX()))
                if( (placement.minY() <= corner.y) && (corner.y < placement.maxY()))
                   if( corner.z >= placement.maxZ() )
                       if( project > (corner.z - placement.maxZ()))
                           project = (corner.z - placement.maxZ()) ;
        list.add(new Point(corner.x, corner.y, corner.z-project)) ;    
        
        // y point project x
        corner = new Point(place.minX(),place.maxY(),place.minZ()) ;
        project = place.minX() ;
        for(Placement placement : placements)
            if( (placement.minY() <= corner.y) && (corner.y < placement.maxY()))
                if( (placement.minZ() <= corner.z) && (corner.z < placement.maxZ()))
                   if( corner.x >= placement.maxX() )
                       if( project > (corner.x - placement.maxX()))
                           project = (corner.x - placement.maxX()) ;
        list.add(new Point(corner.x-project, corner.y, corner.z)) ;   
        
        // y point project z
        corner = new Point(place.minX(),place.maxY(),place.minZ()) ;
        project = place.minZ() ;
        for(Placement placement : placements)
            if( (placement.minX() <= corner.x) && (corner.x < placement.maxX()))
                if( (placement.minY() <= corner.y) && (corner.y < placement.maxY()))
                   if( corner.z >= placement.maxZ() )
                       if( project > (corner.z - placement.maxZ()))
                           project = (corner.z - placement.maxZ()) ;
        list.add(new Point(corner.x, corner.y, corner.z-project)) ; 
        
        // z point project x
        corner = new Point(place.minX(),place.minY(),place.maxZ()) ;
        project = place.minX() ;
        for(Placement placement : placements)
            if( (placement.minY() <= corner.y) && (corner.y < placement.maxY()))
                if( (placement.minZ() <= corner.z) && (corner.z < placement.maxZ()))
                   if( corner.x >= placement.maxX() )
                       if( project > (corner.x - placement.maxX()))
                           project = (corner.x - placement.maxX()) ;
        list.add(new Point(corner.x-project, corner.y, corner.z)) ;  
        
        // z point project y
        corner = new Point(place.minX(),place.minY(),place.maxZ()) ;
        project = place.minY() ;
        for(Placement placement : placements)
            if( (placement.minX() <= corner.x) && (corner.x < placement.maxX()))
                if( (placement.minZ() <= corner.z) && (corner.z < placement.maxZ()))
                   if( corner.y >= placement.maxY() )
                       if( project > (corner.y - placement.maxY()))
                           project = (corner.y - placement.maxY()) ;
        list.add(new Point(corner.x, corner.y-project, corner.z)) ;   
                                
        return list ;
        }
    
    public void
    list()
        {
        for(Point p : this)
            {
            System.out.println("extreme "+p.x+" "+p.y+" "+p.z) ;
            }
        }
    }
