package com.deckerchan.infoRetr;

import java.util.Arrays;

import static java.lang.System.exit;
import static java.lang.System.out;

/**
 * Created by Decke on 08-Aug-16.
 */
public class EntryPoint {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            out.println("Wrong argument format!");
            System.exit(0);
        }

        String[] newArgs = new String[]{};

        if (args.length > 1) {
            newArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        String launchClass = args[0];

        switch (launchClass) {
            case "FileIndexBuilder":
                FileIndexBuilder.main(newArgs);
                break;
            case "SimpleSearchRanker":
                SimpleSearchRanker.main(newArgs);
                break;
            default:
                out.println("No specific class found!");
                exit(0);
        }

    }


}
