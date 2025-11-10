import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    //16 bit instead of 32 (sunglasses emoji)

    val a = Input(UInt(16.W)) //operand 1
    val b = Input(UInt(16.W)) //operand 2
    val sel = Input(UInt(4.W)) // operation selector (4 bit as opcode states)
    val out = Output(UInt(16.W)) // result
    val zero = Output(Bool()) // true if result == 0, allows branching.
  })

  val result = Wire(UInt(16.W))
  result := 0.U // Default to zero

  //Implement this module here

  /*
   *  from ISA and OPCode
   *   0000 -> ADD   : a + b
   *   0001 -> SUB   : a - b
   *   0010 -> OR    : a | b
   *   0011 -> AND   : a & b
   *   0100 -> NOT   : ~a
   *   0101 -> GT    : (a > b) ? 1 : 0
   *   0110 -> MOD   : a % b
   */

  switch(io.sel) {

    is("b0000".U) {        // ADD
      result := io.a + io.b
    }

    is("b0001".U) {        // SUB
      result := io.a - io.b
    }

    is("b0010".U) {        // OR
      result := io.a | io.b
    }

    is("b0011".U) {        // AND
      result := io.a & io.b
    }

    is("b0100".U) {        // NOT (bitwise negation)
      // Only uses 'a' - the value of 'b' is ignored.
      // ~ inverts in chisel (flips every bit in the register)
      result := ~io.a
    }

    is("b0101".U) {        // GREATER THAN
      // If a > b, output 1; else, output 0
      // using Mux is the equivalent of if-else expression (kinda), Mux(condition, whenTrue, whenFalse)
      result := Mux(io.a > io.b, 1.U(16.W), 0.U(16.W))
    }

    is("b0110".U) {        // MODULO
      // Compute a % b, but avoid division-by-zero errors.....
      // =/= is the not equal operator
      result := Mux(io.b =/= 0.U, io.a % io.b, 0.U)
    }

  }

  /*
   * Output:
   *   - out  : the 16-bit result of the operation
   *   - zero : true if the result == 0, false otherwise, branching
   */

  // assign the computed value to the output port
  io.out := result

  // generate the zero flag (true when result is zero)
  io.zero := (result === 0.U(16.W))
}