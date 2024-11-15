%
%   cif.w -- access to CIF card DPM 
%
%   Author: Mark Woodworth
%           
%   History:
%     2018-01-21 -- initial version reworked from older trak engine 
%
%         C O N F I D E N T I A L
%
%     This information is confidential and should not be disclosed to
%     anyone who does not have a signed non-disclosure agreement on file
%     with Numina Systems Corporation.  
%
%
%
%
%
%     (C) Copyright 2018 Numina Systems Corporation.  All Rights Reserved.
%
%
%

% --- macros ---
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\dot{\qquad\item{$\bullet$}}

% --- title ---
%
\def\title{CIF Device}

\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

\bigskip
\centerline{\boxit{10pt}{\hsize 4in
\bigskip
\centerline{\bf CONFIDENTIAL}
\smallskip
This material is confidential.  
It must not be disclosed to any person
who does not have a current signed non-disclosure form on file with Numina
Systems Corporation.  
It must only be disseminated on a need-to-know basis.
It must be stored in a secure location. 
It must not be left out unattended.  
It must not be copied.  
It must be destroyed by burning or shredding. 
\smallskip
}}
\bigskip

\centerline{Author: Mark Woodworth}
\centerline{Revision Date: \today}
\centerline{RCS Date $ $Date: 2002/04/22 13:01:17 $ $}
}

%
\def\botofcontents{\vfill
\centerline{\copyright 2018 Numina Systems Corporation.  
All Rights Reserved.}
}

% --- overview ---
%
@* Overview. 

A device driver to allow application space access to the CIF shared memory
segment.

@c
@<Defines@>@;
@<Includes@>@;
@<Statics@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Operations@>@;
@<Module Information@>@;
@<Module Functions@>@;

@ We will give names to the device and class.
@<Defines@>+=
#define DEVICE_NAME "cif0"
#define CLASS_NAME  "cif"

@ We need to declare some information.
@<Module Information@>+=
MODULE_LICENSE("GPL") ;
MODULE_AUTHOR("NuminaGroup") ;

@ These require include files.
@<Includes@>+=
#include <linux/init.h>
#include <linux/module.h>
#include <linux/device.h>
#include <linux/kernel.h>
#include <linux/fs.h>
#include <linux/uaccess.h>

