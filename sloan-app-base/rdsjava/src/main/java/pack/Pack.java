package pack;

public class 
Pack
    {
    public static void 
    main(String[] args)
        {
        Job job = new Job("job 1") ;
        
        job.addContainer(new Box("A", new Point(8,4,3),20.0,10)) ;
        job.addContainer(new Box("C", new Point(32,10,18),20.0,10)) ;
        
        job.addContent(new Box("item 1",new Point(8,3,1),2.0)) ;   
        job.addContent(new Box("item 1",new Point(8,3,1),2.0)) ;  
        job.addContent(new Box("item 1",new Point(8,3,1),2.0)) ;
        job.addContent(new Box("item 1",new Point(8,3,1),2.0)) ;
        
        job.pack();
        
        for(Manifest m : job.manifests)
            m.list();
        
        }
    }
