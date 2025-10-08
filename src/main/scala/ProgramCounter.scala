import chisel3._
import chisel3.util._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val stop = Input(Bool())
    val jump = Input(Bool())
    val run = Input(Bool())
    val programCounterJump = Input(UInt(16.W))
    val programCounter = Output(UInt(16.W))
  })


  // creates 16-bit register for the PC
  //reset using 0
  val pcReg = RegInit(0.U(16.W))

  //implementation of Table 1 behaviour from the assignment:
    // 1) If run == 0, hold
    // 2) Else if stop == 1, hold
    // 3) Else if jump == 1, load jump target
    // 4) Else, increment by 1 (wraps mod 2^16 automatically for UInt(16.W)

  //if were not running, or the program has ended, stop, do nothing keep current PC
  //ignores jumo, just like the truth table
  when(!io.run || io.stop) {
    // Hold current PC (no change)
    pcReg := pcReg //Case 1 and 2, row 1 and 2 in truth table

    //only evaluated if were running and not stopped.
    //loads the jump target into the PC
  }.elsewhen(io.jump) {
    // Jump: load the provided 16-bit target address
    pcReg := io.programCounterJump // case 3, row 4 in truth table

  }.otherwise {
    // increment PC by 1 to fetch the next instruction
    pcReg := pcReg + 1.U // case 4, row 3 in truth table
  }

  // output
  io.programCounter := pcReg

}