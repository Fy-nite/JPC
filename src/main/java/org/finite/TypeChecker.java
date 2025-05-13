package org.finite;

public class TypeChecker {
    public static enum ValueType {
        INTEGER,
        FLOAT,
        STRING,
        BOOLEAN,
        CHAR
    }
    public static enum oprandType {
        REGISTER,
        IMMEDIATE,
        MEMORY,
        LABEL,
        ADDRESS,
        INSTRUCTION,
        PORT,
        FILE_DESCRIPTOR,
        STRING_LITERAL,
        CHAR_LITERAL,
        BOOLEAN_LITERAL,
        FLOAT_LITERAL,
        INTEGER_LITERAL,
    }
    public static enum OperationType {
        MOV,
        ADD,
        SUB,
        MUL,
        DIV,
        OUT,
        IN,
        JMP,
        CALL,
        RET
    }
    /*
    * The TypeChecker class is responsible for checking the types of operands in MASM instructions.
    * It ensures that the operands are valid and compatible with the specified operation.
    * and also provides a way to get what type the opperands are.
    * @see parser
    * @see Instructions
    * @see Main
    */

    public static parser parser = new parser();

    public static String get_Operand_Type(String operand) {
        if (operand == null || operand.isEmpty()) {
            return "UNKNOWN";
        }
        // Check if the operand is a register
        for (String validRegister : parser.validRegisters) {
            if (validRegister.equalsIgnoreCase(operand)) {
                return oprandType.REGISTER.toString();
            }
        }
        // Check if the operand is a string literal
        if (operand.matches("\".*\"")) {
            return oprandType.STRING_LITERAL.toString();
        }
        // Check if the operand is a char literal
        if (operand.matches("'.'") || operand.matches("'\\.'")) { // handles 'c' and '\n' etc.
            return oprandType.CHAR_LITERAL.toString();
        }
        // Check if the operand is a boolean literal
        if (operand.equalsIgnoreCase("true") || operand.equalsIgnoreCase("false")) {
            return oprandType.BOOLEAN_LITERAL.toString();
        }
        // Check if the operand is a float literal
        if (operand.matches("-?\\d+\\.\\d+f?") || operand.matches("-?\\d+f")) {
            return oprandType.FLOAT_LITERAL.toString();
        }
        // Check if the operand is an integer literal (immediate value)
        if (operand.matches("-?\\d+")) {
            // This could be an IMMEDIATE or a PORT. Context from instruction is needed for full disambiguation.
            // For now, we'll default to IMMEDIATE. Instruction.toString() can override for OUT.
            return oprandType.IMMEDIATE.toString();
        }
        // Check if the operand is a memory address, $<num> or $<reg>
        if (operand.startsWith("$")) {
            String potentialReg = operand.substring(1);
            if (potentialReg.matches("\\d+")) {
                return oprandType.MEMORY.toString(); // Memory address like $123
            }
            for (String validRegister : parser.validRegisters) {
                if (validRegister.equalsIgnoreCase(potentialReg)) {
                    return oprandType.MEMORY.toString(); // Memory address like $RAX
                }
            }
        }
        // Check if the operand is a label, lbl <label>
        // Assuming labels are simple alphanumeric strings, possibly prefixed by "lbl " if that's a convention
        // For a standalone label identifier:
        if (operand.matches("[a-zA-Z_][a-zA-Z0-9_]*:") || operand.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // e.g. my_label: or my_label
             // if it's part of a JMP instruction, it's a label.
            return oprandType.LABEL.toString();
        }


        // If none of the above, return unknown type
        return "UNKNOWN";
    }
    public static boolean isValidLabel(String label) {
        // lbl <name> is a valid label
        if (label.matches("lbl [a-zA-Z_][a-zA-Z0-9_]*")) {
            return true;
        }
        return false;
    }
    public static boolean isValidOperation(String operation) {
        for (String validOperation : parser.validOperations) {
            if (validOperation.equalsIgnoreCase(operation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean areOperandsCompatibleForComparison(String operand1, String operand2) {
        String type1 = get_Operand_Type(operand1);
        String type2 = get_Operand_Type(operand2);

        // Allow comparison between registers, immediates, or memory
        return (type1.equals(oprandType.REGISTER.toString()) || type1.equals(oprandType.IMMEDIATE.toString()) || type1.equals(oprandType.MEMORY.toString())) &&
               (type2.equals(oprandType.REGISTER.toString()) || type2.equals(oprandType.IMMEDIATE.toString()) || type2.equals(oprandType.MEMORY.toString()));
    }
}
