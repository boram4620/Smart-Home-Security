/*
 * SmartThingsAnalysisTools Copyright 2016 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

package iot.security.smartthings.overprivilege

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.GeneratorContext

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.messages.LocatedMessage
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.transform.GroovyASTTransformation

import simplenlg.framework.*
import simplenlg.lexicon.*
import simplenlg.realiser.english.*
import simplenlg.phrasespec.*

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class OPAnalysisAST extends CompilationCustomizer {

    Map allCommands
    Map allProps

    Map dev2cap
    Map cap2dev

    // Define capability description mapping.
    Map<String, String> Cap_Map = [
            'accelerationsensor'         : 'acceleration sensor',
            'alarm'                      : 'alarm',
            'battery'                    : 'battery',
            'beacon'                     : 'beacon',
            'button'                     : 'button',
            'carbonDioxideMeasurement'   : 'carbon dioxide sensor',
            'carbonMonoxideDetector'     : 'carbon monoxide detector',
            'colorControl'               : 'light color control',
            'colorTemperature'           : 'light color temperature',
            'contactSensor'              : 'contact sensor',
            'doorControl'                : 'door control',
            'energyMeter'                : 'energy meter',
            'garageDoorControl'          : 'garage door control',
            'illuminanceMeasurement'     : 'light sensor',
            'imageCapture'               : 'camera',
            'indicator'                  : 'LED indicator',
            'lock'                       : 'lock',
            'mediaController'            : 'media controller',
            'motionSensor'               : 'motion sensor',
            'musicPlayer'                : 'music player',
            'occupancy'                  : 'occupancy sensor',
            'pHMeasurement'              : 'PH sensor',
            'polling'                    : 'polling device',
            'powerMeter'                 : 'power meter',
            'power'                      : 'power',
            'presenceSensor'             : 'presence sensor',
            'relativeHumidityMeasurement': 'humidity sensor',
            'relaySwitch'                : 'relay switch',
            'shockSensor'                : 'shock sensor',
            'sleepSensor'                : 'sleep sensor',
            'smokeDetector'              : 'smoke detector',
            'soundSensor'                : 'sound sensor',
            'speechRecognition'          : 'speech recognition device',
            'speechSynthesis'            : 'speech synthesis device',
            'stepSensor'                 : 'step sensor',
            'switch'                     : 'switch',
            'switchLevel'                : 'light level controller',
            'soundPressureLevel'         : 'sound pressure level sensor',
            'tamperAlert'                : 'tamper sensor',
            'temperaturemeasurement'     : 'temperature sensor',
            'thermostat'                 : 'thermostat',
            'thermostatCoolingSetpoint'  : 'thermostat cooling set-point',
            'thermostatFanMode'          : 'thermostat fan mode',
            'thermostatHeatingSetpoint'  : 'thermostat heating set-point',
            'thermostatMode'             : 'thermostat mode',
            'thermostatOperatingState'   : 'thermostat operating state',
            'thermostatSetpoint'         : 'thermostat set-point',
            'threeAxis'                  : 'three axis sensor',
            'tone'                       : 'beeper',
            'touchSensor'                : 'touch sensor',
            'ultravioletIndex'           : 'ultraviolet index sensor',
            'valve'                      : 'valve',
            'voltageMeasurement'         : 'voltage sensor',
            'watersensor'                : 'water sensor',
            'windowShade'                : 'window shade',
    ]

    // Define sensitive command description mapping.
    Map<String, String> Sen_Com_Map = [
            'on'                  : 'turn on',
            'strobe'              : 'strobe',
            'siren'               : 'siren',
            'both'                : 'strobe and siren',
            'open'                : 'open',
            'close'               : 'close',
            'take'                : 'take photo',
            'unlock'              : 'unlock',
            'startActivity'       : 'start activity',
            'getAllActivities'    : 'get all activities',
            'getCurrentActivity'  : 'get current activity',
            'play'                : 'play',
            'pause'               : 'pause',
            'stop'                : 'stop',
            'playTrack'           : 'play track',
            'setHeatingSetpoint'  : 'adjust thermostat heating set-point',
            'setCoolingSetpoint'  : 'adjust thermostat cooling set-point',
            'heat'                : 'adjust thermostat heat mode',
            'cool'                : 'adjust thermostat cool mode',
            'setThermostatMode'   : 'adjust thermostat mode',
            'setThermostatFanMode': 'adjust thermostat fan mode',
    ]

    // Define less sensitive command description mapping.
    Map<String, String> Less_Com_Map = [
            'off'                : 'turn off',
            'setHue'             : 'set light hue',
            'setSaturation'      : 'set light saturation',
            'setColor'           : 'set light color',
            'setColorTemperature': 'set light color temperature',
            'open'               : 'open',
            'close'              : 'close',
            'take'               : 'take photo',
            'lock'               : 'lock',
            'startActivity'      : 'start activity',
            'getAllActivities'   : 'get all activities',
            'getCurrentActivity' : 'get current activity',
            'play'               : 'play',
            'pause'              : 'pause',
            'stop'               : 'stop',
            'playTrack'          : 'play track',
    ]

    // Define subscription description mapping.
    Map<String, String> Sub_Map = [
            'acceleration.active'       : 'acceleration active',
            'acceleration.inactive'     : 'acceleration inactive',
            'alarm.strobe'              : 'alarm strobe',
            'alarm.siren'               : 'alarm siren',
            'alarm.both'                : 'alarm strobe and siren',
            'alarm.off'                 : 'alarm off',
            'presenceSensor.present'    : 'people present',
            'presenceSensor.not present': 'people not present',
            'button.held'               : 'button held',
            'button.pushed'             : 'button pushed',
            'carbonMonoxide.tested'     : 'carbon monoxide tested',
            'carbonMonoxide.clear'      : 'carbon monoxide clear',
            'carbonMonoxide.detected'   : 'carbon monoxide detected',
    ]

    // Define attribute description mapping.
    Map<String, String> Attr_Map = [
            'alarm'                    : 'alarm',
            'button'                   : 'button',
            'doorControl'              : 'door control',
            'garageDoorControl'        : 'garage door control',
            'lock'                     : 'lock',
            'mediaController'          : 'media controller',
            'motionSensor'             : 'motion sensor',
            'musicPlayer'              : 'music player',
            'relaySwitch'              : 'relay switch',
            'speechRecognition'        : 'speech recognition device',
            'speechSynthesis'          : 'speech synthesis device',
            'switch'                   : 'switch',
            'thermostat'               : 'thermostat',
            'thermostatCoolingSetpoint': 'thermostat cooling set-point',
            'thermostatFanMode'        : 'thermostat fan mode',
            'thermostatHeatingSetpoint': 'thermostat heating set-point',
            'thermostatMode'           : 'thermostat mode',
            'thermostatOperatingState' : 'thermostat operating state',
            'thermostatSetpoint'       : 'thermostat set-point',
            'tone'                     : 'beeper',
            'valve'                    : 'valve',
            'windowShade'              : 'window shade',
    ]

    List allCommandsList
    List allPropsList
    List allCapsList

    int numCmdOverpriv
    int numAttrOverpriv
    int numTotalOverpriv
    int numReflection
    int type2_numCaps
    int samename_flags
    int type2_cmdattr_uses

    int numSendSms
    int numOAuth
    int numInternet

    Logger log

    public OPAnalysisAST(Logger logger) {
        super(CompilePhase.SEMANTIC_ANALYSIS)

        allCommands = new HashMap()
        allProps = new HashMap()

        cap2dev = new HashMap()
        dev2cap = new HashMap()

        allCommandsList = new ArrayList()
        allPropsList = new ArrayList()
        allCapsList = new ArrayList()

        numCmdOverpriv = 0
        numAttrOverpriv = 0
        numTotalOverpriv = 0
        numReflection = 0

        type2_numCaps = 0

        samename_flags = 0
        type2_cmdattr_uses = 0

        numSendSms = 0
        numOAuth = 0
        numInternet = 0

        log = logger
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {

        //Use method code visitor to collect declaration expressions
        MethodCodeVisitor mcvDex = new MethodCodeVisitor()
        classNode.visitContents(mcvDex)

        //Run an instruction visitor
        InsnVisitor insnVis = new InsnVisitor(mcvDex.dexpressions, mcvDex.bexpressions)
        classNode.visitContents(insnVis)

        def allMethodNodes = classNode.getAllDeclaredMethods()
        def allFieldNodes = classNode.getFields()

        ArrayList<String> declaredMethods = new ArrayList<String>()
        allMethodNodes.each { it -> declaredMethods.add(it.getName().toLowerCase()) }

        processApp(insnVis, declaredMethods)

        // Analyze the number of unused capabilities. These unused capabilities come from the device
        // handlers with multiple capabilities.
        analyzePermissions(insnVis, declaredMethods)
    }

    class MethodCodeVisitor extends ClassCodeVisitorSupport {
        public ArrayList<String> globals
        public ArrayList<DeclarationExpression> dexpressions
        public ArrayList<BinaryExpression> bexpressions
        public ArrayList<IfStatement> conditions

        protected ClassNode currentClassNode
        protected Set<ClosureExpression> transformedExpressions = new HashSet<ClosureExpression>()

        public MethodCodeVisitor() {
            globals = new ArrayList<String>()
            dexpressions = new ArrayList<DeclarationExpression>()
            bexpressions = new ArrayList<BinaryExpression>()
            conditions = new ArrayList<IfStatement>()
        }

        // Defines binary expressions
        @Override
        public void visitBinaryExpression(BinaryExpression bex) {
            bexpressions.add(bex)
        }

        // Defines declararion expressions
        @Override
        public void visitDeclarationExpression(DeclarationExpression dex) {

            dexpressions.add(dex)

            if (!dex.isMultipleAssignmentDeclaration()) {
                VariableExpression left = dex.getVariableExpression()
                globals.add(left.getName().toLowerCase())
            } else {
                TupleExpression tex = dex.getTupleExpression()
                List<Expression> lefts = tex.getExpressions()
                lefts.each { it -> globals.add(it.getName().toLowerCase()) }
            }

            super.visitDeclarationExpression(dex)
        }

        // Defines the visit classes
        @Override
        public void visitClass(ClassNode node) {
            try {
                this.currentClassNode = node
                super.visitClass(node)
            } catch (Exception e) {
                logTransformationError(node, e)
            } finally {
                currentClassNode = null;
                transformedExpressions.clear()
            }
        }

        // Determine if it is the valid methods to be analyzed
        private boolean isCandidateMethod(String methodName, Expression arguments, Set<String> candidateMethods) {
            if (candidateMethods.contains(methodName)) {
                if (arguments instanceof ArgumentListExpression) {
                    ArgumentListExpression ale = (ArgumentListExpression) arguments
                    List<Expression> expressions = ale.getExpressions()
                    if (expressions.size() > 0) {
                        Expression expression = expressions.get(expressions.size() - 1)
                        if (expression instanceof ClosureExpression) {
                            return true
                        } else if (expression instanceof VariableExpression) {
                            VariableExpression ve = (VariableExpression) expression
                            return true
                        }
                    }
                }
            }

            return false;
        }

        // Determine if it is the valid methods to be analyzed
        private boolean isCandidateWhereMethod(String methodName, Expression arguments) {
            return isCandidateMethod(methodName, arguments, CANDIDATE_METHODS)
        }

        // Defines visit fields and expressions in the AST sytax tree
        @Override
        public void visitField(FieldNode node) {
            ClassNode classNode = node.getOwner();
            if (node.isStatic()) {
                Expression initialExpression = node.getInitialExpression()
                if (initialExpression instanceof MethodCallExpression) {
                    MethodCallExpression mce = (MethodCallExpression) initialExpression;

                    if (isCandidateWhereMethod(mce.getMethod(), mce.getArguments())) {
                        ArgumentListExpression args = (ArgumentListExpression) mce.getArguments()
                        Expression target = mce.getObjectExpression()

                        List<Expression> argsExpressions = args.getExpressions()
                        int totalExpressions = argsExpressions.size()
                        if (totalExpressions > 0) {
                            Expression expression = argsExpressions.get(totalExpressions - 1)
                            if (expression instanceof ClosureExpression) {
                                ClosureExpression closureExpression = (ClosureExpression) expression
                                transformClosureExpression(classNode, closureExpression)
                                if (target instanceof VariableExpression) {
                                    VariableExpression ve = (VariableExpression) target
                                    ClassNode type = ve.getType()
                                }

                                MethodCallExpression newInitialExpression = new MethodCallExpression(new ConstructorCallExpression(detachedCriteriaClassNode, new ArgumentListExpression(new ClassExpression(classNode))), buildMethod, new ArgumentListExpression(closureExpression));
                                node.setInitialValueExpression(newInitialExpression)
                            }
                        }
                    }
                }
            } else {
                try {
                    Expression initialExpression = node.getInitialExpression()
                    ClosureExpression newClosureExpression = initialExpression

                    if (newClosureExpression != null) {
                        node.setInitialValueExpression(newClosureExpression)
                    }
                } catch (Exception e) {
                    logTransformationError(node, e)
                }
            }

            super.visitField(node);
        }


        // Transform closure expression for better analysis result
        public void transformClosureExpression(ClassNode classNode, ClosureExpression closureExpression) {
            if (transformedExpressions.contains(closureExpression)) {
                return
            }
            ClassNode previousClassNode = this.currentClassNode
            try {
                this.currentClassNode = classNode
                List<String> propertyNames = classNode.getText()
                Statement code = closureExpression.getCode()
                BlockStatement newCode = new BlockStatement()
                boolean addAll = false

                if (code instanceof BlockStatement) {
                    BlockStatement bs = (BlockStatement) code

                    visitBlockStatement(bs, newCode, addAll, propertyNames, closureExpression.getVariableScope());
                    newCode.setVariableScope(bs.getVariableScope())
                }

                if (!newCode.getStatements().isEmpty()) {
                    transformedExpressions.add(closureExpression)
                    closureExpression.setCode(newCode)
                }
            } finally {
                this.currentClassNode = previousClassNode;
            }
        }

        // Analyze block statements
        public void visitBlockStatement(BlockStatement blockStatement, BlockStatement newCode, boolean addAll, List<String> propertyNames, VariableScope variableScope) {
            List<Statement> statements = blockStatement.getStatements()
            for (Statement statement : statements) {
                visitStatement(statement, newCode, addAll, propertyNames, variableScope)
            }
        }

        // Format block statements
        private Statement flattenStatement(BlockStatement blockStatement) {
            if (blockStatement.getStatements().size() == 1) {
                return blockStatement.getStatements().get(0)
            }
            return blockStatement
        }

        // Analyze conditional statements.
        public void visitStatement(Statement statement, BlockStatement result, boolean addAll,
                                   List<String> stmtName, VariableScope variableScope) {

            if (statement instanceof BlockStatement) {
                visitBlockStatement((BlockStatement) statement, result, addAll, stmtName, variableScope)
            } else if (statement instanceof IfStatement) {
                IfStatement ifs = (IfStatement) statement
                Statement ifb = ifs.getIfBlock()
                BlockStatement newIfBlock = new BlockStatement()
                visitStatement(ifb, newIfBlock, addAll, stmtName, variableScope)
                ifs.setIfBlock(flattenStatement(newIfBlock))

                Statement elseBlock = ifs.getElseBlock()
                if (elseBlock != null) {
                    BlockStatement newElseBlock = new BlockStatement()
                    visitStatement(elseBlock, newElseBlock, addAll, stmtName, variableScope)
                    ifs.setElseBlock(flattenStatement(newElseBlock))
                }
                result.addStatement(ifs)

            } else if (statement instanceof SwitchStatement) {
                SwitchStatement sw = (SwitchStatement) statement

                List<CaseStatement> caseStatements = sw.getCaseStatements()
                for (CaseStatement caseStatement : caseStatements) {
                    Statement existingCode = caseStatement.getCode()
                    BlockStatement newCaseCode = new BlockStatement()
                    visitStatement(existingCode, newCaseCode, addAll, stmtName, variableScope)
                    caseStatement.setCode(flattenStatement(newCaseCode))
                }

                result.addStatement(sw)
            } else if (statement instanceof ForStatement) {
                ForStatement fs = (ForStatement) statement
                Statement loopBlock = fs.getLoopBlock()
                BlockStatement newLoopBlock = new BlockStatement()
                visitStatement(loopBlock, newLoopBlock, addAll, stmtName, variableScope)
                fs.setLoopBlock(flattenStatement(newLoopBlock))
                result.addStatement(fs)
            } else if (statement instanceof WhileStatement) {
                WhileStatement ws = (WhileStatement) statement
                Statement loopBlock = ws.getLoopBlock()
                BlockStatement newLoopBlock = new BlockStatement()
                visitStatement(loopBlock, newLoopBlock, addAll, stmtName, variableScope)
                ws.setLoopBlock(flattenStatement(newLoopBlock))
                result.addStatement(ws)
            } else if (statement instanceof TryCatchStatement) {
                TryCatchStatement tcs = (TryCatchStatement) statement
                Statement tryStatement = tcs.getTryStatement()

                BlockStatement newTryStatement = new BlockStatement()
                visitStatement(tryStatement, newTryStatement, addAll, stmtName, variableScope)
                tcs.setTryStatement(flattenStatement(newTryStatement))

                List<CatchStatement> catchStatements = tcs.getCatchStatements()

                for (CatchStatement catchStatement : catchStatements) {
                    BlockStatement newCatchStatement = new BlockStatement()
                    Statement code = catchStatement.getCode()
                    visitStatement(code, newCatchStatement, addAll, stmtName, variableScope)
                    catchStatement.setCode(flattenStatement(newCatchStatement))
                }

                Statement finallyStatement = tcs.getFinallyStatement()
                if (finallyStatement != null) {
                    BlockStatement newFinallyStatement = new BlockStatement()
                    visitStatement(finallyStatement, newFinallyStatement, addAll, stmtName, variableScope)
                    tcs.setFinallyStatement(flattenStatement(newFinallyStatement))
                }
                result.addStatement(tcs);
            } else if (statement instanceof ReturnStatement) {
                ReturnStatement rs = (ReturnStatement) statement
                visitStatement(new ExpressionStatement(rs.getExpression()), result, addAll, stmtName, variableScope)

            } else {
                result.addStatement(statement)
            }
        }

        // Handel error during code AST syntax transformation.
        private void logTransformationError(ASTNode astNode, Exception e) {
            StringBuilder message = new StringBuilder("Fatal error occurred applying transformations [ " + e.getMessage() + "] to source [" + sourceUnit.getName() + "]. Please report an issue.");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            message.append(System.getProperty("line.separator"));
            message.append(sw.toString());
            sourceUnit.getErrorCollector().addError(new LocatedMessage(message.toString(), Token.newString(astNode.getText(), astNode.getLineNumber(), astNode.getColumnNumber()), sourceUnit));
        }

        // Process AST from a single source unit (which means an SmartApp).
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }

    class InsnVisitor extends ClassCodeVisitorSupport {
        Set<String> calledMethods
        Set<String> calledProps
        Set<String> requestedCaps
        Set<String> requestedComds
        Set<String> usedModes
        Set<String> usedTime
        Set<String> usedPhoneNumber
        Set<String> usedZipCode

        Set<String> securityDes

        List<String> declaredCapVars
        List<String> declaredGlobalVars
        List<String> usedAttrs
        List<String> usedComds
        Map attrSubs
        int globalBodyEntries = 0
        ArrayList<DeclarationExpression> alldexprs
        ArrayList<BinaryExpression> allbexprs

        boolean usesAddChildDevice
        boolean usesOAuth
        boolean usesSendSms
        boolean usesInternet

        Lexicon lexicon = Lexicon.getDefaultLexicon()
        NLGFactory nlgFactory = new NLGFactory(lexicon)
        Realiser realiser = new Realiser(lexicon)

        public InsnVisitor(ArrayList<DeclarationExpression> alldex, ArrayList<BinaryExpression> allbex) {
            calledMethods = new HashSet<String>()
            calledProps = new HashSet<String>()
            requestedCaps = new HashSet<String>()
            usedModes = new HashSet<String>()
            usedTime = new HashSet<String>()
            usedPhoneNumber = new HashSet<String>()
            usedZipCode = new HashSet<String>()

            securityDes = new HashSet<String>()

            declaredCapVars = new ArrayList<String>()
            attrSubs = new HashMap<String, HashSet<String>>()
            usedAttrs = new ArrayList<String>()

            usesAddChildDevice = false
            usesOAuth = false
            usesSendSms = false
            usesInternet = false

            alldexprs = alldex
            allbexprs = allbex
        }

        @Override
        void visitMethodCallExpression(MethodCallExpression mce) {
            println mce.getMethodAsString()

            def methText

            // Get method text from method calls in code.
            if (mce.getMethodAsString() == null) {
                methText = mce.getText()
            } else {
                methText = mce.getMethodAsString()
            }

            def recver = mce.getReceiver()
            if (recver instanceof VariableExpression) {

                VariableExpression recvex = (VariableExpression) recver

                // Capability commands will never be invoked on the "this" static variable.
                // So have to filter those out from the collection of method text.
                if (!recvex.getName().equals("this")) {

                    // Handle special cases with imageCapture.take and list.take
                    if (methText.equals("take")) {
                        if (mce.getArguments().toList().size() > 0) {
                            mce.getArguments().each { arg ->
                                if (arg instanceof NamedArgumentListExpression) {
                                    NamedArgumentListExpression nnale = (NamedArgumentListExpression) arg
                                    nnale.mapEntryExpressions.each { mee ->
                                        def keyexprText = mee.getKeyExpression().getText()

                                        if (keyexprText.equals("delay")) {
                                            calledMethods.add(methText)
                                        }
                                    }
                                }
                            }
                        } else
                            calledMethods.add(methText)

                    } else
                        calledMethods.add(methText)
                }
            }

            if (recver instanceof PropertyExpression) {
                PropertyExpression pex = (PropertyExpression) recver
                if (!pex.getPropertyAsString().equals("this")) {
                    if (methText.equals("take")) {
                        if (mce.getArguments().toList().size() > 0) {
                            mce.getArguments().each { arg ->
                                if (arg instanceof NamedArgumentListExpression) {
                                    NamedArgumentListExpression nnale = (NamedArgumentListExpression) arg
                                    nnale.mapEntryExpressions.each { mee ->
                                        def keyexprText = mee.getKeyExpression().getText()

                                        if (keyexprText.equals("delay")) {
                                            calledMethods.add(methText)
                                        }
                                    }
                                }
                            }
                        } else
                            calledMethods.add(methText)

                    } else
                        calledMethods.add(methText)
                }
            }

            // Handle cases when in some Apps input sets can be redefined as ifSet sets.
            if (methText.equals("input") ||
                    methText.equals("ifSet")) {

                def args = mce.getArguments()

                args.each { arg ->
                    if (arg instanceof ConstantExpression) {
                        def txt = arg.getText()?.toLowerCase()

                        // Collect requested capability names from the App.
                        if (txt.contains("capability.")) {
                            requestedCaps.add(txt)
                            if (Cap_Map.containsKey(requestedCaps)) {
                                SPhraseSpec capInfo = nlgFactory.createClause()
                                capInfo.setSubject("The App")
                                capInfo.setVerb("request")
                                capInfo.setObject(requestedCaps.toString())
                                String capMsg = realiser.realiseSentence(capInfo)
                            }
                            SPhraseSpec capInfo = nlgFactory.createClause()
                            capInfo.setSubject("The App")
                            capInfo.setVerb("request")
                            capInfo.setObject(requestedCaps.toString())
                            String capMsg = realiser.realiseSentence(capInfo)
                            securityDes.add(capMsg)
                        } else if (txt.contains("mode")) {
                            // Collect requested mode information from the App.
                            usedModes.add(txt)
                        } else if (txt.contains("time")) {
                            // Collect requested time information from the App.
                            usedTime.add(txt)
                        } else if (txt.contains("phone")) {
                            // Collect requested phone information from the App.
                            usedPhoneNumber.add(txt)
                        } else if (txt.toLowerCase().contains("zipcode")) {
                            // Collect requested zip code information from the App.
                            usedZipCode.add(txt)
                        }

                    } else if (arg instanceof MapExpression ||
                            arg instanceof VariableExpression ||
                            arg instanceof PropertyExpression) {
                        MapExpression mex = null

                        if (arg instanceof PropertyExpression) {
                            def property = ((PropertyExpression) arg).getText()

                            // Search through the collected binary expressions from AST.
                            allbexprs.each { bexpr ->
                                if (bexpr.getLeftExpression() instanceof PropertyExpression) {
                                    def leftProperty = (PropertyExpression) bexpr.getLeftExpression()
                                    if (leftProperty.getText().equals(property)) {
                                        if (bexpr.getRightExpression() instanceof ConstantExpression) {
                                            def right = (ConstantExpression) bexpr.getRightExpression()
                                            def rightText = right.getText()
                                            println "followed a Binary Property Constant expression"
                                            println rightText + " is assigned to " + leftProperty.getText()
                                            if (rightText.contains("capability."))
                                                requestedCaps.add(rightText.toLowerCase())
                                        }
                                    }
                                }
                            }
                        } else if (arg instanceof VariableExpression) {
                            // Search through the AST once to find the DeclarationExpression
                            // for this VariableExpression.
                            VariableExpression argvex = (VariableExpression) arg
                            def varName = argvex.getName()

                            for (BinaryExpression bexp in allbexprs) {

                                // Handle input map variable.
                                if (bexp.getRightExpression() instanceof MapExpression) {
                                    if (bexp.getLeftExpression() instanceof VariableExpression) {

                                        // Case 1: the left is a Variable, right is a Map.
                                        def testVarName = ((VariableExpression) bexp.getLeftExpression()).getName()
                                        if (testVarName.equals(varName)) {
                                            mex = (MapExpression) bexp.getRightExpression()
                                            break
                                        }
                                    }
                                } else if (bexp.getRightExpression() instanceof ConstantExpression) {
                                    if (bexp.getLeftExpression() instanceof VariableExpression) {

                                        // Case 2: the left is a Variable, right is a constant.
                                        def testVarName = ((VariableExpression) bexp.getLeftExpression()).getName()
                                        if (testVarName.equals(varName)) {
                                            def txt = bexp.getRightExpression().getText()?.toLowerCase()
                                            if (txt.contains("capability.")) {
                                                requestedCaps.add(txt.toLowerCase())
                                                SPhraseSpec capInfo = nlgFactory.createClause()
                                                capInfo.setSubject("The App")
                                                capInfo.setVerb("request")
                                                capInfo.setObject(requestedCaps)
                                                String capMsg = realiser.realiseSentence(capInfo)
                                                securityDes.add(capMsg)
                                            }
                                        }
                                    }
                                }
                            }
                        } else
                            mex = (MapExpression) arg

                        mex?.mapEntryExpressions.each { inner ->

                            def keyExpr = inner.getKeyExpression()
                            def valExpr = inner.getValueExpression()

                            if (valExpr instanceof ConstantExpression) {
                                ConstantExpression cap_exp = (ConstantExpression) valExpr
                                def txt = cap_exp.getText()?.toLowerCase()
                                if (txt.contains("capability.")) {
                                    requestedCaps.add(txt)
                                    SPhraseSpec capInfo = nlgFactory.createClause()
                                    capInfo.setSubject("The App")
                                    capInfo.setVerb("request")
                                    capInfo.setObject(requestedCaps.toString())
                                    String capMsg = realiser.realiseSentence(capInfo)
                                    securityDes.add(capMsg)
                                }
                            }
                        }
                    } else if (arg instanceof NamedArgumentListExpression) {
                        NamedArgumentListExpression nnale = (NamedArgumentListExpression) arg
                        nnale?.mapEntryExpressions.each { inner ->

                            def keyExpr = inner.getKeyExpression()
                            def valExpr = inner.getValueExpression()

                            if (valExpr instanceof ConstantExpression) {
                                ConstantExpression cap_exp = (ConstantExpression) valExpr
                                def txt = cap_exp.getText()?.toLowerCase()
                                if (txt.contains("capability.")) {
                                    requestedCaps.add(txt)
                                    SPhraseSpec capInfo = nlgFactory.createClause()
                                    capInfo.setSubject("The App")
                                    capInfo.setVerb("request")
                                    capInfo.setObject(requestedCaps.toString())
                                    String capMsg = realiser.realiseSentence(capInfo)
                                    securityDes.add(capMsg)
                                }
                            }
                        }
                    }
                }
            }


            // Search for the declared capability variable and the capability itself.
            if (methText.equals("input")) {
                def args = mce.getArguments()

                List cexp = new ArrayList()

                args.each { arg ->
                    if (arg instanceof ConstantExpression) {
                        cexp.add((ConstantExpression) arg)
                    } else if (arg instanceof MapExpression) {
                        MapExpression mex = (MapExpression) arg
                        mex?.mapEntryExpressions.each { inner ->

                            def keyExpr = inner.getKeyExpression()
                            def valExpr = inner.getValueExpression()

                            if (keyExpr instanceof ConstantExpression &&
                                    valExpr instanceof ConstantExpression) {
                                ConstantExpression capVar_cexp = (ConstantExpression) keyExpr
                                ConstantExpression cap_exp = (ConstantExpression) valExpr

                                if (capVar_cexp.getText()?.toLowerCase().equals("name")) {
                                    if (capVar_cexp.getText()?.toLowerCase().equals("name")) {
                                        cexp[0] = cap_exp
                                    } else if (capVar_cexp.getText()?.toLowerCase().equals("type")) {
                                        cexp[1] = cap_exp
                                    }
                                }
                            }

                            if (cexp.size() == 2) {

                                def theReqCap = cexp[1]?.getText()?.toLowerCase()
                                def theDecCapVar = cexp[0]?.getText()

                                requestedCaps.add(theReqCap)
                                declaredCapVars.add(theDecCapVar)

                                println theDecCapVar + "," + theReqCap

                                cexp.clear()
                            }
                        }
                    } else if (arg instanceof NamedArgumentListExpression) {
                        NamedArgumentListExpression nnale = (NamedArgumentListExpression) arg
                        nnale?.mapEntryExpressions.each { inner ->

                            def keyExpr = inner.getKeyExpression()
                            def valExpr = inner.getValueExpression()

                            if (keyExpr instanceof ConstantExpression &&
                                    valExpr instanceof ConstantExpression) {
                                ConstantExpression capVar_cexp = (ConstantExpression) keyExpr
                                ConstantExpression cap_exp = (ConstantExpression) valExpr

                                if (capVar_cexp.getText()?.toLowerCase().equals("name")) {
                                    cexp[0] = cap_exp
                                } else if (capVar_cexp.getText()?.toLowerCase().equals("type")) {
                                    cexp[1] = cap_exp
                                }
                            }

                            // Find the requested capability and name.
                            if (cexp.size() == 2) {

                                def theReqCap = cexp[1]?.getText()?.toLowerCase()
                                def theDecCapVar = cexp[0]?.getText()

                                requestedCaps.add(theReqCap)
                                declaredCapVars.add(theDecCapVar)

                                println theDecCapVar + "," + theReqCap

                                cexp.clear()
                            }
                        }
                    }

                    // When none of the Map Expression declarations are used,
                    // use this to get the requested capability.
                    if (cexp.size() == 2) {

                        def theReqCap = cexp[1].getText()?.toLowerCase()
                        def theDecCapVar = cexp[0].getText()

                        requestedCaps.add(theReqCap)
                        declaredCapVars.add(theDecCapVar)

                        cexp.clear()
                    }
                }
            }


            // Listen to whether a device was sent the requested command.
            if (methText.equals("subscribeToCommand")) {
                log.append "subscribeToCommand"
                this.append_manual()

            }

            if (methText.equals("subscribe")) {
                def args = mce.getArguments()
                def skipCheck = false

                if (args.size() > 0) {
                    // Handle location and app subscriptions.
                    if (args[0] instanceof VariableExpression) {
                        VariableExpression argvex0 = (VariableExpression) args[0]
                        if (argvex0.getName().equals("location") ||
                                argvex0.getName().equals("app")) {
                            skipCheck = true
                        }
                    }

                    if (!skipCheck) {
                        if (args[1] instanceof ConstantExpression) {
                            ConstantExpression cexp = (ConstantExpression) args[1]
                            def cexptext = cexp.getText()
                            def attrUse = cexptext
                            if (cexptext.contains(".")) {
                                int index = cexptext.indexOf('.')
                                attrUse = cexptext.substring(0, index)
                            }

                            usedAttrs.add(attrUse)

                        } else {
                            log.append "subscribe: is not a Constant Expression!"
                            this.append_manual()

                            if (!(args[0] instanceof VariableExpression)) {
                                log.append "subscribe: first argument is not a Variable Expression!"
                                this.append_manual()
                            }
                        }
                    }

                    if (args[0] instanceof VariableExpression &&
                            args[1] instanceof ConstantExpression) {

                        // devexp is an item from the input method call and represents a device
                        // cexp is the use of the device attribute. A subscribe call often listens
                        // to state changes in attribute values of capabilities. So as to count
                        // a subscribe expression as a use of an attribute.
                        VariableExpression devexp = (VariableExpression) args[0]
                        ConstantExpression cexp = (ConstantExpression) args[1]

                        def cexptext = cexp.getText()
                        def attrUse = cexptext
                        if (cexptext.contains(".")) {
                            int index = cexptext.indexOf('.')
                            attrUse = cexptext.substring(0, index)
                        }

                        def capVar = devexp.getText()
                        if (attrSubs.containsKey(capVar)) {
                            attrSubs[capVar].add(attrUse)
                        } else {
                            Set uses = new HashSet<String>()
                            uses.add(attrUse)
                            attrSubs.put(capVar, uses)
                        }
                    }
                }
            }

            if (methText.equals("currentState") || methText.equals("currentValue")) {
                def args = mce.getArguments()

                // Only a constant is passed in the method.
                if (args.size() == 1) {

                    if (args[0] instanceof ConstantExpression) {
                        ConstantExpression cexp = (ConstantExpression) args[0]
                        usedAttrs.add(cexp.getText()?.toLowerCase())
                        SPhraseSpec attrInfo = nlgFactory.createClause()
                        attrInfo.setSubject("The App")
                        attrInfo.setVerb("use")
                        attrInfo.setObject(usedAttrs.toString())
                        String attrMsg = realiser.realiseSentence(attrInfo)
                        securityDes.add(attrMsg)
                    } else {
                        log.append mce.getMethodAsString() + ", argument not a Constant Expression!"
                        this.append_manual()
                    }
                }
            }

            if (methText.equals("latestState") || methText.equals("latestValue")) {
                def args = mce.getArguments()

                // Only a constant is passed in the method.
                if (args.size() == 1) {

                    if (args[0] instanceof ConstantExpression) {
                        ConstantExpression cexp = (ConstantExpression) args[0]
                        usedAttrs.add(cexp.getText()?.toLowerCase())
                        SPhraseSpec attrInfo = nlgFactory.createClause()
                        attrInfo.setSubject("The App")
                        attrInfo.setVerb("use")
                        attrInfo.setObject(usedAttrs.toString())
                        String attrMsg = realiser.realiseSentence(attrInfo)
                        securityDes.add(attrMsg)
                    } else {
                        log.append mce.getMethodAsString() + ", argument not a Constant Expression!"
                        this.append_manual()
                    }
                }
            }

            if (methText.equals("statesSince") || methText.equals("statesBetween")) {
                def args = mce.getArguments()

                // For states methods, only use the first argument, which is the attribute name.
                if (args[0] instanceof ConstantExpression) {
                    ConstantExpression cexp = (ConstantExpression) args[0]
                    usedAttrs.add(cexp.getText()?.toLowerCase())
                } else {
                    log.append mce.getMethodAsString() + ", argument not a Constant Expression!"
                    this.append_manual()
                }
            }

            // Filter out possible reflection call.
            if (methText.contains("\$")) {
                def args = mce.getArguments()
                if (args.toList().size() > 0) {
                    log.append "Reflective call: " + methText + ", count:" + args.toList().size()
                    SPhraseSpec methInfo = nlgFactory.createClause()
                    methInfo.setSubject("The App")
                    methInfo.setVerb("invoke")
                    methInfo.setObject("reflective method call")
                    String methMsg = realiser.realiseSentence(methInfo)
                    securityDes.add(methMsg)
                }
            }

            // Add child devices gives the SmartApp access to a device handler without using
            // an input statement is also a case with over-privileged.
            if (methText.contains("addChildDevice")) {
                usesAddChildDevice = true
                SPhraseSpec methInfo = nlgFactory.createClause()
                methInfo.setSubject("The App")
                methInfo.setVerb("add")
                methInfo.setObject("child device")
                String methMsg = realiser.realiseSentence(methInfo)
                securityDes.add(methMsg)
            }

            if (methText.contains("sendSms") || methText.contains("sendSmsMessage")) {
                usesSendSms = true
                SPhraseSpec methInfo = nlgFactory.createClause()
                methInfo.setSubject("The App")
                methInfo.setVerb("send")
                methInfo.setObject("SMS Messages")
                String methMsg = realiser.realiseSentence(methInfo)
                securityDes.add(methMsg)
            }

            if (methText.contains("mappings")) {
                usesOAuth = true
                SPhraseSpec methInfo = nlgFactory.createClause()
                methInfo.setSubject("The App")
                methInfo.setVerb("establish")
                methInfo.setObject("OAuth Connection")
                String methMsg = realiser.realiseSentence(methInfo)
                securityDes.add(methMsg)
            }

            if (methText.contains("httpDelete") ||
                    methText.contains("httpGet") ||
                    methText.contains("httpHead") ||
                    methText.contains("httpPost") ||
                    methText.contains("httpPostJson") ||
                    methText.contains("httpPut") ||
                    methText.contains("httpPutJson")) {
                usesInternet = true
                SPhraseSpec methInfo = nlgFactory.createClause()
                methInfo.setSubject("The App")
                methInfo.setVerb("use")
                methInfo.setObject("Internet")
                String methMsg = realiser.realiseSentence(methInfo)
                securityDes.add(methMsg)
            }

            super.visitMethodCallExpression(mce)
        }

        public List getSubscriptionAttrs() {

            List usedAttrs = new ArrayList<String>()
            def matchAttrs = []

            attrSubs.each { k, v ->
                if (declaredCapVars.find { it.equals(k) } != null) {
                    v.each { usedAttrs.add it }
                }
            }

            if (usedAttrs != null) {

                usedAttrs.each {

                    println "Matched Found!" + usedAttrs.toString()

                    Attr_Map.findAll {
                        it.key == usedAttrs
                    }.each {
                        matchAttrs << it?.value
                    }
                }

                println "Processed Matched Attr: " + matchAttrs.toString()

                String attrDes = matchAttrs.toString()

                SPhraseSpec attrInfo = nlgFactory.createClause()
                attrInfo.setSubject("The App")
                attrInfo.setVerb("has sensitive device subscription: ")
                attrInfo.setObject(attrDes.substring(1, attrDes.length() - 1))
                String subscribeInfo = realiser.realiseSentence(attrInfo)
                println "!--> Subscribe Info: " + subscribeInfo
            } else {

                String attrDes = matchAttrs.toString()

                SPhraseSpec attrInfo = nlgFactory.createClause()
                attrInfo.setSubject("The App")
                attrInfo.setVerb("has less sensitive device subscription: ")
                attrInfo.setObject(attrDes.substring(1, attrDes.length() - 1))
                String subscribeInfo = realiser.realiseSentence(attrInfo)
                println "!--> Subscribe Info: " + subscribeInfo

                println "The App has no subscription."
            }

            return usedAttrs

        }

        @Override
        void visitPropertyExpression(PropertyExpression pe) {
            Expression expr = pe.getObjectExpression()
            if (expr instanceof VariableExpression) {
                VariableExpression pvex = (VariableExpression) expr
                if (pvex.getName().equals("location") ||
                        pvex.getName().equals("settings") ||
                        pvex.getName().equals("state")) {
                } else {
                    calledProps.add(pe.getPropertyAsString()?.toLowerCase())
                }
            }

            super.visitPropertyExpression(pe)
        }

        @Override
        void visitMethod(MethodNode meth) {

            MethodCodeVisitor mcv = new MethodCodeVisitor()

            String returnType = meth.getReturnType().getName()
            String methName = meth.getName()

            if (returnType.equals("java.lang.Object") && methName.equals("run")) {
                globalBodyEntries += 1

                assert globalBodyEntries == 1

                meth.getCode()?.visit(mcv)

                declaredGlobalVars = mcv.globals
            }

            super.visitMethod(meth)
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }

    def loadCapRef(def file) {
        file.splitEachLine(",") { fields ->
            allCommands[fields[1]?.toLowerCase()] = fields[3]?.toLowerCase()
            allProps[fields[1]?.toLowerCase()] = fields[2]?.toLowerCase()
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

        println "All commands:" + allCommandsList.size()
        println "All attributes:" + allPropsList.size()
    }

    def loadCapRefAll(def file) {
        file.splitEachLine(",") { fields ->
            allCommands[fields[0]?.toLowerCase()] = fields[2]?.toLowerCase()
            allProps[fields[0]?.toLowerCase()] = fields[1]?.toLowerCase()
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

        println "all commands full:" + allCommandsList.size()
        println "all attrs full:" + allPropsList.size()
    }

    // Load capability to device list from file.
    def loadCap2Dev(def file) {
        file.splitEachLine(",") { fields ->
            def capname = fields[0].toLowerCase()
            def copyFields = fields.toList()
            copyFields.remove(0)

            def listOfDeviceIds = new ArrayList()
            copyFields.each { devId -> listOfDeviceIds.add(devId) }

            cap2dev[capname] = listOfDeviceIds
        }
    }

    // Load device to capability list from file.
    def loadDev2Cap(def file) {
        file.splitEachLine(",") { fields ->
            def devname = fields[0].toLowerCase()
            def copyFields = fields.toList()
            copyFields.remove(0)

            def listOfCaps = new ArrayList()
            copyFields.each { cap -> listOfCaps.add(cap.toLowerCase()) }

            dev2cap[devname] = listOfCaps
        }
    }

    // Find out unique combination of used capabilities.
    def comb_test(s) {
        def input = new LinkedHashMap<String, Vector<String>>()
        input.put("c1", new Vector<String>(Arrays.asList("d1", "d2", "d3")))
        input.put("c2", new Vector<String>(Arrays.asList("d4", "d5", "d6")))
        input.put("c3", new Vector<String>(Arrays.asList("d7", "d8")))

        Utils.allUniqueCombinations(input).each { comb ->
            println Arrays.toString(comb)
        }
    }

    // Output summary code information by the App.
    def summarize() {

        log.append "--> Summary Info:"
        log.append "1. Overpriv Commands:" + numCmdOverpriv
        log.append "2. Overpriv Attributes:" + numAttrOverpriv
        log.append "3. Reflection Calls:" + numReflection
        log.append "4. Total Number:" + numTotalOverpriv

        log.append "type2 overprivilege total:" + type2_numCaps
        log.append "samename_flags:" + samename_flags
        log.append "type2 cmd/attr uses:" + type2_cmdattr_uses

        log.append "numSendSms: " + numSendSms
        log.append "numOAuth: " + numOAuth
        log.append "numInternet: " + numInternet

    }

    // Analyze permissions from declared methods.
    def analyzePermissions(InsnVisitor insnVis, ArrayList<String> declaredMethods) {
        def reqCaps = insnVis.requestedCaps.intersect(allCapsList)
        Map supportedCapDevs = new HashMap()

        reqCaps.each { cap ->
            //find out which devices support this cap
            def devicesForCap = cap2dev[cap]
            supportedCapDevs[cap] = devicesForCap
        }

        def rawCaps = new LinkedHashMap<String, Vector<String>>()

        //basically indicates how many different types of device handlers (and hence devices) a setup might have
        def cutoff = 5
        def cutoff_caps = 8

        //second stage cutoff. if we have too many caps in an app, we need to reduce cutoff value
        if (supportedCapDevs.keySet()?.size() > cutoff_caps)
            cutoff = 2

        supportedCapDevs.each { cap, listOfDevs ->

            if (listOfDevs != null) {
                def size = listOfDevs.size()

                if (size > cutoff)
                    size = cutoff

                //we try to always compute minimal bound on overprivilege
                //so when selecting which devices when pruning the list of devices
                //we select devices that implement the minimum number of capabilities
                rawCaps.put(cap, selectMinimalDevicesOfSize(listOfDevs, size - 1))
            }
        }

        def allUniqueCombs = Utils.allUniqueCombinations(rawCaps).toList()
        def hdrCaps = allUniqueCombs[0]
        allUniqueCombs.remove(0)

        println "---> The program analysis is completed successfully."

        // Determine what permissions the App actually uses.
        def calledCmdAttr = getCalledMethodsProps(insnVis)
        def calledMethods = calledCmdAttr[0]
        def calledProps = calledCmdAttr[1]
        def subAttrs = calledCmdAttr[2]
        def reqCmdAttrs = getCmdAttr(reqCaps)
        def reqCmds = reqCmdAttrs[0]
        def reqAttrs = reqCmdAttrs[1]
        def comb2overpriv = new ArrayList()

        // For each possible combinations, compute the final set of used capabilities in the App
        // and then get the set of unused capabilities, which then considered over-privileged.
        allUniqueCombs.each { auc ->
            def univOfCapsAtThisPoint = new ArrayList()
            auc.each { aDevice ->
                univOfCapsAtThisPoint.addAll(dev2cap[aDevice])
            }

            // println "Analyzing capability combination: " + Arrays.toString(auc)

            def allUniqueCapsAtPoint = univOfCapsAtThisPoint.toSet()
            def type2OverprivCaps = new ArrayList()

            allUniqueCapsAtPoint.each { ucap ->
                def cmdsAttrs = getCmdAttr([ucap])

                def allCmds = cmdsAttrs[0].toSet()
                def allAttrs = cmdsAttrs[1].toSet()

                if (allCmds.intersect(calledMethods.toSet()).toList().size() == 0 &&
                        allAttrs.intersect(calledProps.toSet()).toList().size() == 0 &&
                        (allCmds.size() > 0 || allAttrs.size() > 0)) //there should be atleast some cmds/attrs
                {

                    type2OverprivCaps.add(ucap)
                } else {
                    // In this case, the App uses some commands or attributes.
                }
            }

            SimpleContainer sc = new SimpleContainer(auc, type2OverprivCaps)
            comb2overpriv.add(sc)

        }

        //Select the minimum amount of extraneous caps and report as the type 2 overprivilege for this app

        comb2overpriv.sort { it -> it.two?.size() }

        def minOverprivCaps = comb2overpriv[0]

        if (minOverprivCaps.two?.size() > 0) {

            log.append "type2 overprivilege unused caps:"
            log.append "type2 driver combination: " + minOverprivCaps.one
            log.append Arrays.toString(minOverprivCaps.two)

            type2_numCaps += 1
        }

    }

    // Sort the list of devices by number of capabilities implemented in the App
    // and then return the smallest number of items.
    def selectMinimalDevicesOfSize(def listOfDevices, def count) {
        def devWithCount = new ArrayList()
        listOfDevices.each { dev ->
            int size = (dev2cap[dev] != null) ? dev2cap[dev].size() : 0
            SimpleContainer sc = new SimpleContainer(dev, size)
            devWithCount.add(sc)
        }

        devWithCount.sort { it -> it.two }

        return devWithCount*.one[0..count]
    }

    // Post-processing of App analysis results.
    def processApp(InsnVisitor insnVis, ArrayList<String> declaredMethods) {

        def calledCmdAttr = getCalledMethodsProps(insnVis)
        def calledMethods = calledCmdAttr[0].toSet()
        def calledProps = calledCmdAttr[1].toSet()
        def subAttrs = calledCmdAttr[2].toSet()

        Lexicon lexicon = Lexicon.getDefaultLexicon()
        NLGFactory nlgFactory = new NLGFactory(lexicon)
        Realiser realiser = new Realiser(lexicon)

        def reqCaps = insnVis.requestedCaps.intersect(allCapsList)
        def declaredGlobals = insnVis.declaredGlobalVars

        log.append "req caps: " + reqCaps.toSet()
        log.append "req cap size: " + reqCaps.size()

        def reqCmdAttrs = getCmdAttr(reqCaps)
        def reqCmds = reqCmdAttrs[0].toSet()
        def reqAttrs = reqCmdAttrs[1].toSet()

        log.append "requested commands:" + reqCmds
        log.append "requested attrs:" + reqAttrs

        //1. Handle cases when the App requests for no capabilities at all.
        if (reqCmds.toList().size() == 0 && reqAttrs.toList().size() == 0)
            return

        def reflIndex = calledMethods?.toList().any { v -> v?.contains("\$") }
        if (reflIndex) {
            log.append "Dynamic Method Invocation"
            this.append_manual()
            numReflection += 1
        }

        def sameNameCommands = allCommandsList.intersect(declaredMethods)

        if (sameNameCommands.size() > 0 && (reqCmds.toList().size() > 0 || reqAttrs.toList().size() > 0)) {
            log.append "Some app-defined methods have the same name as known IoT commands:"
            this.append_manual()
            sameNameCommands.each { it -> log.append it }
        }

        //2. Compute the same thing for properties (attributes).
        def sameNameAttrs = allPropsList.intersect(declaredGlobals)
        if (sameNameAttrs.size() > 0 && (reqCmds.toList().size() > 0 || reqAttrs.toList().size() > 0)) {
            log.append "Some app-defined globally-scoped properites have the same name as known IoT attributes:"
            this.append_manual()
            sameNameAttrs.each { it -> log.append it }
        }

        if (sameNameCommands.size() > 0 || sameNameAttrs.size() > 0)
            samename_flags += 1

        def filteredCalledMethods = calledMethods.intersect(reqCmds)
        def filteredCalledProps = calledProps.intersect(reqAttrs)

        def filteredExtraneousCalledMethods = calledMethods.intersect(allCommandsList)
        def filteredExtraneousCalledProps = calledProps.intersect(allPropsList)

        def type2Uses_Cmds = filteredExtraneousCalledMethods.toSet() - filteredCalledMethods.toSet()
        def type2Uses_Attrs = filteredExtraneousCalledProps.toSet() - filteredCalledProps.toSet()

        if (insnVis.usesAddChildDevice) {
            log.append "addChildDevice usage"
            this.append_manual()
        }

        if (type2Uses_Cmds.toList().size() > 0) {
            log.append "type 2 command uses"
            type2Uses_Cmds.each { it -> log.append it }
        }

        if (type2Uses_Attrs.toList().size() > 0) {
            log.append "type 2 attr uses"
            type2Uses_Attrs.each { it -> log.append it }
        }

        if (type2Uses_Cmds.size() > 0 || type2Uses_Attrs.size() > 0)
            type2_cmdattr_uses += 1

        log.append "called cap-methods by app"
        filteredCalledMethods.each { it ->

            log.append it

            SPhraseSpec calledCapMethod = nlgFactory.createClause()
            calledCapMethod.setSubject("The App")
            calledCapMethod.setVerb("call")
            calledCapMethod.setObject(it.toString())
            String calledCapMethodMsg = realiser.realiseSentence(calledCapMethod)

            println "Called Method: " + calledCapMethodMsg
        }

        log.append "called cap-props by app"
        filteredCalledProps.each { it -> log.append it }

        log.append "attribute uses through subscriptions"
        subAttrs.toSet().each { log.append it }

        // Calculate over-privileged commands or attributes.
        def cmdOverpriv = reqCmds.toSet() - filteredCalledMethods.toSet()
        def attrOverpriv = (reqAttrs.toSet() - filteredCalledProps.toSet())

        log.append "cmd overpriv:" + cmdOverpriv
        log.append "attr overpriv:" + attrOverpriv

        if (cmdOverpriv.toList().size() > 0)
            numCmdOverpriv += 1

        if (attrOverpriv.toList().size() > 0)
            numAttrOverpriv += 1

        if (cmdOverpriv.toList().size() > 0 || attrOverpriv.toList().size() > 0) {
            numTotalOverpriv += 1

            log.append("This App has over-privileged requests.")
        }

        if (insnVis.usesSendSms) {
            numSendSms += 1
        }

        if (insnVis.usesOAuth) {
            numOAuth += 1
        }

        if (insnVis.usesInternet) {
            numInternet += 1
        }
    }

    // Analyze all requested attributes.
    def getCalledMethodsProps(InsnVisitor insnVis) {
        def calledMethods = insnVis.calledMethods.toList()
        def reqCaps = insnVis.requestedCaps.toList()

        // Define attributes used via subscriptions (events).
        def subAttrs = insnVis.getSubscriptionAttrs().toList()

        if ("events" in calledMethods ||
                "eventsBetween" in calledMethods ||
                "eventsSince" in calledMethods) {
            def cA = getCmdAttr(reqCaps)

            // Get all known attributes for the capabilities for which this App requested.
            subAttrs.addAll(cA[1])

        }

        //Define all attributes used through subscriptions.
        def calledProps_initial = insnVis.calledProps.toList() + subAttrs

        //Collect attribute accesses of two forms: current<Attribute> or <attribute>State.
        def replacementList = new ArrayList()
        def deletionList = new ArrayList()
        calledProps_initial.each { attr ->
            if (attr?.contains("current")) {
                deletionList.add(attr)
                replacementList.add(attr.replace("current", "").toLowerCase())
            }

            if (attr?.contains("State")) {
                deletionList.add(attr)
                replacementList.add(attr.replace("State", "").toLowerCase())
            }
        }

        def calledProps = (calledProps_initial - deletionList) + replacementList

        def processed_calledMethods = calledMethods?.collect { item ->
            def x = item?.toLowerCase()
            x
        }

        def processed_calledProps = calledProps?.collect { item ->
            def x = item?.toLowerCase()
            x
        }

        def processed_subAttrs = subAttrs?.collect { item ->
            def x = item?.toLowerCase()
            x
        }

        return [
                processed_calledMethods.toSet(),
                processed_calledProps.toSet(),
                processed_subAttrs.toSet()
        ]

    }

    //Get all its commands and attributes as lists from a list of capability names.
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

    def append_manual() {
        log.append "This App needs further inspection on dynamic method calls."
    }

    class SimpleContainer {
        public def one
        public def two

        public SimpleContainer(def o, def t) {
            one = o
            two = t
        }
    }

}