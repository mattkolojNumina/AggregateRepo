## Carton Status Definitions

picked
: `rdsCarton.pickStamp is not null`

short
: `rdsCarton.shortStamp is not null`

shipShort
: `rdsCarton.shortStamp is not null AND rdsCarton.pickStamp is not null`

canceled
: `rdsCarton.cancelStamp is not null`

picking
: open picks exist `rdsPicks.pickStamp is null`

packed
: `packStamp is not null`

## Pick Status Definitions

## Carton Workflow Definitions

audit required
: `rdsCarton.auditRequired == 1`

repack required (set at pack workstation)
: `rdsCarton.repackRequired == 1`

carton flagged for exception as pack station (set at pack workstation)
: `rdsCarton.packException == 1`

## Inline Pack Workstation Controls

| Control Param Name | East Pakt Value    | West Pakt Value    |
|--------------------|--------------------|--------------------|
| mode               | inlinePack         | inlinePack         |
| ip                 | 172.27.13.27       | 172.27.13.XXX      |
| startScreen        | termApp.initScreen | termApp.initScreen |
| scanner | /east-pakt-exception/srlScan | /west-pakt-exception/srlScan |

| Runtime name | Purpose                                                    |
|--------------|------------------------------------------------------------|
|eastPackLPN| LPN of carton released into pack station gravity zone      |
|westPackLPN| LPN of carton released into West pack station gravity zone |

## Exception Workstation Controls

## Document Attributes

8.5" x 11" pack list
: `docType = 'packList', refType = 'cartonSeq', refValue = [cartonSeq]`
: generated when carton moves to picked status

1" x 4" order info label
: `docType = 'label', refType = 'cartonSeq', refValue = [cartonSeq]`
: generated at cartonization

4" x 6" carton content label (PNA)
: `docType = 'shipLabel', refType = 'cartonSeq', refValue = [cartonSeq]`
: generated when carton moves to picked status

## Pack Workstation Attributes

### Trigger to print documents

value exists in runtime parameter (but valid LPN scan will also fill in runtime parameter)

### Decision to set Pack Stamp

`rdsCarton.pickStamp is not null` 
AND `rdsCarton.cancelStamp is null` 
AND `rdsCarton.auditRequired == 0` 
AND `rdsCarton.repackRequired == 0` 
AND `rdsCarton.packException == 0`
AND pack list is verified
AND order info label is verified

Triggered when operator pushes OK button.  
Also need to clear runtime LPN value at the same time.

## Print and Apply Attributes

### Ready to Label

`rdsCarton.packStamp == 1`

### Label Verification Failure

Exception station needs to look at `eastCartons` or `westCartons` to determine if there was a label failure
for entries with a status less than zero.

0 = in process

### Divert decision logic

Check `eastCarton` or `westCarton` for issues
OR `rdsCarton.auditRequired == 1` 
OR `rdsCarton.repackRequired == 1` 
OR `rdsCarton.packException == 1`

## Bander Attributes

`If rdsCarton.cartonType == 'Tote', band.`

When containers leave bander, set stamp readyToSort.
(This is needed to trigger confirmations back to Sloane)