package pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import static rds.RDSLog.*;

public class Job {
   public static final int DEFAULT_TIMEOUT_MINUTES = 60 ; // minute
   public static final long DEFAULT_TIMEOUT_MSEC = DEFAULT_TIMEOUT_MINUTES * 60 * 1000 ; // msec
   public static final int ERROR_DEFAULT = -1;
   public static final int ERROR_BAD_SKU = -2;
   public static final int ERROR_TIMEOUT = -3;
   
   private long start, timeout; 
   public int errorType;
   
   public String seq = null;
   public BoxList items = null;
   public BoxList containers = null;
   public ManifestList manifests = null;
   public double maxFluid = 0.0;
   public double maxFill = 1.0;
   public double singleLineFill = 0.8;
   public boolean debug;
   public int maxSkuCount = 8;
   

   public Job(String seq) {
      this.seq = seq;
      items = new BoxList();
      containers = new BoxList();
      manifests = new ManifestList();
      debug = false;
      timeout = DEFAULT_TIMEOUT_MSEC;
      errorType = 0;
   }

   public void addContainer(Box container) {
      containers.add(container);
   }

   public void addContent(Box box) {
      items.add(box);
      box.checkFluid(maxFluid);
   }

   public void setMaxFluid(double maxFluid) {
      this.maxFluid = maxFluid;
   }

   public void setMaxFill(double maxFill) {
      this.maxFill = maxFill;
   }
   
   public void setMaxSkuCount(int maxSkuCount) {
      this.maxSkuCount = maxSkuCount;
   }
   
   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public void debug(String message) {
      if (debug)
         inform("%s",message) ;
   }

   public void setTimeout(long timeout) {
      this.timeout = timeout;
      if (this.timeout <= 0)
         this.timeout = DEFAULT_TIMEOUT_MSEC;
   }
   
   public boolean unpack() {
      for (Manifest m : manifests)
         for (Placement p : m.placements)
            items.add(p.box);
      manifests = new ManifestList();

      return true;
   }

   public boolean pack() {
      try {
         return doPack();
      } catch (TimeoutException ex) {
         alert("timeout");
         errorType = ERROR_TIMEOUT;
         return false;
      } catch (Exception ex) {
         alert("error: %s", ex.toString());
         ex.printStackTrace();
         errorType = ERROR_DEFAULT;
         return false;
      }
   }
   
