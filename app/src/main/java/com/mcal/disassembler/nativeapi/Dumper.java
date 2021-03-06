package com.mcal.disassembler.nativeapi;

import java.util.Vector;

public class Dumper {
    public static Vector<DisassemblerSymbol> symbols = new Vector<>();
    public static Vector<DisassemblerVtable> exploed = new Vector<>();
    public static Vector<DisassemblerClass> classes = new Vector<>();

    public static void readData() {
        symbols.clear();
        exploed.clear();
        classes.clear();
        for (int i = 0; i < DisassemblerDumper.getSize(); ++i) {
            String demangledName = DisassemblerDumper.getDemangledNameAt(i);
            if (demangledName == null || demangledName.isEmpty() || demangledName.equals(" "))
                demangledName = DisassemblerDumper.getNameAt(i);
            DisassemblerSymbol newSymbol = new DisassemblerSymbol(DisassemblerDumper.getNameAt(i), demangledName, DisassemblerDumper.getTypeAt(i), DisassemblerDumper.getBindAt(i));
            symbols.addElement(newSymbol);
        }
    }
}