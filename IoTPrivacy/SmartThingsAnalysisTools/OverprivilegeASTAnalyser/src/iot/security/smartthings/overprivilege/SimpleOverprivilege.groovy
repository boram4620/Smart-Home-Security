/*
 * SmartThingsAnalysisTools Copyright 2016 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

package iot.security.smartthings.overprivilege

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase

class SimpleOverprivilege {

    Map allCommands
    Map allProps

    List allCommandsList
    List allPropsList
    List allCapsList

    int numCmdOverpriv
    int numAttrOverpriv
    int numTotalOverpriv

    public SimpleOverprivilege() {
        allCommands = new HashMap()
        allProps = new HashMap()

        allCommandsList = new ArrayList()
        allPropsList = new ArrayList()
        allCapsList = new ArrayList()

        numCmdOverpriv = 0
        numAttrOverpriv = 0
        numTotalOverpriv = 0
    }

    def loadCapRef(def file) {
        file.splitEachLine(",") { fields ->
            allCommands[fields[1]] = fields[3]
            allProps[fields[1]] = fields[2]
        }

        allCommands.each { k, v ->
            def values = v?.split(" ")
            values?.each { allCommandsList.add(it.toLowerCase()) }

            allCapsList.add(k.toLowerCase())
        }

        allProps.each { k, v ->
            def values = v?.split(" ")
            values?.each { allPropsList.add(it.toLowerCase()) }
        }

    }

    def processAllApps(def dir) {
        new File(dir).eachFile { file ->
            try {

                println "processing ${file.getName()}"
                processApp(file)


            } catch (MissingMethodException mme) {
                println("missing method: " + mme.toString())
            }
        }

        println "--> 1. Summary"
        println "1-1 Command Over-Privileged:" + numCmdOverpriv
        println "1-2 Attributes Over-Privileged:" + numAttrOverpriv
        println "Total Over-Privileged:" + numTotalOverpriv
    }

    def processApp(def file) {
        def astClassNodes = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, file.text).find {
            it.class == ClassNode.class
        }
        InsnVisitor insnVis = new InsnVisitor()
        astClassNodes.visitContents(insnVis)

        def calledMethods = insnVis.calledMethods
        def calledProps = insnVis.calledProps
        def reqCaps = insnVis.requestedCaps.intersect(allCapsList)

        println "${file.getName()} has asked for caps: " + reqCaps

        def reqCmdAttrs = getCmdAttr(reqCaps)
        def reqCmds = reqCmdAttrs[0]
        def reqAttrs = reqCmdAttrs[1]

        println "---> 2. Requested commands:" + reqCmds
        println "---> 3. Requested attrs:" + reqAttrs

        def filteredCalledMethods = calledMethods.intersect(allCommandsList)
        def filteredCalledProps = calledProps.intersect(allPropsList)

        println "called cap-methods by app"
        filteredCalledMethods.each { it -> println it }

        println "called cap-props by app"
        filteredCalledProps.each { it -> println it }

        // Compute over-privileged info.
        def cmdOverpriv = reqCmds.toSet() - filteredCalledMethods.toSet()
        def attrOverpriv = reqAttrs.toSet() - filteredCalledProps.toSet()

        println "cmd overpriv:" + cmdOverpriv
        println "attr overpriv:" + attrOverpriv

        if (cmdOverpriv.toList().size() > 0)
            numCmdOverpriv += 1

        if (attrOverpriv.toList().size() > 0)
            numAttrOverpriv += 1

        if (cmdOverpriv.toList().size() > 0 || attrOverpriv.toList().size() > 0)
            numTotalOverpriv += 1
    }

    // Given a list of cap name, get all its commands and attributes as lists.
    def getCmdAttr(def caps) {
        def cmds = new ArrayList()
        def attrs = new ArrayList()

        caps.each { capname ->
            def listOfCmds = allCommands[capname]

            def values = listOfCmds?.split(" ")
            values.each { cmds.add it }

            def listOfAttrs = allProps[capname]

            def values2 = listOfAttrs?.split(" ")
            values2.each { attrs.add it }
        }

        def combined = [cmds, attrs]
        return combined
    }
}