   private boolean doPack() throws TimeoutException {
      BoxList remains = new BoxList();
      int manifestSequence = 0;
      Collections.sort(containers);
      Collections.sort(items);
      start = System.currentTimeMillis();
      
      Iterator<Box> itemIterator = items.iterator();
      while(itemIterator.hasNext())
      {
         Box item = itemIterator.next();
         if (!item.fullcaseShippable)
            continue;
         if (isCartonizable(item))
            continue;
         debug("unit item "+item.sku + " cannot cartonize, unit ship") ;
         manifestSequence = fullcaseShip(item, manifestSequence);
         itemIterator.remove();
      }
      if (items.isEmpty()) return true; 
      
      Map<String,Double> bdVolume = new HashMap<>();
      Map<String,Double> bdWeight = new HashMap<>();
      Map<Integer,Double> lineVolume = new HashMap<>();
      Map<Integer,Double> lineWeight = new HashMap<>();
      Map<Integer,Integer> lineTotalQty = new HashMap<>();
      Map<Integer,Integer> maxSingleLineQty = new HashMap<>();
      Map<Integer,Integer> minSplitLineQty = new HashMap<>();
      double totalVolume = 0.0;
      double totalWeight = 0.0;
      
      // step zero, update lineTotalVolume and buyingDepartmentTotalVolume
      for (int i = 0; i < items.size(); i++) {
      	String bd = items.get(i).buyingDepartment;
      	int line = items.get(i).orderLineSeq;
      	if( bdVolume.containsKey(bd) ) {
      		bdVolume.put(bd, bdVolume.get(bd)+items.get(i).volume);
      		bdWeight.put(bd, bdWeight.get(bd)+items.get(i).weight);
      	} else {
      		bdVolume.put(bd, items.get(i).volume);
      		bdWeight.put(bd, items.get(i).weight);
      	}
      	if( lineVolume.containsKey(line) ) {
      		lineVolume.put(line, lineVolume.get(line)+items.get(i).volume);
      		lineWeight.put(line, lineWeight.get(line)+items.get(i).weight);
      		lineTotalQty.put(line, lineTotalQty.get(line)+1);
      	} else {
      		lineVolume.put(line, items.get(i).volume);
      		lineWeight.put(line, items.get(i).weight);
      		lineTotalQty.put(line, 1);
      		maxSingleLineQty.put(line, maxSingleLineQty(items.get(i)));
      	}
      	totalVolume += items.get(i).volume;
      	totalWeight += items.get(i).weight;
      }
      
      for( int lineSeq : lineTotalQty.keySet() ) {
      	int totalQty = lineTotalQty.get(lineSeq);
      	int maxQty = maxSingleLineQty.get(lineSeq);
      	int splitQty = totalQty % maxQty;
      	if( splitQty == 0 )
      		splitQty = maxQty;
      	minSplitLineQty.put(lineSeq, splitQty);
      }
      
      while (!items.isEmpty()) {
         if (items.isEmpty())
            return true;
         for( int i = 0; i < items.size(); i++ ) {
         	String bd = items.get(i).buyingDepartment;
         	int line = items.get(i).orderLineSeq;
         	items.get(i).buyingDepartmentTotalVolume = bdVolume.get(bd);
         	items.get(i).buyingDepartmentTotalWeight = bdWeight.get(bd);
         	items.get(i).lineTotalVolume = lineVolume.get(line);
         	items.get(i).lineTotalWeight = lineWeight.get(line);
         	items.get(i).lineTotalQty = lineTotalQty.get(line);
         	/*
         	debug(String.format("pickSeq %s, sku %s, bd %s, line %d, weight %.2f, volume %.2f, bdV %.2f, bdW %.2f, lineV %.2f, lineW %.2f, "
         			+ "lineTotalQty %d, minSplitQty %d",
         			items.get(i).id,items.get(i).sku,
         			items.get(i).buyingDepartment,items.get(i).orderLineSeq,
         			items.get(i).weight,items.get(i).volume,
         			items.get(i).buyingDepartmentTotalVolume,items.get(i).buyingDepartmentTotalWeight,
         			items.get(i).lineTotalVolume,items.get(i).lineTotalWeight,
         			items.get(i).lineTotalQty, minSplitLineQty.get(line)));
         			*/
         }
         Collections.sort(items);
         int bestCount = 0;
         double bestFraction = 0.0;
         Manifest bestManifest = null;

      	boolean firstContainer = true;
         for (Box trialContainer : containers) {
            if (trialContainer.count > 0)
               if (trialContainer.count < bestCount) {
                  debug("  " + trialContainer.id + " cannot hold more, skip");
                  continue;
               }
            
            /*
             * don't use canFit since dimension is not accurate
            if (!canFit(items.get(0), trialContainer )) {
               debug("  " + trialContainer.id + " cannot hold first, skip");
               continue;
            }*/
            
            int trialCount = 0;
            double trialFraction = 0.0;
            Manifest trialManifest = new Manifest(trialContainer.id);
            trialManifest.setDebug(debug);

            debug("  try " + trialContainer.id);
            trialManifest.setContainer(trialContainer);
            setMaxFill(trialContainer.fill);
            double volume = trialManifest.getVolume();
            double weight = 0;
            double remainingItemVolume = totalVolume;
            double remainingItemWeight = totalWeight;
            
         	Map<String,Double> bdVolume_copy = new HashMap<>(bdVolume);
         	Map<String,Double> bdWeight_copy = new HashMap<>(bdWeight);
         	Map<Integer,Double> lineVolume_copy = new HashMap<>(lineVolume);
         	Map<Integer,Double> lineWeight_copy = new HashMap<>(lineWeight);
         	Map<Integer,Integer> lineTotalQty_copy = new HashMap<>(lineTotalQty);
            
            // no non-fluid pack
            /*
            for (int i = 0; i < items.size(); i++) {
               checkTime();
               if (items.get(i).isFluid)
                  continue;
               debug("    item " + items.get(i).sku);
               if (canFit(items.get(i), trialManifest.getContainer())) {
                  int restriction = restricted(items.get(i), trialManifest);
                  if (restriction == 0) {
                     if ( items.get(i).isNest ) {
                     	int start = i;
                     	int stop = i;
                     	for( int j=start+1;j<items.size();j++ ) {
                     		if( !items.get(j).isNest ) break;
                     		if( !items.get(j).sku.equals(items.get(i).sku)) break;
                     		stop = j;
                     	}
                     	while( start<=stop ) {
                     		Box nestBox = createNextBox( items.get(start), start, stop );
                     		for( int j=start;j<=stop;j++ ) {
                     			nestBox.addBox(items.get(j));
                     		}
   	                     if (maxFill == 0 || (volume + nestBox.getVolume()) < (maxFill * trialContainer.getVolume())) {
   	                        if (trialContainer.getWeight() == 0 || (weight + nestBox.getWeight()) < (trialContainer.getWeight())) {
   	                           if ((trialContainer.count == 0) || ((trialCount + nestBox.count) <= trialContainer.count)) {
   	                              trialManifest.addContent(nestBox);
   	                              if (trialManifest.pack()) {
   	                                 trialCount+=nestBox.count;
   	                                 break;
   	                              } else
   	                                 trialManifest.removeContent(nestBox);
   	                           } else
   	                              debug("      too many");
   	                        } else
   	                           debug("      too heavy");
   	                     } else
   	                        debug("      too big");                     		
                     		if( stop>start )
                     			stop--;
                     		else
                     			break;
                     	}
                     	i=stop;
                     } else {
	                     if (maxFill == 0 || (volume + items.get(i).getVolume()) < (maxFill * trialContainer.getVolume())) {
	                        if (trialContainer.getWeight() == 0 || (weight + items.get(i).getWeight()) < (trialContainer.getWeight())) {
	                           if ((trialContainer.count == 0) || ((trialCount + 1) <= trialContainer.count)) {
	                              trialManifest.addContent(items.get(i));
	                              if (trialManifest.pack())
	                                 trialCount++;
	                              else
	                                 trialManifest.removeContent(items.get(i));
	                           } else
	                              debug("      too many");
	                        } else
	                           debug("      too heavy");
	                     } else
	                        debug("      too big");
                     }
                  } else
                     debug("      restriction " + restriction);
               } else
                  debug("      never fit");
            }*/
            
            //fluid
            boolean hasFirstBd = false;
            boolean fitBd = false;
            boolean fitLine = false;
            String firstBd = "";
            String currentBd = "";
            int currentLine = -1;
            for (int i = 0; i < items.size(); i++) {
            	items.get(i).selected = false;
            }
            
            // step 1 fill entire bds or 1 bd (line level) that could not fit in 1 box
            for (int i = 0; i < items.size(); i++) {
               checkTime();
               int restriction = restricted(items.get(i), trialManifest, firstContainer);
               if (restriction == 0) {
                  if (maxFill == 0 || (volume + items.get(i).volume) <= (maxFill * trialContainer.getVolume())) {
                     if (trialContainer.getWeight() == 0 || (weight + items.get(i).weight) <= (trialContainer.getWeight()-trialContainer.tare)) {
                        if ((trialContainer.count == 0) || ((trialCount + 1) <= trialContainer.count)) {
                        	String thisBd = items.get(i).buyingDepartment;
                        	int thisLine = items.get(i).orderLineSeq;
                        	//inform("step 1: fit sku %s, bd %s, line %d", items.get(i).sku, thisBd, thisLine);
                        	if( !hasFirstBd ) {
                        		firstBd = thisBd;
                        		currentBd = thisBd;
                        		currentLine = thisLine;
                        		hasFirstBd = true;
                        		if( (bdVolume_copy.get(currentBd) + volume <= (maxFill * trialContainer.getVolume())) &&  
                        			 (bdWeight_copy.get(currentBd) + weight <= (trialContainer.getWeight()-trialContainer.tare))) {
                        			fitBd = true;
                        			fitLine = true;
                        		} else {
                           		if( (lineVolume_copy.get(currentLine) + volume <= (maxFill * trialContainer.getVolume())) &&  
                             			 (lineWeight_copy.get(currentLine) + weight <= (trialContainer.getWeight()-trialContainer.tare))) {   
                           			fitLine = true;
                           		} else 
                           			fitLine = false;
                        		}
                        	}
                        	if( thisBd.equals(currentBd) ) {
                        		if( thisLine!=currentLine ) {
                           		if( !(lineVolume_copy.get(thisLine) + volume <= (maxFill * trialContainer.getVolume())) ||  
                               			 !(lineWeight_copy.get(thisLine) + weight <= (trialContainer.getWeight()-trialContainer.tare))) 
                             			continue;
                           		else
                           			currentLine = thisLine;
                        		}
                        	} else if(fitBd){
                        		if( !(bdVolume_copy.get(thisBd) + volume <= (maxFill * trialContainer.getVolume())) ||  
                          			 !(bdWeight_copy.get(thisBd) + weight <= (trialContainer.getWeight()-trialContainer.tare))) 
                        			continue;
                        		else {
                        			currentBd = thisBd;
                        			currentLine = thisLine;
                        		}
                        	} else {
                        		if( (bdVolume_copy.get(currentBd) <= (maxFill * trialContainer.getVolume())) &&  
                          			 (bdWeight_copy.get(currentBd) <= (trialContainer.getWeight()-trialContainer.tare))) {
                           		if( !(bdVolume_copy.get(thisBd) + volume <= (maxFill * trialContainer.getVolume())) ||  
                               			 !(bdWeight_copy.get(thisBd) + weight <= (trialContainer.getWeight()-trialContainer.tare))) 
                             			continue;
                             		else {
                             			currentBd = thisBd;
                             			currentLine = thisLine;
                             			fitBd = true;
                             		}                       		
                        		} else 
                        			continue;
                        	}
                        	debug(String.format("fit pickSeq %s, item %s", items.get(i).id,items.get(i).sku));
                           trialManifest.addContent(items.get(i));
                           trialManifest.skus.add(items.get(i).sku);
                           Placement place = new Placement(items.get(i));
                           place.setLocation(new Point(0, 0, trialContainer.size.z));
                           trialManifest.placements.add(place);
                           double thisVolume = items.get(i).volume;
                           double thisWeight = items.get(i).weight;
                           volume += thisVolume;
                           weight += thisWeight;
                           items.get(i).selected = true;
                           remainingItemVolume -= thisVolume;
                           remainingItemWeight -= thisWeight;
                           bdVolume_copy.put(thisBd, bdVolume_copy.get(thisBd)-thisVolume);
                           bdWeight_copy.put(thisBd, bdWeight_copy.get(thisBd)-thisWeight);
                           lineVolume_copy.put(thisLine, lineVolume_copy.get(thisLine)-thisVolume);
                           lineWeight_copy.put(thisLine, lineWeight_copy.get(thisLine)-thisWeight);
                           lineTotalQty_copy.put(thisLine, lineTotalQty_copy.get(thisLine)-1);
                           trialCount++;
                        } else
                           debug("      too many");
                     } else
                        debug("      too heavy");
                  } else
                     debug("      too big");
               } else
                  debug("      restriction " + restriction);
            }
            
            // step 2 if step 1 fills 1 bd that couldn't fit in 1 box, and remaining item couldn't fit in 1 box, fill the same bd item not at line level
            if( !fitBd && !currentBd.isEmpty() ) {
            	//inform("step 2: currentbd %s", currentBd);
            	if( !(bdVolume_copy.get(currentBd) <= (maxFill * trialContainer.getVolume())) ||  
             		 !(bdWeight_copy.get(currentBd) <= (trialContainer.getWeight()-trialContainer.tare))) {
	               for (int i = items.size()-1; i >=0; i--) {
	                  checkTime();
	                  if( items.get(i).selected ) continue;
	                  int restriction = restricted(items.get(i), trialManifest, firstContainer);
	                  if (restriction == 0 ) {
                     	String thisBd = items.get(i).buyingDepartment;
                     	if( !thisBd.equals(currentBd) ) continue;
                     	int thisLine = items.get(i).orderLineSeq;	   
                     	int lineQty = lineTotalQty_copy.get(thisLine);
                     	int splitLineQty = minSplitLineQty.get(thisLine);
                     	int minRequiredQty = Math.min(splitLineQty, lineQty);
	                     if (maxFill == 0 || (volume + items.get(i).volume*minRequiredQty) <= (maxFill * trialContainer.getVolume())) {
	                        if (trialContainer.getWeight() == 0 || (weight + items.get(i).weight*minRequiredQty) <= (trialContainer.getWeight()-trialContainer.tare)) {
	                           if ((trialContainer.count == 0) || ((trialCount + minRequiredQty) <= trialContainer.count)) {
	                           	debug(String.format("step 2: fit %d split line item %s", minRequiredQty, items.get(i).sku));
	                           	for( int j=0; j<minRequiredQty; j++ ) {
	                           		debug(String.format("fit pickSeq %s, item %s", items.get(i-j).id,items.get(i-j).sku));
		                              trialManifest.addContent(items.get(i-j));
		                              trialManifest.skus.add(items.get(i-j).sku);
		                              Placement place = new Placement(items.get(i-j));
		                              place.setLocation(new Point(0, 0, trialContainer.size.z));
		                              trialManifest.placements.add(place);
		                              double thisVolume = items.get(i-j).volume;
		                              double thisWeight = items.get(i-j).weight;
		                              volume += thisVolume;
		                              weight += thisWeight;
		                              items.get(i-j).selected = true;
		                              remainingItemVolume -= thisVolume;
		                              remainingItemWeight -= thisWeight;
		                              bdVolume_copy.put(thisBd, bdVolume_copy.get(thisBd)-thisVolume);
		                              bdWeight_copy.put(thisBd, bdWeight_copy.get(thisBd)-thisWeight);
		                              lineVolume_copy.put(thisLine, lineVolume_copy.get(thisLine)-thisVolume);
		                              lineWeight_copy.put(thisLine, lineWeight_copy.get(thisLine)-thisWeight);
		                              lineTotalQty_copy.put(thisLine, lineTotalQty_copy.get(thisLine)-1);
		                              trialCount++;
	                           	}
	                           	i = i-minRequiredQty+1;
	                           } else
	                              debug("      too many");
	                        } else
	                           debug("      too heavy");
	                     } else
	                        debug("      too big");
	                  } else
	                     debug("      restriction " + restriction);
	               } 
            	}
            }
            
            //step 3, if remaining volume and weight is big enough to fill another entire line, fill it. Find it from a bd that couldn't fit into 1 box.
            
            double currentVolumeFill = volume/trialContainer.getVolume();
            double currentWeightFill = weight/(trialContainer.getWeight()-trialContainer.weight);
            double volume_mod = remainingItemVolume % (trialContainer.getVolume()*maxFill);
            double weight_mod = remainingItemWeight % (trialContainer.getWeight()-trialContainer.weight);
            double remainingVolumeFill = volume_mod / trialContainer.getVolume();
            double remainingWeightFill = weight_mod / (trialContainer.getWeight()-trialContainer.weight);
            
            if( (currentVolumeFill < singleLineFill && currentWeightFill < singleLineFill ) || 
              	 ((remainingVolumeFill+currentVolumeFill) < maxFill && (remainingWeightFill+currentWeightFill) <1) ) {
            	  //inform("step 3: fill another line");
                 for (int i = 0; i<items.size(); i++) {
                    checkTime();
                    if( items.get(i).selected ) continue;
                    int restriction = restricted(items.get(i), trialManifest, firstContainer);
                    if (restriction == 0 ) {
                       if (maxFill == 0 || (volume + items.get(i).volume) <= (maxFill * trialContainer.getVolume())) {
                          if (trialContainer.getWeight() == 0 || (weight + items.get(i).weight) <= (trialContainer.getWeight()-trialContainer.tare)) {
                             if ((trialContainer.count == 0) || ((trialCount + 1) <= trialContainer.count)) {
                          			int thisLine = items.get(i).orderLineSeq;
                          			String thisBd = items.get(i).buyingDepartment;
                          			if( (bdVolume_copy.get(thisBd) <= (maxFill * trialContainer.getVolume())) &&  
                          					(bdWeight_copy.get(thisBd) <= (trialContainer.getWeight()-trialContainer.tare))) {
                          				continue;
                          			}
                          			if( thisLine != currentLine ) {
                          				if( !(lineVolume_copy.get(thisLine) + volume <= (maxFill * trialContainer.getVolume())) ||  
  	                           			 !(lineWeight_copy.get(thisLine) + weight <= (trialContainer.getWeight()-trialContainer.tare))) 
                          					continue;
                          				else
                          					currentLine = thisLine;
                          			}
                          			debug(String.format("fit pickSeq %s, item %s", items.get(i).id,items.get(i).sku));
                                trialManifest.addContent(items.get(i));
                                trialManifest.skus.add(items.get(i).sku);
                                Placement place = new Placement(items.get(i));
                                place.setLocation(new Point(0, 0, trialContainer.size.z));
                                trialManifest.placements.add(place);
                                double thisVolume = items.get(i).volume;
                                double thisWeight = items.get(i).weight;
                                volume += thisVolume;
                                weight += thisWeight;
                                items.get(i).selected = true;
                                remainingItemVolume -= thisVolume;
                                remainingItemWeight -= thisWeight;
                                bdVolume_copy.put(thisBd, bdVolume_copy.get(thisBd)-thisVolume);
                                bdWeight_copy.put(thisBd, bdWeight_copy.get(thisBd)-thisWeight);
                                lineVolume_copy.put(thisLine, lineVolume_copy.get(thisLine)-thisVolume);
                                lineWeight_copy.put(thisLine, lineWeight_copy.get(thisLine)-thisWeight);
                                lineTotalQty_copy.put(thisLine, lineTotalQty_copy.get(thisLine)-1);
                                trialCount++;
                             } else
                                debug("      too many");
                          } else
                             debug("      too heavy");
                       } else
                          debug("      too big");
                    } else
                       debug("      restriction " + restriction);
                 }            	
              }
            
            //step 4, if remaining volume and weight is big enough to fill another entire line, fill it. Find it from the smallest line.
            
            if( (currentVolumeFill < singleLineFill && currentWeightFill < singleLineFill ) || 
            	 ((remainingVolumeFill+currentVolumeFill) < maxFill && (remainingWeightFill+currentWeightFill) <1) ) {
               for (int i = items.size()-1; i >=0; i--) {
                  checkTime();
                  //inform("step 4: fill another line again");
                  if( items.get(i).selected ) continue;
                  int restriction = restricted(items.get(i), trialManifest, firstContainer);
                  if (restriction == 0 ) {
                     if (maxFill == 0 || (volume + items.get(i).volume) <= (maxFill * trialContainer.getVolume())) {
                        if (trialContainer.getWeight() == 0 || (weight + items.get(i).weight) <= (trialContainer.getWeight()-trialContainer.tare)) {
                           if ((trialContainer.count == 0) || ((trialCount + 1) <= trialContainer.count)) {
                           	int thisLine = items.get(i).orderLineSeq;
                           	String thisBd = items.get(i).buyingDepartment;
                           	if( thisLine != currentLine ) {
	                        		if( !(lineVolume_copy.get(thisLine) + volume <= (maxFill * trialContainer.getVolume())) ||  
	                           			 !(lineWeight_copy.get(thisLine) + weight <= (trialContainer.getWeight()-trialContainer.tare))) 
	                         			continue;
	                        		else
	                        			currentLine = thisLine;
                           	}
                           	debug(String.format("fit pickSeq %s, item %s", items.get(i).id,items.get(i).sku));
                              trialManifest.addContent(items.get(i));
                              trialManifest.skus.add(items.get(i).sku);
                              Placement place = new Placement(items.get(i));
                              place.setLocation(new Point(0, 0, trialContainer.size.z));
                              trialManifest.placements.add(place);
                              double thisVolume = items.get(i).volume;
                              double thisWeight = items.get(i).weight;
                              volume += thisVolume;
                              weight += thisWeight;
                              items.get(i).selected = true;
                              remainingItemVolume -= thisVolume;
                              remainingItemWeight -= thisWeight;
                              bdVolume_copy.put(thisBd, bdVolume_copy.get(thisBd)-thisVolume);
                              bdWeight_copy.put(thisBd, bdWeight_copy.get(thisBd)-thisWeight);
                              lineVolume_copy.put(thisLine, lineVolume_copy.get(thisLine)-thisVolume);
                              lineWeight_copy.put(thisLine, lineWeight_copy.get(thisLine)-thisWeight);
                              lineTotalQty_copy.put(thisLine, lineTotalQty_copy.get(thisLine)-1);
                              trialCount++;
                           } else
                              debug("      too many");
                        } else
                           debug("      too heavy");
                     } else
                        debug("      too big");
                  } else
                     debug("      restriction " + restriction);
               }            	
            }
            
            
            //step 5, fill split line items
            currentVolumeFill = volume/trialContainer.getVolume();
            currentWeightFill = weight/(trialContainer.getWeight()-trialContainer.weight);
            volume_mod = remainingItemVolume % (trialContainer.getVolume()*maxFill);
            weight_mod = remainingItemWeight % (trialContainer.getWeight()-trialContainer.weight);
            remainingVolumeFill = volume_mod / trialContainer.getVolume();
            remainingWeightFill = weight_mod / (trialContainer.getWeight()-trialContainer.weight);
            
            if( (currentVolumeFill < singleLineFill && currentWeightFill < singleLineFill ) || 
              	 ((remainingVolumeFill+currentVolumeFill) < maxFill && (remainingWeightFill+currentWeightFill) <1) ) {
            	for (int i = items.size()-1; i >=0; i--) {
            		checkTime();
            		if( items.get(i).selected ) continue;
            		int restriction = restricted(items.get(i), trialManifest, firstContainer);
            		if (restriction == 0) {
            			int thisLine = items.get(i).orderLineSeq;
            			String thisBd = items.get(i).buyingDepartment;
                    	int lineQty = lineTotalQty_copy.get(thisLine);
                    	int splitLineQty = minSplitLineQty.get(thisLine);
                    	int minRequiredQty = Math.min(splitLineQty, lineQty);
                    	if (maxFill == 0 || (volume + items.get(i).volume*minRequiredQty) <= (maxFill * trialContainer.getVolume())) {
                    		if (trialContainer.getWeight() == 0 || (weight + items.get(i).weight*minRequiredQty) <= (trialContainer.getWeight()-trialContainer.tare)) {
                    			if ((trialContainer.count == 0) || ((trialCount + minRequiredQty) <= trialContainer.count)) {
                    				debug(String.format("step 5: fit %d split line item %s", minRequiredQty, items.get(i).sku));
                    				for( int j=0; j<minRequiredQty; j++ ) {
                    					debug(String.format("fit pickSeq %s, item %s", items.get(i-j).id,items.get(i-j).sku));
	                    				trialManifest.addContent(items.get(i-j));
	                    				trialManifest.skus.add(items.get(i-j).sku);
	                    				Placement place = new Placement(items.get(i-j));
	                    				place.setLocation(new Point(0, 0, trialContainer.size.z));
	                    				trialManifest.placements.add(place);
	                    				double thisVolume = items.get(i-j).volume;
	                    				double thisWeight = items.get(i-j).weight;
	                    				volume += thisVolume;
	                    				weight += thisWeight;
	                    				items.get(i-j).selected = true;
	                    				remainingItemVolume -= thisVolume;
	                    				remainingItemWeight -= thisWeight;
	                    				bdVolume_copy.put(thisBd, bdVolume_copy.get(thisBd)-thisVolume);
	                    				bdWeight_copy.put(thisBd, bdWeight_copy.get(thisBd)-thisWeight);
	                    				lineVolume_copy.put(thisLine, lineVolume_copy.get(thisLine)-thisVolume);
	                    				lineWeight_copy.put(thisLine, lineWeight_copy.get(thisLine)-thisWeight);
	                    				lineTotalQty_copy.put(thisLine, lineTotalQty_copy.get(thisLine)-1);
	                    				trialCount++;
                    				}
                    				i = i-minRequiredQty+1;
                    			} else
                    				debug("      too many");
                    		} else
                    			debug("      too heavy");
                    	} else
                    		debug("      too big");
            		} else
            			debug("      restriction " + restriction);
            	}            	
            }
            
          
            trialFraction = trialManifest.getFraction();

            debug("  trial container " + trialContainer.id + " count " + trialCount + " fraction " + trialFraction);

            if (trialCount >= bestCount) {
               if ((trialCount > bestCount) || (trialFraction > bestFraction)) {
                  bestCount = trialCount;
                  bestFraction = trialFraction;
                  bestManifest = trialManifest;
               }
            }
            firstContainer = false;
         }

         if (bestManifest != null) {
            debug("  best " + bestManifest.id);
            bestManifest.id = "" + (++manifestSequence);
            manifests.add(bestManifest);
            for (Placement p : bestManifest.placements) {
         		items.remove(p.box);
         		String bd = p.box.buyingDepartment;
         		int line = p.box.orderLineSeq;
         		double currentVolume = p.box.volume;
         		double currentWeight = p.box.weight;
         		bdVolume.put(bd, bdVolume.get(bd)-currentVolume);
         		bdWeight.put(bd, bdWeight.get(bd)-currentWeight);
         		lineVolume.put(line, lineVolume.get(line)-currentVolume);
         		lineWeight.put(line, lineWeight.get(line)-currentWeight);
         		lineTotalQty.put(line, lineTotalQty.get(line)-1);
         		totalVolume -= currentVolume;
         		totalWeight -= currentWeight;
            }
         } else {
            debug("has unpackable items");
            remains = items;
            items = new BoxList();
         }
      }

      items = remains;

      /*
      if (debug) {
         for (Manifest repack : manifests) {
            double bestFraction = repack.getFraction();
            Box bestContainer = repack.container;

            for (Box container : containers) {
               repack.unpack();
               repack.setContainer(container);
               if (repack.pack())
                  if (bestFraction < repack.getFraction()) {
                     bestFraction = repack.getFraction();
                     bestContainer = container;
                  }
            }
            repack.unpack();
            repack.setContainer(bestContainer);
            repack.pack();
         }
      }
      */

      boolean ok = items.isEmpty();
      errorType = ERROR_BAD_SKU;
      
      return ok;
   }
   
