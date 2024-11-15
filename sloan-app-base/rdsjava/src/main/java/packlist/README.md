#Packlist Generation

The packlist app is a polling app that takes in requests every `pollPeriod` ms and creates a corresponding packlist for a cartonSeq.

## 1. Controls table:

|zone   |name      |value|
|-------|----------|-----|
|pclPack|pollDelay |1000 |
|pclPack|pollPeriod|1000 |
|pclPack|maxLines  |55   |
|pclPack|maxLines2 |55   |

## 2. Request packlist

```
INSERT INTO rdsDocumentQueue
SET `type`='packlist', 
refType='cartonSeq', 
refValue=x
```
Where x = cartonSeq of carton that requires a packlist.

## 3. Get Generated Packlist

```
SELECT FROM_BASE64(printDoc) FROM rdsDocuments
WHERE docType='packlist'
AND refType='cartonSeq'
AND refValue=x
```
Where x = cartonSeq of carton that requires packlist to be printed.

## Packlist template

The template for a packlist lives in the `cfgDocFields` table, where each item is defined. See `doc/PclDocument.java` for more info.
