package org.finite;

import java.util.ArrayList;

public class parser {
    // masm to QBE compiler helpers


    /*
    * The parser class is responsible for parsing assembly code and converting it to QBE format.
     */

    // List to hold parsed instructions, instance the class or access through already created parser to read this at runtime
    public ArrayList<Instruction> instructions = new ArrayList<>();

    // use this to check if the operation is valid.
    public static String[] validOperations = {"MOV", "ADD", "SUB", "MUL", "DIV", "OUT", "IN", "JMP", "CALL", "HLT","RET", "DB", "LBL", "CMP", "JZ", "JNZ", "JG", "JL", "JE", "JNE", "JGE", "JLE", "AND", "OR", "XOR", "NOT", "SHL", "SHR", "SAR", "ROL", "ROR", "NOP", "PUSH", "POP", "CALL", "RET", "DB",
    };
    public static String[] validRegisters = {"RAX", "RBX", "RCX", "RDX", "RSI", "RDI", "RBP", "RSP", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15"};
    public static int[] stringmap = new int[65536]; // to hold string map for QBE
    public static StringBuilder fasmCode = new StringBuilder(); // to hold the QBE code
    // Add a new StringBuilder for the .data section
    public static StringBuilder dataSection = new StringBuilder();



    public class Instruction {
        String operation;
        String[] operands;

        public Instruction(String operation, String[] operands) {
            this.operation = operation;
            this.operands = operands;
        }
        public Instruction(String operation, String operand1, String operand2) {
            this.operation = operation;
            this.operands = new String[]{operand1, operand2};
        }
        public String getOperation() {
            return operation;
        }
        public String getOperand1() {
            return operands[0];
        }
        public String getOperand2() {
            return operands.length > 1 ? operands[1] : null;
        }




        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Instruction{operation='").append(operation).append("'");

            if (getOperand1() != null) {
                String op1Type = TypeChecker.get_Operand_Type(getOperand1());
                String op1Key = op1Type;

                if (operation.equals("OUT") && op1Type.equals(TypeChecker.oprandType.IMMEDIATE.toString())) {
                    op1Key = TypeChecker.oprandType.PORT.toString();
                } else if (op1Type.equals(TypeChecker.oprandType.REGISTER.toString())) {
                    op1Key = "Register";
                } else if (op1Type.equals(TypeChecker.oprandType.IMMEDIATE.toString())) {
                    op1Key = "Immediate";
                }
                // Add more specific key names if needed, e.g., "Label", "MemoryAddress"
                // Default to the type name if not specifically handled
                sb.append(", ").append(op1Key).append("='").append(getOperand1()).append("'");
            }

            if (getOperand2() != null) {
                String op2Type = TypeChecker.get_Operand_Type(getOperand2());
                String op2Key = op2Type;

                if (op2Type.equals(TypeChecker.oprandType.REGISTER.toString())) {
                    op2Key = "Register";
                } else if (op2Type.equals(TypeChecker.oprandType.IMMEDIATE.toString())) {
                    op2Key = "Immediate";
                }
                // Add more specific key names if needed
                sb.append(", ").append(op2Key).append("='").append(getOperand2()).append("'");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    public void parse(String[] codeLines) {
        instructions.clear();
        for (String line : codeLines) {
            // Remove comments (// or ;)
            int commentIdx = line.indexOf("//");
            if (commentIdx == -1) commentIdx = line.indexOf(";");
            if (commentIdx != -1) line = line.substring(0, commentIdx);
            line = line.trim();
            if (line.isEmpty()) continue;

            // Split into tokens
            String[] tokens = line.split("\\s+", 2); // Split into operation and the rest
            if (tokens.length == 0) continue;
            // if there's a lbl main, skip it
            if (tokens[0].equalsIgnoreCase("lbl")) {
                // Skip label lines
                continue;
            }
            String op = tokens[0];
            if (op.equalsIgnoreCase("HLT") || op.equalsIgnoreCase("RET")) {
                // Special handling for HLT and RET instructions
                instructions.add(new Instruction(op, new String[1]));
                continue;
            }
            if (op.equalsIgnoreCase("DB") && tokens.length > 1) {
                // Special handling for DB instruction
                String rest = tokens[1].trim();
                int spaceIdx = rest.indexOf(' ');
                if (spaceIdx > 0) {
                    // Split into memory address and content
                    String memAddress = rest.substring(0, spaceIdx).trim();
                    String content = rest.substring(spaceIdx + 1).trim();
                    instructions.add(new Instruction(op, new String[]{memAddress, content}));
                } else {
                    // If there's only one operand
                    instructions.add(new Instruction(op, new String[]{rest}));
                }
            } else {
                // Handle other instructions: split operands by whitespace (not comma)
                String[] operands = tokens.length > 1 ? tokens[1].trim().split("\\s+") : new String[0];
                instructions.add(new Instruction(op, operands));
            }
        }
    }

}