   private int maxSingleLineQty(Box item) {
   	Box trialContainer = containers.get(0);
		int maxQtyByVolume = (int) Math.floor(maxFill * trialContainer.getVolume()/item.volume);
		int maxQtyByWeight = (int) Math.floor((trialContainer.getWeight()-trialContainer.tare) / (item.weight));
		int maxAllowed = Math.min(maxQtyByWeight, maxQtyByVolume);
		if( maxAllowed <= 0 ) maxAllowed = 1;
      return maxAllowed;
   }   

   public boolean canFit(Box item, Box container) {
      debug( "can fit pick xyz " + item.size.x + " " + item.size.y + " " + item.size.z);
      debug( "can fit carton xyz " + container.size.x + " " + container.size.y + " " + container.size.z);
      if (item.size.x > container.size.x)
         return false;
      if (item.size.y > container.size.y)
         return false;
      if (item.size.z > container.size.z)
         return false;
      return true;
   }

   public int restricted(Box newItem, Manifest trial, boolean firstContainer) {
   	if( !firstContainer && newItem.getCount() > 1 && !canFit(newItem,trial.getContainer()) )
   		return 1;
      return 0;
   }
   
   public Box createNextBox( Box item, int start, int stop ) {
   	Box nestBox = new Box();
   	nestBox = item;
   	int count = stop - start + 1;
   	nestBox.count = count;
   	nestBox.size.x = item.size.x + (count-1)*item.delta.x;
   	nestBox.size.y = item.size.y + (count-1)*item.delta.y;
   	nestBox.size.z = item.size.z + (count-1)*item.delta.z;
   	nestBox.weight = item.weight * count;
		nestBox.boxes = new ArrayList<>();
		nestBox.isStack = true;
		//inform("pack nest box sku%s, count %d",item.sku, count);
   	return nestBox;
   }

