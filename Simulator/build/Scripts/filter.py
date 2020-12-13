# Python script for running a filter script on multiple statistics files

# Example:
# defaultHeading="Model\tE[NQ]\tE[N]\tE[W]\tStd[W]\tE[V]\tStd[V]\trho"
# defaultDescription=lambda fileName: fileName.replace(".zip","")
# runFilter(defaultHeading,defaultDescription)

import glob
import subprocess

def runCmd(process, cmd):
    print("COMMAND: "+cmd)
    process.stdin.write(bytearray(cmd+"\n","utf-8"))
    process.stdin.flush()

def readLines(process):
    while (True):
        process.stdout.flush()
        output=process.stdout.readline()
        output=output.decode(encoding="utf-8", errors="ignore")
        if output.startswith("Bereit.") or output.startswith("Ready."): break
        print(output,end='')

def runFilter(heading, descriptionFunc, filterFile="filter.js", outputFile="results.txt", simulatorFile="../simulator.jar"):
    args=["java", "-jar", simulatorFile, "interactive"]
    process=subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    readLines(process)

    files=glob.glob("*.zip")

    output=open(outputFile, "w+")
    output.write(heading+"\n")
    output.close()

    nr=1
    for file in sorted(files):
        print("\nProcessing "+str(nr)+" of "+str(len(files)))
        nr=nr+1

        output=open(outputFile, "a+");
        output.write(descriptionFunc(file)+"\t")
        output.close()
        runCmd(process,"filter "+file+" "+filterFile+" "+outputFile)
        readLines(process)

    print("\nTerminating simulator")
    runCmd(process,"exit")
    process.communicate()