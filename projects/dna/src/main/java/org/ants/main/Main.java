package org.ants.main;

import org.jline.builtins.Completers;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.terminal();
        Completers.FileNameCompleter completer = new Completers.FileNameCompleter();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer).build();

        usage();

        while (true) {
            try {
                String line = reader.readLine("DNA>");
                line = line.trim();

                if (line.equalsIgnoreCase("exit")) break;
                ParsedLine pl = reader.getParser().parse(line, 0);

                if ("init".equals(pl.word()) && pl.words().size() == 2) {
                    DNA.init(pl.words().get(1));
                }
                else if("update".equals(pl.word())) {
                    if(pl.words().size() == 1)
                        DNA.update();
                    else if(pl.words().size() == 2)
                        DNA.update(pl.words().get(1));
                    else
                        usage();
                }
                else if("dump".equals(pl.word()) && pl.words().size() == 2) {
                    String content = pl.words().get(1);
                    switch (content) {
                        case "topo" : DNA.dumpTopo(System.out); break;
                        case "fib" : DNA.dumpFib(System.out); break;
                        case "policy" : DNA.dumpPolicy(System.out); break;
                        case "atf" : DNA.dumpAllToFile(); break;
                        default: usage();
                    }
                }
                else {
                    usage();
                }
            }
            catch (EndOfFileException e) {
                return;
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void usage() {
        String[] usage = {
                "Differential Network Analysis",
                "  Usage: ",
                "    init <snapshot>                    initialize with a network snapshot",
                "    update [<snapshot>]                push changes on the init or provide an updated snapshot",
                "    dump topo | fib | policy           dump topology, fibs or policies",
        };

        for (String u: usage) System.out.println(u);
        System.out.println();
    }
}