@ Open.
@<Functions@>+=
static int
cif_open(struct inode *i, struct file *f)
  {
  printk(KERN_INFO "cif_open called\n") ;
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
static int 
cif_open(struct inode *i, struct file *f) ;

@ Release.
@<Functions@>+=
static int
cif_release(struct inode *i, struct file *f)
  {
  printk(KERN_INFO "cif_release called\n") ;
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
static int 
cif_release(struct inode *i, struct file *f) ;

@ Read.
@<Functions@>+=
static ssize_t
cif_read(struct file *f, char *c, size_t len, loff_t *offset)
  {
  int i ;

  // printk(KERN_INFO "cif_read called, len %ld\n",len) ;
 
  i=0 ;
  while( (i<len) && (i<2048) )
    {
    put_user( *(cif_read_addr+i), c+i ) ;
    i++ ;
    }

  return i ;
  }

@ Proto.
@<Prototypes@>+=
static ssize_t 
cif_read(struct file *f, char *c, size_t len, loff_t *offset) ; 

@ Write.
@<Functions@>+=
static ssize_t
cif_write(struct file *f, const char *c, size_t len, loff_t *offset)
  {
  int i ;

  // printk(KERN_INFO "cif_write called, len %ld\n",len) ;
 
  i=0 ;
  while( (i<len) && (i<2048) )
    {
    get_user( *(cif_write_addr+i), c+i ) ;
    i++ ;
    }

  return i ;
  }

@ Proto.
@<Prototypes@>+=
static ssize_t
cif_write(struct file *f, const char *c, size_t len, loff_t *offset) ;

@ File Operations.
@<Operations@>+=
static struct file_operations
fops = 
  {
  .open    = cif_open,
  .read    = cif_read,
  .write   = cif_write,
  .release = cif_release,
  } ;

@ Init.
@<Module Functions@>+=
static int __init
cif_init(void) 
   {
   printk(KERN_INFO "cif_init\n") ;

   @<Register a major number@>@;
   @<Register the device class@>@;
   @<Register the device driver@>@;

   @<PCI init@>@;

   return 0 ;
   }

@ Register a major number.
@<Register a major number@>+=
  {
  majorNumber = register_chrdev(0, DEVICE_NAME, &fops) ;
  if(majorNumber<0)
    {
    printk(KERN_ALERT "%s failed to register a major number\n",DEVICE_NAME) ;
    return majorNumber ;
    }
  printk(KERN_INFO "%s registered major number %d\n",DEVICE_NAME,majorNumber) ;
  }

@ The major number.
@<Statics@>+=
static int majorNumber ;

@ Register the device class.
@<Register the device class@>+=
  {
  trakClass = class_create(THIS_MODULE, CLASS_NAME) ;
  if(IS_ERR(trakClass))
    {
    unregister_chrdev(majorNumber,DEVICE_NAME) ;
    printk(KERN_ALERT "%s failed to register device class\n",DEVICE_NAME) ;
    return PTR_ERR(trakClass) ;
    }
  printk(KERN_INFO "%s registered device class\n",DEVICE_NAME) ;
  }

@ The device class.
@<Statics@>+=
static struct class *trakClass = NULL ;

@ Register the device driver.
@<Register the device driver@>+=
  {
  trakDevice = device_create(trakClass,NULL,
                             MKDEV(majorNumber,0),NULL,
                            DEVICE_NAME) ;
  if(IS_ERR(trakDevice))
    {
    class_destroy(trakClass) ;
    unregister_chrdev(majorNumber,DEVICE_NAME) ;
    printk(KERN_ALERT "%s failed to create device\n",DEVICE_NAME) ;
    return PTR_ERR(trakDevice) ;
    }
  printk(KERN_INFO "%s created device\n",DEVICE_NAME) ;
  }

@ The device.
@<Statics@>+=
static struct device *trakDevice = NULL ;

@ Exit.
@<Module Functions@>+=
static void __exit
cif_exit(void) 
   {

   @<Destroy the device@>@;
   @<Unregister and destroy the class@>@;
   @<Unregister the major number@>@;

   @<PCI exit@>@;

   printk(KERN_INFO "cif_exit\n") ;
   }

@ Destroy the device.
@<Destroy the device@>+=
device_destroy(trakClass,MKDEV(majorNumber,0)) ;

@ Unregister and destroy the class@>@;
@<Unregister and destroy the class@>+=
class_unregister(trakClass) ;
class_destroy(trakClass) ;

@ Unregister the major number.
@<Unregister the major number@>+=
unregister_chrdev(majorNumber, DEVICE_NAME) ;

@ The entry and exit points.
@<Module Functions@>+=
module_init(cif_init) ;
module_exit(cif_exit) ;

@* PCI.
@<Includes@>+=
#include <linux/pci.h>

@ ID.
@<Statics@>+=
static struct pci_device_id
cif_ids[] = 
  {
     { PCI_DEVICE(0x10b5,0x9030) },
     { 0, },
  } ;

@ Remapped IO addresses.
@<Statics@>+=
unsigned char *cif_read_addr  = NULL ;
unsigned char *cif_write_addr = NULL ;

@ PCI probe.
@<Functions@>+=
static int
cif_probe(struct pci_dev *dev,
          const struct pci_device_id *id)
  {
  int err ;
  int bar ;
  unsigned long addr ;

  printk(KERN_INFO "cif_probe called\n") ;
   
  err = pci_enable_device(dev) ;
  printk(KERN_INFO "pci_enable_device() = %d\n",err) ;

  bar = 2 ;

  addr = pci_resource_start(dev,bar) ;
  printk(KERN_INFO "io %d %lx\n",bar,addr) ;

  cif_write_addr = (unsigned char *) ioremap(addr+0x000,0xE00) ;
  cif_read_addr  = (unsigned char *) ioremap(addr+0xE00,0xE00) ;

  printk(KERN_INFO "map write %p  read %p\n",cif_read_addr,cif_write_addr) ;

  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
static int
cif_probe(struct pci_dev *dev,
          const struct pci_device_id *id) ;


@ PCI remove.
@<Functions@>+=
static void
cif_remove(struct pci_dev *dev)
  {
  printk(KERN_INFO "cif_remove called\n") ;
  }

@ Proto.
@<Prototypes@>+=
static void
cif_remove(struct pci_dev *dev) ;

@ PCI driver.
@<Module Information@>+=
static struct pci_driver
cif_driver =
  {
  .name = "cif",
  .id_table = cif_ids,
  .probe    = cif_probe,
  .remove   = cif_remove,
  } ;

@ PCI init.
@<PCI init@>+=
  {
  int err ;

  err = pci_register_driver(&cif_driver) ;
  printk(KERN_INFO "cif pci_register_driver() = %d\n",err) ;
  }

@ PCI exit.
@<PCI exit@>+=
  {
  pci_unregister_driver(&cif_driver) ;
  }

@* Index.

