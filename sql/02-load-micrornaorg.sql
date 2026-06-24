USE targets;

LOAD DATA INFILE '/carga/micrornaorg/miRTarBase_MTI.csv'
INTO TABLE micrornaorg
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(@col1, @col2, @col3, @col4, @col5, @col6, @col7, @col8, @col9)
SET mirna = @col2, gene = @col4;

ALTER TABLE micrornaorg ADD INDEX idx_mirna (mirna);
