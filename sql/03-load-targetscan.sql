USE targetscan;

LOAD DATA INFILE '/carga/Targetscan/miR_Family_Info.txt'
INTO TABLE mirna
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@mirfamily, @seed, @speciesid, @mirbaseid, @seq, @conservation, @accession)
SET mirna = @mirbaseid, miRFamily = @mirfamily;

ALTER TABLE mirna ADD INDEX idx_mirna (mirna);

LOAD DATA INFILE '/carga/Targetscan/Predicted_Targets_Info.default_predictions.txt'
INTO TABLE targets
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@mirfamily, @geneid, @genesymbol, @transcriptid, @speciesid, @utrstart, @utrend, @msastart, @msaend, @seedmatch, @pct)
SET miRFamily = @mirfamily, Gene = @genesymbol;

ALTER TABLE targets ADD INDEX idx_mirfamily (miRFamily);
