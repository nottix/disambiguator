CREATE TABLE  `chaos`.`frase` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `idfrase` int(10) unsigned NOT NULL default '0',
  `frase` text NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1; 



                    CREATE TABLE  `chaos`.`costituente` (
  `idcostituente` int(10) unsigned NOT NULL auto_increment,
  `id` int(10) unsigned NOT NULL default '0',
  `type` varchar(45) NOT NULL default '',
  `mf` varchar(60) NOT NULL default '',
  `head` int(10) unsigned NOT NULL default '0',
  `potgov` int(10) unsigned NOT NULL default '0',
  `idfrase` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`idcostituente`),
  KEY `FKFrase` (`idfrase`),
  CONSTRAINT `FKFrase` FOREIGN KEY (`idfrase`) REFERENCES `frase` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;                     

CREATE TABLE  `chaos`.`icd` (
  `idicd` int(10) unsigned NOT NULL auto_increment,
  `from` int(10) unsigned NOT NULL default '0',
  `to` int(10) unsigned NOT NULL default '0',
  `type` varchar(45) NOT NULL default '',
  `plaus` double NOT NULL default '0',
  `idfrase` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`idicd`),
  KEY `FKFrase1` (`idfrase`),
  CONSTRAINT `FKFrase1` FOREIGN KEY (`idfrase`) REFERENCES `frase` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;