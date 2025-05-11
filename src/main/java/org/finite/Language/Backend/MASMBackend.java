package org.finite.Language.Backend;

/*
 * The MASMBackend annotation is used to mark classes that are to be used as
 * backend code generators for the MASM (Micro-Assembly) language. This annotation
 * can be used to identify classes that implement the necessary methods and
 * interfaces for generating Language code like QBE code from MASM assembly.
 *
 */



/*
 * Class annotation that marks a class as a MASM backend code generator.
 *
 * any class this is applied to should implement the methods for generating
 * other code from MASM assembly code:
 *
 * - Compile<Language> - method to compile the assembly code to the specified
 * language
 * - Handle_<instruction> - method to handle the specified instruction.
 * for more infomation about what functions and their types are needed, see the
 * built javadoc for the QBE language class.
 */

public @interface MASMBackend {

    /* The name of the backend code generator.
     *
     * @return The name of the backend code generator.
     */
    String name() default "MASMBackend";
    /* The version of the backend code generator.
     *
     * used to identify the version of the backend code generator.
     * and for debugging purposes, versioning of the backend code generator, ect.
     *
     * @return The version of the backend code generator.
     */
    String version() default "1.0.0";

}
