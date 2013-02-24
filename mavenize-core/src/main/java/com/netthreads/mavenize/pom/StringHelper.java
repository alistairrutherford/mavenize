/*
 * Copyright 2011 - Alistair Rutherford - www.netthreads.co.uk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.netthreads.mavenize.pom;

/**
 *
 * @author Alistair
 */
public class StringHelper
{
    /**
     * Nicked this straight from wikipedia. God bless the internet.
     * 
     * @param first
     * @param second
     * 
     * @return longest common substring.
     */
    public static int longestSubstr(String first, String second)
    {
        if (first == null || second == null || first.length() == 0 || second.length() == 0)
        {
            return 0;
        }

        int maxLen = 0;
        int fl = first.length();
        int sl = second.length();
        int[][] table = new int[fl][sl];

        for (int i = 0; i < fl; i++)
        {
            for (int j = 0; j < sl; j++)
            {
                if (first.charAt(i) == second.charAt(j))
                {
                    if (i == 0 || j == 0)
                    {
                        table[i][j] = 1;
                    }
                    else
                    {
                        table[i][j] = table[i - 1][j - 1] + 1;
                    }
                    if (table[i][j] > maxLen)
                    {
                        maxLen = table[i][j];
                    }
                }
            }
        }
        return maxLen;
    }

    /**
     * Return the number of occurrences of target character in the supplied string.
     * 
     * @param character
     * @param text 
     * 
     * @return Occurrence count.
     */
    public static int countOf(char character, String text)
    {
        int count = 0;
        int indexFrom = 0;
        int nextIndex = 0;
        while (nextIndex >= 0)
        {
            nextIndex = text.indexOf(character, indexFrom);
            if (nextIndex > 0)
            {
                count++;
            }
            // Skip past target character.
            indexFrom = nextIndex + 1;
        }

        return count;
    }

    /**
     * Chop the string off past the last stated occurrence of character
     * 
     * @param text
     * @param character
     * @param count
     * 
     * @return Chopped string.
     */
    public static String chopFromLeft(String text, char character, int count)
    {
        String target = text;
        int inTotal = countOf(character, text);

        if (inTotal > count && count > 0)
        {
            int total = count;
            int indexFrom = 0;
            int nextIndex = 0;
            while (total > 0)
            {
                nextIndex = text.indexOf(character, indexFrom);
                if (nextIndex > 0)
                {
                    total--;
                }
                // Skip past target character.
                indexFrom = nextIndex + 1;
            }

            target = text.substring(0, indexFrom - 1);
        }

        return target;
    }

    /**
     * Chop the string off up to the last occurrence of character.
     * 
     * @param text
     * @param character
     * @param count
     * 
     * @return Chopped string.
     */
    public static String chopFromRight(String text, char character, int count)
    {
        String target = text;
        int inTotal = countOf(character, text);

        if (inTotal > count && count > 0)
        {
            int total = (inTotal - count) + 1;
            int indexFrom = 0;
            int nextIndex = 0;
            while (total > 0)
            {
                nextIndex = text.indexOf(character, indexFrom);
                if (nextIndex > 0)
                {
                    total--;
                }
                // Skip past target character.
                indexFrom = nextIndex + 1;
            }

            target = text.substring(indexFrom, text.length());
        }


        return target;
    }
}
