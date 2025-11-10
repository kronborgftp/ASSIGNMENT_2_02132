import chisel3._
import chisel3.util._

/*
 * Instruction layout:
 * [15:12] opcode
 * [11:8]  rd
 * [7:4]   rs
 * [3:0]   rt
 */
class ControlUnit extends Module {
  val io = IO(new Bundle {
    val instr = Input(UInt(16.W))

    // register file interface
    val rdSel = Output(UInt(4.W))
    val rsSel = Output(UInt(4.W))
    val rtSel = Output(UInt(4.W))
    val regWrite = Output(Bool())

    // ALU
    val aluSel = Output(UInt(4.W))

    // memory
    val memWrite = Output(Bool())
    val useMemData = Output(Bool()) // 1 => write back data from memory (LD)
    val useImmediate = Output(Bool()) // 1 => write back immediate constant (LI)

    // program counter
    val pcJump = Output(Bool())
    val pcStop = Output(Bool())
  })

  //  decode fields
  val opcode = io.instr(15,12)
  val rd     = io.instr(11,8)
  val rs     = io.instr(7,4)
  val rt     = io.instr(3,0)

  //defaults
  io.rdSel := rd
  io.rsSel := rs
  io.rtSel := rt

  io.regWrite := false.B
  io.aluSel := 0.U
  io.memWrite := false.B
  io.useMemData := false.B
  io.useImmediate := false.B
  io.pcJump := false.B
  io.pcStop := false.B

  //  main decode
  switch(opcode) {
    is("b0000".U) { } // NOP

    is("b0001".U) { // ADD
      io.aluSel := "b0001".U; io.regWrite := true.B
    }
    is("b0010".U) { // SUB
      io.aluSel := "b0010".U; io.regWrite := true.B
    }
    is("b0011".U) { // OR
      io.aluSel := "b0011".U; io.regWrite := true.B
    }
    is("b0100".U) { // AND
      io.aluSel := "b0100".U; io.regWrite := true.B
    }
    is("b0101".U) { // NOT
      io.aluSel := "b0101".U; io.regWrite := true.B
    }
    is("b0110".U) { // GT
      io.aluSel := "b0110".U; io.regWrite := true.B
    }
    is("b0111".U) { // MOD
      io.aluSel := "b0111".U; io.regWrite := true.B
    }

    // memory operations
    is("b1000".U) { // LD  rd = mem(rs)
      io.useMemData := true.B
      io.regWrite := true.B
    }
    is("b1001".U) { // LI  rd = immediate
      io.useImmediate := true.B
      io.regWrite := true.B
    }
    is("b1010".U) { // SD mem(rs) = rd
      io.memWrite := true.B
    }

    // control
    is("b1011".U) { // GO rd = address, rs = condition
      io.pcJump := true.B // PC jump will be conditioned in top-level
    }
    is("b1111".U) { // STOP
      when (io.instr === "hFFFF".U) {
        io.pcStop := true.B
      }
    }
  }
}
