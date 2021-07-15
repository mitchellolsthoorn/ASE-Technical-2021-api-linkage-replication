root=<ABOSOLUTE LOCATION OF evomaster.jar>

for i in {1..20}
do
        java -jar $evomaster --outputFormat JAVA_JUNIT_4 --maxTime 30m --algorithm MODELMOSA --crossoverModel AC --recalculationInterval 10 --run $i
done

for i in {1..20}
do
        java -jar $evomaster --outputFormat JAVA_JUNIT_4 --maxTime 30m --algorithm MIO --run $i
done

for i in {1..20}
do
        java -jar $evomaster --outputFormat JAVA_JUNIT_4 --maxTime 30m --algorithm MOSA --run $i
done
