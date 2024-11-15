insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','geekplus','user_id','numina','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','geekplus','user_key','12345','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','geekplus','hostname','192.168.146.2','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','geekplus','port','80','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','geekplus','warehouse_code','Tifton','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','geekplus','owner_code','sloane','','no');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','InventoryAdjustFeedback','pollDelay','3000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','InventoryAdjustFeedback','pollPeriod','5000','','no');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventoryAdjustmentUpload','pollPeriod','300000','5 mins','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventoryAdjustmentUpload','pollDelay','3000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventoryAdjustmentUpload','fileName','GIA','Inventory adjustments file creation program','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventoryAdjustmentUpload','filePath','/home/rds/xfer/out/active/','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventoryAdjustmentUpload','fileExtension','CNF','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventoryAdjustmentUpload','archiveFilePath','/home/rds/xfer/out/archive/','','no');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','pollDelay','3000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','pollPeriod','12000','2 mins','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','username','OPER','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','password','OPER','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','localDirectoryPath','/home/rds/xfer/out/active/','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','remoteDirectoryPath','\\POWER\\RDR\\V','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','ftpServer','192.168.1.249','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ContinuousFTPUploader','ftpPort','21','','no');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','fileName','TIFTVOICEITEM.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','rowCountText','PRODUCTLINECOUNT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','filePrefix','TIFTVOICEITEM.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','fileSuffix','TIFTVOICEITEM.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','pollDelay','3000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','pollPeriod','5000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','filePath','/sftp/base/orgdatatransfer/in/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','archiveFilePath','/home/rds/xfer/in/archive/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','expectedFieldCount','18','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','ProductDownload','preReqFileCSV','','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','pollDelay','3000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','pollPeriod','5000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','expectedFieldCount','9','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','fileSuffix','.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','filePrefix','RDSINBOUNDTRANS','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','rowCountText','INBOUNDORDERCOUNT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','fileName','RDSINBOUNDTRANS','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','archiveFilePath','/home/rds/xfer/in/archive/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','filePath','/sftp/base/orgdatatransfer/in/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayOrderDownload','preReqFileCSV','TIFTVOICEITEM.TXT,BUYINGDEPT.TXT','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','fileName','TIFTVOICECUST.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','rowCountText','CUSTOMERLINECOUNT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','filePrefix','TIFTVOICECUST.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','fileSuffix','TIFTVOICECUST.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','pollDelay','3000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','pollPeriod','5000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','filePath','/sftp/base/orgdatatransfer/in/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','archiveFilePath','/home/rds/xfer/in/archive/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','expectedFieldCount','9','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','CustomerDownload','preReqFileCSV','','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','fileName','BUYINGDEPT.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','rowCountText','BUYDEPTLINECOUNT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','filePrefix','BUYINGDEPT.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','fileSuffix','BUYINGDEPT.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','pollDelay','3000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','pollPeriod','5000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','filePath','/sftp/base/orgdatatransfer/in/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','archiveFilePath','/home/rds/xfer/in/archive/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','expectedFieldCount','2','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BuyingDeptDownload','preReqFileCSV','','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','fileName','TIFTVOICELOC.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','rowCountText','LOCATIONLINECOUNT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','filePrefix','TIFTVOICELOC.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','fileSuffix','TIFTVOICELOC.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','pollDelay','3000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','pollPeriod','5000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','filePath','/sftp/base/orgdatatransfer/in/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','archiveFilePath','/home/rds/xfer/in/archive/','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','expectedFieldCount','6','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','LocationDownload','preReqFileCSV','','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','fileName','TIFTUSERS.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','rowCountText','USERLINECOUNT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','filePrefix','TIFTUSERS.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','fileSuffix','TIFTUSERS.TXT','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','pollDelay','3000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','pollPeriod','5000','','yes');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','filePath','/sftp/base/orgdatatransfer/in/','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','archiveFilePath','/home/rds/xfer/in/archive/','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','expectedFieldCount','4','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','UserDownload','preReqFileCSV','','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BaseData','pollDelay','3000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','BaseData','pollPeriod','5000','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayTranslator','pollDelay','3000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','PutawayTranslator','pollPeriod','5000','','yes');

insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventorySnapshotReconUpload','fileExtension','CNF','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventorySnapshotReconUpload','pollPeriod','5000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventorySnapshotReconUpload','pollDelay','3000','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventorySnapshotReconUpload','fileName','geekinventory','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventorySnapshotReconUpload','archiveFilePath','/home/rds/xfer/out/archive/','','no');
insert  into `controls`(`host`,`zone`,`name`,`value`,`description`,`editable`) values ('build','GeekInventorySnapshotReconUpload','filePath','/home/rds/xfer/out/active/','','no');