   private boolean isCartonizable(Box item) {
      // check if item can fit in any container, if so skip
      for(Box trialContainer : containers) {
         // skip containers that can hold the item
         //if (!canFit(item, trialContainer))
         //   continue ;

         if ( ( trialContainer.weight > 0.0 ) && ( (item.weight ) > (trialContainer.weight-trialContainer.tare ) ) ) 
            continue;

         if ( ( trialContainer.fill   > 0.0 ) && ( (item.volume) > (trialContainer.fill * trialContainer.getVolume() ) ) )
            continue;

         // skip containers that are restricted 
         Manifest trialManifest = new Manifest(trialContainer.id) ;
         trialManifest.setContainer(trialContainer) ;
         int restriction = restricted(item,trialManifest,true) ;
         if(restriction!=0)
            continue;

         // unit item can be cartonized
         return true;
      }
      // unit item cannot be cartonized
      return false;
   }
   
   private int fullcaseShip(Box item, int manifestSequence) {
      Box container = new Box("fullcase",item.size) ;
      Manifest fullcase = new Manifest(item.sku+" fullcase") ;
      fullcase.setContainer(container) ;
      fullcase.addContent(item) ;
      Placement place = new Placement(item) ;
      fullcase.placements 
      = fullcase.placements.addPlacement(place,fullcase.placements) ;


      fullcase.id = ""+(++manifestSequence) ;
      fullcase.items = new BoxList() ;
      fullcase.count = 1 ;
      fullcase.weight = item.weight ;
      fullcase.volume = item.size.x
            * item.size.y
            * item.size.z ;
      manifests.add(fullcase) ; 

      return manifestSequence;
   }
   
   private void checkTime() throws TimeoutException {
      long duration = System.currentTimeMillis() - start;
      if (duration > timeout)
         throw new TimeoutException("timeout " + timeout + " msec reached");
   }
   
   /** An {@code Exception} due to timeout during processing. */
   public static class TimeoutException
         extends Exception {
      public TimeoutException( String message ) {
         super( message );
      }
      public TimeoutException( String message, Throwable cause ) {
         super( message, cause );
      }
   }

}
