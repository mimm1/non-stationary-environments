
@ echo OFF

@echo "1CDT"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\1CDT.arff) -i 16000 -f 300"


@echo "2CDT"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\2CDT.arff) -i 16000 -f 300"

@echo "4CR"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\4CR.arff) -i 150000 -f 300"


@echo "4CRE-V2"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\4CRE-V2.arff) -i 190000 -f 300"

@echo "1CSurr"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\1CSurr.arff) -i 56000 -f 300"

@echo "UG_2C_2D"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\UG_2C_2D.arff) -i 100000 -f 300"


@echo "UG_2C_3D"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\UG_2C_3D.arff) -i 200000 -f 300"


@echo "UG_2C_5D"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l moa.labelscarcity.SCARGC -s (ArffFileStream -f C:\Users\OrhanEyhab\Documents\UG_2C_5D.arff) -i 200000 -f 300"

@echo "UG_2C_2D"

java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l (moa.labelscarcity.HDWM -p 1)  -s (clustering.FileStream -f C:\Test\Dataset\Synthetic\UG_2C_2D.arff -n) -k (Do not Normalize Stream) -i 100000 -f 1000"

@echo "UG_2C_3D"

java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l (moa.labelscarcity.HDWM -p 1)  -s (clustering.FileStream -f C:\Test\Dataset\Synthetic\UG_2C_3D.arff -n) -k (Do not Normalize Stream) -i 100000 -f 1000"


@echo "1CSurr"

java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l (moa.labelscarcity.HDWM -p 1)  -s (clustering.FileStream -f C:\Test\Dataset\Synthetic\1CSurr.arff -n) -k (Do not Normalize Stream) -i 100000 -f 1000"

@echo "4CR"

java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l (moa.labelscarcity.HDWM -p 1)  -s (clustering.FileStream -f C:\Test\Dataset\Synthetic\4CR.arff -n) -k (Do not Normalize Stream) -i 100000 -f 1000"

@echo "4CRE-V2"

java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -l (moa.labelscarcity.HDWM -p 1)  -s (clustering.FileStream -f C:\Test\Dataset\Synthetic\4CRE-V2.arff -n) -k (Do not Normalize Stream) -i 100000 -f 1000"


@echo "STAGGER"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPeriodicHeldOut -l (moa.labelscarcity.HDWM -p 1) -s (ArffFileStream -f C:\test\Dataset\Drifts\stagger.arff) -z Random -n 100 -i 120 -f 1"



@echo "R Tree"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluatePrequential -s (RecurrentConceptDriftStream -x 10000 -s (generators.RandomTreeGenerator -o 0) -d (generators.RandomTreeGenerator -u 0) -p 25000 -w 1) -i 100000 -f 1000"

@ echo "LED"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (ConceptDriftStream -s generators.LEDGenerator -d (generators.LEDGeneratorDrift -d 7) -p 50000) -i 100000 -f 1000"


@ echo HyperPlane_Gradual
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (generators.HyperplaneGenerator -k 10 -t 0.01) -i 100000 -f 1000"

@ echo "SEA Mixed"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (ConceptDriftStream -s (generators.SEAGenerator -f 2) -d (ConceptDriftStream -s (generators.SEAGenerator -f 3) -d (generators.SEAGenerator -f 4) -p 50000 -w 1) -p 25000 -w 10000) -i 100000 -f 1000"

@ echo "RandomRBFG"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (clustering.RandomRBFGeneratorEvents -n) -i 100000 -f 1000"


@ echo "Agrawal Sudden"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (ConceptDriftStream -s generators.AgrawalGenerator -d (ConceptDriftStream -s (generators.AgrawalGenerator -f 2) -d (ConceptDriftStream -s generators.AgrawalGenerator -d (generators.AgrawalGenerator -f 4) -p 25000 -w 1) -p 25000 -w 1) -p 25000 -w 1) -i 100000 -f 1000"

@ echo "Agrawal Mixed"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (ConceptDriftStream -s generators.AgrawalGenerator -d (ConceptDriftStream -s (generators.AgrawalGenerator -f 2) -d (ConceptDriftStream -s generators.AgrawalGenerator -d (generators.AgrawalGenerator -f 4) -p 25000 -w 10000) -p 25000 -w 1) -p 25000 -w 10000) -i 100000 -f 1000 -m"

@ echo "Sensor"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (ArffFileStream -f C:\test\Dataset\Drifts\sensor.arff) -i 100000 -f 1000"

@ echo "CoverType"
java -cp moa.jar;lib/* -javaagent:sizeofag.jar moa.DoTask "EvaluateSSLPrequential -s (ArffFileStream -f C:\test\DataSet\Drifts\covtypeNorm.arff) -i 100000 -f 1000"


[0.0, 3.0, 2.0, 3.0, 4.0
value2,value4,value3,value4,value5