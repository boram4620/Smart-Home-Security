/*
 * SmartThingsAnalysisTools Copyright 2016 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

package iot.security.smartthings.overprivilege;

// Calculate unique combinations for capabilities.
public class Utils {
    public static String[][] allUniqueCombinations(LinkedHashMap<String, ArrayList<String>> resList) {
        int n = resList.keySet().size();
        int solutions = 1;

        for (ArrayList<String> capList : resList.values()) {
            solutions = solutions * capList.size();
        }

        String[][] allCombinations = new String[solutions + 1][];
        allCombinations[0] = resList.keySet();

        for (int i = 0; i < solutions; i++) {
            ArrayList<String> combination = new ArrayList<String>(n);
            int j = 1;
            for (ArrayList<String> capList : resList.values()) {
                int capSize = capList.size()
                int dif = (int) i / j;
                combination.add(capList.get(dif % capSize));
                j = j * capSize;
            }
            allCombinations[i + 1] = combination;
        }

        return allCombinations;
    }
}