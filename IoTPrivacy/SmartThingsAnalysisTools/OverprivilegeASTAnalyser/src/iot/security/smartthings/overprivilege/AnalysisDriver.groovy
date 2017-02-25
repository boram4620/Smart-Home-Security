/*
 * SmartThingsAnalysisTools Copyright 2016 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

package iot.security.smartthings.overprivilege

import org.codehaus.groovy.control.CompilerConfiguration

class AnalysisDriver {

    static main(def args) {

        def project_root = new File(".").getCanonicalPath()

        println("Working Path: " + project_root.toString())

        def outputFileName = project_root + "/" + "overprivout.txt"

        println("Output File Path: " + outputFileName.toString())

        def capsAsPerSamsungFile = project_root + "/" + "Capabilities.csv"

        def allCapsFile = project_root + "/" + "capfull.csv"

        println("allCaps File Path: " + allCapsFile.toString())

        def sourceCodeDir = project_root + "/" + "Top200Apps"

        def manualAnalysesReflection = project_root + "/" + "skip_apps_reflection_falsepos.txt"

        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        Logger log = new Logger(outputFileName)

        OPAnalysisAST opal = new OPAnalysisAST(log)
        def allCaps = new File(capsAsPerSamsungFile)
        def allCapsAll = new File(allCapsFile)
        opal.loadCapRefAll(allCapsAll)
        //opal.loadCapRef(allCaps)

        opal.loadCap2Dev(new File(project_root + "/" + "cap2dev.txt"))
        opal.loadDev2Cap(new File(project_root + "/" + "devhandlers2cap.txt"))
        //opal.dump_Dev2Cap()

        cc.addCompilationCustomizers(opal)

        //2016-6-30: Adds Supporting Libraries
        cc.classpath.add(project_root + "/pcompile/spring-beans-4.3.0.RELEASE.jar")
        cc.classpath.add(project_root + "/pcompile/grails-bootstrap-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/grails-core-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/grails-encoder-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/grails-web-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/grails-web-boot-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/grails-web-common-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/http-builder-0.7.1.jar")
        cc.classpath.add(project_root + "/pcompile/httpclient-4.5.2.jar")
        cc.classpath.add(project_root + "/pcompile/grails-compat-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/grails-plugin-converters-3.1.9.jar")
        cc.classpath.add(project_root + "/pcompile/httpcore-4.4.5.jar")
        cc.classpath.add(project_root + "/pcompile/smartthings-stub-classes.jar")
        cc.classpath.add(project_root + "/pcompile/json-20160212.jar")

        // NLG Support
        cc.classpath.add(project_root + "/pcompile/SimpleNLG-4.4.8.jar")

        // Static Analysis Support
        cc.classpath.add(project_root + "/pcompile/CodeNarc-0.25.2.jar")

        def reflFile = new File(manualAnalysesReflection)
        def reflectionSkip = new ArrayList();
        reflFile.eachLine { line -> reflectionSkip.add(line + ".txt") }

        GroovyShell gshell = new GroovyShell(cc)

        new File(sourceCodeDir).eachFile { file ->
            if (file.name.equals(".DS_Store")) {
                return;
            } else try {

                println "--> Start processing: ${file.getName()}"

                // To compute basic statistics, disable skipping of the reflection skip list files.

                if (!(file.getName() in reflectionSkip)) {
                    log.append "--app-start--"
                    log.append "processing ${file.getName()}"
                    gshell.evaluate(file)
                    log.append "--app-end--"
                } else
                    println "skipping ${file.getName()} due to reflection manual analyses"


            } catch (MissingMethodException mme) {
                // Skip method on *.definition since it does not contain any permission info.

                def missingMethod = mme.toString()

                if (!missingMethod.contains("definition()"))
                    log.append("missing method: " + missingMethod)
            }
        }

        opal.summarize()

    }
}
