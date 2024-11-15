package pack;

import java.util.ArrayList;

public class 
PlacementList 
extends ArrayList<Placement>
    {
    private static final long serialVersionUID = 1L;

    public PlacementList
    addPlacement(Placement place, PlacementList placements)
        {
        PlacementList list = new PlacementList() ;
        for(Placement old : placements)
            list.add(old) ;
        list.add(place) ;
        return list ;
        }
    }
