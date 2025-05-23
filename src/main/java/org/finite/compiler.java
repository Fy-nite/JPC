package org.finite;

import org.finite.parser.Instruction;

import java.util.ArrayList;

public class compiler {
    public static void Compile(ArrayList<Instruction> instructions) {
        // Call QBE code generation by default

    }

    public static String convert_register(String reg) {
        // Convert register names to lowercase
        switch (reg.toUpperCase()) {
            case "RAX":
                return "%rax";
            case "RBX":
                return "%rbx";
            case "RCX":
                return "%rcx";
            case "RDX":
                return "%rdx";
            case "RSI":
                return "%rsi";
            case "RDI":
                return "%rdi";
            case "RBP":
                return "%rbp";
            case "RSP":
                return "%rsp";
            default:
                return reg; // Return the original string if not a register
        }
    }

    public static void CompileQBE(ArrayList<Instruction> instructions, String outputFile) {
        StringBuilder qbe = new StringBuilder();

        // Data section
        qbe.append("# Data section\n");
        qbe.append("data $strfmt = { b \"%s\\n\", b 0 }\n\n");
        qbe.append("data $numfmt = { b \"%d\\n\", b 0 }\n\n");

        int dbCounter = 0;
        for (Instruction instr : instructions) {
            String op = instr.getOperation().toUpperCase();
            String op1 = instr.getOperand1();
            String op2 = instr.getOperand2();
//            System.out.println("Compiling instruction: " + instr.toString());
            if (op.equals("DB")) {
                // Support: DB $100 "Hello World!" or DB label "Hello World!"
                if (op1 != null && op1.startsWith("$") && op2 != null) {
                    // Direct address DB with string content
                    String memAddress = op1.substring(1); // Remove the $ symbol
                    String value = op2;
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    // Emit a real data definition for the string
                    qbe.append("data $mem").append(memAddress).append(" = { b \"").append(value).append("\", b 0 }\n");
                } else if (op1 != null && op2 != null) {
                    // DB label "Hello World!"
                    String label = op1.replace(":", "");
                    String value = op2;
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    qbe.append("data $").append(label).append(" = { b \"").append(value).append("\", b 0 }\n");
                }
            }
        }
        qbe.append("\n");

        // Function section
        qbe.append("export function w $main() {\n");
        qbe.append("@start\n");
        qbe.append("""
                    %rax =l copy 0
                    %rbx =l copy 0
                    %rcx =l copy 0
                    %rdx =l copy 0
                    %rsi =l copy 0
                """);
        String lastCmpResultVar = null; // Track last cmp result variable
        for (Instruction instr : instructions) {
            String op = instr.getOperation().toUpperCase();
            String op1 = instr.getOperand1();
            String op2 = instr.getOperand2();
            if (op.equals("LBL") && op1 != null) {
                qbe.append("@").append(op1).append("\n");
            } else if (op.equals("MOV") && op1 != null && op2 != null) {
//                qbe.append("    %").append(op1.toLowerCase()).append(" =w copy %").append(op2.toLowerCase()).append("\n");
                if (op2.startsWith("$")) {
                    // MOV $100, RAX
                    String memAddress = op2.substring(1); // Remove the $ symbol
                    qbe.append("    %").append(op1.toLowerCase()).append(" =w load $mem").append(memAddress).append("\n");
                } else if (op2.startsWith("#")) {
                    // MOV @label, RAX
                    String label = op2.replace("@", "");
                    qbe.append("    %").append(op1.toLowerCase()).append(" =w load @").append(label).append("\n");
                } else {
                    qbe.append("    %").append(op1.toLowerCase()).append(" =w copy ").append(op2.toLowerCase()).append("\n");
                }
            } else if (op.equals("ADD") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w add %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            }
            else if (op.equals("AND") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w and %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            } else if (op.equals("OR") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w or %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            } else if (op.equals("XOR") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w xor %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            }
            else if (op.equals("SUB") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w sub %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            } else if (op.equals("MUL") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w mul %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            } else if (op.equals("DIV") && op1 != null && op2 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w div %").append(op1.toLowerCase()).append(", %").append(op2.toLowerCase()).append("\n");
            } // hlt

            else if (op.equals("HLT")) {
                qbe.append("ret 0\n");
            }
           
            else if (op.equals("OUT") && op1 != null && op2 != null) {
                // OUT port value
                String value = op2;
                String port = op1;
                // Handle $-prefixed (memory/string) output
                if (value.startsWith("$")) {
                    String afterDollar = value.substring(1);
                    // Register reference with $ (pointer to string in register)
                    boolean isRegister = false;
                    for (String reg : parser.validRegisters) {
                        if (reg.equalsIgnoreCase(afterDollar)) {
                            isRegister = true;
                            break;
                        }
                    }
                    if (isRegister) {
                        // Pointer to string in register (not implemented)
                        qbe.append("    #; OUT pointer-to-string in register not implemented, placeholder:\n");
                        qbe.append("    #; call $puts(l %").append(afterDollar.toLowerCase()).append(")\n");
                    } else if (afterDollar.matches("\\d+")) {
                        // Direct memory address (string)
                        qbe.append("    call $printf(l $mem").append(afterDollar).append(")\n");
                    } else {
                        // Unknown $-prefixed value
                        qbe.append("    #; OUT $-prefixed value not recognized: ").append(value).append("\n");
                    }
                } else if (TypeChecker.get_Operand_Type(value).equals(TypeChecker.oprandType.REGISTER.toString())) {
                    // Register output as number
                    String newvalue = convert_register(value);
                    qbe.append("    call $printf(l $numfmt, w ").append(newvalue.toLowerCase()).append(")\n");
                } else if (TypeChecker.get_Operand_Type(value).equals(TypeChecker.oprandType.LABEL.toString())) {
                    // Label or variable (assume string label)
                    qbe.append("    call $printf(l $").append(value).append(")\n");
                } else if (value.matches("-?\\d+")) {
                    // Immediate integer value
                    qbe.append("    call $printf(l ").append(value).append(")\n");
                } else if (value.startsWith("\"") && value.endsWith("\"")) {
                    // String literal (not supported directly, emit comment)
                    qbe.append("   # ; OUT string literal not supported directly: ").append(value).append("\n");
                } else {
                    // Fallback
                    qbe.append("    #; OUT unknown value: ").append(value).append("\n");
                }
            } else if (op.equals("RET")) {
                qbe.append("    ret\n");
            } 
            // Fix for JMP (Unconditional Jump)
            else if (op.equals("JMP") && op1 != null) {
                qbe.append("    jmp @").append(op1.replace("#", "")).append("\n");
            }
            // Fix for JE (Jump if Equal)
            else if (op.equals("JE") && op1 != null) {
                String fallthroughLabel = parser.generateLabel("fallthrough");
                String condVar = (lastCmpResultVar != null) ? lastCmpResultVar : "%cond";
                qbe.append("    jnz ").append(condVar).append(", @").append(op1.replace("#", "")).append(", @").append(fallthroughLabel).append("\n");
                qbe.append("@").append(fallthroughLabel).append("\n");
                lastCmpResultVar = null;
            }
            // Fix for JNE (Jump if Not Equal)
            else if (op.equals("JNE") && op1 != null) {
                String fallthroughLabel = parser.generateLabel("fallthrough");
                String condVar = (lastCmpResultVar != null) ? lastCmpResultVar : "%cond";
                // Invert the condition: jump if not equal (i.e., condVar == 0)
                qbe.append("    jnz ").append(condVar).append(", @").append(fallthroughLabel).append(", @").append(op1.replace("#", "")).append("\n");
                qbe.append("@").append(fallthroughLabel).append("\n");
                lastCmpResultVar = null;
            }
            // Add support for JZ/JNZ/JE/JNE (conditional jump, simulate with QBE)
            else if ((op.equals("JZ") || op.equals("JE")) && op1 != null) {
                String tempLabel = parser.generateLabel("jz");
                String fallthroughLabel = parser.generateLabel("fallthrough");
                String condVar = (lastCmpResultVar != null) ? lastCmpResultVar : "%cond";
                qbe.append("    jnz ").append(condVar).append(", @").append(tempLabel).append(", @").append(fallthroughLabel).append("\n");
                qbe.append("@").append(fallthroughLabel).append("\n");
                lastCmpResultVar = null;
            }
            else if ((op.equals("JNZ") || op.equals("JNE")) && op1 != null) {
                String fallthroughLabel = parser.generateLabel("fallthrough");
                String condVar = (lastCmpResultVar != null) ? lastCmpResultVar : "%cond";
                // Invert the condition: jump if not equal (i.e., condVar == 0)
                qbe.append("    jnz ").append(condVar).append(", @").append(fallthroughLabel).append(", @").append(op1.replace("#", "")).append("\n");
                qbe.append("@").append(fallthroughLabel).append("\n");
                lastCmpResultVar = null;
            }
            // Add support for JG/JL/JGE/JLE (signed comparisons)
            else if (op.equals("JG") && op1 != null) {
                qbe.append("    %cond =w cgtw %rax, 0\n");
                qbe.append("    jnz %cond, @").append(op1.replace("#", "")).append("\n");
            }
            else if (op.equals("JL") && op1 != null) {
                qbe.append("    %cond =w cltw %rax, 0\n");
                qbe.append("    jnz %cond, @").append(op1.replace("#", "")).append("\n");
            }
            else if (op.equals("JGE") && op1 != null) {
                qbe.append("    %cond =w cgew %rax, 0\n");
                qbe.append("    jnz %cond, @").append(op1.replace("#", "")).append("\n");
            }
            else if (op.equals("JLE") && op1 != null) {
                qbe.append("    %cond =w clew %rax, 0\n");
                qbe.append("    jnz %cond, @").append(op1.replace("#", "")).append("\n");
            }
            else if (op.equals("DB")) {
                // Already handled in data section, emit comment in code
                qbe.append("   # ; DB instruction handled in data section: ").append(instr.toString()).append("\n");
            }
            // Add support for INC
            else if (op.equals("INC") && op1 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w add %").append(op1.toLowerCase()).append(", 1\n");
            }
            // Add support for DEC
            else if (op.equals("DEC") && op1 != null) {
                qbe.append("    %").append(op1.toLowerCase()).append(" =w sub %").append(op1.toLowerCase()).append(", 1\n");
            }
            // Add support for PUSH (simulate with stack pointer if needed)
            else if (op.equals("PUSH") && op1 != null) {
                qbe.append("    #; PUSH not natively supported in QBE, simulate if needed\n");
            }
            // Add support for POP (simulate with stack pointer if needed)
            else if (op.equals("POP") && op1 != null) {
                qbe.append("    #; POP not natively supported in QBE, simulate if needed\n");
            }
            // Add support for CMP (set flags, here just emit comment)
            else if (op.equals("CMP") && op1 != null && op2 != null) {
                String tempLabel = parser.generateLabel("cmp");
                qbe.append("    %cmp_result =w sub %").append(op1.toLowerCase()).append(", ").append(op2.toLowerCase()).append("\n");
                qbe.append("    %").append(tempLabel).append(" =w ceqw %cmp_result, 0\n");
                lastCmpResultVar = "%" + tempLabel; // Save for next conditional jump
            }
            // Always output the instruction as a comment if not handled above
            else {
                qbe.append("    #; ").append(instr.toString()).append("\n");
            }
        }
        qbe.append("   ret #; End of instructions\n");
        qbe.append("}\n");

        // Save or print QBE code
        parser.fasmCode.setLength(0);
        parser.fasmCode.append(qbe);
        // write to the output file

        try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
            writer.write(qbe.toString());
        } catch (java.io.IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }

    }
}
